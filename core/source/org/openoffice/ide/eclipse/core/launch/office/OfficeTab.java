/*************************************************************************
 *
 * $RCSfile:  $
 *
 * $Revision:  $
 *
 * last change: $Author:  $ $Date:  $
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
 * Contributor(s): Cedric Bosdonnat, Dan Corneanu
 *
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.launch.office;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputer;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.UnoProjectLabelProvider;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * Tab for configuring the OpenOffice launch properties.
 * 
 * @author cdan
 * 
 */
public class OfficeTab extends AbstractLaunchConfigurationTab {

    private static final int LAYOUT_COLUMNS = 3;
    private Text mProjectTxt;
    private Button mProjectBtn;
    private Button mUseCleanUserInstallation;
    private SelectionListener mListener = new ChangeListener();

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite pParent) {
        Composite comp = new Composite(pParent, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setLayout(new GridLayout());

        createProjectGroup(comp);

        createOptionsGroup(comp);

        setControl(comp);
    }

    /**
     * Creates a group with UI controls for changing the launcher's options.
     * 
     * @param pParent
     *            the parent composite to add our self to.
     */
    private void createOptionsGroup(Composite pParent) {
        Group group = new Group(pParent, SWT.NONE);
        group.setText(Messages.OfficeTab_Options);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mUseCleanUserInstallation = createCheckButton(group, Messages.OfficeTab_ChkUseCleanUserInstallation);
        mUseCleanUserInstallation.addSelectionListener(mListener);
        mUseCleanUserInstallation.setToolTipText(Messages.OfficeTab_ChkUseCleanUserInstallation_ToolTip);
    }

    /**
     * Creates a group with UI controls for selecting the target project.
     * 
     * @param pParent
     *            the parent composite to add our self to.
     */
    private void createProjectGroup(Composite pParent) {
        Group group = new Group(pParent, SWT.NONE);
        group.setText(Messages.OfficeTab_UnoProject);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite field = new Composite(group, SWT.NONE);
        field.setLayout(new GridLayout(LAYOUT_COLUMNS, false));
        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label lbl = new Label(field, SWT.NONE);
        lbl.setText(Messages.OfficeTab_ProjectNameLabel);
        lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        mProjectTxt = new Text(field, SWT.SINGLE | SWT.BORDER);
        mProjectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        // mProjectTxt.addModifyListener(mListener);

        mProjectBtn = new Button(field, SWT.PUSH);
        mProjectBtn.setText("...");
        mProjectBtn.addSelectionListener(mListener);
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return Messages.OfficeTab_Title;
    }

    /**
     * {@inheritDoc}
     */
    public void initializeFrom(ILaunchConfiguration pConfiguration) {
        try {
            mProjectTxt.setText(pConfiguration.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, ""));
            mUseCleanUserInstallation.setSelection(pConfiguration.getAttribute(
                            IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, false));
        } catch (CoreException e) {
            PluginLogger.error(Messages.OfficeTab_Configurationerror, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void performApply(ILaunchConfigurationWorkingCopy pConfiguration) {
        pConfiguration.setAttribute(IOfficeLaunchConstants.PROJECT_NAME, mProjectTxt.getText().trim());
        pConfiguration.setAttribute(IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, mUseCleanUserInstallation
                        .getSelection());

        try {
            String projectName = pConfiguration.getAttribute(
                            IOfficeLaunchConstants.PROJECT_NAME, "");
            IUnoidlProject project = ProjectsManager.getProject(projectName);
            if (null != project) {
                project.getLanguage().configureSourceLocator(pConfiguration);
            }
        } catch (CoreException e) {
            PluginLogger.error("Could not set language specific source locator attributes.", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy pConfiguration) {
        pConfiguration.setAttribute(IOfficeLaunchConstants.PROJECT_NAME, "");
        pConfiguration.setAttribute(IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, true);
    }

    @Override
    public boolean isValid(ILaunchConfiguration pLaunchConfig) {
        boolean valid = false;

        try {

            boolean projectSet = !pLaunchConfig.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, "")
                .equals("");//$NON-NLS-1$ //$NON-NLS-2$
            if (projectSet) {
                String name = pLaunchConfig.getAttribute(IOfficeLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
                valid = ProjectsManager.getProject(name) != null;
            }
        } catch (CoreException e) {
            PluginLogger.error(Messages.OfficeTab_Configurationerror, e);
        }

        return valid;
    }

    /**
     * Change listener to be notified when the user touches the UI controls :).
     * 
     * @author cdan
     * 
     */
    private class ChangeListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent pEvent) {
            if (pEvent.getSource().equals(mProjectBtn)) {
                ILabelProvider labelProvider = new UnoProjectLabelProvider();
                ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
                dialog.setTitle("ProjectChooserTitle"); //$NON-NLS-1$
                dialog.setMessage("ProjectChooserMessage"); //$NON-NLS-1$
                dialog.setElements(ProjectsManager.getProjects());

                if (dialog.open() == Window.OK) {
                    IUnoidlProject mProject = (IUnoidlProject) dialog.getFirstResult();
                    mProjectTxt.setText(mProject.getName());
                }
            }
            setDirty(true);
            getLaunchConfigurationDialog().updateButtons();
        }
    }
}
