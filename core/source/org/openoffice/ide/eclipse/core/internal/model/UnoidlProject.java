/*************************************************************************
 *
 * $RCSfile: UnoidlProject.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:49 $
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
import org.eclipse.core.runtime.QualifiedName;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.builders.TypesBuilder;
import org.openoffice.ide.eclipse.core.internal.helpers.LanguagesHelper;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.ILanguage;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
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
	 * <code>org.openoffice.ide.eclipse.idllocation</code> is a
	 * persistent project property that stores the idl location
	 */
	public static final String IDL_LOCATION = "idllocation"; //$NON-NLS-1$
	
	/**
	 * <code>org.openoffice.ide.eclipse.ouputextension</code> 
	 * is a persistent project property that stores the output
	 * extension
	 */
	public static final String OUTPUT_EXT = "outputextension"; //$NON-NLS-1$

	/**
	 * <code>org.openoffice.ide.eclipse.sdkname</code> 
	 * is a persistent project property that stores the 
	 * sdk name
	 */
	public static final String SDK_NAME = "sdkname"; //$NON-NLS-1$
	
	/**
	 * <code>org.openoffice.ide.eclipse.oooname</code> 
	 * is a persistent project property that stores the 
	 * ooo name
	 */
	public static final String OOO_NAME = "oooname"; //$NON-NLS-1$
	
	/**
	 * <code>org.openoffice.ide.eclipse.language</code>
	 * is a persistent project property that stores the
	 * language name. This will help reloading the right
	 * class to reconfigure the project nature.
	 */
	public static final String LANGUAGE = "language"; //$NON-NLS-1$
	
	/**
	 * <code>org.openoffice.ide.eclipse.unoproject</code> 
	 * is a persistent project property that indicates that 
	 * the project supports the uno nature
	 */
	public final static String UNO_PROJECT = "unoproject"; //$NON-NLS-1$
	
	private IProject mProject;
	
	private String mCompanyPrefix;

	private String mOutputExtension;

	private ISdk mSdk;

	private IOOo mOOo;
	
	private ILanguage mLanguage;
	
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
					getLanguage().removeOOoDependencies(getOOo(), getProject());
					
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
					getLanguage().removeOOoDependencies(getOOo(), getProject());
					getLanguage().addOOoDependencies(getOOo(), getProject());
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
		
		SDKContainer.getInstance().addListener(mConfigListener);
		OOoContainer.getInstance().addListener(mConfigListener);
	}
	
	public void dispose() {
		SDKContainer.getInstance().removeListener(mConfigListener);
		OOoContainer.getInstance().removeListener(mConfigListener);
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
			mLanguage.addProjectNature(getProject());
			PluginLogger.debug("Language specific nature added"); //$NON-NLS-1$
			
			try {
				getProject().setPersistentProperty(
						new QualifiedName(
								OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								LANGUAGE), 
								LanguagesHelper.getNameFromLanguage(mLanguage));
				PluginLogger.debug(
						"Persistent language property set"); //$NON-NLS-1$
			} catch (CoreException e) {
				PluginLogger.error(
					Messages.getString("UnoidlProject.SetOOoError")+getName(), e); //$NON-NLS-1$
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setOOo(org.openoffice.ide.eclipse.preferences.IOOo)
	 */
	public void setOOo(IOOo ooo) {
		
		if (null != ooo && null != getSdk()){
			try {
				getProject().deleteMarkers(IMarker.PROBLEM, true,
					IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				PluginLogger.error(
					Messages.getString("UnoidlProject.RemoveMarkerError"), e); //$NON-NLS-1$
			}
		} else if (null == ooo && null != getSdk()){
			// Toggle ooo error marker if it doesn't exist
			IProject prjRes = getProject();
			try {
				IMarker marker = prjRes.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.MESSAGE, 
					Messages.getString("UnoidlProject.NoOOoSdkError")); //$NON-NLS-1$
			} catch (CoreException e){
				PluginLogger.error(
					Messages.getString("UnoidlProject.CreateMarkerError") +  //$NON-NLS-1$
								getProjectPath().toString(), e);
			}
		}
		
		this.mOOo = ooo;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							OOO_NAME), ooo.getName());
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProject.SetOOoError")+getName(), e); //$NON-NLS-1$
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setSdk(org.openoffice.ide.eclipse.preferences.ISdk)
	 */
	public void setSdk(ISdk sdk) {
		
		if (sdk != null && null != getOOo()) {
			try {
				getProject().deleteMarkers(IMarker.PROBLEM, true,
						IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				PluginLogger.error(
					Messages.getString("UnoidlProject.RemoveMarkerError"), e); //$NON-NLS-1$
			}
		} else if (null == sdk && null != getOOo()) {
			// Toggle error marker if SDK doesn't exist
			IProject prjRes = getProject();
			try {
				IMarker marker = prjRes.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
				marker.setAttribute(IMarker.MESSAGE, 
					Messages.getString("UnoidlProject.NoOOoSdkError")); //$NON-NLS-1$
			} catch (CoreException e){
				PluginLogger.error(
					Messages.getString("UnoidlProject.CreateMarkerError") +  //$NON-NLS-1$
							getProjectPath().toString(), e);
			}
		}
		
		this.mSdk = sdk;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							SDK_NAME), sdk.getId());
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProject.SetSdkError")+getName(), e); //$NON-NLS-1$
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#addProperty(org.eclipse.core.runtime.QualifiedName, java.lang.String)
	 */
	public void addProperty(QualifiedName name, String value) {
		try {
			if (name != null) {
				getProject().setPersistentProperty(name, value);
			}
		} catch (CoreException e) {
			PluginLogger.error(
					Messages.getString("UnoidlProject.AddPropertyError") + name, e); //$NON-NLS-1$
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.IUnoidlProject#getProperty(org.eclipse.core.runtime.QualifiedName)
	 */
	public String getProperty(QualifiedName name) {
		
		String value = null;
		
		try {
			if (name != null) {
				value = getProject().getPersistentProperty(name);
			}
		} catch (CoreException e) {
			PluginLogger.error(
					Messages.getString("UnoidlProject.GetPropertyError") + name, e); //$NON-NLS-1$
		}
		return value;
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
	public void setCompanyPrefix(String prefix){
		mCompanyPrefix = prefix;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							IDL_LOCATION), prefix);
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProject.SetIdlError")+getName(), e); //$NON-NLS-1$
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#setOutputExtension(java.lang.String)
	 */
	public void setOutputExtension(String outputExt){
		mOutputExtension = outputExt;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							OUTPUT_EXT), mOutputExtension);
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProject.SetOutputError")+getName(), e); //$NON-NLS-1$
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getBuildPath()
	 */
	public IPath getBuildPath(){
		return getFolder(UnoidlProjectHelper.BUILD_BASIS).getProjectRelativePath();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getIdlPath()
	 */
	public IPath getIdlPath() {
		return getFolder(UnoidlProjectHelper.IDL_BASIS).getProjectRelativePath();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getImplementationPath()
	 */
	public IPath getImplementationPath(){
		String path = new String(mCompanyPrefix+"."+mOutputExtension).replace('.', '/'); //$NON-NLS-1$
		return getFolder(UnoidlProjectHelper.SOURCE_BASIS).getProjectRelativePath().append(path);
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
		return getFolder(UnoidlProjectHelper.SOURCE_BASIS).getProjectRelativePath();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.IUnoidlProject#getUrdPath()
	 */
	public IPath getUrdPath(){
		return getFolder(UnoidlProjectHelper.URD_BASIS).getProjectRelativePath();
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

	
	
	//*************************************************************************
	// IProjectNature Implementation
	//*************************************************************************
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		
		// Load all the persistent properties into the members
		String sdkKey = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, SDK_NAME));
		mSdk = SDKContainer.getInstance().getSDK(sdkKey);
		
		String oooKey = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, OOO_NAME));
		mOOo = OOoContainer.getInstance().getOOo(oooKey);
		
		String idllocation = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IDL_LOCATION));
		mCompanyPrefix = idllocation;
		
		String outputExt = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, OUTPUT_EXT));
		mOutputExtension = outputExt;
		
		String languageName = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, LANGUAGE));
		setLanguage(LanguagesHelper.getLanguageFromName(languageName));
		
		setBuilders();
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
			ICommand[] newCommands = new ICommand[1];
		
			ICommand typesbuilderCommand = descr.newCommand();
			typesbuilderCommand.setBuilderName(TypesBuilder.BUILDER_ID);
			newCommands[0] = typesbuilderCommand;
			
			descr.setBuildSpec(newCommands);
			getProject().setDescription(descr, null);
		}
	}
	
	public String toString() {
		return "UNO Project " + getName(); //$NON-NLS-1$
	}
}
