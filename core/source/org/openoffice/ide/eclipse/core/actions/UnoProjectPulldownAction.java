package org.openoffice.ide.eclipse.core.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;

public class UnoProjectPulldownAction extends AbstractPulldownAction {

	public UnoProjectPulldownAction() {
		super("unoproject");
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		new NewUnoProjectAction().run(action);
	}

	@Override
	public boolean isValidSelection(IStructuredSelection selection) {
		return true;
	}
}
