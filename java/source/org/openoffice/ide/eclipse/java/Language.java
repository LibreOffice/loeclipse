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
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.model.ILanguage;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

public class Language implements ILanguage {

	public Language(){
		// Nothing to do ;)
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.ILanguage#addProjectNature(org.eclipse.core.resources.IProject)
	 */
	public void addProjectNature(IProject project) {
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
			newNatureIds[natureIds.length] = JavaCore.NATURE_ID;
			
			description.setNatureIds(newNatureIds);
			project.setDescription(description, null);
			
		} catch (CoreException e) {
			// TODO Log Java nature set failed
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.model.ILanguage#addLanguageBuilder(org.eclipse.core.resources.IProject)
	 */
	public void addLanguageBuilder(IProject project) {
		
		try {
			IProjectDescription descr = project.getDescription();
			ICommand[] oldCommands = descr.getBuildSpec();
			ICommand[] newCommands = new ICommand[oldCommands.length+1];
		
			System.arraycopy(oldCommands, 0, newCommands, 0, oldCommands.length);
			
			ICommand typesbuilderCommand = descr.newCommand();
			typesbuilderCommand.setBuilderName(JavaCore.BUILDER_ID);
			newCommands[oldCommands.length] = typesbuilderCommand;
			
			descr.setBuildSpec(newCommands);
			project.setDescription(descr, null);
			
		} catch(CoreException e) {
			// TODO log cannot Add Java Builder
		}
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
					
					IPath ooTypesPath = new Path (ooo.getHome()).append(
							"/program/types.rdb");
					
					// TODO What if the user creates other root modules ?
					String firstModule = rootModule.split("::")[0];
					
					// HELP quotes are placed here to prevent Windows path 
					// names with spaces
					String command = "javamaker -T" + firstModule + 
						".* -nD -Gc -BUCR " + 
						"-O ." + System.getProperty("file.separator") + 
						buildFolder.getProjectRelativePath().toOSString() + " " +
						typesFile.getProjectRelativePath().toOSString() + " " +
						"-X\"" + ooTypesPath.toOSString() + "\"";
					
					
					Process process = OOEclipsePlugin.runTool(
							ProjectsManager.getInstance().getProject(
									typesFile.getProject().getName()),
							command, monitor);
					
					LineNumberReader lineReader = new LineNumberReader(
							new InputStreamReader(process.getErrorStream()));
					
					// Only for debugging purpose
					if (null != System.getProperties().getProperty("DEBUG")){
					
						String line = lineReader.readLine();
						while (null != line){
							System.out.println(line);
							line = lineReader.readLine();
						}
					}
					
					process.waitFor();
				}
			} catch (InterruptedException e) {
				// interrupted process: the code generation failed
			} catch (IOException e) {
				// Error whilst reading the error stream
			}
		}
	}
	
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
	
	public final static String[] KEPT_JARS = {
		"unoil.jar",
		"ridl.jar",
		"juh.jar",
		"jurt.jar",
		"unoloader.jar",
		"officebean.jar"
	};
	
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
					IClasspathEntry entry = JavaCore.newLibraryEntry(jarPathi, null, null);
					entries[oldEntries.length+i] = entry;
				}
				
				javaProject.setRawClasspath(entries, null);
			} catch (JavaModelException e){
				// TODO log error PROJECT_CLASSPATH_ERROR
			}
		}
	}
	
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
						ooo.getClassesPath()+"/"+contenti);
				jarsPath.add(jariPath);
			}
		}
		
		return jarsPath;
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
			
			// TODO log error PROJECT_CLASSPATH_ERROR
		}
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
	
}
