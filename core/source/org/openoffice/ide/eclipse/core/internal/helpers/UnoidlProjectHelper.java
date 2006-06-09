package org.openoffice.ide.eclipse.core.internal.helpers;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoFactory;

/**
 * Helper class for UNO-IDL project handling.
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlProjectHelper {

	/**
	 * Project relative path to the build directory
	 */
	public static final String BUILD_BASIS = "build";
	
	/**
	 * Project relative path to the source directory
	 */
	public static final String SOURCE_BASIS = "source";
	
	/**
	 * Project relative path to the urd output folder.
	 */
	public static final String URD_BASIS = BUILD_BASIS + "/urd";
	
	/**
	 * Project relative path to the idl root folder
	 */
	public static final String IDL_BASIS = "idl";
	
	/**
	 * This method creates and opens the project with the Java and Uno natures
	 * 
	 * @param project project to create
	 * @param monitor monitor used to report the creation state
	 */
	public static void createProject(IProject project, 
			IProgressMonitor monitor) {
		try {
			if (!project.exists()){
				project.create(monitor);
				PluginLogger.getInstance().debug("Project resource created: " + 
						project.getName());
			}
			
			if (!project.isOpen()){
				project.open(monitor);
				PluginLogger.getInstance().debug("Project is opened: " + 
						project.getName());
			}
			
			IProjectDescription description = project.getDescription();
			String[] natureIds = description.getNatureIds();
			String[] newNatureIds = new String[natureIds.length+1];
			System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);
			
			// Adding the Uno Nature
			newNatureIds[natureIds.length] = OOEclipsePlugin.UNO_NATURE_ID;
			
			description.setNatureIds(newNatureIds);
			project.setDescription(description, monitor);
			PluginLogger.getInstance().debug("UNO-IDL nature set");
			
			UnoidlProject unoProject = (UnoidlProject)project.getNature(
					OOEclipsePlugin.UNO_NATURE_ID);
			ProjectsManager.getInstance().addProject(unoProject);
			
		} catch (CoreException e) {
			PluginLogger.getInstance().error(
				OOEclipsePlugin.getTranslationString(
					I18nConstants.NATURE_SET_FAILED), e);
		}
	}
	
	/**
	 * Deletes the given project resources (for ever)
	 */
	public static void deleteProject(IUnoidlProject unoProject,
			IProgressMonitor monitor){
		if (unoProject != null) {
			try {
				((UnoidlProject)unoProject).getProject().delete(true, true, monitor);
			} catch (CoreException e) {
				// Nothing to do
			}
		}
	}
	
	/**
	 * Set the project builders and run the build
	 */
	public static void setProjectBuilders(IUnoidlProject unoProject,
			IProgressMonitor monitor){
		
		UnoidlProject project = (UnoidlProject)unoProject;
		try {
			// Add the project builders
			project.setBuilders();
			
			// Initial build of the project 
			project.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
		} catch (CoreException e) {
			PluginLogger.getInstance().error(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.NOT_UNO_PROJECT), e);
		}
	}
	
	/**
	 * Refreshes the given uno project
	 */
	public static void refreshProject(IUnoidlProject unoproject,
			IProgressMonitor monitor) {
		
		if (unoproject != null){
			try {
				((UnoidlProject)unoproject).getProject().refreshLocal(
					IResource.DEPTH_INFINITE, monitor);
			} catch(CoreException e) {
			}
		}
	}
	
	/**
	 * Creates the modules directories with the module fully qualified name.
	 * 
	 * @param fullName module fully qualified name (eg: 
	 * 			<code>foo::bar</code>)
	 * @param unoproject the uno project on which to perform the action
	 * @param monitor a progress monitor
	 */
	public static void createModules(String fullName, IUnoidlProject unoproject,
						IProgressMonitor monitor) throws Exception {
		
		if (fullName != null && !fullName.equals("")) {
			// Create the directories
			IUnoComposite moduleDir = UnoFactory.createModuleDir(
					fullName, unoproject);
			moduleDir.create(true);
			
			((UnoidlProject)unoproject).getProject().refreshLocal(
					IProject.DEPTH_INFINITE, monitor);
			
			// Get all the folders for the modules
			String[] modules = fullName.split("::");
			IFolder currentFolder = unoproject.getFolder(unoproject.getIdlPath());
			
			for (int i=0; i<modules.length; i++) {
				currentFolder = currentFolder.getFolder(modules[i]);
				
				// Sets the IDL Capable property
				if (currentFolder.exists()){
					currentFolder.setPersistentProperty(
							new QualifiedName(
									OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
									IUnoidlProject.IDL_FOLDER), "true");
				}
			}
		} else {
			throw new Exception(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FOLDER_CREATION_FAILED));
		}
	}
	
	/**
	 * This method creates the folder described by the company prefix. 
	 * It assumes that the company prefix is already set, otherwise no
	 * package will be created
	 * 
	 * @param unoproject the uno project on which to perform the action
	 * @param monitor progress monitor
	 */
	public static void createUnoidlPackage(IUnoidlProject unoproject,
			IProgressMonitor monitor) {
		
		try {
			if (null != unoproject.getRootModule().replaceAll("::", ".")){

				PluginLogger.getInstance().debug("Creating unoidl packages");
				
				IFolder basis = unoproject.getFolder(unoproject.getIdlPath());
				if (!basis.exists()) {
					basis.create(true, true, monitor);
					PluginLogger.getInstance().debug(
							"Unoidl base directory created");
				}
				
				// Adds the idl capable property
				basis.setPersistentProperty(
						new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								IUnoidlProject.IDL_FOLDER), "true");
				
				createModules(unoproject.getRootModule(), unoproject, monitor);
				PluginLogger.getInstance().debug(
						"All the modules dir have been created");
			}
		} catch (Exception e) {
			PluginLogger.getInstance().error(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FOLDER_CREATION_FAILED)+
							unoproject.getRootModulePath().toString(),
					e);
		}
	}

	/**
	 * Method that creates the directory where to produce the code
	 * 
	 * @param unoproject the uno project on which to perform the action
	 * @param monitor monitor to report
	 */
	public static void createCodePackage(IUnoidlProject unoproject,
			IProgressMonitor monitor) {

		try {
			PluginLogger.getInstance().debug("Creating source directories");
			// Create the sources directory
			IFolder codeFolder = unoproject.getFolder(SOURCE_BASIS);
			if (!codeFolder.exists()){
				codeFolder.create(true, true, monitor);
				PluginLogger.getInstance().debug(
					"source folder created");
			}
			
			// Create the implementation directory
			IPath implementationPath = unoproject.getImplementationPath();
			String tmpPath = "";
			
			for (int i=0, length=implementationPath.segmentCount(); i<length; i++){
				tmpPath = tmpPath + "/" + implementationPath.segment(i);
				IFolder folder = unoproject.getFolder(tmpPath);
				
				// create the folder
				if (!folder.exists()){
					folder.create(true, true, monitor);
					PluginLogger.getInstance().debug(
						folder.getName() + " folder created");
				}
			}
			
			unoproject.getLanguage().addLanguageDependencies(unoproject,
					((UnoidlProject)unoproject).getProject(), monitor);
			PluginLogger.getInstance().debug("Language dependencies added");
			
			unoproject.getLanguage().addOOoDependencies(unoproject.getOOo(),
					((UnoidlProject)unoproject).getProject());
			PluginLogger.getInstance().debug("OOo dependencies added");
			
		} catch (CoreException e) {
			PluginLogger.getInstance().error(
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FOLDER_CREATION_FAILED) + SOURCE_BASIS,
					e);
		}
		
	}
	
	/**
	 * Creates the urd directory
	 * 
	 * @param monitor a progress monitor
	 */
	public static void createUrdDir(IUnoidlProject unoproject,
			IProgressMonitor monitor){
		
		try {
			PluginLogger.getInstance().debug("Creating ouput directories");
			IFolder urdFolder = unoproject.getFolder(URD_BASIS);
			if (!urdFolder.exists()){
				
				String[] basis_dirs = URD_BASIS.split("/");
				String path = "";
				int i = 0;
				while (i < basis_dirs.length){
					
					path = path + basis_dirs[i] + "/";
					IFolder tmpFolder = unoproject.getFolder(path);
					
					if (!tmpFolder.exists()) {
						tmpFolder.create(true, true, monitor);
						tmpFolder.setDerived(true);
						PluginLogger.getInstance().debug(
								tmpFolder.getName() + " folder created");
					}
					i++;
				}
			}
		} catch (CoreException e) {
			PluginLogger.getInstance().error(
				OOEclipsePlugin.getTranslationString(
					I18nConstants.FOLDER_CREATION_FAILED)+ URD_BASIS, e);
		}
	}
	
	/**
	 * Set the IDL property on the IDL folder of the project
	 * 
	 * @param unoproject project on which to set the IDL property
	 */
	public static void setIdlProperty(IUnoidlProject unoproject){
		
		// Get the children of the prefix company folder
		IPath rootPath = unoproject.getRootModulePath();
		if (null != rootPath){
			IFolder unoidlFolder = unoproject.getFolder(rootPath);
		
			recurseSetIdlProperty(unoidlFolder, unoproject);
		}
	}

	/**
	 * Returns the UNO project underlying <code>IProject</code> resource
	 *
	 * @return the underlying <code>IProject</code> or <code>null</code>
	 * 			if the given project is <code>null</code> or any problem
	 * 			appear.
	 */
	public static IProject getProject(IUnoidlProject unoProject) {
		
		IProject project = null;
		if (unoProject != null && unoProject instanceof UnoidlProject) {
			project = ((UnoidlProject)unoProject).getProject();
		}
		
		return project;
	}
	
	/**
	 * Recursion method to set the IDL property
	 *  
	 * @param container the folder on which children to set the property 
	 * @param unoproject the containing project
	 */
	private static void recurseSetIdlProperty(IFolder container,
			IUnoidlProject unoproject){

		try {

			if (container.exists()){
				IResource[] children = container.members();
				
				for (int i=0, length=children.length; i<length; i++){
				
					IResource child = children[i];
					String childPath = child.getProjectRelativePath().toString(); 
					
					// if the child is a folder that is not contained in the code location
					// set it's unoidl property to true
					// and recurse
					if (IResource.FOLDER == child.getType() && 
						!childPath.endsWith(SOURCE_BASIS) &&
						!childPath.endsWith(BUILD_BASIS)){
						
						IFolder folder = (IFolder)child;
						
						// Sets the property
						folder.setPersistentProperty(
								new QualifiedName(
										OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, 
										IUnoidlProject.IDL_FOLDER),
							    "true");
						
						// Recurse
						recurseSetIdlProperty(folder, unoproject);
					}
				}
			}
		} catch (CoreException e) {
			PluginLogger.getInstance().error(
					 OOEclipsePlugin.getTranslationString(
							 I18nConstants.GET_CHILDREN_FAILED) + 
							 container.getName(), e);
		}
	}
}
