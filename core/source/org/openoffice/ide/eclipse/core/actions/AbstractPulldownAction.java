/*************************************************************************
 *
 * $RCSfile: AbstractPulldownAction.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/03 21:42:13 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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

/**
 * Abstract class to create a pulldown menu action.
 * 
 * @author cedricbosdo
 *
 */
public abstract class AbstractPulldownAction implements IWorkbenchWindowPulldownDelegate {
	
	private String mParameterName = ""; //$NON-NLS-1$
	
	public AbstractPulldownAction(String parameterName) {
		mParameterName = parameterName;
	}
	
	/**
	 * Check if the selection is valid, and if the pulldown action can be enabled.
	 * @param selection the current selection
	 * @return <code>true</code> if the wizards can be launched.
	 */
	public abstract boolean isValidSelection(IStructuredSelection selection);
	
	/**
	 * Open the new wizard dialog
	 * @param wizard the wizard to open
	 */
	protected void openWizard(INewWizard wizard) {
		if (isValidSelection(getSelection())) {
			Shell shell = Display.getDefault().getActiveShell();

			wizard.init(PlatformUI.getWorkbench(), getSelection());

			WizardDialog dialog= new WizardDialog(shell, wizard);
			dialog.create();
			centerOnScreen(dialog);
			dialog.open();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowPulldownDelegate#getMenu(org.eclipse.swt.widgets.Control)
	 */
	public Menu getMenu(Control parent) {
		
		MenuManager menuMngr = new MenuManager();
		
		// Fill the menu from the new wizards with parameter unoproject
		Action[] actions = getActionsFromConfig();
		for (Action action : actions) {
			menuMngr.add(action);
		}
		Menu menu = menuMngr.createContextMenu(parent);
		
		return menu;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init(IWorkbenchWindow window) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	/**
	 * @return the actions to put in the popup menu
	 */
	private Action[] getActionsFromConfig() {
		ArrayList<Action> containers = new ArrayList<Action>();
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, "newWizards"); //$NON-NLS-1$
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element= elements[i];
				if (element.getName().equals("wizard") && isCorrectWizard(element)) { //$NON-NLS-1$
					containers.add(new OpenUnoProjectWizardAction(element));
				}
			}
		}
		return containers.toArray(new Action[containers.size()]);
	}
	
	/**
	 * Check if the wizard defined by the configuration element has to be added
	 * to the pulldown button menu.
	 * 
	 * @param element the wizard configuration element to check
	 * @return <code>true</code> if the wizard has to be added, 
	 * 			<code>false</code> otherwise.
	 */
	private boolean isCorrectWizard(IConfigurationElement element) {
		IConfigurationElement[] classElements= element.getChildren("class"); //$NON-NLS-1$
		if (classElements.length > 0) {
			for (int i= 0; i < classElements.length; i++) {
				IConfigurationElement[] paramElements= classElements[i].getChildren("parameter"); //$NON-NLS-1$
				for (int k = 0; k < paramElements.length; k++) {
					IConfigurationElement curr= paramElements[k];
					if (mParameterName.equals(curr.getAttribute("name"))) { //$NON-NLS-1$
						return Boolean.valueOf(curr.getAttribute("value")).booleanValue(); //$NON-NLS-1$
					}
				}
			}
		}
		// old way, deprecated
		if (Boolean.valueOf(element.getAttribute("unoproject")).booleanValue()) { //$NON-NLS-1$
			return true;
		}
		return false;
	}
	
	/**
	 * @return The current selection in the workbench
	 */
	private IStructuredSelection getSelection() {
		IWorkbenchWindow window= OOEclipsePlugin.getActivePage().getWorkbenchWindow();
		if (window != null) {
			ISelection selection= window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				return (IStructuredSelection) selection;
			}
		}
		return StructuredSelection.EMPTY;
	}
	
	/**
	 * Center the new wizard on the screen
	 * @param dialog the wizard dialog to center
	 */
	private void centerOnScreen(WizardDialog dialog) {
		Shell shell = dialog.getShell();
		Point size = shell.getSize();
		Rectangle screenBounds = Display.getDefault().getBounds();
		
		int x = (screenBounds.width - size.y)/2;
		int y = (screenBounds.height - size.y)/2;
		Rectangle bounds = new Rectangle(x, y, size.x, size.y);
		shell.setBounds(bounds);
	}
	
	/**
	 * Action class used in the pulldown action's menu. This action is configured
	 * using the newWizard configuration. 
	 * 
	 * @author cedricbosdo
	 */
	private class OpenUnoProjectWizardAction extends Action {
		
		private IConfigurationElement mConfigurationElement;
		
		/**
		 * Create a new action associated with a new wizard configuration element
		 * @param element the configuration element representing the wizard.
		 */
		public OpenUnoProjectWizardAction(IConfigurationElement element) {
			mConfigurationElement= element;
			setText(element.getAttribute("name")); //$NON-NLS-1$
			
			String description= getDescriptionFromConfig(mConfigurationElement);
			setDescription(description);
			setToolTipText(description);
			setImageDescriptor(getIconFromConfig(mConfigurationElement));
		}
		
		/**
		 * Get the action text from the new wizard configuration. 
		 * 
		 * @param config the configuration element where to look for the description
		 * @return the text of the description or <code>""</code> if not defined
		 */
		private String getDescriptionFromConfig(IConfigurationElement config) {
			IConfigurationElement [] children = config.getChildren("description"); //$NON-NLS-1$
			if (children.length>=1) {
				return children[0].getValue();
			}
			return ""; //$NON-NLS-1$
		}

		/**
		 * Get the action's icon from the new wizard configuration
		 *  
		 * @param config the element from which to find the icon
		 * @return the image descriptor or <code>null</code> if no icon is defined.
		 */
		private ImageDescriptor getIconFromConfig(IConfigurationElement config) {
			String iconName = config.getAttribute("icon"); //$NON-NLS-1$
			if (iconName != null) {	
				return OOEclipsePlugin.getDefault().getImageManager().getImageDescriptorFromPath(iconName);
			}
			return null;
		}
		
		/**
		 * Creates the new wizard from the configuration
		 * 
		 * @return the created wizard
		 * @throws CoreException if anything wrong happens
		 */
		private INewWizard createWizard() throws CoreException {
			return (INewWizard)mConfigurationElement.createExecutableExtension("class"); //$NON-NLS-1$
		}
		
		@Override
		public void run() {
			try {
				openWizard(createWizard());
			} catch (CoreException e) {
			}
		}
		
		
	}
}
