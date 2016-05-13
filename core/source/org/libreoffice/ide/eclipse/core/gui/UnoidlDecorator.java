/*************************************************************************
 *
 * $RCSfile: UnoidlDecorator.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:28 $
 *
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
package org.libreoffice.ide.eclipse.core.gui;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.ILabelDecorator;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * For Eclipse a decorator is a small class changing an element image and/or label. This decorator replaces the icons
 * for IDL files and registries.
 *
 *
 */
public class UnoidlDecorator extends LabelProvider implements ILabelDecorator {

    /**
     * {@inheritDoc}
     */
    @Override
    public Image decorateImage(Image pImage, Object pElement) {

        Image newImage = null;

        if (isIdlFolder(pElement)) {
            newImage = new OverlayImageIcon(pImage, OOEclipsePlugin.getImage(ImagesConstants.IDL_MODIFIER),
                OverlayImageIcon.TOP_LEFT).getImage();
        } else if (pElement instanceof IProject) {
            IProject project = (IProject) pElement;
            try {
                if (project.hasNature(OOEclipsePlugin.UNO_NATURE_ID)) {
                    newImage = new OverlayImageIcon(pImage, OOEclipsePlugin.getImage(ImagesConstants.PRJ_MODIFIER),
                        OverlayImageIcon.BOTTOM_RIGHT).getImage();
                }
            } catch (CoreException e) {
                // Nothing to do: no uno nature found
            }
        } else if (isDbFolder(pElement)) {
            newImage = new OverlayImageIcon(pImage, OOEclipsePlugin.getImage(ImagesConstants.DB_MODIFIER),
                OverlayImageIcon.TOP_LEFT).getImage();
        }

        return newImage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String decorateText(String pText, Object pElement) {

        if (isIdlFolder(pElement)) {

            pText = pText.replaceAll("\\.", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return pText;
    }

    /**
     * Tests if the element is a folder contained in the project idl directory.
     *
     * @param pElement
     *            element to check
     * @return <code>true</code> if the element is an IDL directory, <code>false</code> otherwise.
     */
    private boolean isIdlFolder(Object pElement) {
        boolean result = false;

        if (pElement instanceof IResource) {
            IResource resource = (IResource) pElement;

            try {
                if (IResource.FOLDER == resource.getType()) {
                    IProject project = resource.getProject();
                    IUnoidlProject unoPrj = ProjectsManager.getProject(project.getName());

                    IPath idlPath = unoPrj.getIdlPath();
                    IPath resPath = resource.getProjectRelativePath();

                    result = resPath.toOSString().startsWith(idlPath.toOSString());
                }
            } catch (Exception e) {
                result = false;
            }
        }

        return result;
    }

    /**
     * Tests if the elements is the urd folder of a unoidl project or one of its children.
     *
     * @param pElement
     *            the element to test
     * @return <code>true</code> if the element is the urd folder of a unoidl project or one of its children. Otherwise
     *         of if the element is a urd child but not a folder, <code>false</code> is returned
     */
    private boolean isDbFolder(Object pElement) {
        boolean result = false;

        if (pElement instanceof IFolder) {

            try {
                IFolder folder = (IFolder) pElement;

                IUnoidlProject project = ProjectsManager.getProject(folder.getProject().getName());

                if (folder.getProjectRelativePath().toString().startsWith(project.getUrdPath().toString())) {

                    result = true;
                }
            } catch (Exception e) {
                result = false;
            }

        }

        return result;
    }
}
