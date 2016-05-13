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
package org.libreoffice.ide.eclipse.core.utils;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;

/**
 * Provides a set of useful method to perform actions on the Eclipse workbench.
 *
 *
 */
public class WorkbenchHelper {

    /**
     * Simply shows the file in the IDE.
     *
     * @param pFile
     *            the file to show
     * @param pPage
     *            the active workbench page
     */
    public static void showFile(IFile pFile, IWorkbenchPage pPage) {

        try {
            IWorkbench workbench = PlatformUI.getWorkbench();
            BasicNewResourceWizard.selectAndReveal(pFile, workbench.getActiveWorkbenchWindow());

            final IWorkbenchPage activePage = pPage;
            final IFile toShow = pFile;

            if (activePage != null) {
                final Display display = Display.getDefault();
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                IDE.openEditor(activePage, toShow, true);
                            } catch (PartInitException e) {
                                PluginLogger.debug(e.getMessage());
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            PluginLogger.error("Can't open file", e); //$NON-NLS-1$
        }
    }

    /**
     * Convenience method returning the active workbench page.
     *
     * @return the active page
     */
    public static IWorkbenchPage getActivePage() {
        IWorkbenchPage page = null;

        IWorkbenchWindow window = OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (null != window) {
            page = window.getActivePage();
        }
        return page;
    }
}
