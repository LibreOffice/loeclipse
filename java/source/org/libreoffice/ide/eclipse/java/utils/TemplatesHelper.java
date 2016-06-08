/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Cédric Bosdonnat.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.java.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;

/**
 * Provides convenient methods to load, and save templates of Java source
 * files into a target projet.
 *
 * <p>In order to generate the template <tt>foo/bar.java.tpl</tt>, the following
 * call call be used, where <tt>LoadingClass</tt> is a class from which it is
 * possible to run a <tt>getResource( "foo/bar.java.tpl" )</tt> to get the file.</p>
 *
 * <code>
 * TemplatesHelper.copyTemplate( myProject, "foo/bar", LoadingClass.class );
 * </code>
 *
 * @author cbosdonnat
 *
 */
public class TemplatesHelper {

    public static final String JAVA_EXT = ".java"; //$NON-NLS-1$
    private static final String TEMPLATE_EXT = ".tpl"; //$NON-NLS-1$

    /**
     * Copies the template to the UNO project.
     *
     * @param pProject the project where to copy the file
     * @param pTemplateName the template name (without the extension)
     * @param pClazz the class from which to load the resource file
     * @param pDestSuffix the subpath in which the file should be copied, relatively to
     *          the implementation package.
     * @param pArgs additional arguments to pass to the formatter
     */
    public static void copyTemplate(IUnoidlProject pProject, String pTemplateName,
        Class<?> pClazz, String pDestSuffix, Object... pArgs) {

        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pProject.getName());

        IPath relPath = pProject.getImplementationPath();
        relPath = relPath.append(pDestSuffix);
        IFolder dest = pProject.getFolder(relPath);

        // Compute the name of the project's implementation package
        String implPkg = pProject.getCompanyPrefix() + "." + pProject.getOutputExtension(); //$NON-NLS-1$

        Object[] args = new Object[pArgs.length + 1];
        args[0] = implPkg;
        System.arraycopy(pArgs, 0, args, 1, pArgs.length);

        copyTemplate(prj, pTemplateName, pClazz,
            dest.getProjectRelativePath().toString(), args);
    }

    /**
     * Copies the template to the project.
     *
     * @param pProject the project where to copy the file
     * @param pTemplateName the template name (without the extension)
     * @param pClazz the class from which to load the resource file
     * @param pDestPath the path in which the file should be copied, relatively to
     *          the project root.
     * @param pArgs additional arguments to pass to the formatter
     */
    public static void copyTemplate(IProject pProject, String pTemplateName,
        Class<?> pClazz, String pDestPath, Object... pArgs) {

        String fileName = pTemplateName + TEMPLATE_EXT;

        // Get the path where to place the files
        IContainer dest = pProject;
        if (pDestPath != null && !pDestPath.equals(new String())) {
            dest = pProject.getFolder(pDestPath);
            dest.getLocation().toFile().mkdirs();
        }

        // Read the template into a buffer
        FileWriter writer = null;

        BufferedReader patternReader = null;
        InputStream in = null;
        try {
            // Destination file opening
            IFile classIFile = dest.getFile(new Path(pTemplateName));
            File classFile = classIFile.getLocation().toFile();

            if (!classFile.exists()) {
                classFile.getParentFile().mkdirs();
                classFile.createNewFile();
            }
            writer = new FileWriter(classFile);

            // Input template opening
            in = pClazz.getResourceAsStream(fileName);
            patternReader = new BufferedReader(new InputStreamReader(in));

            // Loop over the lines, format and write them.
            String line = patternReader.readLine();
            while (line != null) {
                line = MessageFormat.format(line, pArgs);
                writer.append(line + "\n"); //$NON-NLS-1$
                line = patternReader.readLine();
            }
        } catch (IOException e) {
            // log the error
            String msg = MessageFormat.format(Messages.getString("TemplatesHelper.ErrorPattern"), //$NON-NLS-1$
                pTemplateName, Messages.getString("TemplatesHelper.ReadError")); //$NON-NLS-1$
            PluginLogger.error(msg, e);
        } finally {
            try {
                patternReader.close();
                in.close();
                writer.close();
            } catch (Exception e) {
            }
        }
    }
}
