package org.openoffice.ide.eclipse.core.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;

public class ProjectsManager {

	private Hashtable projects;
	
	private static ProjectsManager __instance;
	
	/**
	 * Gets the instance of the ProjectsManager singleton
	 */
	public static ProjectsManager getInstance() {
		
		if(__instance == null){
			__instance = new ProjectsManager();
		}
		
		return __instance;
	}
	
	/**
	 * Returns the unoidl project with the given name, if it exists. Otherwise
	 * <code>null</code> is returned
	 * 
	 * @param name the name of the project to find
	 * @return the found project.
	 */
	public IUnoidlProject getProject(String name){
		
		IUnoidlProject result = null;
		if(projects.containsKey(name)){
			result = (IUnoidlProject)projects.get(name);
		}
		return result;
	}
	
	/**
	 * Adds a project to the manager only if there is no other project with the
	 * same name
	 * 
	 * @param project
	 */
	public void addProject(IUnoidlProject project) {
		if(project != null && !projects.containsKey(project.getName())){
			projects.put(project.getName(), project);
		}
	}
	
	/**
	 * This method will release all the stored project references. There
	 * is no need to call this method in any other place than the plugin
	 * stop method.
	 */
	public void clear(){
		projects.clear();
	}
	
	private ProjectsManager(){
		projects = new Hashtable();
		
		/* Load all the existing unoidl projects */
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i=0, length=projects.length; i<length; i++){
			IProject project = projects[i];
			
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
				PluginLogger.getInstance().error("Failed to load the project " + 
						project.getName(), e); // TODO i18n
			}
		}
	}
}
