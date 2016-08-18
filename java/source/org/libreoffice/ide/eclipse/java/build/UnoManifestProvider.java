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
package org.libreoffice.ide.eclipse.java.build;

import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.internal.ui.jarpackager.ManifestProvider;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * Class providing the MANIFEST.MF contents to the Jar writer.
 */
@SuppressWarnings("restriction")
public class UnoManifestProvider extends ManifestProvider {

    private String mRegClass;
    private IUnoidlProject mUnoProject;
    private List<IFile> mExternalJars;

    /**
     * Constructor.
     *
     * @param pRegClassname the registration class name
     */
    public UnoManifestProvider(String pRegClassname, IUnoidlProject unoProject, List<IFile> externalJars) {
        mRegClass = pRegClassname;
        mUnoProject = unoProject;
        mExternalJars = externalJars;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void putAdditionalEntries(Manifest pManifest, JarPackageData pJarPackage) {
        Name regClassName = new Attributes.Name("RegistrationClassName"); //$NON-NLS-1$
        pManifest.getMainAttributes().put(regClassName, mRegClass);

        Name classPath = new Attributes.Name("Class-Path");
        List<String> classPathList = new ArrayList<>();
        for (IFile file:mExternalJars) {
            classPathList.add(UnoPackage.getPathRelativeToBase(SystemHelper.getFile(file),
                SystemHelper.getFile(mUnoProject)));
        }
        pManifest.getMainAttributes().put(classPath, String.join(",", classPathList));
    }
}
