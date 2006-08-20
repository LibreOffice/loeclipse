package org.openoffice.ide.eclipse.core.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
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
public class ProjectsManager implements IResourceChangeListener {

	private Hashtable mProjects;
	
	private static ProjectsManager sInstance;
	
	/**
	 * Gets the instance of the ProjectsManager singleton
	 */
	public static ProjectsManager getInstance() {
		
		if(sInstance == null){
			sInstance = new ProjectsManager();
		}
		
		return sInstance;
	}
	
	/**
	 * This method will release all the stored project references. There
	 * is no need to call this method in any other place than the plugin
	 * stop method.
	 */
	public void dispose() {
		mProjects.clear();
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
		if(mProjects.containsKey(name)){
			result = (IUnoidlProject)mProjects.get(name);
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
		if(project != null && !mProjects.containsKey(project.getName())){
			mProjects.put(project.getName(), project);
		}
	}
	
	/**
	 * Private constructor for the singleton. Its charge is to load all the
	 * existing UNO-IDL projects
	 *
	 */
	private ProjectsManager(){
		mProjects = new Hashtable();
		
		/* Load all the existing unoidl projects */
		IProject[] projects = ResourcesPlugin.getWorkspace().
				getRoot().getProjects();
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
				PluginLogger.error(
					Messages.getString("ProjectsManager.LoadProjectError") +  //$NON-NLS-1$
					project.getName(), e);
			}
		}
		
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, 
				IResourceChangeEvent.PRE_DELETE);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {
		
		// detect UNO IDL project about to be deleted
		IResource removed = event.getResource();
		if (mProjects.containsKey(removed.getName())) {
			((UnoidlProject)mProjects.get(removed.getName())).dispose();
			mProjects.remove(removed.getName());
		}
	}
}
