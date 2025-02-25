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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.libreoffice.ide.eclipse.core.PluginLogger;
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
    public boolean visit(IResourceDelta delta) throws CoreException {

        boolean visitChildren = true;

        IResource res = delta.getResource();
        if (res != null && res.getType() == IResource.FILE) {

            IProject prj = res.getProject();
            IUnoidlProject unoPrj = ProjectsManager.getProject(prj.getName());
            if (unoPrj != null) {
                // The resource is a UNO project or is contained in a UNO project
                visitChildren = true;
                // Check if the resource is a service implementation
                if (delta.getKind() == IResourceDelta.ADDED) {
                    addImplementation(unoPrj, res);
                } else if (delta.getKind() == IResourceDelta.REMOVED) {
                    removeImplementation(unoPrj, res);
                } else if (delta.getKind() == IResourceDelta.CHANGED) {
                    changeImplementation(unoPrj, res);
                }
            }
        }

        return visitChildren;
    }

    /**
     * Remove the delta resource from the implementations.
     *
     * @param prj the concerned UNO project
     * @param res the concerned delta resource
     */
    private void removeImplementation(IUnoidlProject prj, IResource res) {
        if (isJavaSourceFile(res)) {
            IType type = getJavaImplementationType(prj.getProject(), res);
            if (type != null) {
                RegistrationHelper.removeImplementation(prj, type.getFullyQualifiedName());
            }
        }
    }

    /**
     * Add the delta resource to the implementations.
     *
     * @param prj the concerned UNO project
     * @param res the concerned delta resource
     */
    private void addImplementation( IUnoidlProject prj, IResource res) {
        if (isJavaSourceFile(res)) {
            IType type = getJavaImplementationType(prj.getProject(), res);
            if (type != null) {
                IField field = getServiceNameField(type);
                if (field.exists()) {
                    addImplementation(prj, type, field);
                }
            }
        }
    }

    /**
     * Change the delta resource to the java project.
     *
     * @param prj the concerned UNO project
     * @param res the concerned delta resource
     */
    private void changeImplementation(IUnoidlProject prj, IResource res) {
        if (isEclipseClassPath(res) && prj.hasBuildFile()) {
            changeJavaBuildProperties(prj, res);
        } else if (isJavaSourceFile(res)) {
            IType type = getJavaImplementationType(prj.getProject(), res);
            if (type != null) {
                IField field = getServiceNameField(type);
                if (field.exists()) {
                    addImplementation(prj, type, field);
                } else {
                    RegistrationHelper.removeImplementation(prj, type.getFullyQualifiedName());
                }
            }
        }
    }

    /**
     * Add the type to the implementations.
     *
     * @param prj the concerned UNO project
     * @param type the concerned delta resource
     * @param field the concerned delta resource
     */
    private void addImplementation(IUnoidlProject prj, IType type, IField field) {
        String serviceName = getServiceNameValue(field);
        if (serviceName != null) {
            RegistrationHelper.addImplementation(prj, type.getFullyQualifiedName(), serviceName);
        }
    }

    /**
     * Is resource a Java source file.
     *
     * @param res the resource to check.
     * @return <code>true</code> if resource is a Java source file or <code>false</code> if not.
     */
    private boolean isJavaSourceFile(IResource res) {
        return "java".equals(res.getFileExtension()); //$NON-NLS-1$
    }

    /**
     * Is resource the Eclipse .classpath.
     *
     * @param res the resource to check.
     * @return <code>true</code> if resource is the Eclipse .classpath file or <code>false</code> if not.
     */
    private boolean isEclipseClassPath(IResource res) {
        return res.getName().equals(".classpath"); //$NON-NLS-1$
    }

    /**
     * Change the delta resource to the java project.
     *
     * @param prj the concerned UNO project
     * @param res the concerned delta resource
     */
    private void changeJavaBuildProperties(IUnoidlProject prj, IResource res) {
        List <IResource> libs = JavaClassPathProvider.getWorkspaceLibs(prj);
        PluginLogger.debug("Found " + libs.size() + " Jars"); //$NON-NLS-1$ //$NON-NLS-2$
        prj.saveJavaBuildProperties(libs);
    }

    /**
     * Get the java type of resource.
     *
     * @param prj the project.
     * @param res the resource to check.
     * @return <code>type</code> if it contains the necessary static methods for Java
     *      UNO service implementation registration or <code>null</code> if not.
     */
    private IType getJavaImplementationType(IProject prj, IResource res) {

        IType implementation = null;
        IJavaElement element = JavaCore.create(res);
        if (element.getElementType() == IJavaElement.COMPILATION_UNIT) {
            ICompilationUnit unit = (ICompilationUnit) element;
            String[] parameters = {"QXComponentContext;"}; //$NON-NLS-1$
            try {
                for (IType type : unit.getAllTypes()) {
                    // Does this resource has a constructor with XComponentContext has unique parameter
                    IMethod method = type.getMethod(type.getElementName(), parameters);
                    if (method.exists() && method.isConstructor()) {
                        implementation = type;
                        break;
                    }
                }
            } catch (JavaModelException e) {
                e.printStackTrace();
            }
        }
        return implementation;
    }

    /**
     * Get service name filed.
     *
     * @param type the type to get field.
     * @return the service name field from the given type.
     */
    private IField getServiceNameField(IType type) {
        return type.getField("m_serviceName"); //$NON-NLS-1$
    }

    /**
     * Get service name value.
     *
     * @param field the field having service name.
     * @return the service name value from the given field.
     */
    private String getServiceNameValue(IField field) {
        Object obj = null;
        try {
            obj = field.getConstant();
        } catch (JavaModelException e) { }
        String value = null;
        if (obj != null) {
            value = (String) obj;
            if (value != null && !value.isEmpty() &&
                value.charAt(0) == '"' &&
                value.charAt(value.length() - 1) == '"') {
                value = value.substring(1, value.length() - 1);
            }
        }
        return value;
    }

}
