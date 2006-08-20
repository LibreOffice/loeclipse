package org.openoffice.ide.eclipse.java;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Vector;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.jdt.launching.JavaRuntime;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.ILanguage;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;
import org.openoffice.ide.eclipse.core.wizards.LanguageWizardPage;

/**
 * Implementation for the Java language
 * 
 * @author cbosdonnat
 */
public class Language implements ILanguage {
	
	private final static QualifiedName P_REGISTRATION_CLASSNAME = new QualifiedName(
			OOoJavaPlugin.PLUGIN_ID, "regclassname");  //$NON-NLS-1$
	private final static QualifiedName P_JAVA_VERSION = new QualifiedName(
			OOoJavaPlugin.PLUGIN_ID, "javaversion");  //$NON-NLS-1$
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.ILanguage#addProjectNature(org.eclipse.core.resources.IProject)
	 */
	public void addProjectNature(IProject project) {
		try {
			if (!project.exists()){
				project.create(null);
				PluginLogger.debug(
						"Project created during language specific operation"); //$NON-NLS-1$
			}
			
			if (!project.isOpen()){
				project.open(null);
				PluginLogger.debug("Project opened"); //$NON-NLS-1$
			}
			
			IProjectDescription description = project.getDescription();
			String[] natureIds = description.getNatureIds();
			String[] newNatureIds = new String[natureIds.length+1];
			System.arraycopy(natureIds, 0, newNatureIds, 0, natureIds.length);
			
			// Adding the nature
			newNatureIds[natureIds.length] = JavaCore.NATURE_ID;
			
			description.setNatureIds(newNatureIds);
			project.setDescription(description, null);
			PluginLogger.debug(Messages.getString("Language.JavaNatureSet")); //$NON-NLS-1$
			
		} catch (CoreException e) {
			PluginLogger.error(Messages.getString("Language.NatureSettingFailed")); //$NON-NLS-1$
		}
	}

	public void configureProject(UnoFactoryData data) 
		throws Exception {
		
		// Get the project from data
		IProject prj = (IProject)data.getProperty(
				IUnoFactoryConstants.PROJECT_HANDLE);
		IUnoidlProject unoprj = ProjectsManager.getInstance().getProject(
				prj.getName());
		
		// Add Java builder
		IProjectDescription descr = prj.getDescription();
		ICommand[] oldCommands = descr.getBuildSpec();
		ICommand[] newCommands = new ICommand[oldCommands.length+1];

		System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);

		ICommand typesbuilderCommand = descr.newCommand();
		typesbuilderCommand.setBuilderName(JavaCore.BUILDER_ID);
		newCommands[oldCommands.length] = typesbuilderCommand;

		descr.setBuildSpec(newCommands);
		prj.setDescription(descr, null);
		
		// Set some properties on the project
		
		// Registration class name
		String regclass = (String)data.getProperty(
				JavaWizardPage.REGISTRATION_CLASS_NAME);
		unoprj.addProperty(P_REGISTRATION_CLASSNAME,
				regclass);
		
