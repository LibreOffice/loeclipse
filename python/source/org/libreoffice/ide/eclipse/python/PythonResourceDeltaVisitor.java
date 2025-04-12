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
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
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

        boolean visit = true;

        IResource res = delta.getResource();
        if (res != null && res.getType() == IResource.FILE) {

            IFile file = (IFile) res;
            IUnoidlProject project = ProjectsManager.getProject(res.getProject().getName());
            // The resource is a UNO project or is contained in a UNO project
            if (project != null && isPythonSourceFile(file)) {
                // Check if the resource is a service implementation
                if (delta.getKind() == IResourceDelta.ADDED) {
                    addImplementation(project, file);
                } else if (delta.getKind() == IResourceDelta.REMOVED) {
                    removeImplementation(project, file);
                } else if (delta.getKind() == IResourceDelta.CHANGED) {
                    changeImplementation(project, file);
                }
            }
        }

        return visit;
    }

    private boolean isPythonSourceFile(IFile file) {
        return file.getFileExtension().equals("py"); //$NON-NLS-1$
    }

    /**
     * Remove the delta resource from the implementations.
     *
     * @param prj the concerned UNO project
     * @param file the resource to remove
     */
    private void removeImplementation(IUnoidlProject prj, IFile file) {
        String content = getPythonFileContent(file);
        if (content != null) {
            String uri = file.getProjectRelativePath().toString();
            RegistrationHelper.removeImplementation(prj, uri);
        }
    }

    /**
     * Add the delta resource to the implementations.
     *
     * @param prj the concerned UNO project
     * @param file the resource to add.
     */
    private void addImplementation(IUnoidlProject prj, IFile file) {
        String content = getPythonFileContent(file);
        if (content != null) {
            String uri = file.getProjectRelativePath().toString();
            String implementation = getImplementationName(content);
            if (implementation != null) {
                addImplementation(prj, uri, implementation, content);
            }
        }
    }

    /**
     * Change the delta resource to the java project.
     *
     * @param prj the concerned UNO project
     * @param file the concerned resource
     */
    private void changeImplementation(IUnoidlProject prj, IFile file) {
        String content = getPythonFileContent(file);
        if (content != null) {
            String uri = file.getProjectRelativePath().toString();
            String implementation = getImplementationName(content);
            if (implementation != null) {
                addImplementation(prj, uri, implementation, content);
            } else {
                RegistrationHelper.removeImplementation(prj, uri);
            }
        }
    }

    /**
     * Add the implementations.
     *
     * @param prj the concerned UNO project
     * @param uri the concerned uri
     * @param implementation the concerned implementation
     * @param content the concerned python file content
     */
    private void addImplementation(IUnoidlProject prj, String uri, String implementation, String content) {
        String[] services = getServiceNames(content);
        if (services.length > 0) {
            RegistrationHelper.addImplementation(prj, uri, implementation, services);
        } else {
            RegistrationHelper.removeImplementation(prj, uri);
        }
    }

    /**
     * Get the Python source file content.
     *
     * @param file the resource to check.
     * @return <code>file content</code> if it contains the necessary static methods for Python
     *      UNO service implementation registration or <code>null</code> if not.
     */
    private String getPythonFileContent(IFile file) {

        String fileContent = null;
        FileInputStream in = null;
        BufferedReader reader = null;
        try {
            in = new FileInputStream(file.getLocation().toFile());
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

            String pattern = "g_ImplementationHelper\\s*=\\s*"; //$NON-NLS-1$
            pattern += "unohelper.ImplementationHelper\\s*\\(\\s*\\)"; //$NON-NLS-1$
            if (content.split(pattern).length > 1) {
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
     * Get the Python implementation name.
     *
     * @param content the .py file content.
     * @return <code>implementation name</code> if it contains the necessary static methods for Python
     *      UNO service implementation registration or <code>null</code> if not.
     */
    private String getImplementationName(String content) {
        String implementation = null;
        String pattern = "g_ImplementationName\\s*=\\s*"; //$NON-NLS-1$
        pattern += "[\"']([^\"' ][\\w\\.\\-_]+)[\"']"; //$NON-NLS-1$
        String value = getPropertyValue(content, pattern);
        if (value != null) {
            implementation = value.trim();
        }
        return implementation;
    }


    /**
     * Get the Python service names.
     *
     * @param content the .py file content.
     * @return <code>service names</code> if it contains the necessary static methods for Python
     *      UNO service implementation registration or <code>null</code> if not.
     */
    private String[] getServiceNames(String content) {
        List<String> services = new ArrayList<>();
        String pattern = "g_ServiceNames\\s*=\\s*"; //$NON-NLS-1$
        pattern += "[\\(\\[]([^\\(\\)\\[\\]][\\w\\.\\-_\\s,\"']+)[\\)\\]]"; //$NON-NLS-1$
        String values = getPropertyValue(content, pattern);
        if (values != null) {
            for (String value : values.split(",")) {
                String service = value.trim().replace("'", "").replace("\"", "");
                if (!service.isBlank()) {
                    services.add(service);
                }
            }
        }
        return services.toArray(new String[services.size()]);
    }

    /**
     * Get the python property value.
     *
     * @param content the .py file content.
     * @param pattern the property to search.
     * @return <code>property value</code> if it contains the necessary static methods for Python
     *      UNO service implementation registration or <code>null</code> if not.
     */
    private String getPropertyValue(String content, String pattern) {
        String value = null;
        Matcher matcher = Pattern.compile(pattern).matcher(content);
        if (matcher.find()) {
            value = matcher.group(1);
        }
        return value;
    }

}
