package org.openoffice.ide.eclipse.core.internal.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.openoffice.ide.eclipse.core.model.UnoPackage;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

/**
 * Helper class for UNO-IDL project handling.
 * 
 * @author Cedric Bosdonnat
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
	public static final String SOURCE_BASIS = "/source"; //$NON-NLS-1$
	
	/**
	 * Project relative path to the urd output folder.
	 */
	public static final String URD_BASIS = "/urd"; //$NON-NLS-1$
	
	/**
	 * Project relative path to the idl root folder
	 */
	public static final String IDL_BASIS = "/idl"; //$NON-NLS-1$
	
	/**
	 * Create a default configuration file for UNO-IDL projects
	 * 
	 * @param configFile the descriptor of the file to create
	 */
	public static void createDefaultConfig(File configFile) {
		Properties properties = new Properties();
		properties.setProperty(UnoidlProject.IDL_DIR, IDL_BASIS);
		properties.setProperty(UnoidlProject.BUILD_DIR, BUILD_BASIS);
		
		try {
			FileOutputStream out = new FileOutputStream(configFile);
			properties.store(out, "UNO project configuration file");
		} catch (Exception e) {
			PluginLogger.warning("Can't create default uno configuration file", e);
		}
	}
	
	/**
	 * Create the basic structure of a UNO-IDL project
	 * 
	 * @param data the data describing the uno project to create
	 * @param monitor the progress monitor reporting the creation progress
	 * 				in the User Interface
	 * @return the created Uno project 
	 * 
	 * @throws Exception is thrown if anything wrong happens
	 */
	public static IUnoidlProject createStructure(UnoFactoryData data, 
			IProgressMonitor monitor) throws Exception {
		
		IUnoidlProject unoProject = null;
		
		// Creates the new project whithout it's builders
		IProject project = (IProject)data.getProperty(
				IUnoFactoryConstants.PROJECT_HANDLE);
		
		createProject(project, monitor);
		unoProject = ProjectsManager.getProject(
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
		ISdk sdk = SDKContainer.getSDK(sdkname);
		unoProject.setSdk(sdk);

		// Set the OOo runtime
		String oooname = (String)data.getProperty(
				IUnoFactoryConstants.PROJECT_OOO);
		IOOo ooo = OOoContainer.getOOo(oooname);
		unoProject.setOOo(ooo);


		// Set the idl directory
		String idlDir = (String)data.getProperty(IUnoFactoryConstants.PROJECT_IDL_DIR);
		if (idlDir == null || idlDir.equals("")) {
			idlDir = IDL_BASIS;
		}
		unoProject.setIdlDir(idlDir);
		
		// Set the sources directory
		String sourcesDir = (String)data.getProperty(IUnoFactoryConstants.PROJECT_SRC_DIR);
		if (sourcesDir == null || sourcesDir.equals("")) {
			sourcesDir = SOURCE_BASIS;
		}
		unoProject.setSourcesDir(sourcesDir);
		
		
		// Save all the properties to the configuration file
		unoProject.saveAllProperties();
		
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
		} catch (Exception e) {
			PluginLogger.error(
					Messages.getString("UnoidlProjectHelper.NotUnoProjectError"), e); //$NON-NLS-1$
		}
	}
	
	/**
	 * Set the project builders and run the build
	 */
	public static void setProjectBuilders(IUnoidlProject unoProject){
		
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
		} else {
			throw new IllegalArgumentException(
					Messages.getString("UnoidlProjectHelper.BadFullnameError")); //$NON-NLS-1$
		}
	}
	
	/**
	 * This method creates the folder described by the company prefix. 
	 * It assumes that the company prefix is already set, otherwise no
	 * package will be created
	 * 
	 * @param unoproject the UNO project on which to perform the action
	 * @param monitor progress monitor
	 */
	public static void createUnoidlPackage(IUnoidlProject unoproject,
			IProgressMonitor monitor) {
		
		try {
			if (null != unoproject.getRootModule().replaceAll("::", ".")){ //$NON-NLS-1$ //$NON-NLS-2$

				PluginLogger.debug("Creating unoidl packages"); //$NON-NLS-1$
				
				// Create the IDL folder and all its parents
				IPath idlPath = unoproject.getIdlPath();
				String currentPath = "/";
				
				for (int i=0, length=idlPath.segmentCount(); i<length; i++) {
					currentPath += idlPath.segment(i) + "/";
					IFolder folder = unoproject.getFolder(currentPath);
					if (!folder.exists()) {
						folder.create(true, true, monitor);
					}
				}

				PluginLogger.debug(
						"Unoidl base directory created"); //$NON-NLS-1$
				
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

		String sourcesDir = unoproject.getSourcePath().toPortableString();
		if (sourcesDir == null || sourcesDir.equals("")) {
			sourcesDir = SOURCE_BASIS;
		}
		
		try {
			PluginLogger.debug("Creating source directories"); //$NON-NLS-1$
			
			// Create the sources directory
			IFolder codeFolder = unoproject.getFolder(sourcesDir);
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
					Messages.getString("UnoidlProjectHelper.FolderCreationError") + sourcesDir, //$NON-NLS-1$
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
			IFolder urdFolder = unoproject.getFolder(unoproject.getUrdPath());
			if (!urdFolder.exists()){
				
				String[] basis_dirs = unoproject.getUrdPath().segments();
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
			
			// TODO Allow custom configuration
			createDefaultConfig(unoProject.getConfigFile());
			
			ProjectsManager.addProject(unoProject);
			
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("UnoidlProjectHelper.NatureSetError"), e); //$NON-NLS-1$
		}
	}
	
	public static UnoPackage createMinimalUnoPackage(IUnoidlProject prj, File dest, File dir) {

		UnoPackage unoPackage = new UnoPackage(dest, dir);
		
		// Add content to the package
		unoPackage.addTypelibraryFile(new File(dir, "types.rdb"), "RDB"); //$NON-NLS-1$ //$NON-NLS-2$
		prj.getLanguage().getLanguageBuidler().fillUnoPackage(unoPackage, prj);
		
		return unoPackage;
	}
}
