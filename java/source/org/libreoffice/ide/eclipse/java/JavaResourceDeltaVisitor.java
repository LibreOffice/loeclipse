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
package org.libreoffice.ide.eclipse.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.java.registration.RegistrationHelper;

/**
 * This class will visit a resource delta and perform the necessary changes
 * on Java resources included in UNO projects.
 */
public class JavaResourceDeltaVisitor implements IResourceDeltaVisitor {

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
        IResource res = pDelta.getResource();
        if (res.getName().endsWith(".java")) { //$NON-NLS-1$
            String prjPath = pDelta.getProjectRelativePath().toString();
            prjPath = prjPath.replace(".java", ""); //$NON-NLS-1$ //$NON-NLS-2$
            prjPath = prjPath.replace("/", "."); //$NON-NLS-1$ //$NON-NLS-2$

            Vector<String> classes = RegistrationHelper.readClassesList(pUnoprj);
            for (String implName : classes) {
                if (prjPath.endsWith(implName)) {
                    RegistrationHelper.removeImplementation(pUnoprj, implName);
                }
            }
        } else if (res.getName().endsWith(".jar")) {
            RegistrationHelper.isFileEmpty(pUnoprj);
        }
    }

    /**
     * Add the delta resource to the implementations.
     *
     * @param pDelta the delta resource to add.
     * @param pUnoProject the concerned UNO project
     */
    private void addImplementation(IResourceDelta pDelta, IUnoidlProject pUnoProject) {
        String className = isJavaServiceImpl(pDelta.getResource());
        if (className != null) {
            RegistrationHelper.addImplementation(pUnoProject, className);
        }
    }

    /**
     * Check whether a resource is a UNO implementation.
     *
     * @param pResource the resource to check.
     * @return <code>true</code> if it contains the necessary static methods for Java
     *      UNO service implementation registration.
     */
    private String isJavaServiceImpl(IResource pResource) {

        String className = null;
        if (pResource.getType() == IResource.FILE && pResource.getName().endsWith(".java")) { //$NON-NLS-1$
            /*
             * For sure the resource is a Java class file.
             * Now the file has to be read to find out if it contains the two
             * following methods:
             *
             *    + public static XSingleComponentFactory __getComponentFactory
             *    + public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey )
             */
            FileInputStream in = null;
            BufferedReader reader = null;
            try {
                File file = pResource.getLocation().toFile();
                in = new FileInputStream(file);
                reader = new BufferedReader(new InputStreamReader(in));

                // Read the file into a string without line delimiters
                String line = reader.readLine();
                String fileContent = ""; //$NON-NLS-1$
                while (line != null) {
                    fileContent = fileContent + line;
                    line = reader.readLine();
                }

                String getFactoryRegex = "public\\s+static\\s+XSingleComponentFactory" + //$NON-NLS-1$
                    "\\s+__getComponentFactory"; //$NON-NLS-1$
                boolean containsGetFactory = fileContent.split(getFactoryRegex).length > 1;

                String writeServiceRegex = "public\\s+static\\s+boolean\\s+__writeRegistryServiceInfo"; //$NON-NLS-1$
                boolean containsWriteService = fileContent.split(writeServiceRegex).length > 1;

                // Do not consider the RegistrationHandler class as a service implementation
                if (containsGetFactory && containsWriteService &&
                    !pResource.getName().equals("RegistrationHandler.java")) { //$NON-NLS-1$
                    /*
                     * Computes the class name
                     */
                    Matcher m3 = Pattern.compile("[^;]*package\\s+([^;]+);.*").matcher(fileContent); //$NON-NLS-1$
                    if (m3.matches()) {
                        String packageName = m3.group(1);

                        String fileName = pResource.getName();
                        className = fileName.substring(0, fileName.length() - ".java".length()); //$NON-NLS-1$

                        className = packageName + "." + className; //$NON-NLS-1$
                    }
                }

            } catch (Exception e) {
                // nothing to log
            } finally {
                try {
                    reader.close();
                    in.close();
                } catch (Exception e) {
                }
            }
        }

        return className;
    }
}
