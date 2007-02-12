/*************************************************************************
 *
 * $RCSfile: UnoPackage.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/12 20:21:16 $
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.utils.FileHelper;
import org.openoffice.ide.eclipse.core.utils.ZipContent;

/**
 * This class represents a UNO package and should be used to create a uno package.
 * In the same way than ant jar target does, the UNO package is defined by an
 * output file and a root directory. All the file that will be added to the
 * package will have to be contained in this directory or one of its children.
 *
 * @author cedricbosdo
 *
 */
public class UnoPackage {

	public static final String ZIP = "zip"; //$NON-NLS-1$
	public static final String UNOPKG = "uno.pkg"; //$NON-NLS-1$
	public static final String OXT = "oxt"; //$NON-NLS-1$
	
	private File mDestination;
	private File mOrigin;
	private boolean mBuilding = false;
	
	private HashMap<String, ZipContent> mZipEntries = new HashMap<String, ZipContent>();
	private HashMap<String, String> mManifestEntries = new HashMap<String, String>();
	private Vector<File> mTemporaryFiles = new Vector<File>();
	
	/**
	 * Create a new package object. The extension has be one of the following. 
	 * The default extension is {@link #ZIP}. If the extension is invalid or 
	 * missing, the file will be renamed in <code>.zip</code>.
	 * <ul>
	 * 	 <li>{@link #ZIP}</li>
	 *   <li>{@link #UNOPKG}</li>
	 *   <li>{@link #OXT}</li>
	 * <ul>
	 * 
	 * @param out the file of the package.
	 * @param dir the root directory of the package content. 
	 */
	public UnoPackage(File out, File dir) {
		if (! (out.getName().endsWith(ZIP) || out.getName().endsWith(UNOPKG) || 
				out.getName().endsWith(OXT)) ) {
			int pos = out.getName().lastIndexOf("."); //$NON-NLS-1$
			if (pos > 0) {
				String name = out.getName().substring(0, pos);
				out = new File(out.getParentFile(), name + "." + ZIP); //$NON-NLS-1$
			} else {
				out = new File(out.getParentFile(), out.getName()+".zip"); //$NON-NLS-1$
			}
		}
		
		mDestination = out;
		mOrigin = dir;
	}
	
	/**
	 * Cleans up the data structure. There is no need to call this method if the
	 * package has been closed using {@link #close()}
	 */
	public void dispose() {
		mDestination = null;
		mOrigin = null;
		mManifestEntries.clear();
		mZipEntries.clear();
		mTemporaryFiles.clear();
	}
	
	/**
	 * Add a uno component file, for example a jar, shared library or python file
	 * containing the uno implementation. The type of the file defines the 
	 * language and should be given as defined in the OOo Developer's Guide, like
	 * Java, native, Python.
	 * 
	 * @param file the file to add to the package
	 * @param type the type of the file to add.
	 * 
	 * @see #addComponentFile(File, String, String) for platform support
	 */
	public void addComponentFile(File file, String type) {
		addComponentFile(file, type, null);
	}
	
	/**
	 * Add a uno component file, for example a jar, shared library or python file
	 * containing the uno implementation. The type of the file defines the 
	 * language and should be given as defined in the OOo Developer's Guide, like
	 * Java, native, Python.
	 * 
	 * @param file the file to add to the package
	 * @param type the type of the file to add.
	 * @param platform optional parameter to use only with native type. Please
	 * 		refer to the OOo Developer's Guide for more information.
	 */
	public void addComponentFile(File file, String type, String platform) {
		initializeOutput(); // Do not change the extension from now
	
		String mediaType = "application/vnd.sun.star.uno-component;type=" + type; //$NON-NLS-1$
		if (platform != null && type.equals("native")) { //$NON-NLS-1$
			mediaType += ";platform=" + platform; //$NON-NLS-1$
		}
		
		String relPath = getOriginRelativePath(file);
		
		// create the manifest entry
		addManifestEntry(relPath, mediaType);
		
		// create the ZipContent
		addZipContent(relPath, file);
	}
	
