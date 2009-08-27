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
package org.openoffice.ide.eclipse.java.tests;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.junit.buildpath.JUnitContainerInitializer;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.java.utils.TemplatesHelper;

/**
 * Class used to generate the test files in the Java project.
 * 
 * @author cbosdonnat
 *
 */
public class TestsHelper {
    
    private static final String[] TEMPLATES = new String[] {
        "AllTests", //$NON-NLS-1$
        "ProjectTest", //$NON-NLS-1$
        "base/Bootstrap", //$NON-NLS-1$
        "base/UnoTestCase", //$NON-NLS-1$
        "base/UnoTestSuite" //$NON-NLS-1$
    };
    private static final String TEST_PATH = "tests"; //$NON-NLS-1$
    
    /**
     * Creates all the test classes files in the UNO project.
     * 
     * @param pProject the destination UNO project
     */
    public static void writeTestClasses( IUnoidlProject pProject ) {
        for (String template : TEMPLATES) {
            TemplatesHelper.copyTemplate( pProject, template, TestsHelper.class, TEST_PATH );
        }
    }
    
    /**
     * Add the JUnit3 library to the project libraries.
     * 
     * @param pProject the project to add the libraries on
     */
    @SuppressWarnings("restriction")
    public static void addJUnitLibraries( IJavaProject pProject ) {
        try {
            IClasspathEntry[] oldEntries = pProject.getRawClasspath();
            IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + 1];
            
            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
            
            IPath path = JUnitContainerInitializer.JUNIT3_PATH;
            IClasspathEntry containerEntry = JavaCore.newContainerEntry(path);
            entries[entries.length - 1] = containerEntry;
            
            pProject.setRawClasspath(entries, null);
        } catch (JavaModelException e) {
            PluginLogger.error(
                    Messages.getString("TestsHelper.AddJUnitError"), e); //$NON-NLS-1$
        }
    }
}
