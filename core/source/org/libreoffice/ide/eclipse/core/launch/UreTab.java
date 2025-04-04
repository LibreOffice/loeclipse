/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.UnoProjectLabelProvider;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * URE application launch configuration tab.
 */
public class UreTab extends AbstractLaunchConfigurationTab {

    /**
     * Class listening to the UI field changes.
     *
         *
     */
    private class ChangeListener implements SelectionListener, ModifyListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetDefaultSelected(SelectionEvent event) {
            widgetSelected(event);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void widgetSelected(SelectionEvent event) {

            if (event.getSource().equals(mProjectBtn)) {
                ILabelProvider labelProvider = new UnoProjectLabelProvider();
                ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
                dialog.setTitle(Messages.getString("UreTab.ProjectChooserTitle")); //$NON-NLS-1$
                dialog.setMessage(Messages.getString("UreTab.ProjectChooserMessage")); //$NON-NLS-1$
                dialog.setElements(ProjectsManager.getProjects());

                if (dialog.open() == Window.OK) {
                    mProject = (IUnoidlProject) dialog.getFirstResult();
                    mProjectTxt.setText(mProject.getName());
                    setDirty(true);
                    getLaunchConfigurationDialog().updateButtons();
                }
            } else if (event.getSource().equals(mMainBtn)) {
                ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
                    @Override
                    public String getText(Object element) {
                        String label = null;
                        if (element instanceof String) {
                            label = (String) element;
                        }
                        return label;
                    }
                });
                dialog.setTitle(Messages.getString("UreTab.MainImplementationChooserTitle")); //$NON-NLS-1$
                dialog.setMessage(Messages.getString("UreTab.MainImplementationChooserMessage")); //$NON-NLS-1$
                dialog.setElements(new MainImplementationsProvider().getImplementations(mProject));

                if (dialog.open() == Window.OK) {
                    mMainTxt.setText((String) dialog.getFirstResult());
                    setDirty(true);
                    getLaunchConfigurationDialog().updateButtons();
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void modifyText(ModifyEvent event) {

            if (event.getSource() == mProjectTxt) {
                IUnoidlProject prj = ProjectsManager.getProject(mProjectTxt.getText().trim());
                if (prj != null) {
                    mProject = prj;
                    setErrorMessage(null);
                } else {
                    mProject = null;
                    setErrorMessage(Messages.getString("UreTab.ProjectChooserError")); //$NON-NLS-1$
                }
                getLaunchConfigurationDialog().updateMessage();
            }
            setDirty(true);
            getLaunchConfigurationDialog().updateButtons();
        }
    }

    private static final int LAYOUT_COLUMNS = 3;

    private IUnoidlProject mProject;

    private Text mProjectTxt;
    private Button mProjectBtn;
    private Text mMainTxt;
    private Button mMainBtn;
    private Text mArgumentsTxt;

    private ChangeListener mListener = new ChangeListener();

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite pParent) {

        Composite comp = new Composite(pParent, SWT.NONE);
        comp.setLayoutData(new GridData(GridData.FILL_BOTH));
        comp.setLayout(new GridLayout());

        Group group = new Group(comp, SWT.NONE);
        group.setText(Messages.getString("UreTab.RunApplicationLabel")); //$NON-NLS-1$
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        createProjectField(group);

        createMainField(group);

        Group argGroup = new Group(comp, SWT.NONE);
        argGroup.setText(Messages.getString("UreTab.ApplicationArgsLabel")); //$NON-NLS-1$
        argGroup.setLayout(new GridLayout());
        argGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

        mArgumentsTxt = new Text(argGroup, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        mArgumentsTxt.setLayoutData(new GridData(GridData.FILL_BOTH));
        mArgumentsTxt.addModifyListener(mListener);

        setControl(comp);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage() {
        return OOEclipsePlugin.getImage(ImagesConstants.URE_APP);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return Messages.getString("UreTab.TabTitle"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            mProjectTxt.setText(configuration.getAttribute(IUreLaunchConstants.PROJECT_NAME, "")); //$NON-NLS-1$
            mMainTxt.setText(configuration.getAttribute(IUreLaunchConstants.MAIN_TYPE, "")); //$NON-NLS-1$
            mArgumentsTxt.setText(configuration.getAttribute(IUreLaunchConstants.PROGRAM_ARGS, "")); //$NON-NLS-1$
        } catch (CoreException e) {
            PluginLogger.error(Messages.getString("UreTab.ConfigurationError"), e); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {

        configuration.setAttribute(IUreLaunchConstants.PROJECT_NAME, mProjectTxt.getText().trim());
        configuration.setAttribute(IUreLaunchConstants.MAIN_TYPE, mMainTxt.getText().trim());
        configuration.setAttribute(IUreLaunchConstants.PROGRAM_ARGS, mArgumentsTxt.getText().trim());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {

        boolean valid = false;

        try {

            boolean projectSet = !launchConfig.getAttribute(
                IUreLaunchConstants.PROJECT_NAME, "").equals(""); //$NON-NLS-1$ //$NON-NLS-2$
            boolean mainImplSet = !launchConfig.getAttribute(
                IUreLaunchConstants.MAIN_TYPE, "").equals(""); //$NON-NLS-1$ //$NON-NLS-2$
            if (projectSet && mainImplSet) {
                String name = launchConfig.getAttribute(IUreLaunchConstants.PROJECT_NAME, ""); //$NON-NLS-1$
                valid = ProjectsManager.getProject(name) != null;
            }
        } catch (CoreException e) {
        }

        return valid;
    }

    /**
     * Creates the UI field for the UNO project selection.
     *
     * @param pParent
     *            the parent composite where to draw the field
     */
    private void createProjectField(Composite pParent) {
        Composite field = new Composite(pParent, SWT.NONE);
        field.setLayout(new GridLayout(LAYOUT_COLUMNS, false));
        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label lbl = new Label(field, SWT.NONE);
        lbl.setText(Messages.getString("UreTab.ProjectLabel")); //$NON-NLS-1$
        lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        mProjectTxt = new Text(field, SWT.SINGLE | SWT.BORDER);
        mProjectTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        mProjectTxt.addModifyListener(mListener);

        mProjectBtn = new Button(field, SWT.PUSH);
        mProjectBtn.setText("..."); //$NON-NLS-1$
        mProjectBtn.addSelectionListener(mListener);
    }

    /**
     * Create the UI field for the <code>XMain</code> implementation class selection.
     *
     * @param pParent
     *            the parent composite where to draw the field
     */
    private void createMainField(Composite pParent) {
        Composite field = new Composite(pParent, SWT.NONE);
        field.setLayout(new GridLayout(LAYOUT_COLUMNS, false));
        field.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label lbl = new Label(field, SWT.NONE);
        lbl.setText(Messages.getString("UreTab.MainImplementationLabel")); //$NON-NLS-1$
        lbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        mMainTxt = new Text(field, SWT.SINGLE | SWT.BORDER);
        mMainTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
        mMainTxt.addModifyListener(mListener);

        mMainBtn = new Button(field, SWT.PUSH);
        mMainBtn.setText("..."); //$NON-NLS-1$
        mMainBtn.addSelectionListener(mListener);
    }
}
