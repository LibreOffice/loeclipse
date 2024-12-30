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
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.ILanguageBuilder;

/**
 * <p>
 * Main builder of the UNO-IDL projects, it computes the language specific type files and types registry from the
 * <code>idl</code> files. In order to split the work, the different tasks have been split into several builders:
 * <ul>
 * <li>{@link RegmergeBuilder} merging the urd files into the types registry</li>
 * <li>
 * {@link ILanguageBuilder#generateFromTypes(ISdk, org.libreoffice.ide.eclipse.core.preferences.IOOo, IProject,
 *                                           File, File, String, IProgressMonitor)}
 * generating the language specific type files</li>
 * </ul>
 * </p>
 */
public class TypesBuilder extends IncrementalProjectBuilder {

    /**
     * The builder ID as set in the <code>plugin.xml</code> file.
     */
    public static final String BUILDER_ID = OOEclipsePlugin.OOECLIPSE_PLUGIN_ID + ".types"; //$NON-NLS-1$

    public static final int IDLC_STATE = 1;

    public static final int REGMERGE_STATE = 2;

    public static final int GENERATE_TYPES_STATE = 3;

    public static final int COMPLETED_STATE = 4;

    public static final int NOT_STARTED_STATE = -1;

    static int sBuildState = NOT_STARTED_STATE;

