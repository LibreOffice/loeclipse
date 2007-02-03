/*************************************************************************
 *
 * $RCSfile: PackagePropertiesModel.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/03 21:42:13 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
 *
 * Sun Microsystems Inc., October, 2000
 *
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2000 by Sun Microsystems, Inc.
 * 901 San Antonio Road, Palo Alto, CA 94303, USA
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License version 2.1, as published by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston,
 * MA 02111-1307 USA
 * 
 * The Initial Developer of the Original Code is: Sun Microsystems, Inc..
 *
 * Copyright: 2002 by Sun Microsystems, Inc.
 *
 * All Rights Reserved.
 *
 * Contributor(s): Cedric Bosdonnat
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.openoffice.ide.eclipse.core.PluginLogger;

/**
 * @author cedricbosdo
 *
 */
public class PackagePropertiesModel {

	private static final String CONTENTS = "contents"; //$NON-NLS-1$
	private static final String BASICLIBS = "basicLibs"; //$NON-NLS-1$
	private static final String DIALOGLIBS = "dialogLibs"; //$NON-NLS-1$
	private static final String DESCRIPTION = "description"; //$NON-NLS-1$
	
	private IFile mPropertiesFile;
	private Properties mProperties = new Properties();
	
	private boolean mIsDirty = false;
	private boolean mIsQuiet = false;
	private Vector<IPackageChangeListener> mListeners = new Vector<IPackageChangeListener>();
	
	/**
	 * Create a new package.properties model for a given file. If the file
	 * can be read, the existing properties will be imported.
	 * 
	 * @param file the package.properties file represented by the object.
	 * @throws IllegalArgumentException if the file is <code>null</code>
	 */
	public PackagePropertiesModel(IFile file) throws IllegalArgumentException {
		
		FileInputStream is = null;
		
		try {
			is = new FileInputStream(file.getLocation().toFile());
			mPropertiesFile = file;
		} catch (FileNotFoundException e) {
			mPropertiesFile = null;
			throw new IllegalArgumentException(Messages.getString("PackagePropertiesModel.NullFileException")); //$NON-NLS-1$
		}
		
		try {
			mProperties.load(is);
		} catch (IOException e) {
			try { is.close(); } catch (IOException ex){ } ;
			PluginLogger.warning(Messages.getString("PackagePropertiesModel.FileReadException") + file.getLocation()); //$NON-NLS-1$
		}
	}
	
	public void setQuiet(boolean quiet) {
		mIsQuiet = quiet;
	}
	
	public void addChangeListener(IPackageChangeListener listener) {
		mListeners.add(listener);
	}
	
	public void removeChangedListener(IPackageChangeListener listener) {
		if (mListeners.contains(listener)) mListeners.remove(listener);
	}
	
	public void firePackageSaved() {
		if (!mIsQuiet) {
			mIsDirty = false;
			for (IPackageChangeListener listener : mListeners) {
				listener.packagePropertiesSaved();
			}
		}
	}
	
	public void firePackageChanged() {
		if (!mIsQuiet) {
			mIsDirty = true;
			for (IPackageChangeListener listener : mListeners) {
				listener.packagePropertiesChanged();
			}
		}
	}
	
	public boolean isDirty() {
		return mIsDirty;
	}
	
	/**
	 * Writes the Package properties to the file.
	 * 
	 * @throws Exception if the data can't be written
	 */
	public void write() throws Exception {
		FileOutputStream os = new FileOutputStream(
				mPropertiesFile.getLocation().toFile());
		try {
			mProperties.store(os, 
				Messages.getString("PackagePropertiesModel.Comment")); //$NON-NLS-1$
			firePackageSaved();
		} catch (IOException e) {
			os.close();
			throw e;
		}
	}
	
	/**
	 * Clears all the content of the package properties and replace it by a 
	 * string as if it would have been the properties file content.
	 * 
	 * @param content the string describing the data
	 */
	public void reloadFromString(String content) {
		String initContent = writeToString();
		if (!content.equals(initContent)) {
			mProperties.clear();
			try {
				mProperties.load(new StringBufferInputStream(content));
			} catch (IOException e) {
				// Nothing to log
			}
			firePackageChanged();
		}
	}
	
