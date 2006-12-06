package org.openoffice.ide.eclipse.core.internal.helpers;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.builders.TypesBuilder;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.CompositeFactory;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

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
	public static final String BUILD_BASIS = "build"; //$NON-NLS-1$
	
	/**
	 * Project relative path to the source directory
	 */
	public static final String SOURCE_BASIS = "source"; //$NON-NLS-1$
	
	/**
	 * Project relative path to the urd output folder.
	 */
	public static final String URD_BASIS = BUILD_BASIS + "/urd"; //$NON-NLS-1$
	
	/**
	 * Project relative path to the idl root folder
	 */
	public static final String IDL_BASIS = "idl"; //$NON-NLS-1$
	
	public static IUnoidlProject createStructure(UnoFactoryData data, 
			IProgressMonitor monitor) throws Exception {
		
		IUnoidlProject unoProject = null;
		
		// Creates the new project whithout it's builders
		IProject project = (IProject)data.getProperty(
				IUnoFactoryConstants.PROJECT_HANDLE);
		createProject(project, monitor);
		unoProject = ProjectsManager.getInstance().getProject(
				project.getName());

		// Set the company prefix
		String prefix = (String)data.getProperty(
				IUnoFactoryConstants.PROJECT_PREFIX);
		unoProject.setCompanyPrefix(prefix);

		// Set the output extension
		String comp = (String)data.getProperty(
				IUnoFactoryConstants.PROJECT_COMP);
		unoProject.setOutputExtension(comp);

		// Set the language
		ILanguage language = (ILanguage)data.getProperty(
				IUnoFactoryConstants.PROJECT_LANGUAGE);
		unoProject.setLanguage(language);

		// Set the SDK
		String sdkname = (String)data.getProperty(
				IUnoFactoryConstants.PROJECT_SDK);
		ISdk sdk = SDKContainer.getInstance().getSDK(sdkname);
		unoProject.setSdk(sdk);

		// Set the OOo runtime
		String oooname = (String)data.getProperty(
				IUnoFactoryConstants.PROJECT_OOO);
		IOOo ooo = OOoContainer.getInstance().getOOo(oooname);
		unoProject.setOOo(ooo);


		// Creation of the unoidl package
		createUnoidlPackage(unoProject, monitor);

		// Creation of the Code Packages
		createCodePackage(unoProject, monitor);

		// Creation of the urd output directory
		createUrdDir(unoProject, monitor);
		
		return unoProject;
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
	
	public static void forceBuild(IUnoidlProject unoProject, 
			IProgressMonitor monitor) {
		
		UnoidlProject project = (UnoidlProject)unoProject;
		try {
			TypesBuilder.build(project.getProject(), monitor);
		} catch (CoreException e) {
			PluginLogger.error(
					Messages.getString("UnoidlProjectHelper.NotUnoProjectError"), e); //$NON-NLS-1$
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
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProjectHelper.NotUnoProjectError"), e); //$NON-NLS-1$
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
		
		if (fullName != null && !fullName.equals("")) { //$NON-NLS-1$
			// Create the directories
			IUnoComposite moduleDir = CompositeFactory.createModuleDir(
					fullName, unoproject);
			moduleDir.create(true);
			moduleDir.dispose();
			
			((UnoidlProject)unoproject).getProject().refreshLocal(
					IProject.DEPTH_INFINITE, monitor);
			
			// Get all the folders for the modules
			String[] modules = fullName.split("::"); //$NON-NLS-1$
			IFolder currentFolder = unoproject.getFolder(unoproject.getIdlPath());
			
			for (int i=0; i<modules.length; i++) {
				currentFolder = currentFolder.getFolder(modules[i]);
				
				// Sets the IDL Capable property
				if (currentFolder.exists()){
					currentFolder.setPersistentProperty(
							new QualifiedName(
									OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
									IUnoidlProject.IDL_FOLDER), "true"); //$NON-NLS-1$
				}
			}
		} else {
			throw new IllegalArgumentException(Messages.getString("UnoidlProjectHelper.BadFullnameError")); //$NON-NLS-1$
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
			if (null != unoproject.getRootModule().replaceAll("::", ".")){ //$NON-NLS-1$ //$NON-NLS-2$

				PluginLogger.debug("Creating unoidl packages"); //$NON-NLS-1$
				
				IFolder basis = unoproject.getFolder(unoproject.getIdlPath());
				if (!basis.exists()) {
					basis.create(true, true, monitor);
					PluginLogger.debug(
							"Unoidl base directory created"); //$NON-NLS-1$
				}
				
				// Adds the idl capable property
				basis.setPersistentProperty(
						new QualifiedName(OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								IUnoidlProject.IDL_FOLDER), "true"); //$NON-NLS-1$
				
				createModules(unoproject.getRootModule(), unoproject, monitor);
				PluginLogger.debug(
						"All the modules dir have been created"); //$NON-NLS-1$
			}
		} catch (Exception e) {
			PluginLogger.error(
					Messages.getString("UnoidlProjectHelper.FolderCreationError") + //$NON-NLS-1$
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
			PluginLogger.debug("Creating source directories"); //$NON-NLS-1$
			// Create the sources directory
			IFolder codeFolder = unoproject.getFolder(SOURCE_BASIS);
			if (!codeFolder.exists()){
				codeFolder.create(true, true, monitor);
				PluginLogger.debug(
					"source folder created"); //$NON-NLS-1$
			}
			
			unoproject.getLanguage().getProjectHandler().addLanguageDependencies(
					unoproject, monitor);
			PluginLogger.debug("Language dependencies added"); //$NON-NLS-1$
			
			unoproject.getLanguage().getProjectHandler().addOOoDependencies(
					unoproject.getOOo(), ((UnoidlProject)unoproject).getProject());
			PluginLogger.debug("OOo dependencies added"); //$NON-NLS-1$
			
		} catch (CoreException e) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openError(Display.getDefault().getActiveShell(), 
							Messages.getString("UnoidlProjectHelper.CreationErrorTitle"),  //$NON-NLS-1$
							Messages.getString("UnoidlProjectHelper.CreationErrorMessage")); //$NON-NLS-1$
				}
			});
			PluginLogger.error(
					Messages.getString("UnoidlProjectHelper.FolderCreationError") + SOURCE_BASIS, //$NON-NLS-1$
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
			PluginLogger.debug("Creating ouput directories"); //$NON-NLS-1$
			IFolder urdFolder = unoproject.getFolder(URD_BASIS);
			if (!urdFolder.exists()){
				
				String[] basis_dirs = URD_BASIS.split("/"); //$NON-NLS-1$
				String path = ""; //$NON-NLS-1$
				int i = 0;
				while (i < basis_dirs.length){
					
					path = path + basis_dirs[i] + "/"; //$NON-NLS-1$
					IFolder tmpFolder = unoproject.getFolder(path);
					
					if (!tmpFolder.exists()) {
						tmpFolder.create(true, true, monitor);
						tmpFolder.setDerived(true);
						PluginLogger.debug(
								tmpFolder.getName() + " folder created"); //$NON-NLS-1$
					}
					i++;
				}
			}
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProjectHelper.FolderCreationError") + URD_BASIS, e); //$NON-NLS-1$
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
							    "true"); //$NON-NLS-1$
						
						// Recurse
						recurseSetIdlProperty(folder, unoproject);
					}
				}
			}
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProjectHelper.ReadFolderError") +  //$NON-NLS-1$
					container.getName(), e);
		}
	}
	
	
	//---------------------------------------------------------- Private methods
	
	
	
	/**
	 * This method creates and opens the project with the Java and Uno natures
	 * 
	 * @param project project to create
	 * @param monitor monitor used to report the creation state
	 */
	private static void createProject(IProject project, 
			IProgressMonitor monitor) {
		try {
			if (!project.exists()){
				project.create(monitor);
				PluginLogger.debug("Project resource created: " +  //$NON-NLS-1$
						project.getName());
			}
			
			if (!project.isOpen()){
				project.open(monitor);
				PluginLogger.debug("Project is opened: " +  //$NON-NLS-1$
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
			PluginLogger.debug("UNO-IDL nature set"); //$NON-NLS-1$
			
			UnoidlProject unoProject = (UnoidlProject)project.getNature(
					OOEclipsePlugin.UNO_NATURE_ID);
			ProjectsManager.getInstance().addProject(unoProject);
			
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProjectHelper.NatureSetError"), e); //$NON-NLS-1$
		}
	}
}