    private boolean mChangedIdl = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected IProject[] build(int pKind, Map<String, String> pArgs, IProgressMonitor pMonitor) throws CoreException {

        mChangedIdl = false;

        if (sBuildState < 0) {
            IResourceDelta delta = getDelta(getProject());
            if (delta != null) {
                delta.accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta pDelta) throws CoreException {

                        boolean visitChildren = false;
                        IProject prj = getProject();
                        IUnoidlProject unoprj = ProjectsManager.getProject(prj.getName());

                        if (unoprj != null) {
                            IPath idlPath = unoprj.getIdlPath();
                            IPath resPath = pDelta.getResource().getProjectRelativePath();

                            if (pDelta.getResource() instanceof IContainer
                                && resPath.segmentCount() < idlPath.segmentCount()) {
                                visitChildren = true;
                            } else if (pDelta.getResource() instanceof IContainer
                                && resPath.toString().startsWith(idlPath.toString())) {
                                visitChildren = true;
                            } else if (pDelta.getResource() instanceof IFile
                                && "idl".equalsIgnoreCase(resPath.getFileExtension())) { //$NON-NLS-1$
                                visitChildren = false;
                                mChangedIdl = true;
                            } else if (pDelta.getResource() instanceof IFile
                                && resPath.toString().endsWith(unoprj.getTypesPath().toString())) {
                                sBuildState = COMPLETED_STATE;
                            }
                        }
                        return visitChildren;
                    }
                });
            } else {
                mChangedIdl = true;
            }

            if (mChangedIdl && sBuildState < 0) {
                try {
                    build(getProject(), pMonitor);
                } catch (Exception e) {
                    sBuildState = NOT_STARTED_STATE;
                    CoreException thrown = new CoreException(
                        new Status(IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                            Messages.getString("TypesBuilder.BuildError0"), e)); //$NON-NLS-1$
                    if (e instanceof CoreException) {
                        thrown = (CoreException) e;
                    }
                    throw thrown;
                }
                sBuildState = NOT_STARTED_STATE;
            } else if (sBuildState == COMPLETED_STATE) {
                sBuildState = NOT_STARTED_STATE;
            }
        }

        return null;
    }

    /**
     * Build the types of a project.
     *
     * @param pPrj
     *            the project to build
     * @param pMonitor
     *            a monitor to report the build progress
     *
     * @throws Exception
     *             if anything wrong happens during the build
     */
    public static void build(IProject pPrj, IProgressMonitor pMonitor) throws Exception {

        IUnoidlProject unoprj = ProjectsManager.getProject(pPrj.getName());

        // Clears the registries before beginning
        sBuildState = IDLC_STATE;
        removeAllRegistries(pPrj);
        buildIdl(unoprj, pMonitor);

        sBuildState = REGMERGE_STATE;
        RegmergeBuilder.build(unoprj, pMonitor);

        sBuildState = GENERATE_TYPES_STATE;
        File types = pPrj.getLocation().append(unoprj.getTypesPath()).toFile();
        File build = pPrj.getLocation().append(unoprj.getBuildPath()).toFile();

        ILanguageBuilder languageBuilder = unoprj.getLanguage().getLanguageBuilder();
        languageBuilder.generateFromTypes(unoprj.getSdk(), unoprj.getOOo(), pPrj, types, build, unoprj.getRootModule(),
            pMonitor);

        pPrj.refreshLocal(IResource.DEPTH_INFINITE, pMonitor);
        sBuildState = NOT_STARTED_STATE;
    }

    /**
     * Removes all the registries, ie <code>.urd</code> and <code>types.rdb</code> files.
     *
     * @param pPrj
     *            the project from which to remove the registries
     */
    private static void removeAllRegistries(IProject pPrj) {

        IUnoidlProject unoprj = ProjectsManager.getProject(pPrj.getName());

        if (unoprj == null) {
            return;
        }

        try {
            IPath rdbPath = unoprj.getTypesPath();
            IFile rdbFile = pPrj.getFile(rdbPath);
            if (rdbFile.exists()) {
                rdbFile.delete(true, null);
            }

            IPath urdPath = unoprj.getUrdPath();
            IFolder urdFolder = pPrj.getFolder(urdPath);
            IResource[] members = urdFolder.members();

            for (int i = 0, length = members.length; i < length; i++) {
                IResource resi = members[i];
                if (resi.exists()) {
                    resi.delete(true, null);
                }
            }

        } catch (CoreException e) {
            PluginLogger.debug(e.getMessage());
        }
    }

    /**
     * Runs the idl files compilation.
     *
     * @param pProject
     *            the uno project to build
     * @param pMonitor
     *            a monitor to watch the progress
     * @throws Exception
     *             if anything wrong happened
     */
    public static void buildIdl(IUnoidlProject pProject, IProgressMonitor pMonitor) throws Exception {

        // compile each idl file
        IFolder idlFolder = pProject.getFolder(pProject.getIdlPath());
        if (idlFolder.exists()) {
            idlFolder.accept(new IdlcBuildVisitor(pMonitor));
        }
    }

    /**
     * Convenience method to execute the <code>idlc</code> tool on a given file.
     *
     * @param pFile
     *            the file to run <code>idlc</code> on.
     * @param pMonitor
     *            a progress monitor
     */
    static void runIdlcOnFile(IFile pFile, IProgressMonitor pMonitor) {

        IUnoidlProject project = ProjectsManager.getProject(pFile.getProject().getName());

        ISdk sdk = project.getSdk();

        if (null != sdk) {

            // Get local references to the SDK used members
            String sdkHome = sdk.getHome();

            Path sdkPath = new Path(sdkHome);
            int segmentCount = project.getIdlPath().segmentCount();

            if (!project.getUrdPath().toFile().exists()) {
                project.getUrdPath().toFile().mkdirs();
            }

            IPath outputLocation = project.getUrdPath().append(
                pFile.getProjectRelativePath().removeLastSegments(1).removeFirstSegments(segmentCount));

            String command = "idlc -O \"" + outputLocation.toOSString() + "\"" + //$NON-NLS-1$ //$NON-NLS-2$
                " -I \"" + sdkPath.append("idl").toOSString() + "\"" + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                " -I \"" + project.getIdlPath().toOSString() + "\"" + //$NON-NLS-1$ //$NON-NLS-2$
                " " + pFile.getProjectRelativePath().toOSString(); //$NON-NLS-1$

            Process process = project.getSdk().runTool(project, command, pMonitor);

            IdlcErrorReader errorReader = new IdlcErrorReader(process.getErrorStream(), pFile);
            errorReader.readErrors();
        }
    }
}
