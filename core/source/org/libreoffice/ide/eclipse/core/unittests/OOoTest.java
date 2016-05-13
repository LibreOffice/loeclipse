/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.unittests;

import java.io.File;
import java.io.FileFilter;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.libreoffice.ide.eclipse.core.internal.model.AbstractOOo;
import org.libreoffice.ide.eclipse.core.internal.model.OOo;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;

import junit.framework.TestCase;

/**
 * Unit test class checking the OOo structure recognition.
*/
public class OOoTest extends TestCase {

    private static final String TEST_PROP = "ooo.tests"; //$NON-NLS-1$

    /**
     * Test if the directories checks for various versions of OOo are working.
     *
     * <p>
     * In order to make this test run, a <tt>ooo.tests</tt> variable has to be set. This should point to a directory
     * containing a directory per supported OS, which are:
     * </p>
     * <ul>
     * <li>linux</li>
     * <li>macosx</li>
     * <li>win32</li>
     * </ul>
     *
     * <p>
     * Each one of these directories have to contain the one directory per installation to test. For example, if one
     * wants to test OpenOffice.org 2.4 and 3.1 for Linux, the following directories should be created in the
     * <tt>linux</tt> folder:
     * </p>
     * <ul>
     * <li>OpenOffice.org 2.4</li>
     * <li>OpenOffice.org 3.1</li>
     * </ul>
     *
     * <p>
     * The names are only used for readability purpose. The tested OOo folders will be these ones.
     * </p>
     */
    public void testOOoChecks() {
        String pattern = "Unexpected OOo load failure for OS {0}, installation {1}"; //$NON-NLS-1$
        String testsPath = System.getProperty(TEST_PROP);
        File testsDir = new File(testsPath);

        // Loop over the platforms
        File[] dirs = testsDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pPathname) {
                boolean isDir = pPathname.isDirectory() && pPathname.canRead();
                boolean isMacos = pPathname.getName().equals(Platform.OS_MACOSX);
                boolean isWin32 = pPathname.getName().equals(Platform.OS_WIN32);
                boolean isLinux = pPathname.getName().equals(Platform.OS_LINUX);

                return isDir && (isMacos || isWin32 || isLinux);
            }
        });
        for (File platformDir : dirs) {
            // Emulate the given platform
            AbstractOOo.setPlatform(platformDir.getName());

            // Loop over the installation directories
            File[] instDirs = platformDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pPathname) {
                    return pPathname.isDirectory() && pPathname.canRead();
                }
            });

            for (File inst : instDirs) {
                try {
                    new OOo(inst.getAbsolutePath());
                } catch (InvalidConfigException e) {
                    fail(MessageFormat.format(pattern, platformDir.getName(), inst.getName()));
                }
            }
        }
    }
}
