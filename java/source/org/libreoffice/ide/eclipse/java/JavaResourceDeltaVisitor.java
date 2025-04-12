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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.java.registration.ASTParserHelper;
import org.libreoffice.ide.eclipse.java.registration.CompilationUnitHelper;
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

        boolean visit = true;

        IResource res = delta.getResource();
        if (res != null && res.getType() == IResource.FILE) {

            IFile file = (IFile) res;
            IUnoidlProject project = ProjectsManager.getProject(res.getProject().getName());
            // The resource is a UNO project or is contained in a UNO project
            if (project != null) {
                // Check if the resource is a service implementation
                if (delta.getKind() == IResourceDelta.ADDED) {
                    addImplementation(project, file);
                } else if (delta.getKind() == IResourceDelta.REMOVED) {
                    removeImplementation(project, file);
                } else if (delta.getKind() == IResourceDelta.CHANGED) {
                    changeProjectResource(project, file);
                }
            }
        }

        return visit;
    }

    /**
     * Remove the delta resource from the implementations.
     *
     * @param prj the concerned UNO project
     * @param file the concerned IFile resource
     * @throws JavaModelException 
     */
    private void removeImplementation(IUnoidlProject prj, IFile file) throws JavaModelException {
        if (isJavaSourceFile(file)) {
            ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
            if (unit != null) {
                IType type = ASTParserHelper.getImplementationType(unit);
                if (type != null) {
                    RegistrationHelper.removeImplementation(prj, type.getFullyQualifiedName());
                }
            }
        }
    }

    /**
     * Add the delta resource to the implementations.
     *
     * @param prj the concerned UNO project
     * @param file the concerned delta resource
     * @throws JavaModelException 
     */
    private void addImplementation(IUnoidlProject prj, IFile file) throws JavaModelException {
        if (isJavaSourceFile(file)) {
            changeImplementation(prj, file, false);
        }
    }

    /**
     * Change the delta resource to the java project.
     *
     * @param prj the concerned UNO project
     * @param file the concerned delta resource
     * @throws JavaModelException 
     */
    private void changeProjectResource(IUnoidlProject prj, IFile file) throws JavaModelException {
        if (isEclipseClassPath(file) && prj.hasBuildFile()) {
            changeJavaBuildProperties(prj);
        } else if (isJavaSourceFile(file)) {
            changeImplementation(prj, file, true);
        }
    }

    /**
     * Is resource a Java source file.
     *
     * @param file the resource to check.
     * @return <code>true</code> if resource is a Java source file or <code>false</code> if not.
     */
    private boolean isJavaSourceFile(IFile file) {
        return "java".equals(file.getFileExtension()); //$NON-NLS-1$
    }

    /**
     * Is resource the Eclipse .classpath.
     *
     * @param file the resource to check.
     * @return <code>true</code> if resource is the Eclipse .classpath file or <code>false</code> if not.
     */
    private boolean isEclipseClassPath(IFile file) {
        return ".classpath".equals(file.getName()); //$NON-NLS-1$
    }

    /**
     * Change the delta resource to the java project.
     *
     * @param prj the concerned UNO project
     */
    private void changeJavaBuildProperties(IUnoidlProject prj) {
        List<IResource> libs = JavaClassPathProvider.getWorkspaceLibs(prj);
        PluginLogger.debug("Found " + libs.size() + " Jars"); //$NON-NLS-1$ //$NON-NLS-2$
        prj.saveJavaBuildProperties(libs);
    }

    /**
     * change an implementations.
     *
     * @param prj the concerned UNO project
     * @param file the Java resource file
     * @param remove the implementation if not found
     * @throws JavaModelException 
     */
    private void changeImplementation(IUnoidlProject prj, IFile file, boolean remove) throws JavaModelException {
        ICompilationUnit unit = JavaCore.createCompilationUnitFrom(file);
        if (unit != null) {
            IType type = ASTParserHelper.getImplementationType(unit);
            if (type != null) {
                changeImplementation(prj, new CompilationUnitHelper(type, unit), remove);
            }
        }
    }

    /**
     * change an implementations.
     *
     * @param prj the concerned UNO project
     * @param unit the CompilationUnitHelper
     * @param remove the implementation if not found
     * @throws JavaModelException 
     */
    private void changeImplementation(IUnoidlProject prj, CompilationUnitHelper unit, boolean remove)
        throws JavaModelException {
        ASTNode field = ASTParserHelper.getFieldDeclarationNode(unit.getCompilationUnit(), "m_serviceNames");
        if (field != null) {
            addImplementation(prj, unit, field);
        } else if (remove) {
            RegistrationHelper.removeImplementation(prj, unit.getTypeName());
        }
    }

    /**
     * Add the type to the implementations.
     *
     * @param prj the concerned UNO project
     * @param unit the CompilationUnitHelper
     * @param field the concerned field declaration node
     * @throws JavaModelException 
     */
    private void addImplementation(IUnoidlProject prj, CompilationUnitHelper unit, ASTNode field)
        throws JavaModelException {
        String[] services = ASTParserHelper.getServiceNames(unit, field);
        if (services.length > 0) {
            RegistrationHelper.addImplementation(prj, unit.getTypeName(), services);
        } else {
            RegistrationHelper.removeImplementation(prj, unit.getTypeName());
        }
    }

}
