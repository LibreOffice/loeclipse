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

public abstract class AbstractPulldownAction implements IWorkbenchWindowPulldownDelegate {
	
	private String mParameterName = "";
	
	public AbstractPulldownAction(String parameterName) {
		mParameterName = parameterName;
	}
	
	public abstract boolean isValidSelection(IStructuredSelection selection);
	
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
	
	private Action[] getActionsFromConfig() {
		ArrayList<Action> containers = new ArrayList<Action>();
		
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(PlatformUI.PLUGIN_ID, "newWizards");
		if (extensionPoint != null) {
			IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement element= elements[i];
				if (element.getName().equals("wizard") && isCorrectWizard(element)) {
					containers.add(new OpenUnoProjectWizardAction(element));
				}
			}
		}
		return containers.toArray(new Action[containers.size()]);
	}
	
	private boolean isCorrectWizard(IConfigurationElement element) {
		IConfigurationElement[] classElements= element.getChildren("class");
		if (classElements.length > 0) {
			for (int i= 0; i < classElements.length; i++) {
				IConfigurationElement[] paramElements= classElements[i].getChildren("parameter");
				for (int k = 0; k < paramElements.length; k++) {
					IConfigurationElement curr= paramElements[k];
					if (mParameterName.equals(curr.getAttribute("name"))) {
						return Boolean.valueOf(curr.getAttribute("value")).booleanValue();
					}
				}
			}
		}
		// old way, deprecated
		if (Boolean.valueOf(element.getAttribute("unoproject")).booleanValue()) {
			return true;
		}
		return false;
	}
	
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
	
	private void centerOnScreen(WizardDialog dialog) {
		Shell shell = dialog.getShell();
		Point size = shell.getSize();
		Rectangle screenBounds = Display.getDefault().getBounds();
		
		int x = (screenBounds.width - size.y)/2;
		int y = (screenBounds.height - size.y)/2;
		Rectangle bounds = new Rectangle(x, y, size.x, size.y);
		shell.setBounds(bounds);
	}
	
	private class OpenUnoProjectWizardAction extends Action {
		
		IConfigurationElement mConfigurationElement;
		
		public OpenUnoProjectWizardAction(IConfigurationElement element) {
			mConfigurationElement= element;
			setText(element.getAttribute("name"));
			
			String description= getDescriptionFromConfig(mConfigurationElement);
			setDescription(description);
			setToolTipText(description);
			setImageDescriptor(getIconFromConfig(mConfigurationElement));
		}
		
		private String getDescriptionFromConfig(IConfigurationElement config) {
			IConfigurationElement [] children = config.getChildren("description");
			if (children.length>=1) {
				return children[0].getValue();
			}
			return ""; //$NON-NLS-1$
		}

		private ImageDescriptor getIconFromConfig(IConfigurationElement config) {
			String iconName = config.getAttribute("icon");
			if (iconName != null) {	
				return OOEclipsePlugin.getDefault().getImageManager().getImageDescriptorFromPath(iconName);
			}
			return null;
		}
		
		private INewWizard createWizard() throws CoreException {
			return (INewWizard)mConfigurationElement.createExecutableExtension("class");
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
