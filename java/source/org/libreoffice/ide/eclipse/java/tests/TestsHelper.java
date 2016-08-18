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
package org.libreoffice.ide.eclipse.java.tests;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.java.utils.TemplatesHelper;

/**
 * Class used to generate the test files in the Java project.
 */
public class TestsHelper {

    private static final String[] TEMPLATES = new String[] {
        "UnoTests", //$NON-NLS-1$
        "base/UnoSuite", //$NON-NLS-1$
        "helper/UnoHelper",
        "uno/WriterTest"
    };
    private static final String TEST_PATH = "tests"; //$NON-NLS-1$

    private static final String JUNIT_CONTAINER = "org.eclipse.jdt.junit.JUNIT_CONTAINER"; //$NON-NLS-1$
    private static final String JUNIT4 = "4"; //$NON-NLS-1$
    private static final IPath JUNIT4_PATH = new Path(JUNIT_CONTAINER).append(JUNIT4);

    /**
     * Creates all the test classes files in the UNO project.
     *
     * @param pProject the destination UNO project
     */
    public static void writeTestClasses(IUnoidlProject pProject) {
        for (String template : TEMPLATES) {
            TemplatesHelper.copyTemplate(pProject, template + TemplatesHelper.JAVA_EXT,
                TestsHelper.class, TEST_PATH);
        }
    }

    /**
     * Add the JUnit3 library to the project libraries.
     *
     * @param pProject the project to add the libraries on
     */
    public static void addJUnitLibraries(IJavaProject pProject) {
        try {
            IClasspathEntry[] oldEntries = pProject.getRawClasspath();
            IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + 1];

            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);

            IClasspathEntry containerEntry = JavaCore.newContainerEntry(JUNIT4_PATH);
            entries[entries.length - 1] = containerEntry;

            pProject.setRawClasspath(entries, null);
        } catch (JavaModelException e) {
            PluginLogger.error(
                Messages.getString("TestsHelper.AddJUnitError"), e); //$NON-NLS-1$
        }
    }
}
