package org.openoffice.ide.eclipse.core.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;

/**
 * Singleton mapping the UNO-IDL projects to their name to provide an easy
 * acces to UNO-IDL projects.
 * 
 * @author cbosdonnat
 */
public class ProjectsManager {

	private static Hashtable<String, IUnoidlProject> mProjects = new Hashtable<String, IUnoidlProject>();
	
	/**
	 * This method will release all the stored project references. There
	 * is no need to call this method in any other place than the plugin
	 * stop method.
	 */
	public static void dispose() {
		mProjects.clear();
	}
	
	/**
	 * Returns the unoidl project with the given name, if it exists. Otherwise
	 * <code>null</code> is returned
	 * 
	 * @param name the name of the project to find
	 * @return the found project.
	 */
	public static IUnoidlProject getProject(String name){
		
		IUnoidlProject result = null;
		if(mProjects.containsKey(name)){
			result = mProjects.get(name);
		}
		return result;
	}
	
	/**
	 * Add a project that isn't already loaded
	 * 
	 * @param project the project to load and add
	 */
	public static void addProject(IProject project) {
		try {
			if (project.hasNature(OOEclipsePlugin.UNO_NATURE_ID)){
				
				// Load the nature
				UnoidlProject unoproject = (UnoidlProject)project.getNature(
						OOEclipsePlugin.UNO_NATURE_ID);
				
				unoproject.configure();
				
				// Add the project to the manager
				addProject(unoproject);
			}
		} catch (CoreException e) {
			PluginLogger.error(
				Messages.getString("ProjectsManager.LoadProjectError") +  //$NON-NLS-1$
				project.getName(), e);
		}
	}
	
	/**
	 * Adds a project to the manager only if there is no other project with the
	 * same name
	 * 
	 * @param project
	 */
	public static void addProject(IUnoidlProject project) {
		if (project != null && !mProjects.containsKey(project.getName())){
			mProjects.put(project.getName(), project);
		}
	}
	
	public static void removeProject(String name) {
		if (mProjects.containsKey(name)) {
			IUnoidlProject prj = mProjects.get(name);
			prj.dispose();
			mProjects.remove(name);
		}
	}
	
	/**
	 * @return an array containing all the defined UNO projects
	 */
	public static IUnoidlProject[] getProjects() {
		IUnoidlProject[] projects = new IUnoidlProject[mProjects.size()];
		
		return mProjects.values().toArray(projects);
	}
	
	/**
	 * Private constructor for the singleton. Its charge is to load all the
	 * existing UNO-IDL projects
	 *
	 */
	public static void load(){
		
		/* Load all the existing unoidl projects */
		IProject[] projects = ResourcesPlugin.getWorkspace().
				getRoot().getProjects();
		for (int i=0, length=projects.length; i<length; i++){
			IProject project = projects[i];
			addProject(project);
		}
	}
}