	/**
	 * Add a type library to the package. Note that by some strange way, a jar
	 * dependency can be added in the package as a type library like RDB files.
	 *  
	 * @param file the file to add 
	 * @param type the type of the file as specified in the OOo Developer's Guide
	 */
	public void addTypelibraryFile(File file, String type) {
		initializeOutput(); // Do not change the extension from now
		
		String mediaType = "application/vnd.sun.star.uno-typelibrary;type=" + type; //$NON-NLS-1$
		String relPath = getOriginRelativePath(file);
		
		// create the manifest entry
		addManifestEntry(relPath, mediaType);
		
		// create the ZipContent
		addZipContent(relPath, file);
	}
	
	/**
	 * Add a basic library to the package. Even if this method may not be used,
	 * it is possible.
	 * 
	 * @param dir the directory of the basic library.
	 */
	public void addBasicLibraryFile(File dir) {
		if (dir.isDirectory()) {
			initializeOutput(); // Do not change the extension from now

			String mediaType = "application/vnd.sun.star.basic-library"; //$NON-NLS-1$
			String relPath = getOriginRelativePath(dir);
			
			addManifestEntry(relPath, mediaType);
			
			addZipContent(relPath, dir);
		}
	}
	
	/**
	 * Add a dialog library to the package. Even if this method may not be used,
	 * it is possible.
	 * 
	 * @param dir the directory of the dialog library.
	 */
	public void addDialogLibraryFile(File dir) {
		if (dir.isDirectory()) {
			initializeOutput(); // Do not change the extension from now

			String mediaType = "application/vnd.sun.star.dialog-library"; //$NON-NLS-1$
			String relPath = getOriginRelativePath(dir);
			
			addManifestEntry(relPath, mediaType);
			
			addZipContent(relPath, dir);
		}
	}
	
	/**
	 * Add an xcu configuration to the package.
	 * 
	 * @param file the xcu file to add
	 */
	public void addConfigurationDataFile(File file) {
		if (file.isFile() && file.getName().endsWith("xcu")) { //$NON-NLS-1$
			initializeOutput();  // Do not change the extension from now
			
			String mediaType = "application/vnd.sun.star.configuration-data"; //$NON-NLS-1$
			String relPath = getOriginRelativePath(file);
			
			addManifestEntry(relPath, mediaType);
			
			addZipContent(relPath, file);
		}
	}
	
	/**
	 * Add an xcs configuration to the package.
	 * 
	 * @param file the xcs file to add
	 */
	public void addConfigurationSchemaFile(File file) {
		if (file.isFile() && file.getName().endsWith("xcs")) { //$NON-NLS-1$
			initializeOutput();  // Do not change the extension from now
			
			String mediaType = "application/vnd.sun.star.configuration-schema"; //$NON-NLS-1$
			String relPath = getOriginRelativePath(file);
			
			addManifestEntry(relPath, mediaType);
			
			addZipContent(relPath, file);
		}
	}
	
