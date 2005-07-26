/*************************************************************************
 *
 * $RCSfile: UnoidlProject.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/26 06:24:00 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.model;

import java.io.File;
import java.util.Vector;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.preferences.sdk.SDK;
import org.openoffice.ide.eclipse.preferences.sdk.SDKContainer;
import org.openoffice.ide.eclipse.preferences.sdk.SDKListener;
import org.openoffice.ide.eclipse.wizards.NewUnoFilePage;

/**
 * This class is used to mark projects as UNO-IDL ones.
 * TODOC fields to be detailled
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlProject implements IProjectNature, SDKListener{

	/**
	 * Constant defining java as the project's language
	 */
	public static final int JAVA_LANGUAGE   = 0;
	
	/**
	 * Constant defining C++ as the project's language
	 */
	public static final int CPP_LANGUAGE    = 1;
	
	/**
	 * Constant defining Python as the project's language
	 */
	public static final int PYTHON_LANGUAGE = 2;

	/**
	 * <code>org.openoffice.ide.eclipse.idlfolder</code> is a
	 * persistent folder property that determines whether the
	 * folder can contain unoidl files or not. 
	 */
	public static final String IDL_FOLDER = "idlfolder";
	
	/**
	 * <code>org.openoffice.ide.eclipse.idllocation</code> is a
	 * persistent project property that stores the idl location
	 */
	public static final String IDL_LOCATION = "idllocation";
	
	/**
	 * <code>org.openoffice.ide.eclipse.ouputextension</code> 
	 * is a persistent project property that stores the output
	 * extension
	 */
	public static final String OUTPUT_EXT = "outputextension";

	/**
	 * <code>org.openoffice.ide.eclipse.sdkname</code> 
	 * is a persistent project property that stores the 
	 * sdk name
	 */
	public static final String SDK_NAME = "sdkname";
	
	/**
	 * <code>org.openoffice.ide.eclipse.unoproject</code> 
	 * is a persistent project property that indicates that 
	 * the project supports the uno nature
	 */
	public final static String UNO_PROJECT = "unoproject";
	
	
	/**
	 * Local reference to the associated project resource
	 */
	private IProject project;
	
	/**
	 * Company prefix value
	 */
	private String companyPrefix;
	
	/**
	 * Ouput extension value
	 */
	private String outputExtension;
	
	/**
	 * selected SDK for the project
	 */
	private SDK sdk;
	
	/**
	 * chosen programming language to use for code generation.
	 * Default is Java.
	 */
	private int language = JAVA_LANGUAGE;
	
	public UnoidlProject() {
		super();
		
		SDKContainer.getSDKContainer().addListener(this);
		
	}
	
	//------------ Unoidl properties Getters and Setters
	
	/**
	 * Returns the project relative path of the base containing the idl definitions
	 * 
	 * @return IPath to the idl base folder, or <code>null</code> if the company prefix is not set
	 */
	public IPath getUnoidlLocation(){
		IPath result = null;
		
		if (null != companyPrefix){
			result = project.getFolder(companyPrefix.replace('.', '/')).getProjectRelativePath();
		}
		return result;
	}
	
	/**
	 * Sets the company prefix
	 * 
	 * @param prefix new company prefix 
	 */
	public void setCompanyPrefix(String prefix){
		companyPrefix = prefix;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							IDL_LOCATION), prefix);
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.SET_IDLLOCATION_FAILED)+getProject().getName(), e);
		}
	}
	
	/**
	 * Sets the output extension
	 * 
	 * @param outputExt new output extension to set
	 */
	public void setOutputExtension(String outputExt){
		outputExtension = outputExt;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							OUTPUT_EXT), outputExtension);
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.SET_OUTPUTEXT_FAILED)+getProject().getName(), e);
		}
	}
	
	/**
	 * Returns the path to the generated code
	 * 
	 * @return path to the generated code
	 */
	public IPath getCodeLocation(){
		String path = new String(companyPrefix+"."+outputExtension).replace('.', '/');
		return project.getFolder(path).getProjectRelativePath();
	}
	
	/**
	 * Gets the selected SDK
	 * 
	 * @return selected SDK
	 */
	public SDK getSdk() {
		return sdk;
	}
	
	/**
	 * Sets the selected SDK
	 * 
	 * @param sdk new selected SDK
	 */
	public void setSdk(SDK sdk) {
		this.sdk = sdk;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							SDK_NAME), sdk.getId());
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.SET_SDKNAME_FAILED)+getProject().getName(), e);
		}
	}
	
	public int getOutputLanguage(){
		return language;
	}
	
	public void setOuputLanguage(int newLanguage) {
		
		switch (newLanguage){
			case CPP_LANGUAGE:
				break;
			case PYTHON_LANGUAGE:
				break;
			case JAVA_LANGUAGE:
			default:
				this.language = newLanguage;
				// TODO Remove all the Python and C++ natures
			
				// Add the Java nature
				addNature(JavaCore.NATURE_ID);
		}
	}
	
	//------------ Nature methods
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {
		
		// Load all the persistent properties into the members
		String sdkKey = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, SDK_NAME));
		sdk = SDKContainer.getSDKContainer().getSDK(sdkKey);
		
		String idllocation = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IDL_LOCATION));
		companyPrefix = idllocation;
		
		
		String outputExt = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, OUTPUT_EXT));
		outputExtension = outputExt;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {
		
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return project;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}
	
	private void addNature(String natureid){
		try {
			if (!project.exists()){
				project.create(null);
			}
			
			if (!project.isOpen()){
				project.open(null);
			}
			
			IProjectDescription description = project.getDescription();
			String[] natureIds = description.getNatureIds();
			String[] newNatureIds = new String[natureIds.length+1];
			System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);
			
			// Adding the nature
			newNatureIds[natureIds.length] = natureid;
			
			description.setNatureIds(newNatureIds);
			project.setDescription(description, null);
			
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.NATURE_SET_FAILED), e);
		}
	}
	
	//------------- Creation methods
	
	/**
	 * This method creates the folder described by the company prefix. 
	 * It assumes that the company prefix is already set, otherwise no
	 * package will be created
	 * 
	 * @param monitor progress monitor
	 */
	public void createUnoidlPackage(IProgressMonitor monitor) {
		
		if (null != companyPrefix){
		
			try {
				// Creation of the idl folders
				String[] directories = companyPrefix.split("\\.");
				String tmpPath = "";
				
				for (int i=0, length=directories.length; i<length; i++){
					tmpPath = tmpPath + "/" + directories[i];
					IFolder folder = project.getFolder(tmpPath);
					
					// create the folder
					folder.create(true, true, monitor);
				}
				
				// Add a persistent property to the folder to remember
				// It is an idl file. All it's subfolder except the code
				// output folder is an idl capable folder too.
				IFolder folder = getProject().getFolder(getUnoidlLocation());
				folder.setPersistentProperty(
						new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, 
										  IDL_FOLDER),
						"true");
				
				// Create a basic new idl file with the project name
				NewUnoFilePage.createUnoidlFile(folder, getProject().getName()+".idl");
				
			} catch (CoreException e) {
				OOEclipsePlugin.logError(
						OOEclipsePlugin.getTranslationString(I18nConstants.FOLDER_CREATION_FAILED)+
							getUnoidlLocation().toString(),
						e);
			}
		}
	}

	/**
	 * Method that creates the directory where to produce the code
	 * 
	 * @param monitor monitor to report
	 */
	public void createCodePackage(IProgressMonitor monitor) {

		try {
			// Create the ouput directory
			IFolder codeFolder = getProject().getFolder(getCodeLocation());
			if (!codeFolder.exists()){
				codeFolder.create(true, true, monitor);
			}
			
			switch (language) {
				case CPP_LANGUAGE:
					// TODO implement the include adding for C++
					break;
					
				case PYTHON_LANGUAGE:
					// TODO implement the import adding for Python
					break;
					
				case JAVA_LANGUAGE:
					IJavaProject javaProject = JavaCore.create(getProject());
					IClasspathEntry[] entries = new IClasspathEntry[1]; 
						
					// Adds the project to the classpath
					IClasspathEntry entry = JavaCore.newSourceEntry(
							getProject().getFullPath());
					entries[0] = entry;
					
					javaProject.setRawClasspath(entries, monitor);
					
					
					// Adds the jars contained in the OOo program folder
					// specified in the SDK to the class path.
					addJarsFromSDK();
					
				default:
					break;
			}
		} catch (CoreException e) {
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(I18nConstants.FOLDER_CREATION_FAILED) + getCodeLocation().toString(),
					e);
		}
		
	}
	
	//---------------- Implementation for the SDKListener
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.sdk.SDKListener#SDKAdded(org.openoffice.ide.eclipse.preferences.sdk.SDK)
	 */
	public void SDKAdded(SDK sdk) {
		// the selected SDK cannot be added again...
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.sdk.SDKListener#SDKRemoved(org.openoffice.ide.eclipse.preferences.sdk.SDK)
	 */
	public void SDKRemoved(SDK sdk) {
		if (sdk == this.sdk){
			
			// Removes all the jars
			removeJars();
			
			// Sets the selected SDK to null, it will tag the project as invalid
			this.sdk = null;
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.sdk.SDKListener#SDKUpdated(org.openoffice.ide.eclipse.preferences.sdk.SDK)
	 */
	public void SDKUpdated(SDK sdk) {
		if (sdk == this.sdk){
			// the sdk is updated thanks to it's reference. Remove the old jar files
			// from the classpath and the new ones
			
			removeJars();
			addJarsFromSDK();
		}
	}

	/**
	 * Adds the jars from the sdk to the classpath of the javaproject
	 *
	 */
	private void addJarsFromSDK(){

		IJavaProject javaProject = JavaCore.create(getProject());
		
		if (null != sdk){
			// Find the jars in the first level of the directory
			Vector jarPaths = findJarsFromPath(sdk.getClassesPath());
			try {
				IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
				IClasspathEntry[] entries = new IClasspathEntry[jarPaths.size()+
				                                                oldEntries.length];
				
				System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
				
				for (int i=0, length=jarPaths.size(); i<length; i++){
					IPath jarPathi = (IPath)jarPaths.get(i);
					IClasspathEntry entry = JavaCore.newLibraryEntry(jarPathi, null, null);
					entries[oldEntries.length+i] = entry;
				}
				
				javaProject.setRawClasspath(entries, null);
			} catch (JavaModelException e){
				OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(I18nConstants.PROJECT_CLASSPATH_ERROR), e);
			}
		}
	}
	
	/**
	 * Removes all the libraries
	 *
	 */
	private void removeJars(){
		IJavaProject javaProject = JavaCore.create(getProject());
		
		try {
			IClasspathEntry[] entries = javaProject.getRawClasspath();
			Vector newEntries = new Vector();

			// Copy all the sources in a new entry container
			for (int i=0, length=entries.length; i<length; i++){
				IClasspathEntry entry = entries[i];

				if (entry.getContentKind() == IPackageFragmentRoot.K_SOURCE){
					newEntries.add(entry);
				}
			}
			
			IClasspathEntry[] result = new IClasspathEntry[newEntries.size()];
			for (int i=0, length=newEntries.size(); i<length; i++){
				result[i] = (IClasspathEntry)newEntries.get(i);
			}
			
			javaProject.setRawClasspath(result, null);
			
		} catch (JavaModelException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(I18nConstants.PROJECT_CLASSPATH_ERROR), e);
		}
		
	}
	
	/**
	 * returns the path of all the jars contained in the folder pointed by path.
	 * 
	 * @param path path of the container folder
	 * @return a vector of Path pointing to each jar.
	 */
	private Vector findJarsFromPath(String path){
		Vector jarsPath = new Vector();
		
		Path folderPath = new Path(path);
		File programFolder = folderPath.toFile();
		
		String[] content = programFolder.list();
		for (int i=0, length=content.length; i<length; i++){
			String contenti = content[i];
			if (contenti.endsWith(".jar")){
				Path jariPath = new Path (sdk.getClassesPath()+"/"+contenti);
				jarsPath.add(jariPath);
			}
		}
		
		return jarsPath;
	}
	
	/**
	 * 
	 *
	 */
	public void setIdlProperty(){
	
			// Get the children of the prefix company folder
			IPath prefixFolder = getUnoidlLocation();
			if (null != prefixFolder){
				IFolder unoidlFolder = getProject().getFolder(prefixFolder);
			
				recurseSetIdlProperty(unoidlFolder);
			}
	}
	
	/**
	 * 
	 * @param container
	 */
	private void recurseSetIdlProperty(IFolder container){

		try {

			if (container.exists()){
				IResource[] children = container.members();
				String codeLocation = getCodeLocation().toString();
				
				for (int i=0, length=children.length; i<length; i++){
				
					IResource child = children[i];
					String childPath = child.getProjectRelativePath().toString(); 
					
					// if the child is a folder that is not contained in the code location
					// set it's unoidl property to true
					// and recurse
					if (IResource.FOLDER == child.getType() && 
						!childPath.endsWith(codeLocation)){
						
						IFolder folder = (IFolder)child;
						
						// Sets the property
						folder.setPersistentProperty(
								new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, 
										          IDL_FOLDER),
							    "true");
						
						// Recurse
						recurseSetIdlProperty(folder);
					}
				}
			}
		} catch (CoreException e) {
			OOEclipsePlugin.logError(
					 OOEclipsePlugin.getTranslationString(I18nConstants.GET_CHILDREN_FAILED) + container.getName(), e);
		}
	}
}
