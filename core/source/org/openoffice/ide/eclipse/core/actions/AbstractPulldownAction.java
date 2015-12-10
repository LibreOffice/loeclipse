/*************************************************************************
 *
 * $RCSfile: AbstractPulldownAction.java,v $
 *
 * $Revision: 1.4 $
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
package org.openoffice.ide.eclipse.core.actions;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate;
import org.eclipse.ui.PlatformUI;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.utils.WorkbenchHelper;

/**
 * Abstract class to create a pulldown menu action.
 *
 * @author cedricbosdo
 *
 */
public abstract class AbstractPulldownAction implements IWorkbenchWindowPulldownDelegate {

    private String mParameterName = ""; //$NON-NLS-1$

    /**
     * Pulldown action.
     *
     * @param pParameterName
     *            the action parameter
     */
    public AbstractPulldownAction(String pParameterName) {
        mParameterName = pParameterName;
    }

    /**
     * Check if the selection is valid, and if the pulldown action can be enabled.
     * 
     * @param pSelection
     *            the current selection
     * @return <code>true</code> if the wizards can be launched.
     */
    public abstract boolean isValidSelection(IStructuredSelection pSelection);

    /**
     * Open the new wizard dialog.
     *
     * @param pWizard
     *            the wizard to open
     */
    protected void openWizard(INewWizard pWizard) {
        if (isValidSelection(getSelection())) {
            Shell shell = Display.getDefault().getActiveShell();

            pWizard.init(PlatformUI.getWorkbench(), getSelection());

            WizardDialog dialog = new WizardDialog(shell, pWizard);
            dialog.create();
            centerOnScreen(dialog);
            dialog.open();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Menu getMenu(Control pParent) {

        MenuManager menuMngr = new MenuManager();

        // Fill the menu from the new wizards with parameter unoproject
        Action[] actions = getActionsFromConfig();
        for (Action action : actions) {
            menuMngr.add(action);
        }
        Menu menu = menuMngr.createContextMenu(pParent);

        return menu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbenchWindow pWindow) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectionChanged(IAction pAction, ISelection pSelection) {
    }

    /**
     * @return the actions to put in the popup menu
     */
    private Action[] getActionsFromConfig() {
        ArrayList<Action> containers = new ArrayList<Action>();

        IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID,
            "newWizards"); //$NON-NLS-1$
        if (extensionPoint != null) {
            IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
            for (int i = 0; i < elements.length; i++) {
                IConfigurationElement element = elements[i];
                if (element.getName().equals("wizard") && isCorrectWizard(element)) { //$NON-NLS-1$
                    containers.add(new OpenUnoProjectWizardAction(element));
                }
            }
        }
        return containers.toArray(new Action[containers.size()]);
    }

    /**
     * Check if the wizard defined by the configuration element has to be added to the pulldown button menu.
     *
     * @param pElement
     *            the wizard configuration element to check
     * @return <code>true</code> if the wizard has to be added, <code>false</code> otherwise.
     */
    private boolean isCorrectWizard(IConfigurationElement pElement) {
        boolean isCorrect = false;

        IConfigurationElement[] classElements = pElement.getChildren("class"); //$NON-NLS-1$
        if (classElements.length > 0) {
            for (int i = 0; i < classElements.length; i++) {
                IConfigurationElement[] paramElements = classElements[i].getChildren("parameter"); //$NON-NLS-1$
                for (int k = 0; k < paramElements.length; k++) {
                    IConfigurationElement curr = paramElements[k];
                    if (mParameterName.equals(curr.getAttribute("name"))) { //$NON-NLS-1$
                        isCorrect = Boolean.valueOf(curr.getAttribute("value")).booleanValue(); //$NON-NLS-1$
                    }
                }
            }
        }
        // old way, deprecated
        if (Boolean.valueOf(pElement.getAttribute("unoproject")).booleanValue()) { //$NON-NLS-1$
            isCorrect = true;
        }
        return isCorrect;
    }

    /**
     * @return The current selection in the workbench
     */
    private IStructuredSelection getSelection() {
        IStructuredSelection strucSelection = StructuredSelection.EMPTY;

        IWorkbenchWindow window = WorkbenchHelper.getActivePage().getWorkbenchWindow();
        if (window != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection) {
                strucSelection = (IStructuredSelection) selection;
            }
        }
        return strucSelection;
    }

    /**
     * Center the new wizard on the screen.
     *
     * @param pDialog
     *            the wizard dialog to center
     */
    private void centerOnScreen(WizardDialog pDialog) {
        Shell shell = pDialog.getShell();
        Point size = shell.getSize();
        Rectangle screenBounds = Display.getDefault().getBounds();

        int x = (screenBounds.width - size.y) / 2;
        int y = (screenBounds.height - size.y) / 2;
        Rectangle bounds = new Rectangle(x, y, size.x, size.y);
        shell.setBounds(bounds);
    }

    /**
     * Action class used in the pulldown action's menu. This action is configured using the newWizard configuration.
     *
     * @author cedricbosdo
     */
    private class OpenUnoProjectWizardAction extends Action {

        private IConfigurationElement mConfigurationElement;

        /**
         * Create a new action associated with a new wizard configuration element.
         *
         * @param pElement
         *            the configuration element representing the wizard.
         */
        public OpenUnoProjectWizardAction(IConfigurationElement pElement) {
            mConfigurationElement = pElement;
            setText(pElement.getAttribute("name")); //$NON-NLS-1$

            String description = getDescriptionFromConfig(mConfigurationElement);
            setDescription(description);
            setToolTipText(description);
            setImageDescriptor(getIconFromConfig(mConfigurationElement));
        }

        /**
         * Get the action text from the new wizard configuration.
         *
         * @param pConfig
         *            the configuration element where to look for the description
         * @return the text of the description or <code>""</code> if not defined
         */
        private String getDescriptionFromConfig(IConfigurationElement pConfig) {
            String description = ""; //$NON-NLS-1$
            IConfigurationElement[] children = pConfig.getChildren("description"); //$NON-NLS-1$
            if (children.length >= 1) {
                description = children[0].getValue();
            }
            return description;
        }

        /**
         * Get the action's icon from the new wizard configuration.
         *
         * @param pConfig
         *            the element from which to find the icon
         * @return the image descriptor or <code>null</code> if no icon is defined.
         */
        private ImageDescriptor getIconFromConfig(IConfigurationElement pConfig) {
            ImageDescriptor icon = null;
            String iconName = pConfig.getAttribute("icon"); //$NON-NLS-1$
            if (iconName != null) {
                icon = OOEclipsePlugin.getDefault().getImageManager().getImageDescriptorFromPath(iconName);
            }
            return icon;
        }

        /**
         * Creates the new wizard from the configuration.
         *
         * @return the created wizard
         * @throws CoreException
         *             if anything wrong happens
         */
        private INewWizard createWizard() throws CoreException {
            return (INewWizard) mConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            try {
                openWizard(createWizard());
            } catch (CoreException e) {
            }
        }

    }
}