	/**
	 * @return the content of the package properties under the form of a string
	 * 		as it would have been written to the file.
	 */
	public String writeToString() {
		String fileContent = ""; //$NON-NLS-1$
		Set<Entry<Object, Object>> entries = mProperties.entrySet();
		Iterator<Entry<Object, Object>> iter = entries.iterator();
		while (iter.hasNext()) {
			Entry<Object, Object> entry = iter.next();
			fileContent += (String)entry.getKey() + "=" + (String)entry.getValue() + "\n"; //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return fileContent;
	}
	
	/**
	 * Adds a Basic library folder to the package.
	 * 
	 * @param libFolder the library folder to add
	 * @throws IllegalArgumentException is thrown if the argument is 
	 * 			<code>null</code>
	 */
	public void addBasicLibrary(IFolder libFolder) throws IllegalArgumentException {
		
		String libs = mProperties.getProperty(BASICLIBS);
		if (libs == null) {
			libs = "";  //$NON-NLS-1$
		}
		
		try {
			if (!libs.equals("")) libs += ", "; //$NON-NLS-1$ //$NON-NLS-2$
			libs += libFolder.getProjectRelativePath().toString();
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
		mProperties.setProperty(BASICLIBS, libs);
		firePackageChanged();
	}
	
	/**
	 * Adds a basic dialog library folder to the package.
	 * 
	 * @param libFolder the library folder to add
	 * @throws IllegalArgumentException is thrown if the argument is 
	 * 			<code>null</code>
	 */
	public void addDialogLibrary(IFolder libFolder) throws IllegalArgumentException {
		String libs = mProperties.getProperty(DIALOGLIBS);
		if (libs == null) {
			libs = "";  //$NON-NLS-1$
		}
		
		try {
			if (!libs.equals("")) libs += ", "; //$NON-NLS-1$ //$NON-NLS-2$
			libs += libFolder.getProjectRelativePath().toString();
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
		mProperties.setProperty(DIALOGLIBS, libs);
		firePackageChanged();
	}
	
	/**
	 * @return the list of the dialog libraries addedd to the package properties
	 */
	public List<IFolder> getDialogLibraries() {
		
		ArrayList<IFolder> result = new ArrayList<IFolder>();
		
		try {
			String libs = mProperties.getProperty(DIALOGLIBS);
			IProject prj = mPropertiesFile.getProject();

			if (libs != null && !libs.equals("")) { //$NON-NLS-1$
				String[] fileNames = libs.split(","); //$NON-NLS-1$
				for (String fileName : fileNames) {
					fileName = fileName.trim();
					result.add(prj.getFolder(fileName));
				}
			}
		} catch (NullPointerException e) {
			// Nothing to do nor return
		}
		return result;
	}
	
	/**
	 * @return the list of the basic libraries addedd to the package properties
	 */
	public List<IFolder> getBasicLibraries() {
		ArrayList<IFolder> result = new ArrayList<IFolder>();
		
		try {
			String libs = mProperties.getProperty(BASICLIBS);
			IProject prj = mPropertiesFile.getProject();

			if (libs != null && !libs.equals("")) { //$NON-NLS-1$
				String[] fileNames = libs.split(","); //$NON-NLS-1$
				for (String fileName : fileNames) {
					fileName = fileName.trim();
					result.add(prj.getFolder(fileName));
				}
			}
		} catch (NullPointerException e) {
			// Nothing to do nor return
		}
		return result;
	}
	
	/**
	 * Removes all the basic libraries from the package properties
	 */
	public void clearBasicLibraries() {
		mProperties.setProperty(BASICLIBS, ""); //$NON-NLS-1$
		firePackageChanged();
	}
	
	/**
	 * Removes all the dialog libraries from the package properties
	 */
	public void clearDialogLibraries() {
		mProperties.setProperty(DIALOGLIBS, ""); //$NON-NLS-1$
		firePackageChanged();
	}
	
	/**
	 * Adds a file or directory to the package properties. <strong>Do not add
	 * dialog or basic libraries or package descriptions using this method: use
	 * the appropriate method</strong>.
	 * 
	 * @param libFolder the library folder to add
	 * @throws IllegalArgumentException is thrown if the argument is 
	 * 			<code>null</code>
	 */
	public void addContent(IResource res) throws IllegalArgumentException {
		String libs = mProperties.getProperty(CONTENTS);
		if (libs == null) {
			libs = "";  //$NON-NLS-1$
		}
		
		try {
			if (!libs.equals("")) libs += ", "; //$NON-NLS-1$ //$NON-NLS-2$
			libs += res.getProjectRelativePath().toString();
		} catch (Exception e) {
			throw new IllegalArgumentException();
		}
		mProperties.setProperty(CONTENTS, libs);
		firePackageChanged();
	}
	
	/**
	 * @return the list of the the files and directories added to the package 
	 * 		properties that are not dialog or basic liraries or package 
	 * 		descriptions 
	 */
	public List<IResource> getContents() {
		ArrayList<IResource> result = new ArrayList<IResource>();
		
		try {
			String libs = mProperties.getProperty(CONTENTS);
			IProject prj = mPropertiesFile.getProject();

			if (libs != null && !libs.equals("")) { //$NON-NLS-1$
				String[] fileNames = libs.split(","); //$NON-NLS-1$
				for (String fileName : fileNames) {
					fileName = fileName.trim();
					if (prj.getFolder(fileName).exists()) {
						result.add(prj.getFolder(fileName));
					} else if (prj.getFile(fileName).exists()) {
						result.add(prj.getFile(fileName));
					}
				}
			}
		} catch (NullPointerException e) {
			// Nothing to do nor return
		}
		return result;
	}
	
	/**
	 * Removes all the file and directories from the package properties that
	 * has been added using {@link #addContent(IResource)}.
	 */
	public void clearContents() {
		mProperties.setProperty(CONTENTS, ""); //$NON-NLS-1$
		firePackageChanged();
	}
	
	/**
	 * Adds a localized package description file. The description file has to
	 * exist and the locale can't be <code>null</code>. 
	 * 
	 * @param description the description file
	 * @param locale the file locale.
	 * 
	 * @throws IllegalArgumentException is thrown if the file is <code>null</code>
	 * 		or doesn't exists or if the locale is <code>null</code>.
	 */
	public void addDescriptionFile(IFile description, Locale locale) throws IllegalArgumentException {
		
		if (locale == null) {
			throw new IllegalArgumentException(Messages.getString("PackagePropertiesModel.NoLocaleException")); //$NON-NLS-1$
		}
		
		if (description == null || !description.exists()) {
			throw new IllegalArgumentException(Messages.getString("PackagePropertiesModel.NoDescriptionFileException")); //$NON-NLS-1$
		}
		
		String countryName = ""; //$NON-NLS-1$
		if (locale.getCountry() != "") { //$NON-NLS-1$
			countryName = "_" + locale.getCountry(); //$NON-NLS-1$
		}
		
		String propertyName = DESCRIPTION + "-" + locale.getLanguage() + countryName; //$NON-NLS-1$
		mProperties.setProperty(propertyName, description.getProjectRelativePath().toString());
		firePackageChanged();
	}
	
	/**
	 * @return a map of the description files accessed by their locale. There is
	 * 		no support of a default locale.
	 */
	public Map<Locale, IFile> getDescriptionFiles() {
		HashMap<Locale, IFile> descriptions = new HashMap<Locale, IFile>();	
		IProject prj = mPropertiesFile.getProject();
		
		Iterator<Object> keys = mProperties.keySet().iterator();
		while (keys.hasNext()) {
			String key = (String)keys.next();
			String regex = DESCRIPTION + "-([a-zA-Z]{2})(?:_([a-zA-Z]{2}))?"; //$NON-NLS-1$
			Matcher matcher = Pattern.compile(regex).matcher(key);
			if (matcher.matches()) {
				String language = matcher.group(1);
				String country = matcher.group(2);
				
				Locale locale = new Locale(language);
				if (country != null) {
					locale = new Locale(language, country);
				}
				
				IFile file = prj.getFile(mProperties.getProperty(key));

				if (file != null) {
					descriptions.put(locale, file);
				}
			}
		}
		return descriptions;
	}
	
	/**
	 * Removes all the description files from the package properties.
	 */
	public void clearDescriptions() {
		int nbRemoved = 0;
		
		Iterator<Object> keys = ((Properties)mProperties.clone()).keySet().iterator();
		while (keys.hasNext()) {
			String key = (String)keys.next();
			String regex = DESCRIPTION + "-([a-zA-Z]{2})(?:_([a-zA-Z]{2}))?"; //$NON-NLS-1$
			Matcher matcher = Pattern.compile(regex).matcher(key);
			if (matcher.matches()) {
				mProperties.remove(key);
				nbRemoved ++ ;
			}
		}
		
		if (nbRemoved > 0) firePackageChanged();
	}
}
