/*************************************************************************
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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
package org.libreoffice.ide.eclipse.core.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.model.UnoidlProject;

/**
 * Singleton mapping the UNO-IDL projects to their name to provide an easy access to UNO-IDL projects.
 *
 */
public class ProjectsManager {

    private static Hashtable<String, IUnoidlProject> sProjects = new Hashtable<String, IUnoidlProject>();

    /**
     * This method will release all the stored project references. There is no need to call this method in any other
     * place than the plugin stop method.
     */
    public static void dispose() {
        sProjects.clear();
    }

    /**
     * Returns the unoidl project with the given name, if it exists. Otherwise <code>null</code> is returned
     *
     * @param name
     *            the name of the project to find
     * @return the found project.
     */
    public static IUnoidlProject getProject(String name) {

        IUnoidlProject result = null;
        if (name != null && sProjects.containsKey(name)) {
            result = sProjects.get(name);
        }
        return result;
    }

    /**
     * Add a project that isn't already loaded.
     *
     * @param pProject
     *            the project to load and add
     */
    public static void addProject(IProject pProject) {
        try {
            if (pProject.isAccessible() && pProject.hasNature(OOEclipsePlugin.UNO_NATURE_ID)) {

                // Load the nature
                UnoidlProject unoproject = (UnoidlProject) pProject.getNature(OOEclipsePlugin.UNO_NATURE_ID);

                unoproject.configure();

                // Add the project to the manager
                addProject(unoproject);
            }
        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("ProjectsManager.LoadProjectError") + //$NON-NLS-1$
                pProject.getName(), e);
        }
    }

    /**
     * Adds a project to the manager only if there is no other project with the same name.
     *
     * @param pProject
     *            the project to add
     */
    public static void addProject(IUnoidlProject pProject) {
        if (pProject != null && !sProjects.containsKey(pProject.getName())) {
            sProjects.put(pProject.getName(), pProject);
        }
    }

    /**
     * Removes a project from the manager.
     *
     * @param name
     *            the name of the project to remove
     */
    public static void removeProject(String name) {
        if (sProjects.containsKey(name)) {
            IUnoidlProject prj = sProjects.get(name);
            prj.dispose();
            sProjects.remove(name);
        }
    }

    /**
     * @return an array containing all the defined UNO projects
     */
    public static IUnoidlProject[] getProjects() {
        IUnoidlProject[] projects = new IUnoidlProject[sProjects.size()];

        return sProjects.values().toArray(projects);
    }

    /**
     * Private constructor for the singleton. Its charge is to load all the existing UNO-IDL projects
     *
     */
    public static void load() {

        /* Load all the existing unoidl projects */
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (int i = 0, length = projects.length; i < length; i++) {
            IProject project = projects[i];
            addProject(project);
        }
    }
}
