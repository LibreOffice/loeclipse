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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.plugin.core.utils.FileHelper;

/**
 * Builder for the URD files generating the <code>types.rdb</code> registry.
 *
 * <p>
 * This builder should not be associated directly to a UNO project: the right builder for this is {@link TypesBuilder}.
 * This builder doesn't make any difference between full and incremental builds.
 * </p>
 */
public class RegmergeBuilder {

    /**
     * Root of the generated types, used by regmerge and javamaker. UCR is chosen for LibreOffice compatibility
     */
    public static final String TYPE_ROOT_KEY = "/UCR"; //$NON-NLS-1$

    /**
     * Computes the full build of all the <code>urd</code> files into a single <code>types.rdb</code> file. This
     * resulting file is given by {@link IUnoidlProject#getTypesPath()}. This methods simply launches the
     * {@link RegmergeBuildVisitor} on the urd folder.
     *
     * @param unoProject
     *            the project to build
     * @param monitor
     *            a monitor to watch the build progress
     * @throws Exception
     *             is thrown is anything wrong happens
     */
    public static void build(IUnoidlProject unoProject, IProgressMonitor monitor) throws Exception {

        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(unoProject.getName());

        IFile typesFile = unoProject.getFile(unoProject.getTypesPath());
        File mergeFile = prj.getLocation().append(typesFile.getProjectRelativePath()).toFile();
        if (mergeFile != null && mergeFile.exists()) {
            FileHelper.remove(mergeFile);
        }

        // merge each urd file
        IFolder urdFolder = unoProject.getFolder(unoProject.getUrdPath());
        IPath urdPath = prj.getLocation().append(urdFolder.getProjectRelativePath());
        File urdFile = urdPath.toFile();
        VisitableFile visitableUrd = new VisitableFile(urdFile);
        visitableUrd.accept(new RegmergeBuildVisitor(unoProject, monitor));
    }

    /**
     * Convenience method to execute the <code>regmerge</code> tool on a given file.
     *
     * @param file
     *            the file to run <code>regmerge</code> on.
     * @param unoProject
     *            the UNO project on which to run the <code>regmerge</code> tool
     * @param monitor
     *            a progress monitor
     */
    static void runRegmergeOnFile(File file, IUnoidlProject unoProject, IProgressMonitor monitor) {

        // The registry file is placed in the root of the project as announced
        // to the api-dev mailing-list
        IFile mergeFile = unoProject.getFile(unoProject.getTypesPath());

        String existingReg = ""; //$NON-NLS-1$
        if (mergeFile.exists()) {
            existingReg = mergeFile.getProjectRelativePath().toOSString() + " "; //$NON-NLS-1$
        }

        String command = "regmerge types.rdb " + TYPE_ROOT_KEY + " " + //$NON-NLS-1$ //$NON-NLS-2$
            existingReg + "\"" + file.getAbsolutePath() + "\""; //$NON-NLS-1$ //$NON-NLS-2$

        // Process creation. Need to set the PATH value using OOo path: due to some tools changes in 3.1
        String[] sPaths = unoProject.getOOo().getBinPath();
        String sPathValue = "PATH="; //$NON-NLS-1$
        for (String sPath : sPaths) {
            if (!sPathValue.endsWith("=")) { //$NON-NLS-1$
                sPathValue += System.getProperty("path.separator"); //$NON-NLS-1$
            }
            sPathValue += sPath;
        }
        

        Process process = unoProject.getSdk().runToolWithEnv(unoProject, command,
            new String[] { sPathValue }, monitor);

        // Just wait for the process to end before destroying it
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            // Process has been interrupted by the user
        }
    }
}