	/**
	 * Add a localized description of the package.
	 * 
	 * @param descriptionFile the file containing the description for that locale
	 * @param locale the locale of the description. Can be <code>null</code>.
	 */
	public void addPackageDescription(File descriptionFile, Locale locale) {
		try {
			// write the description to a file
			String localeString = ""; //$NON-NLS-1$
			if (locale != null) {
				localeString = locale.toString();
				localeString = localeString.replace("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$
			}

			// Add the file entry to the manifest
			String mediaType = "application/vnd.sun.star.package-bundle-description"; //$NON-NLS-1$
			if (!localeString.equals("")) { //$NON-NLS-1$
				mediaType += ";locale=" + localeString; //$NON-NLS-1$
			}
			String relPath = getOriginRelativePath(descriptionFile);
			
			addManifestEntry(relPath, mediaType);

			// Add the file to the zip entries
			addZipContent(relPath, descriptionFile);
		} catch (Exception e) {
			// Can't add the description file to the package
		}
	}
	
	/**
	 * Adds a file or directory to the package but do not include it in the 
	 * manifest. This could be used for example for images.
	 * 
	 * @param file the file or directory to add.
	 */
	public void addOtherFile(File file) {
		initializeOutput(); // Do not change the extension from now 
		
		String relPath = getOriginRelativePath(file);
		addZipContent(relPath, file);
	}
	
	/**
	 * Writes the package on the disk and cleans up the data. The UnoPackage
	 * instance cannot be used after this operation: it should unreferenced.
	 *
	 * @return the file of the package or <code>null</code> if nothing happened.
	 */
	public File close() {
		File result = null;
		
		if (mBuilding) {
			try {
				// Write the manifest
				File metainfDir = new File(mOrigin, "META-INF"); //$NON-NLS-1$
				if (!metainfDir.exists()) metainfDir.mkdir();
				File manifestFile = new File(metainfDir, "manifest.xml"); //$NON-NLS-1$

				FileWriter writer = new FileWriter(manifestFile);
				writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //$NON-NLS-1$
				writer.append("<manifest:manifest>\n"); //$NON-NLS-1$
				
				// Add the manifest entries
				Iterator<String> manifestIter = mManifestEntries.values().iterator();
				while (manifestIter.hasNext()) {
					String entry = manifestIter.next();
					writer.append("\t" + entry + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
				}
				
				writer.append("</manifest:manifest>\n"); //$NON-NLS-1$
				writer.close();
				
				// Write the ZipContent
				FileOutputStream out = new FileOutputStream(mDestination);
				ZipOutputStream zipOut = new ZipOutputStream(out);
				
				Iterator<ZipContent> entries = mZipEntries.values().iterator();
				while (entries.hasNext()) {
					ZipContent content = entries.next();
					content.writeContentToZip(zipOut);
				}
				
				// Add the manifest to the zip
				ZipContent manifest = new ZipContent("META-INF/manifest.xml", manifestFile); //$NON-NLS-1$
				manifest.writeContentToZip(zipOut);
				
				// close the streams
				zipOut.close();
				out.close();
				
			} catch (Exception e) {
				PluginLogger.error(Messages.getString("UnoPackage.PackageCreationError"), e); //$NON-NLS-1$
			}
			
			// remove the temporary files
			for (File file : mTemporaryFiles) {
				FileHelper.remove(file);
			}
			
			result = mDestination;
	
			dispose();
		}
		return result;
	}
	
	/**
	 * @return a list of the files that are already queued for addition 
	 * 		to the package.
	 */
	public List<File> getContainedFiles() {
		ArrayList<File> files = new ArrayList<File>(mZipEntries.size());
		for (ZipContent content : mZipEntries.values()) {
			files.add(content.getFile());
		}
		return files;
	}
	
	/**
	 * Checks if the resource is contained in the UNO package
	 * @param res the resource to check
	 * @return <code>true</code> if the resource is contained in the package
	 */
	public static boolean isContainedInPackage(IResource res) {
		boolean contained = false;
		
		String prjName = res.getProject().getName();
		IUnoidlProject prj = ProjectsManager.getProject(prjName); 
		
		if (prj != null) {
			
			File outputDir = new File(System.getProperty("user.home")); //$NON-NLS-1$
			
			IPath prjPath = prj.getProjectPath();
			File dir = new File(prjPath.toOSString());
			File dest = new File(outputDir, prj.getName() + ".zip"); //$NON-NLS-1$
			UnoPackage unoPackage = UnoidlProjectHelper.createMinimalUnoPackage(prj, dest, dir);
			
			List<File> files = unoPackage.getContainedFiles();
			String path = res.getLocation().toString();
			int i = 0;
			while (i < files.size() && !contained) {
				File file = files.get(i);
				if (file.getPath().equals(path)) {
					contained = true;
				}
				i++;
			}
			unoPackage.dispose();
		}
		
		return contained;
	}
	
	/**
	 * Checks if the resource is contained in the UNO package
	 * @param res the resource to check
	 * @return <code>true</code> if the resource is contained in the package
	 */
	public static List<IResource> getContainedFile(IProject prj) {
		ArrayList<IResource> resources = new ArrayList<IResource>();
		
		String prjName = prj.getName();
		IUnoidlProject unoprj = ProjectsManager.getProject(prjName); 
		
		if (unoprj != null) {
			
			File outputDir = new File(System.getProperty("user.home")); //$NON-NLS-1$
			
			IPath prjPath = unoprj.getProjectPath();
			File dir = new File(prjPath.toOSString());
			File dest = new File(outputDir, prj.getName() + ".zip"); //$NON-NLS-1$
			UnoPackage unoPackage = UnoidlProjectHelper.createMinimalUnoPackage(unoprj, dest, dir);
			
			List<File> files = unoPackage.getContainedFiles();
			
			// Convert the Files into IResources
			for (File file : files) {
				String relPath = unoPackage.getOriginRelativePath(file);
				IFile iFile = prj.getFile(relPath);
				if (iFile.exists()) {
					resources.add(iFile);
				} else if (prj.getFolder(relPath).exists()) {
					resources.add(prj.getFolder(relPath));
				}
			}
			
			unoPackage.dispose();
		}
		
		return resources;
	}
	
	/**
	 * Creates the main elements for the package creation. After this step, the
	 * extension cannot be changed. Calling this method when the package has
	 * already been initialized does nothing.
	 * 
	 */
	private void initializeOutput() {
		mBuilding = true;
	}
	
	/**
	 * Computes the manifest entry and add it to the Manifest entries table
	 * 
	 * @param relativePath the destination relative path of the file
	 * @param mediaType the media type of the file, as specified in the OOo
	 * 		developer's guide
	 */
	private void addManifestEntry(String relativePath, String mediaType) {
		if (relativePath != null) {
			String entry = "<manifest:file-entry"; //$NON-NLS-1$
			entry += " manifest:full-path=\"" + relativePath + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			entry += " manifest:media-type=\"" + mediaType + "\""; //$NON-NLS-1$ //$NON-NLS-2$
			entry += "/>"; //$NON-NLS-1$

			mManifestEntries.put(relativePath, entry);
		}
	}
	
	/**
	 * Recursively add the file or directory to the Zip entries.
	 * 
	 * @param relativePath the relative path of the file to add
	 * @param file the file or directory to add
	 */
	private void addZipContent(String relativePath, File file) {
		if (relativePath != null) {
			if (file.isDirectory()) {
				// Add all the children
				String[] children = file.list();
				for (String child : children) {
					if (!child.equals(".") && !child.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
						addZipContent(relativePath + "/" + child, new File(file, child)); //$NON-NLS-1$
					}
				}
			} else {
				ZipContent content = new ZipContent(relativePath, file);
				mZipEntries.put(relativePath, content);
			}
		}
	}
	
	/**
	 * Returns the path of the file relatively to the origin directory. If the
	 * file is not contained in the origin directory, null is returned.
	 * 
	 * @param file the file of which to get the relative path
	 * @return the relative path using "/" separators or <code>null</code> if
	 * 		the file isn't contained in the origin directory.
	 */
	private String getOriginRelativePath(File file) {
		String path = file.getAbsolutePath();
		String originPath = mOrigin.getAbsolutePath();
		
		String relativePath = null;
		
		if (path.startsWith(originPath)) {
			relativePath = path.substring(originPath.length()+1);
			relativePath = relativePath.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return relativePath;
	}
}
