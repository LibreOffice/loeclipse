package org.openoffice.ide.eclipse.core.wizards;

public class NewUreAppWizard extends NewUnoProjectWizard {
	
	public NewUreAppWizard() {
		setDisableServicePage("com::sun::star::lang::XMain"); //$NON-NLS-1$
	}
}
