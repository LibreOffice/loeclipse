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
package org.libreoffice.ide.eclipse.core.builders;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.IProgressMonitor;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Class visiting each child of the idl folder to generate the corresponding <code>urd</code> file.
 */
public class IdlcBuildVisitor implements IResourceVisitor {

    private static String sExtension = "idl";
    private IProgressMonitor mProgressMonitor;
    private IUnoidlProject mProject;
    private String mPath;

    /**
     * Default constructor.
     *
     * @param project
     *            the project UNO to visit
     * @param monitor
     *            progress monitor
     */
    public IdlcBuildVisitor(IUnoidlProject project, IProgressMonitor monitor) {
        super();
        mProgressMonitor = monitor;
        mProject = project;
        mPath = mProject.getIdlPath().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(IResource res) {

        boolean visitChildren = false;
        boolean isChild = res.getProjectRelativePath().toString().startsWith(mPath); //$NON-NLS-1$

        // Try to compile the file if it is an idl file
        if (isChild && res.getType() == IResource.FILE &&
            sExtension.equalsIgnoreCase(res.getFileExtension())) { //$NON-NLS-1$
            TypesBuilder.runIdlcOnFile((IFile) res, mProject, mProgressMonitor);
            if (mProgressMonitor != null) {
                mProgressMonitor.worked(1);
            }

        } else if (isChild && res.getType() == IResource.FOLDER) {
            visitChildren = true;

        } else {
            PluginLogger.debug("Non handled resource"); //$NON-NLS-1$
        }

        // cleaning
        mProgressMonitor = null;

        return visitChildren;
    }
}
