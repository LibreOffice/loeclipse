/*************************************************************************
 *
 * $RCSfile: UnoidlProject.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/10/11 18:06:18 $
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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.util.Properties;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.builders.TypesBuilder;
import org.openoffice.ide.eclipse.core.internal.helpers.LanguagesHelper;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

/**
 * This class implements the UNO-IDL and project nature interface.
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlProject implements IUnoidlProject, IProjectNature {
	
	/**
	 * Project property that stores the company prefix
	 */
	public static final String COMPANY_PREFIX = "project.prefix"; //$NON-NLS-1$
	
	/**
	 * Project property that stores the output path extension.
	 * 
	 * <p>If the company prefix is <code>org.openoffice.sample</code> and this
	 * property value is <code>impl</code>, the root package of the 
	 * implementations classes is <code>org.openoffice.sample.impl</code>.</p>
	 */
	public static final String OUTPUT_EXT = "project.implementation"; //$NON-NLS-1$

	/**
	 * Project property that stores the sdk name to use for  
	 * the project build
	 */
	public static final String SDK_NAME = "project.sdk"; //$NON-NLS-1$
	
	/**
	 * Project property that stores the name of the OpenOffice.org instance
	 * used to run / deploy the project.
	 */
	public static final String OOO_NAME = "project.ooo"; //$NON-NLS-1$
	
	/**
	 * Project property that stores the language name. 
	 */
	public static final String LANGUAGE = "project.language"; //$NON-NLS-1$
	
	/**
	 * Project property that stores the path to the folder containing
	 * the sources. 
	 */
	public static final String SRC_DIRECTORY = "project.srcdir"; //$NON-NLS-1$
	
	/**
	 * Property name for the idl folder
	 */
	public static final String IDL_DIR = "project.idl"; //$NON-NLS-1$

	/**
	 * Property name for the build directory
	 */
	public static final String BUILD_DIR = "project.build"; //$NON-NLS-1$
	
	/**
	 * The name of the file containing the UNO project configuration
	 */
	private final static String CONFIG_FILE = ".unoproject"; //$NON-NLS-1$
	
	private IProject mProject;
	
	private String mCompanyPrefix;

	private String mOutputExtension;

	private ISdk mSdk;

	private IOOo mOOo;
	
	private ILanguage mLanguage;
	
	private String mIdlDir;
	
	private String mSourcesDir;
	
	private IConfigListener mConfigListener;
	
	/**
	 * Listener for the configuration to handle the changes on SDK and OOo
	 * instances
	 * 
	 * @author cbosdonnat
	 */
	private class configListener implements IConfigListener {
		
		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.preferences.IConfigListener#ConfigAdded(java.lang.Object)
		 */
		public void ConfigAdded(Object element) {
			// the selected SDK or OOo cannot be added again...
		}

		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.preferences.IConfigListener#ConfigRemoved(java.lang.Object)
		 */
		public void ConfigRemoved(Object element) {
			if (element instanceof ISdk){
				if (element == getSdk()){

					// Sets the selected SDK to null, it will tag the project as invalid
					setSdk(null);
				}
			} else if (element instanceof IOOo) {
				if (element == getOOo()){
					
					// Removes OOo dependencies
					getLanguage().getProjectHandler().removeOOoDependencies(getOOo(), getProject());
					
					// Sets the selected OOo to null, it will tag the project as invalid
					setOOo(null);
				}
			}
		}

		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.preferences.IConfigListener#ConfigUpdated(java.lang.Object)
		 */
		public void ConfigUpdated(Object element) {
			if (element instanceof IOOo){
				if (element == getOOo()){
					// the ooo is updated thanks to it's reference. Remove the old jar files
					// from the classpath and the new ones
					
					// Removes OOo dependencies
					getLanguage().getProjectHandler().removeOOoDependencies(
							getOOo(), getProject());
					getLanguage().getProjectHandler().addOOoDependencies(
							getOOo(), getProject());
				}
			}
		}
	}
	
	//------------------------------------------------------------ Constructors
	
	/**
	 * Default constructor initializing the configuration listener
	 */
	public UnoidlProject() {
		
		mConfigListener = new configListener();
		
		SDKContainer.addListener(mConfigListener);
		OOoContainer.addListener(mConfigListener);
	}
	
	public void dispose() {
		SDKContainer.removeListener(mConfigListener);
		OOoContainer.removeListener(mConfigListener);
	}
	
	/**
	 * Return the path of the file in the idl folder. If the given file doesn't
	 * belong tho the idl folder, <code>null</code> is returned.
	 * 
	 * @param resource resourceof which the idl path is asked
	 * @return idl relative path or <code>null</code>
	 */
	public IPath getIdlRelativePath(IResource resource){
		IPath result = null;
		
		IPath projectRelative = resource.getProjectRelativePath();
		
		if (projectRelative.toString().startsWith(getIdlPath().toString())){
			result = projectRelative.removeFirstSegments(
					getIdlPath().segmentCount());
		}
		return result;
	}
	
	
	//*************************************************************************
	// IUnoidlModel Implementation
	//*************************************************************************

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getLanguage()
	 */
	public ILanguage getLanguage(){
		return mLanguage;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getName()
	 */
	public String getName(){
		return getProject().getName();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getOOo()
	 */
	public IOOo getOOo() {
		return mOOo;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getSdk()
	 */
	public ISdk getSdk() {
		return mSdk;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setLanguage(org.openoffice.ide.eclipse.model.ILanguage)
	 */
	public void setLanguage(ILanguage newLanguage) {
		
		if (mLanguage == null && newLanguage != null){
			mLanguage = newLanguage; 
			mLanguage.getProjectHandler().addProjectNature(getProject());
			PluginLogger.debug("Language specific nature added"); //$NON-NLS-1$
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setOOo(org.openoffice.ide.eclipse.preferences.IOOo)
	 */
	public void setOOo(IOOo ooo) {
		
		setErrorMarker(null == ooo || null == getSdk());
		
		this.mOOo = ooo;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setSdk(org.openoffice.ide.eclipse.preferences.ISdk)
	 */
	public void setSdk(ISdk sdk) {
		
		setErrorMarker(sdk == null || null == getOOo());
			
		this.mSdk = sdk;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#setIdlDir(java.lang.String)
	 */
	public void setIdlDir(String idlDir) {
		mIdlDir = idlDir;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#setSourcesDir(java.lang.String)
	 */
	public void setSourcesDir(String sourcesDir) {
		if (sourcesDir == null || sourcesDir.equals("")) {
			sourcesDir = UnoidlProjectHelper.SOURCE_BASIS;
		}
		
		// Add a / at the beginning of the path
		if (!sourcesDir.startsWith("/")) { //$NON-NLS-1$
			sourcesDir = "/" + sourcesDir; //$NON-NLS-1$
		}
		
		mSourcesDir = sourcesDir;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getRootModule()
	 */
	public String getRootModule(){
		String result = ""; //$NON-NLS-1$
		
		if (null != mCompanyPrefix) {
			result = mCompanyPrefix.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getRootModulePath()
	 */
	public IPath getRootModulePath(){
		IPath result = null;
		
		if (null != mCompanyPrefix){
			result = getIdlPath().append(
					mCompanyPrefix.replaceAll("\\.", "/")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return result;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setCompanyPrefix(java.lang.String)
	 */
	public void setCompanyPrefix(String prefix) {
		mCompanyPrefix = prefix;
		System.out.println("Prefix set to : " + mCompanyPrefix);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#getCompanyPrefix()
	 */
	public String getCompanyPrefix() {
		return mCompanyPrefix;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setOutputExtension(java.lang.String)
	 */
	public void setOutputExtension(String outputExt) {
		mOutputExtension = outputExt;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#getOutputExtension()
	 */
	public String getOutputExtension() {
		return mOutputExtension;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getBuildPath()
	 */
	public IPath getBuildPath(){
		String buildDir = getProperty(BUILD_DIR);
		if (!buildDir.startsWith("/")) {
			buildDir = "/" + buildDir;
		}
		
		return getFolder(buildDir).getProjectRelativePath();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getIdlPath()
	 */
	public IPath getIdlPath() {
		String idlDir = getProperty(IDL_DIR);
		if (!idlDir.startsWith("/")) {
			idlDir = "/" + idlDir;
		}
		
		return getFolder(idlDir).getProjectRelativePath();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getImplementationPath()
	 */
	public IPath getImplementationPath(){
		String path = new String(mCompanyPrefix+"."+mOutputExtension).replace('.', '/'); //$NON-NLS-1$
		return getSourcePath().append(path);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getProjectPath()
	 */
	public IPath getProjectPath() {
		return getProject().getLocation();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getTypesPath()
	 */
	public IPath getTypesPath() {
		return new Path("types.rdb"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#getServicesPath()
	 */
	public IPath getServicesPath() {
		return new Path("services.rdb"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getSourcePath()
	 */
	public IPath getSourcePath() {
		String sourcesDir = getProperty(SRC_DIRECTORY);
		return getFolder(sourcesDir).getProjectRelativePath();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getUrdPath()
	 */
	public IPath getUrdPath(){
		return getFolder(getBuildPath().append(UnoidlProjectHelper.URD_BASIS)).getProjectRelativePath();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getFile(org.eclipse.core.runtime.IPath)
	 */
	public IFile getFile(IPath path) {
		return getProject().getFile(path);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getFile(java.lang.String)
	 */
	public IFile getFile(String path) {
		return getProject().getFile(path);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getFolder(org.eclipse.core.runtime.IPath)
	 */
	public IFolder getFolder(IPath path) {
		return getProject().getFolder(path);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getFolder(java.lang.String)
	 */
	public IFolder getFolder(String path) {
		return getProject().getFolder(path);
	}

	/**
	 * @return the uno project configuration file
	 * 
	 * @see #CONFIG_FILE for the configuration file name
	 */
	public File getConfigFile() {
		return new File(getProjectPath().append(CONFIG_FILE).toOSString());
	}
	
	/**
	 * Reads a property from the Uno project configuration file.
	 * 
	 * <p>Returns the property corresponding to the given name. If the
	 * configuration file doesn't exists, a default one will be created.</p>
	 * 
	 * @param propertyName the name of the property to get
	 * @return the property value or <code>null</code> if not found.
	 * 
	 * @see #CONFIG_FILE for the configuration file name
	 */
	public String getProperty(String propertyName) {
		
		Properties properties = new Properties();
		File configFile = getConfigFile();
		String property = null;
		
		try  {
			// Create a default configuration file if needed
			if (!configFile.exists()) {
				UnoidlProjectHelper.createDefaultConfig(configFile);
			}
		
			FileInputStream in = new FileInputStream(configFile);
			properties.load(in);
			property = properties.getProperty(propertyName);
		} catch (Exception e) {
			PluginLogger.warning("Unreadable uno project configuration file " + CONFIG_FILE, e);
		}
		
		return property;
	}
	
	/**
	 * Define a property in the uno project configuration file 
	 * 
	 * @param name the property name
	 * @param value the property value
	 */
	public void setProperty(String name, String value) {
		Properties properties = new Properties();
		File configFile = getConfigFile();
		
		try  {
			// Create a default configuration file if needed
			if (!configFile.exists()) {
				UnoidlProjectHelper.createDefaultConfig(configFile);
			}
		
			FileInputStream in = new FileInputStream(configFile);
			properties.load(in);
		
			properties.setProperty(name, value);
		
			FileOutputStream out = new FileOutputStream(configFile);
			properties.store(out, "UNO project configuration file");
			
			// Refresh the configuration file
			getFile(CONFIG_FILE).refreshLocal(IResource.DEPTH_ZERO, null);
			
		} catch (Exception e) {
			String message = MessageFormat.format("Error during project property change ({0}, {1})", 
					name, value);
			PluginLogger.warning(message, e);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#saveAllProperties()
	 */
	public void saveAllProperties() {
		
		System.out.println("Saving all the properties");
		
		Properties properties = new Properties();
		File configFile = getConfigFile();
		
		// Create a default configuration file if needed
		if (!configFile.exists()) {
			UnoidlProjectHelper.createDefaultConfig(configFile);
		}
		
		try  {
			FileInputStream in = new FileInputStream(configFile);
			properties.load(in);
		
			properties.setProperty(LANGUAGE, LanguagesHelper.getNameFromLanguage(mLanguage));
			properties.setProperty(OOO_NAME, mOOo.getName());
			properties.setProperty(SDK_NAME, mSdk.getId());
			properties.setProperty(IDL_DIR, mIdlDir);
			properties.setProperty(SRC_DIRECTORY, mSourcesDir);
			properties.setProperty(COMPANY_PREFIX, mCompanyPrefix);
			properties.setProperty(OUTPUT_EXT, mOutputExtension);
		
			FileOutputStream out = new FileOutputStream(configFile);
			properties.store(out, "UNO project configuration file");
			
			out.close();
			
			// Refresh the configuration file
			getFile(CONFIG_FILE).refreshLocal(IResource.DEPTH_ZERO, null);
			
		} catch (Exception e) {
			PluginLogger.warning("Error saving all the project properties", e);
		}
	}
	
	//*************************************************************************
	// IProjectNature Implementation
	//*************************************************************************
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		
		// Load all the persistent properties into the members
		
		String sdkKey = getProperty(SDK_NAME);
		if (sdkKey != null) {
			setSdk(SDKContainer.getSDK(sdkKey));
		}
		
		String oooKey = getProperty(OOO_NAME);
		if (oooKey != null) {
			setOOo(OOoContainer.getOOo(oooKey));
		}
		
		String idlDir = getProperty(COMPANY_PREFIX);
		if (idlDir != null) {
			mCompanyPrefix = idlDir;
			System.out.println("Prefix configured to: " + mCompanyPrefix);
		}
		
		String outputExt = getProperty(OUTPUT_EXT);
		if (outputExt != null) {
			mOutputExtension = outputExt;
		}
		
		String languageName = getProperty(LANGUAGE);
		if (languageName != null) {
			setLanguage(LanguagesHelper.getLanguageFromName(languageName));
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		dispose();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return mProject;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject aProject) {
		if (null == mProject ||
				!aProject.getName().equals(getName())) {
		}
		
		mProject = aProject;
	}
	
	
	//*************************************************************************
	// Useful methods for the nature implementation
	//*************************************************************************
	

	/**
	 * Set the builders for the project. This method configures the builders
	 * using the implementation language informations 
	 */
	public void setBuilders() throws CoreException {
		if (!(null == mSdk || null == mOOo || 
				null == mCompanyPrefix || null == mOutputExtension)){
		
			// Set the types builder
			IProjectDescription descr = getProject().getDescription();
			ICommand[] builders = descr.getBuildSpec();
			ICommand[] newCommands = new ICommand[builders.length + 1];
		
			ICommand typesbuilderCommand = descr.newCommand();
			typesbuilderCommand.setBuilderName(TypesBuilder.BUILDER_ID);
			newCommands[0] = typesbuilderCommand;
			
			System.arraycopy(builders, 0, newCommands, 1, builders.length);
			
			descr.setBuildSpec(newCommands);
			getProject().setDescription(descr, null);
		}
	}
	
	public String toString() {
		return "UNO Project " + getName(); //$NON-NLS-1$
	}
	
	private void setErrorMarker(boolean set) {
		
		IProject prjRes = getProject();
		
		try {
			if (set) {
				IMarker marker = prjRes.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.MESSAGE, 
					Messages.getString("UnoidlProject.NoOOoSdkError")); //$NON-NLS-1$
			} else {
				prjRes.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			}
		} catch (CoreException e) {
			if (set) {
				PluginLogger.error(
						Messages.getString("UnoidlProject.CreateMarkerError") +  //$NON-NLS-1$
									getProjectPath().toString(), e);
			} else {
				PluginLogger.error(
						Messages.getString("UnoidlProject.RemoveMarkerError"), e); //$NON-NLS-1$
			}
		}
		
	}
}
