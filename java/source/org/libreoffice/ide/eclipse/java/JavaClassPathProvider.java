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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * An helper for getting the java project class path.
 */
public class JavaClassPathProvider {

    /**
     * Get the libraries in the classpath that are located in the workspace.
     *
     * @param unoPrj the project from which to extract the libraries
     * 
     * @param javaPrj the Java project from which to extract the libraries
     * 
     * @return a list of all the File pointing to the libraries.
     */
    public static final List<IResource> getProjectLibs(IUnoidlProject unoPrj, IJavaProject javaPrj) {
        return getWorkspaceLibs(unoPrj, javaPrj, false);
    }

    /**
     * Get the libraries in the classpath that are located in the workspace.
     *
     * @param unoPrj the project from which to extract the libraries
     * 
     * @return a list of all the File pointing to the libraries.
     */
    public static final List<IResource> getWorkspaceLibs(IUnoidlProject unoPrj) {
        return getWorkspaceLibs(unoPrj, JavaCore.create(unoPrj.getProject()), true);
    }

    /**
     * Get the libraries in the classpath that are located in the workspace.
     *
     * @param unoPrj the UNO project from which to extract the libraries
     * 
     * @param javaPrj the Java project from which to extract the libraries
     * 
     * @param all get the libraries for workspace or project
     * 
     * @return a list of all the File pointing to the libraries.
     */
    private static final List<IResource> getWorkspaceLibs(IUnoidlProject unoPrj,
                                                          IJavaProject javaPrj,
                                                          boolean all) {
        PluginLogger.debug("Collecting Jars from: .classpath");

        List<IResource> libs = new ArrayList<>();
        try {
            IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
            IClasspathEntry[] entries = javaPrj.getResolvedClasspath(true);
            for (IClasspathEntry entry : entries) {
                IPath path = entry.getPath();
                IResource res = root.findMember(path);
                if (res == null || !res.exists()) {
                    continue;
                }
                PluginLogger.debug("JavaClassPathProvider.getWorkspaceLibs() lib: " + path.toOSString());
                setWorkspaceLibs(root, unoPrj, libs, entry, res, path , all);
            }
        } catch (JavaModelException e) {
            PluginLogger.error(Messages.getString("JavaClassPathProvider.GetWorkspaceLibsFailed"), e);
        }
        return libs;
    }

    private static final void setWorkspaceLibs(IWorkspaceRoot root,
                                               IUnoidlProject unoPrj,
                                               List<IResource> libs,
                                               IClasspathEntry entry,
                                               IResource res,
                                               IPath path,
                                               boolean all) {
        // We will retrieve all libraries located in the workspace
        if (all && entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
            // A project has been added to the build path, we are looking for a Build.jardesc file
            IFile file = root.getFile(path.append("Build.jardesc"));
            if (file.exists()) {
                setLibsFromProjectJarDesc(root, libs, file);
            } else if (!setLibsFromProjectClass(root, libs, path)) {
                String msg = "Collecting Jars from project: %s can't find Build.jardesc file!!!";
                PluginLogger.error(String.format(msg, res.getName()));
            }
        // We will retrieve all libraries located in the project except the build directory
        } else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
            if (!res.getProjectRelativePath().equals(unoPrj.getBuildPath()) &&
                (all || res.getProject() == unoPrj.getProject()) &&
                res.getType() == IResource.FILE) {
                libs.add(res);
            }
        }
    }

    private static final void setLibsFromProjectJarDesc(IWorkspaceRoot root,
                                                        List<IResource> libs,
                                                        IFile file) {
        try {
            FileInputStream byteStream = new FileInputStream(file.getLocation().toFile());
            InputSource source = new InputSource(byteStream);

            // XPath expression evaluation
            XPathFactory factory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = factory.newXPath();
            final String xPathExpr = "//jardesc/jar";
            XPathExpression exp = xpath.compile(xPathExpr);
            Element node = (Element) exp.evaluate(source, XPathConstants.NODE);
            if (node != null) {
                String value = node.getAttribute("path");
                if (value != null && !value.isBlank()) {
                    IResource res = root.findMember(value);
                    if (res != null && res.exists() && res.getType() == IResource.FILE) {
                        libs.add(res);
                    }
                }
            }
            byteStream.close();
        } catch (IOException | XPathExpressionException e) {
            String msg = "Collecting Jars can't parse file: %s!!!";
            PluginLogger.debug(String.format(msg, file.getLocation().toOSString()));
        }
    }

    private static final boolean setLibsFromProjectClass(IWorkspaceRoot root,
                                                         List<IResource> libs,
                                                         IPath path) {
        boolean retrieved = false;
        IFile file = root.getFile(path.append(".classpath"));
        if (file.exists()) {
            IResource res = getLibsFromProjectClass(root, libs, path, file);
            if (res != null) {
                libs.add(res);
                retrieved = true;
            }
        }
        return retrieved;
    }

    private static final IResource getLibsFromProjectClass(IWorkspaceRoot root,
                                                           List<IResource> libs,
                                                           IPath path,
                                                           IFile file) {
        IResource res = null;
        try {
            FileInputStream byteStream = new FileInputStream(file.getLocation().toFile());
            InputSource source = new InputSource(byteStream);

            // XPath expression evaluation
            XPathFactory factory = XPathFactory.newInstance();
            javax.xml.xpath.XPath xpath = factory.newXPath();
            final String xPathExpr = "//classpath/classpathentry[@kind='output']";
            XPathExpression exp = xpath.compile(xPathExpr);
            Element node = (Element) exp.evaluate(source, XPathConstants.NODE);
            if (node != null) {
                String value = node.getAttribute("path");
                if (value != null && !value.isBlank()) {
                    IResource folder = root.getFolder(path.append(value));
                    if (folder != null && folder.exists() && folder.getType() == IResource.FOLDER) {
                        res = folder;
                    }
                }
            }
            byteStream.close();
        } catch (IOException | XPathExpressionException e) {
            String msg = "Collecting Jars can't parse file: %s!!!";
            PluginLogger.debug(String.format(msg, file.getLocation().toOSString()));
        }
        return res;
    }

}
