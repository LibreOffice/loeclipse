/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat
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
 * Copyright: 2013 by SUSE
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.java.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.java.OOoJavaPlugin;
import org.libreoffice.ide.eclipse.java.build.OOoClasspathContainer;

/**
 * Overrides NewJavaProjectWizardPageTwo to add jodconnector.jar to the temporary project.
 *
 *
 */
public class ClientWizardPageTwo extends NewJavaProjectWizardPageTwo {

    private UnoConnectionPage mCnxPage;

    public ClientWizardPageTwo(NewJavaProjectWizardPageOne mainPage, UnoConnectionPage cnxPage) {
        super(mainPage);
        mCnxPage = cnxPage;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);

        if (getContainer().getCurrentPage() == mCnxPage) {
            removeProvisonalProject();
        }
    }

    @Override
    public void init(IJavaProject jproject, IPath defaultOutputLocation, IClasspathEntry[] defaultEntries,
        boolean defaultsOverrideExistingClasspath) {

        IProject project = jproject.getProject();

        // Copy the jodconnector.jar file to the new project
        try {
            URL libUrl = OOoJavaPlugin.getDefault().getBundle().getResource(ClientWizard.JODCONNECTOR_LIBNAME);
            URL libFileUrl = FileLocator.toFileURL(libUrl);
            File libFile = new File(libFileUrl.toURI());
            InputStream in = new FileInputStream(libFile);
            IFile destLib = project.getFile(ClientWizard.JODCONNECTOR_LIBNAME);
            destLib.create(in, true, null);
        } catch (Exception e) {
        }

        // Refresh the project
        try {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (Exception e) {
        }

        // Update the classpath
        IOOo ooo = mCnxPage.getOoo();
        IPath path = new Path(OOoClasspathContainer.ID + IPath.SEPARATOR + ooo);
        IClasspathEntry oooEntry = JavaCore.newContainerEntry(path);

        IPath jodPath = project.getFolder(ClientWizard.JODCONNECTOR_LIBNAME).getFullPath();

        IClasspathEntry[] newEntries = new IClasspathEntry[] {
            oooEntry,
            JavaCore.newLibraryEntry(jodPath, jodPath, jodPath)
        };

        IClasspathEntry[] entries = new IClasspathEntry[defaultEntries.length + newEntries.length];

        System.arraycopy(defaultEntries, 0, entries, 0, defaultEntries.length);
        System.arraycopy(newEntries, 0, entries, defaultEntries.length, newEntries.length);

        super.init(jproject, defaultOutputLocation, entries, defaultsOverrideExistingClasspath);
    }
}
