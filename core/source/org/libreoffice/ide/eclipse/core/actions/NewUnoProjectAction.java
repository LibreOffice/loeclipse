/*************************************************************************
 *
 * $RCSfile: NewUnoProjectAction.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:31 $
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
package org.libreoffice.ide.eclipse.core.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.wizards.NewUnoProjectWizard;

/**
 * This action launches the New UNO Project wizard.
*/
public class NewUnoProjectAction implements IWorkbenchWindowActionDelegate {

    private IWorkbenchWindow mWindow;

    /**
     * Default constructor just initializing the action.
     */
    public NewUnoProjectAction() {
        init(OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        // Nothing to do on dispose
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbenchWindow pWindow) {
        this.mWindow = pWindow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(IAction pAction) {

        // Launch the new IDL Project wizard
        NewUnoProjectWizard wizard = new NewUnoProjectWizard();
        WizardDialog dialog = new WizardDialog(mWindow.getShell(), wizard);

        dialog.setBlockOnOpen(false);
        dialog.open();

        Point size = dialog.getShell().getSize();
        Rectangle bounds = Display.getDefault().getClientArea();
        dialog.getShell().setBounds((bounds.width - size.x) / 2, (bounds.height - size.y) / 2, size.x, size.y);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectionChanged(IAction pAction, ISelection pSelection) {
        // Nothing to do on selection changed
    }

}
