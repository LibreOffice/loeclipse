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

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Class visiting each child of the idl folder to merge it with the common <code>types.rdb</code> registry.
 */
public class IdlwBuildVisitor implements IFileVisitor {

    /**
     * Progress monitor used during all the visits.
     */
    private IProgressMonitor mProgressMonitor;
    private IUnoidlProject mProject;

    /**
     * Default constructor.
     *
     * @param project
     *            the project UNO to visit
     * @param monitor
     *            progress monitor for the regmerge
     */
    public IdlwBuildVisitor(IUnoidlProject project, IProgressMonitor monitor) {
        super();
        mProgressMonitor = monitor;
        mProject = project;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean visit(File res) {

        boolean visitChildren = false;

        if (res.isFile()) {

            // Try to compile the file if it is an idl file
            if (res.getName().endsWith("idl")) { //$NON-NLS-1$

                IdlwBuilder.runIdlwOnFile(res, mProject, mProgressMonitor);
                if (mProgressMonitor != null) {
                    mProgressMonitor.worked(1);
                }
            }

        } else if (res.isDirectory()) {
            String idlBasis = UnoidlProjectHelper.IDL_BASIS;
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                idlBasis = idlBasis.replace("/", "\\"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (res.getAbsolutePath().contains(idlBasis)) {
                visitChildren = true;
            }

        } else {
            PluginLogger.debug("Non handled resource"); //$NON-NLS-1$
        }

        // helps cleaning
        mProgressMonitor = null;

        return visitChildren;
    }
}
