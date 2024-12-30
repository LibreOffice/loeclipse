/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.editors.description;

import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.editors.utils.LocalizedSection;
import org.libreoffice.ide.eclipse.core.gui.ProjectSelectionDialog;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * License section class.
 */
public class LicenseSection extends LocalizedSection<DescriptionModel> {

    private static final int LAYOUT_COLS = 3;

    private Text mFileTxt;
    private Button mFileBrowseBtn;

    private Button mUserAcceptBtn;
    private Button mSuppressUpdateBtn;

    private IProject mProject;

    /**
     * @param pParent
     *            the parent composite where to add the section
     * @param pPage
     *            the parent page
     * @param pProject
     *            the project containing the description.xml file
     */
    public LicenseSection(Composite pParent, DescriptionFormPage pPage, IProject pProject) {
        super(pParent, pPage, ExpandableComposite.TITLE_BAR);

        mProject = pProject;
        setModel(pPage.getModel());
    }

    /**
     * Load the data from the model into the non-localized controls.
     */
    @Override
    public void loadData() {
        getModel().setSuspendEvent(true);
        if (!getModel().getLicenses().isEmpty()) {
            mFileTxt.setText(getModel().getLicenses().get(mCurrentLocale));
        }
        mSuppressUpdateBtn.setSelection(getModel().isSuppressOnUpdate());
        mUserAcceptBtn.setSelection(getModel().isAcceptByUser());
        getModel().setSuspendEvent(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls(FormToolkit pToolkit, Composite pParent) {

        Section section = getSection();
        section.setLayoutData(new GridData(GridData.FILL_BOTH));
        section.setText(Messages.getString("LicenseSection.Title")); //$NON-NLS-1$

        pParent.setLayout(new GridLayout(LAYOUT_COLS, false));

        // Create the checkboxes
        Label descrLbl = pToolkit.createLabel(pParent, Messages.getString("LicenseSection.Description"), //$NON-NLS-1$
            SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS;
        descrLbl.setLayoutData(gd);

        createFileControls(pToolkit, pParent);

        mUserAcceptBtn = pToolkit.createButton(pParent, Messages.getString("LicenseSection.UserAccept"), //$NON-NLS-1$
            SWT.CHECK);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS;
        mUserAcceptBtn.setLayoutData(gd);
        mUserAcceptBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                getModel().setAcceptByUser(mUserAcceptBtn.getSelection());
                markDirty();
            }
        });

        String msg = Messages.getString("LicenseSection.SuppressUpdate"); //$NON-NLS-1$
        mSuppressUpdateBtn = pToolkit.createButton(pParent, msg, SWT.CHECK);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS;
        mSuppressUpdateBtn.setLayoutData(gd);
        mSuppressUpdateBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                getModel().setSuppressOnUpdate(mSuppressUpdateBtn.getSelection());
                markDirty();
            }
        });
    }

    /**
     * Create the file selection control.
     *
     * @param pToolkit
     *            the toolkit used for the controls creation
     * @param pParent
     *            the parent composite where to create the controls
     */
    private void createFileControls(FormToolkit pToolkit, Composite pParent) {

        // Create the folder selection controls
        Label pathLbl = pToolkit.createLabel(pParent, Messages.getString("LicenseSection.LicenseFile")); //$NON-NLS-1$
        pathLbl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        mFileTxt = pToolkit.createText(pParent, new String());
        mFileTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mFileTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent pE) {
                getModel().addLicense(mCurrentLocale, mFileTxt.getText());
                markDirty();
            }
        });

        mFileBrowseBtn = pToolkit.createButton(pParent, "...", SWT.PUSH); //$NON-NLS-1$
        mFileBrowseBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        mFileBrowseBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pE) {
                // Open the folder selection dialog
                ProjectSelectionDialog dlg = new ProjectSelectionDialog(mProject,
                    Messages.getString("LicenseSection.FileChooserTooltip")); //$NON-NLS-1$

                if (dlg.open() == Window.OK) {
                    IResource res = dlg.getSelected();
                    if (res != null && res.getType() == IResource.FILE) {
                        IFile file = (IFile) res;
                        String path = file.getProjectRelativePath().toString();
                        mFileTxt.setText(path);
                    }
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLocale(Locale pLocale) {
        getModel().addLicense(pLocale, new String());

        // enable the text and file
        mFileBrowseBtn.setEnabled(true);
        mFileTxt.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteLocale(Locale pLocale) {
        getModel().removeLicense(pLocale);
        if (getModel().getLicenses().isEmpty()) {
            // disable the text and file
            mFileBrowseBtn.setEnabled(false);
            mFileTxt.setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        if (mCurrentLocale != null) {
            getModel().addLicense(mCurrentLocale, mFileTxt.getText());
        }
        super.selectLocale(pLocale);
        String path = getModel().getLicenses().get(pLocale);
        mFileTxt.setText(path);
    }
}
