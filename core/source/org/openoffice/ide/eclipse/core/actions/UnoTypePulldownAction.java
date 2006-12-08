package org.openoffice.ide.eclipse.core.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.wizards.NewServiceWizard;

public class UnoTypePulldownAction extends AbstractPulldownAction {

	public UnoTypePulldownAction() {
		super("unotype");
	}
	
	public void run(IAction action) {
		openWizard(new NewServiceWizard());
	}

	@Override
	public boolean isValidSelection(IStructuredSelection selection) {
		
		boolean isValid = false;
		if (!selection.isEmpty()) {
			if (selection.getFirstElement() instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable)selection.getFirstElement();
				if (adaptable.getAdapter(IResource.class) != null) {
					IResource res = (IResource)adaptable.getAdapter(IResource.class);
					IProject prj = (res).getProject();
					if (null != ProjectsManager.getInstance().getProject(prj.getName())) {
						isValid = true;
					}
				}
			}
		}
		
		return isValid;
	}
	
	
}
