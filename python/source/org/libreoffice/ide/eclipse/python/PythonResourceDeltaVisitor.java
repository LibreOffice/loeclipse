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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
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
    public boolean visit(IResourceDelta delta) throws CoreException {

        boolean visitChildren = true;

        IResource res = delta.getResource();
        if (res != null && res.getType() == IResource.FILE && isPythonSourceFile(res)) {

            IUnoidlProject project = ProjectsManager.getProject(res.getProject().getName());
            if (project != null) {
                // The resource is a UNO project or is contained in a UNO project
                visitChildren = true;

                // Check if the resource is a service implementation
                if (delta.getKind() == IResourceDelta.ADDED) {
                    addImplementation(project, res);
                } else if (delta.getKind() == IResourceDelta.REMOVED) {
                    removeImplementation(project, res);
                } else if (delta.getKind() == IResourceDelta.CHANGED) {
                    changeImplementation(project, res);
                }
            }
        }

        return visitChildren;
    }

    private boolean isPythonSourceFile(IResource res) {
        return ((IFile) res).getFileExtension().equals("py");
    }

    /**
     * Remove the delta resource from the implementations.
     *
     * @param prj the concerned UNO project
     * @param res the resource to remove
     */
    private void removeImplementation(IUnoidlProject prj, IResource res) {
        String content = getPythonFileContent(res);
        if (content != null) {
            String uri = res.getProjectRelativePath().toString();
            RegistrationHelper.removeImplementation(prj, uri);
        }
    }

    /**
     * Add the delta resource to the implementations.
     *
     * @param prj the concerned UNO project
     * @param res the resource to add.
     */
    private void addImplementation(IUnoidlProject prj, IResource res) {
        String content = getPythonFileContent(res);
        if (content != null) {
            String uri = res.getProjectRelativePath().toString();
            String serviceName = getPythonServiceName(content);
            if (serviceName != null) {
                RegistrationHelper.addImplementation(prj, uri, serviceName);
            }
        }
    }

    /**
     * Change the delta resource to the java project.
     *
     * @param prj the concerned UNO project
     * @param res the concerned resource
     */
    private void changeImplementation(IUnoidlProject prj, IResource res) {
        String content = getPythonFileContent(res);
        if (content != null) {
            String uri = res.getProjectRelativePath().toString();
            String serviceName = getPythonServiceName(content);
            if (serviceName != null) {
                RegistrationHelper.addImplementation(prj, uri, serviceName);
            } else {
                RegistrationHelper.removeImplementation(prj, uri);
            }
        }
    }

    /**
     * Get the python source file content.
     *
     * @param res the resource to check.
     * @return <code>file content</code> if it contains the necessary static methods for Python
     *      UNO service implementation registration or <code>null</code> if not.
     */
    private String getPythonFileContent(IResource res) {

        String fileContent = null;
        FileInputStream in = null;
        BufferedReader reader = null;
        try {
            File file = res.getLocation().toFile();
            in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in));

            // Read the .py file into a string without line delimiters
            StringBuffer buffer = new StringBuffer();
            String line = reader.readLine();
            while (line != null) {
                buffer.append(line);
                buffer.append(' ');
                line = reader.readLine();
            }
            String content = buffer.toString();

            String implHelper = "g_ImplementationHelper *= *unohelper.ImplementationHelper *\\( *\\)"; //$NON-NLS-1$
            if (content.split(implHelper).length > 1) {
                fileContent = content;
            }

        } catch (Exception e) {
            // nothing to log
        } finally {
            try {
                reader.close();
                in.close();
            } catch (Exception e) { }
        }

        return fileContent;
    }

    /**
     * Get the python service implementation name.
     *
     * @param content the .py file content.
     * @return <code>service name</code> if it contains the necessary static methods for Python
     *      UNO service implementation registration or <code>null</code> if not.
     */
    private String getPythonServiceName(String content) {
        String serviceName = null;
        String pattern = "g_ServiceName *= *[\"']([^\"' ][a-zA-Z0-9\\.\\-_]+)[\"']"; //$NON-NLS-1$
        Matcher matcher = Pattern.compile(pattern).matcher(content);
        if (matcher.find()) {
            serviceName = matcher.group(1);
        }
        return serviceName;
    }

}