		// Java version
		String javaversion = (String)data.getProperty(
				JavaWizardPage.JAVA_VERSION);
		unoprj.addProperty(P_JAVA_VERSION,
				javaversion);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.ILanguage#generateFromTypes(org.openoffice.ide.eclipse.preferences.IOOo, org.eclipse.core.runtime.IPath, org.eclipse.core.runtime.IPath, java.lang.String)
	 */
	public void generateFromTypes(ISdk sdk, IOOo ooo, IFile typesFile,
			IFolder buildFolder, String rootModule, IProgressMonitor monitor) {
		
		if (typesFile.exists()){
			
			try {
				
				if (null != sdk && null != ooo){
					
					IPath ooTypesPath = new Path (ooo.getTypesPath());
					
					// TODO What if the user creates other root modules ?
					String firstModule = rootModule.split("::")[0]; //$NON-NLS-1$
					
					// HELP quotes are placed here to prevent Windows path 
					// names with spaces
					String command = "javamaker -T" + firstModule +  //$NON-NLS-1$
						".* -nD -Gc -BUCR " +  //$NON-NLS-1$
						"-O ." + System.getProperty("file.separator") +  //$NON-NLS-1$ //$NON-NLS-2$
						buildFolder.getProjectRelativePath().toOSString() + " " + //$NON-NLS-1$
						typesFile.getProjectRelativePath().toOSString() + " " + //$NON-NLS-1$
						"-X\"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
					
					
					Process process = OOEclipsePlugin.runTool(
							ProjectsManager.getInstance().getProject(
									typesFile.getProject().getName()),
							command, monitor);
					
					LineNumberReader lineReader = new LineNumberReader(
							new InputStreamReader(process.getErrorStream()));
					
					// Only for debugging purpose
					if (PluginLogger.isLevel(PluginLogger.DEBUG)){ //$NON-NLS-1$
					
						String line = lineReader.readLine();
						while (null != line){
							System.out.println(line);
							line = lineReader.readLine();
						}
					}
					
					process.waitFor();
				}
			} catch (InterruptedException e) {
				PluginLogger.error(
						Messages.getString("Language.CreateCodeError"), e); //$NON-NLS-1$
			} catch (IOException e) {
				PluginLogger.warning(
						Messages.getString("Language.UnreadableOutputError")); //$NON-NLS-1$
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.ILanguage#addLanguageDependencies(org.openoffice.ide.eclipse.core.model.IUnoidlProject, org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void addLanguageDependencies(IUnoidlProject unoproject,
			IProject project, IProgressMonitor monitor) throws CoreException {
		
		IJavaProject javaProject = JavaCore.create(project);
		javaProject.open(monitor);
		
		IClasspathEntry[] entries = new IClasspathEntry[3]; 
		
		// Adds the project to the classpath
		IClasspathEntry entry = JavaCore.newSourceEntry(
				unoproject.getFolder(unoproject.getSourcePath()).
					getFullPath());
		entries[0] = entry;
		entries[1] = JavaRuntime.getDefaultJREContainerEntry();
		entries[2] = JavaCore.newLibraryEntry(
				unoproject.getFolder(unoproject.getBuildPath()).getFullPath(), 
				null, null, false);
		
		javaProject.setRawClasspath(entries, monitor);
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.ILanguage#addOOoDependencies(org.openoffice.ide.eclipse.preferences.IOOo, org.eclipse.core.resources.IProject)
	 */
	public void addOOoDependencies(IOOo ooo, IProject project){

		IJavaProject javaProject = JavaCore.create(project);
		
		if (null != ooo){
			// Find the jars in the first level of the directory
			Vector jarPaths = findJarsFromPath(ooo);
			
			try {
				IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
				IClasspathEntry[] entries = new IClasspathEntry[jarPaths.size()+
				                                            oldEntries.length];
				
				System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
				
				for (int i=0, length=jarPaths.size(); i<length; i++){
					IPath jarPathi = (IPath)jarPaths.get(i);
					IClasspathEntry entry = JavaCore.newLibraryEntry(
							jarPathi, null, null);
					entries[oldEntries.length+i] = entry;
				}
				
				javaProject.setRawClasspath(entries, null);
			} catch (JavaModelException e){
				PluginLogger.error(
						Messages.getString("Language.ClasspathSetFailed"), e); //$NON-NLS-1$
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.ILanguage#removeOOoDependencies(org.openoffice.ide.eclipse.preferences.IOOo, org.eclipse.core.resources.IProject)
	 */
	public void removeOOoDependencies(IOOo ooo, IProject project){
		IJavaProject javaProject = JavaCore.create(project);
		
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
			PluginLogger.error(
					Messages.getString("Language.ClasspathSetFailed"), e); //$NON-NLS-1$
		}
	}
	
	//--------------------------------------------- Jar finding private methods
	
	private final static String[] KEPT_JARS = {
		"unoil.jar", //$NON-NLS-1$
		"ridl.jar", //$NON-NLS-1$
		"juh.jar", //$NON-NLS-1$
		"jurt.jar", //$NON-NLS-1$
		"unoloader.jar", //$NON-NLS-1$
		"officebean.jar" //$NON-NLS-1$
	};
	
	/**
	 * returns the path of all the kept jars contained in the folder pointed by path.
	 * 
	 * @param ooo the OOo instance from which to get the jars
	 * @return a vector of Path pointing to each jar.
	 */
	private Vector findJarsFromPath(IOOo ooo){
		Vector jarsPath = new Vector();
		
		Path folderPath = new Path(ooo.getClassesPath());
		File programFolder = folderPath.toFile();
		
		String[] content = programFolder.list();
		for (int i=0, length=content.length; i<length; i++){
			String contenti = content[i];
			if (isKeptJar(contenti)){
				Path jariPath = new Path (
						ooo.getClassesPath()+"/"+contenti); //$NON-NLS-1$
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

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.ILanguage#createLibrary(org.openoffice.ide.eclipse.core.model.IUnoidlProject)
	 */
	public String createLibrary(IUnoidlProject unoProject) throws Exception {
		// TODO Run jar and create projectname.jar
		
		return null;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.ILanguage#getBuildEnv(org.openoffice.ide.eclipse.core.model.IUnoidlProject, org.eclipse.core.resources.IProject)
	 */
	public String[] getBuildEnv(IUnoidlProject unoProject, IProject project) {
		String[] env = new String[1];
		
		// compute the classpath for the project's OOo instance
		String classpath = "CLASSPATH="; //$NON-NLS-1$
		String sep = System.getProperty("path.separator"); //$NON-NLS-1$
		
		// Compute the classpath for the project dependencies
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject != null) {
			try {
				IClasspathEntry[] cpEntry = javaProject.getResolvedClasspath(true);
				for (int i=0; i<cpEntry.length; i++) {
					IClasspathEntry entry = cpEntry[i];
					
					// Transform into the correct path for the entry.
					classpath += entry.getPath().toOSString();
					if (i < cpEntry.length - 1) {
						classpath += sep;
					}
				}
			} catch (JavaModelException e) {
				PluginLogger.error(
						Messages.getString("Language.GetClasspathError"), e); //$NON-NLS-1$
			}
		}
		
		env[0] = classpath;
		
		return env;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.model.ILanguage#getWizardPage(java.lang.String, java.lang.String)
	 */
	public LanguageWizardPage getWizardPage(UnoFactoryData data) {
//		return new JavaWizardPage(data);
		return null; // TODO just for fixing release
	}
}
