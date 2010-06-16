package org.openoffice.ide.eclipse.core.launch.office;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
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

public class OfficeTab extends AbstractLaunchConfigurationTab {

    private static final int LAYOUT_COLUMNS = 3;
    private Text mProjectTxt;
    private Button mProjectBtn;
    private Button mUseCleanUserInstallation;
    private SelectionListener mListener = new ChangeListener();

    public void createControl(Composite pParent) {
        Composite comp = new Composite(pParent, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setLayout(new GridLayout());

        createProjectGroup(comp);

        createOptionsGroup(comp);

        setControl(comp);
    }

    private void createOptionsGroup(Composite pParent) {
        Group group = new Group(pParent, SWT.NONE);
        group.setText(Messages.OfficeTab_Options);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        mUseCleanUserInstallation = new Button(group, SWT.CHECK);
        mUseCleanUserInstallation
                .setText(Messages.OfficeTab_ChkUseCleanUserInstallation);
        mUseCleanUserInstallation.addSelectionListener(mListener);
        mUseCleanUserInstallation.setToolTipText(Messages.OfficeTab_ChkUseCleanUserInstallation_ToolTip);
    }

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
        mProjectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
                | GridData.GRAB_HORIZONTAL));
        // mProjectTxt.addModifyListener(mListener);

        mProjectBtn = new Button(field, SWT.PUSH);
        mProjectBtn.setText("..."); //$NON-NLS-1$
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
            mProjectTxt.setText(pConfiguration.getAttribute(
                    IOfficeLaunchConstants.PROJECT_NAME, new String()));
            mUseCleanUserInstallation.setSelection(pConfiguration.getAttribute(
                    IOfficeLaunchConstants.CLEAN_USER_INSTALLATION, false));
        } catch (CoreException e) {
            PluginLogger.error(Messages.OfficeTab_Configurationerror, e);
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy pConfiguration) {
        pConfiguration.setAttribute(IOfficeLaunchConstants.PROJECT_NAME,
                mProjectTxt.getText().trim());
        pConfiguration.setAttribute(
                IOfficeLaunchConstants.CLEAN_USER_INSTALLATION,
                mUseCleanUserInstallation.getSelection());
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy pConfiguration) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean isValid(ILaunchConfiguration pLaunchConfig) {
        boolean valid = false;

        try {

            boolean projectSet = !pLaunchConfig.getAttribute(
                    IOfficeLaunchConstants.PROJECT_NAME, "").equals("");//$NON-NLS-1$ //$NON-NLS-2$
            if (projectSet) {
                String name = pLaunchConfig.getAttribute(
                        IOfficeLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
                valid = ProjectsManager.getProject(name) != null;
            }
        } catch (CoreException e) {
            PluginLogger.error(Messages.OfficeTab_Configurationerror, e);
        }

        return valid;
    }

    private class ChangeListener extends SelectionAdapter {
        public void widgetSelected(SelectionEvent pEvent) {
            if (pEvent.getSource().equals(mProjectBtn)) {
                ILabelProvider labelProvider = new UnoProjectLabelProvider();
                ElementListSelectionDialog dialog = new ElementListSelectionDialog(
                        getShell(), labelProvider);
                dialog.setTitle("ProjectChooserTitle"); //$NON-NLS-1$
                dialog.setMessage("ProjectChooserMessage"); //$NON-NLS-1$
                dialog.setElements(ProjectsManager.getProjects());

                if (dialog.open() == Window.OK) {
                    IUnoidlProject mProject = (IUnoidlProject) dialog
                            .getFirstResult();
                    mProjectTxt.setText(mProject.getName());
                }
            }
            setDirty(true);
            getLaunchConfigurationDialog().updateButtons();
        }
    }
}
