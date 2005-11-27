/*************************************************************************
 *
 * $RCSfile: UnoidlProject.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:14 $
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
package org.openoffice.ide.eclipse.model;

import java.io.File;
import java.util.Vector;

import org.eclipse.core.resources.ICommand;
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
import org.openoffice.ide.eclipse.builders.JavamakerBuilder;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.preferences.ConfigListener;
import org.openoffice.ide.eclipse.preferences.ooo.OOo;
import org.openoffice.ide.eclipse.preferences.ooo.OOoContainer;
import org.openoffice.ide.eclipse.preferences.sdk.SDK;
import org.openoffice.ide.eclipse.preferences.sdk.SDKContainer;

/**
 * This class is used to mark projects as UNO-IDL ones.
 * TODOC fields to be detailled
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlProject extends TreeNode implements IProjectNature, 
									                   ConfigListener {
	
	public final static String[] KEPT_JARS = {
		"unoil.jar",
		"ridl.jar",
		"juh.jar",
		"unoloader.jar",
		"officebean.jar"
	};

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
	 * <code>org.openoffice.ide.eclipse.oooname</code> 
	 * is a persistent project property that stores the 
	 * ooo name
	 */
	public static final String OOO_NAME = "oooname";
	
	/**
	 * <code>org.openoffice.ide.eclipse.unoproject</code> 
	 * is a persistent project property that indicates that 
	 * the project supports the uno nature
	 */
	public final static String UNO_PROJECT = "unoproject";
	
	/**
	 * Project relative path to the urd output folder.
	 */
	public static final String URD_BASIS = "urd";
	
	/**
	 * Project relative path to the idl root folder
	 */
	public static final String IDL_BASIS = "idl";
	
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
	 * selected OOo for the project
	 */
	private OOo ooo;
	
	/**
	 * chosen programming language to use for code generation.
	 * Default is Java.
	 */
	private int language = JAVA_LANGUAGE;
	
	public UnoidlProject() {
		super(UnoidlModel.getUnoidlModel(), ""); // The name will be set later
		
		SDKContainer.getSDKContainer().addListener(this);
		OOoContainer.getOOoContainer().addListener(this);
	}
	
	//------------ Unoidl properties Getters and Setters
	
	/**
	 * Returns the project relative path of the base containing the idl definitions
	 * 
	 * @return IPath to the idl base folder, or <code>null</code> if the company prefix is not set
	 */
	public IPath getUnoidlPrefixPath(){
		IPath result = null;
		
		if (null != companyPrefix){
			result = project.getFolder(IDL_BASIS + "/" + 
								companyPrefix.replace('.', '/')).getProjectRelativePath();
		}
		return result;
	}
	
	public IPath getUnoidlLocation() {
		return project.getFolder(IDL_BASIS).getProjectRelativePath();
	}
	
	public String getRootScopedName(){
		String result = "";
		
		if (null != companyPrefix) {
			result = companyPrefix.replaceAll("\\.", "::");
		}
		return result;
	}
	
	/**
	 * Returns the project relative path to the project registry file
	 * 
	 * @return path to the registry file
	 */
	public IPath getRegistryPath() {
		return new Path(getProject().getName() + ".rdb");
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
	 * Returns the path to the directory containing the code
	 * 
	 * @return path to the code directory
	 */
	public IPath getCodeLocation(){
		return project.getFolder("source").getProjectRelativePath();
	}
	
	/**
	 * Path to the implementation directory
	 * 
	 * @return path to the implentation directory
	 */
	public IPath getImplementationLocation(){
		String path = new String(companyPrefix+"."+outputExtension).replace('.', '/');
		return getCodeLocation().append(path);
	}
	
	/**
	 * Returns the path to the directory containing the generated urd files
	 * 
	 * @return path to the urd files
	 */
	public IPath getUrdLocation(){
		return new Path (URD_BASIS);
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
	 * Return the path of the file in the idl folder. If the given file doesn't
	 * belong tho the idl folder, <code>null</code> is returned.
	 * 
	 * @param resource resourceof which the idl path is asked
	 * @return idl relative path or <code>null</code>
	 */
	public IPath getIdlRelativePath(IResource resource){
		IPath result = null;
		
		IPath projectRelative = resource.getProjectRelativePath();
		
		if (projectRelative.toString().startsWith(IDL_BASIS)){
			result = projectRelative.removeFirstSegments(IDL_BASIS.split("/").length);
		}
		return result;
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
	
	/**
	 * Gets the selected OOo
	 * 
	 * @return selected OOo
	 */
	public OOo getOOo() {
		return ooo;
	}
	
	/**
	 * Sets the selected OOo
	 * 
	 * @param ooo new selected OOo
	 */
	public void setOOo(OOo ooo) {
		this.ooo = ooo;
		try {
			getProject().setPersistentProperty(
					new QualifiedName(
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							OOO_NAME), ooo.getId());
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.SET_OOONAME_FAILED)+getProject().getName(), e);
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
		
		String oooKey = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, OOO_NAME));
		ooo = OOoContainer.getOOoContainer().getOOo(oooKey);
		
		String idllocation = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IDL_LOCATION));
		companyPrefix = idllocation;
		
		
		String outputExt = getProject().getPersistentProperty(new QualifiedName(
				OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, OUTPUT_EXT));
		outputExtension = outputExt;
		
		setBuilders();
	}

	public void setBuilders() throws CoreException {
		if (!(null == sdk || null == ooo || 
				null == companyPrefix || null == outputExtension)){
		
			/* 
			 * Add the builder at the first place
			 */
			
			// Get the project description
			IProjectDescription descr = getProject().getDescription();
			ICommand[] newCommands = new ICommand[2];
		
			// creates the code generation command
			switch (getOutputLanguage()){
				case CPP_LANGUAGE:
					// TODO Add the cppmaker builder here
					break;
					
				case PYTHON_LANGUAGE:
					// TODO See what to add for python here
					break;
			
				case JAVA_LANGUAGE:
				default:
					// Add javamaker builder here
					ICommand javamakerCommand = descr.newCommand();
					javamakerCommand.setBuilderName(JavamakerBuilder.BUILDER_ID);
					newCommands[0] = javamakerCommand;
					
					ICommand javaCommand = descr.newCommand();
					javaCommand.setBuilderName(JavaCore.BUILDER_ID);
					newCommands[1] = javaCommand;
					
					break;
			}
	
			descr.setBuildSpec(newCommands);
			getProject().setDescription(descr, null);
		}
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
		if (null == this.project ||
				!project.getName().equals(this.project.getName())) {
			try {
				setName(project.getName());
				moveFind(null, project.getName());
			} catch (TreeException e) {
				if (null != System.getProperty("DEBUG")) {
					System.out.println("TreeException (" 
							+ e.getCode() + "): " + e.getMessage());
				}
			}
		}
		
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
	
	public void createModules(ScopedName moduleName, 
						IProgressMonitor monitor) throws TreeException {

		try {
		
			String[] directories = moduleName.getSegments();
			
			Vector modules = new Vector();
			IFolder folder = getProject().getFolder(IDL_BASIS);
			
			for (int i=0, length=directories.length; i<length; i++){
			
				// Create the folder if necessary
				folder = folder.getFolder(directories[i]);
				if (!folder.exists()) {
					folder.create(true, true, monitor);
				}
				
				// Adds the idl capable property
				folder.setPersistentProperty(
						new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								IDL_FOLDER), "true");
				
				// gets the parent node
				TreeNode parent = null;
				if (modules.size() == 0) {
					parent = this;
				} else {
					parent = (TreeNode)modules.lastElement();
				}
				
				String path = parent.getPath() + parent.getSeparator() + 
								directories[i];
				TreeNode node = getTreeRoot().findNode(path);
				
				if (null == node) {
					// Node creation
					node = new Module(parent, directories[i]);
					parent.addNode(node);
					
				} else if (!(node instanceof Module)) {
					// The found node isn't a module, throw a TreeException
					throw new TreeException(TreeException.BAD_TYPE_NODE,
							"Node already exists with another type: " + path);
				}
				
				modules.add(node);
				
			}
		} catch (CoreException e) {
			if (null != System.getProperty("DEBUG")){
				e.printStackTrace();
			}
			
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FOLDER_CREATION_FAILED)+
								getUnoidlPrefixPath().toString(),
					e);
		}
		
	}
	
	/**
	 * This method creates the folder described by the company prefix. 
	 * It assumes that the company prefix is already set, otherwise no
	 * package will be created
	 * 
	 * @param monitor progress monitor
	 */
	public void createUnoidlPackage(IProgressMonitor monitor) {
		
		try {
			if (null != companyPrefix){
			
				IFolder basis = getProject().getFolder(IDL_BASIS);
				if (!basis.exists()) {
					basis.create(true, true, monitor);
				}
				
				// Adds the idl capable property
				basis.setPersistentProperty(
						new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								IDL_FOLDER), "true");
				
				createModules(
						new ScopedName(companyPrefix.replace(".", "::")), 
						monitor);
			}
		} catch (Exception e) {
			if (null != System.getProperty("DEBUG")){
				e.printStackTrace();
			}
			
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FOLDER_CREATION_FAILED)+
						getUnoidlPrefixPath().toString(),
					e);
		}
	}

	/**
	 * Method that creates the directory where to produce the code
	 * 
	 * @param monitor monitor to report
	 */
	public void createCodePackage(IProgressMonitor monitor) {

		try {
			// Create the sources directory
			IFolder codeFolder = getProject().getFolder(getCodeLocation());
			if (!codeFolder.exists()){
				codeFolder.create(true, true, monitor);
			}
			
			// Create the implementation directory
			IPath implementationPath = getImplementationLocation();
			String tmpPath = "";
			
			for (int i=0, length=implementationPath.segmentCount(); i<length; i++){
				tmpPath = tmpPath + "/" + implementationPath.segment(i);
				IFolder folder = project.getFolder(tmpPath);
				
				// create the folder
				if (!folder.exists()){
					folder.create(true, true, monitor);
				}
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
							getProject().getFolder(getCodeLocation()).getFullPath());
					entries[0] = entry;
					
					javaProject.setRawClasspath(entries, monitor);
					
					
					// Adds the jars contained in the OOo program folder
					// to the class path.
					addJarsFromOOo();
					
				default:
					break;
			}
		} catch (CoreException e) {
			OOEclipsePlugin.logError(
					OOEclipsePlugin.getTranslationString(I18nConstants.FOLDER_CREATION_FAILED) + getCodeLocation().toString(),
					e);
		}
		
	}
	
	/**
	 * Creates the urd directory
	 * 
	 * @param monitor
	 */
	public void createUrdDir(IProgressMonitor monitor){
		
		try {
			IFolder urdFolder = getProject().getFolder(URD_BASIS);
			if (!urdFolder.exists()){
				urdFolder.create(true, true, monitor);
				urdFolder.setDerived(true);
			}
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.FOLDER_CREATION_FAILED)+ URD_BASIS, e);
		}
	}
	
	//	---------------- Implementation for the ConfigListener
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ConfigListener#ConfigAdded(java.lang.Object)
	 */
	public void ConfigAdded(Object element) {
		// the selected SDK or OOo cannot be added again...
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.sdk.SDKListener#SDKRemoved(org.openoffice.ide.eclipse.preferences.sdk.SDK)
	 */
	public void ConfigRemoved(Object element) {
		if (element instanceof SDK){
			if (element == this.sdk){

				// Sets the selected OOo to null, it will tag the project as invalid
				this.sdk = null;
				// TODO add a problem marker
			}
		} else if (element instanceof OOo) {
			if (element == this.ooo){
				
				// Removes all the jars
				removeJars();
				
				// Sets the selected SDK to null, it will tag the project as invalid
				this.ooo = null;
				// TODO add a problem marker
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.sdk.SDKListener#SDKUpdated(org.openoffice.ide.eclipse.preferences.sdk.SDK)
	 */
	public void ConfigUpdated(Object element) {
		if (element instanceof OOo){
			if (element == this.ooo){
				// the ooo is updated thanks to it's reference. Remove the old jar files
				// from the classpath and the new ones
				
				removeJars();
				addJarsFromOOo();
			}
		}
	}

	/**
	 * Adds the jars from the ooo to the classpath of the javaproject
	 *
	 */
	private void addJarsFromOOo(){

		IJavaProject javaProject = JavaCore.create(getProject());
		
		if (null != ooo){
			// Find the jars in the first level of the directory
			Vector jarPaths = findJarsFromPath(ooo.getClassesPath());
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
	 * returns the path of all the kept jars contained in the folder pointed by path.
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
			if (isKeptJar(contenti)){
				Path jariPath = new Path (ooo.getClassesPath()+"/"+contenti);
				jarsPath.add(jariPath);
			}
		}
		
		return jarsPath;
	}
	
	/**
	 * Check if the specified jar file is one of those define in the KEPT_JARS constant
	 * 
	 * @param jarName name of the jar file to check
	 * @return <code>true</code> if jarName is one of those defined in KEPT_JARS, 
	 *         <code>false</code> otherwise.
	 */
	private boolean isKeptJar(String jarName){
		
		int i = 0;
		boolean isKept = false;
		
		while (i<KEPT_JARS.length && !isKept){
			if (jarName.equals(KEPT_JARS[i])){
				isKept = true;
			} else {
				i++;
			}
		}
		return isKept;
	}
	
	/**
	 * 
	 *
	 */
	public void setIdlProperty(){
	
		// Get the children of the prefix company folder
		IPath prefixFolder = getUnoidlPrefixPath();
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
