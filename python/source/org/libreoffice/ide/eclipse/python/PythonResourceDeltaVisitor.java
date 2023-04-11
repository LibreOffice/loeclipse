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
package org.libreoffice.ide.eclipse.python;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * This class will visit a resource delta and perform the necessary actions
 * on resources included in UNO projects.
 */
public class PythonResourceDeltaVisitor implements IResourceDeltaVisitor {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(IResourceDelta pDelta) throws CoreException {

        boolean visitChildren = true;

        if (!(pDelta.getResource() instanceof IWorkspaceRoot)) {

            IProject project = pDelta.getResource().getProject();
            IUnoidlProject unoprj = ProjectsManager.getProject(project.getName());
            if (unoprj != null) {
                // The resource is a UNO project or is contained in a UNO project
                visitChildren = true;

                // Check if the resource is a service implementation
                if (pDelta.getKind() == IResourceDelta.ADDED) {
                    addImplementation(pDelta, unoprj);

                } else if (pDelta.getKind() == IResourceDelta.REMOVED) {
                    removeImplementation(pDelta, unoprj);
                }
            }
        }

        return visitChildren;
    }

    /**
     * Remove the delta resource from the implementations.
     *
     * @param pDelta the delta to remove
     * @param pUnoprj the concerned UNO project
     */
    private void removeImplementation(IResourceDelta pDelta,
        IUnoidlProject pUnoprj) {
        // Nothing to do for Python
    }

    /**
     * Add the delta resource to the implementations.
     *
     * @param pDelta the delta resource to add.
     * @param pUnoProject the concerned UNO project
     */
    private void addImplementation(IResourceDelta pDelta, IUnoidlProject pUnoProject) {
        // Nothing to do for Python
    }

}
