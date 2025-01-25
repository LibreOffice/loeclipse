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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.plugin.core.utils.FileHelper;

/**
 * Builder for the IDL files generating the <code>types.rdb</code> registry.
 *
 * <p>
 * This builder should not be associated directly to a UNO project: the right builder for this is {@link TypesBuilder}.
 * This builder doesn't make any difference between full and incremental builds.
 * </p>
 */
public class IdlwBuilder {

    /**
     * Computes the full build of all the <code>idl</code> files into a single <code>types.rdb</code> file. This
     * resulting file is given by {@link IUnoidlProject#getTypesPath()}. This methods simply launches the
     * {@link RegmergeBuildVisitor} on the urd folder.
     *
     * @param project
     *            the project to build
     * @param monitor
     *            a monitor to watch the build progress
     * @throws Exception
     *             is thrown is anything wrong happens
     */
    public static void build(IUnoidlProject project, IProgressMonitor monitor) throws Exception {

        IFile typesFile = project.getFile(project.getTypesPath());
        File mergeFile = project.getProjectPath().append(typesFile.getProjectRelativePath()).toFile();
        if (mergeFile != null && mergeFile.exists()) {
            FileHelper.remove(mergeFile);
        }

        // merge each idl file
        IFolder idlFolder = project.getFolder(project.getIdlPath());
        IPath idlPath = project.getProjectPath().append(idlFolder.getProjectRelativePath());
        File idlFile = idlPath.toFile();
        VisitableFile visitableIdl = new VisitableFile(idlFile);
        visitableIdl.accept(new IdlwBuildVisitor(project, monitor));
    }

    /**
     * Convenience method to execute the <code>unoidl-write</code> tool on a given file.
     *
     * @param file
     *            the file to run <code>unoidl-write</code> on.
     * @param project
     *            the UNO project on which to run the <code>unoidl-write</code> tool
     * @param monitor
     *            a progress monitor
     */
    static void runIdlwOnFile(File file, IUnoidlProject project, IProgressMonitor monitor) {

        ISdk sdk = project.getSdk();

        if (null != sdk) {

            String command = sdk.getCommand("unoidl-write") + " \""; //$NON-NLS-1$ //$NON-NLS-2$

            IPath idlPath = project.getProjectPath().append(project.getIdlPath().toString()); //$NON-NLS-1$
            IPath rdbFile = project.getProjectPath().append(project.getTypesPath().toString()); //$NON-NLS-1$

            List<String> arguments = new ArrayList<>();
            for (String rdbType : project.getOOo().getTypesPath()) {
                arguments.add(rdbType); //$NON-NLS-1$
            }
            arguments.add(file.getAbsolutePath()); //$NON-NLS-1$
            arguments.add(idlPath.toOSString()); //$NON-NLS-1$
            arguments.add(rdbFile.toOSString()); //$NON-NLS-1$

            command += String.join("\" \"", arguments.toArray(new String[0]));
            command += "\"";

            Process process = sdk.runTool(project, command, monitor);

            // Just wait for the process to end before destroying it
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                // Process has been interrupted by the user
            }

        }
    }
}
