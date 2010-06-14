package org.openoffice.ide.eclipse.core.launch.office;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class LaunchConfigurationTabs extends
		AbstractLaunchConfigurationTabGroup {

	/**
	 * {@inheritDoc}
	 */
	public void createTabs(ILaunchConfigurationDialog pDialog, String pMode) {
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new OfficeTab(), new CommonTab() };

		setTabs(tabs);

	}

}
