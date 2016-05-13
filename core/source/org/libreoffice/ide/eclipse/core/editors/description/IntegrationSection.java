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

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.LabelProvider;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.editors.utils.AbstractSection;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * Section showing the compatibility parts of the description.xml file.
*/
public class IntegrationSection extends AbstractSection<DescriptionModel> {

    static final String SEPARATOR = ","; //$NON-NLS-1$
    static final String[] PLATFORMS = { "all", //$NON-NLS-1$
        "freebsd_x86", //$NON-NLS-1$
        "freebsd_x86_64", //$NON-NLS-1$
        "linux_arm_eabi", //$NON-NLS-1$
        "linux_arm_oabi", //$NON-NLS-1$
        "linux_ia64", //$NON-NLS-1$
        "linux_mips_eb", //$NON-NLS-1$
        "linux_mips_el", //$NON-NLS-1$
        "linux_powerpc", //$NON-NLS-1$
        "linux_powerpc64", //$NON-NLS-1$
        "linux_s390", //$NON-NLS-1$
        "linux_s390x", //$NON-NLS-1$
        "linux_sparc", //$NON-NLS-1$
        "linux_x86", //$NON-NLS-1$
        "linux_x86_64", //$NON-NLS-1$
        "macosx_powerpc", //$NON-NLS-1$
        "macosx_x86", //$NON-NLS-1$
        "os2_x86", //$NON-NLS-1$
        "solaris_sparc", //$NON-NLS-1$
        "solaris_x86", //$NON-NLS-1$
        "windows_x86" //$NON-NLS-1$
    };
    private static final int GRID_COLUMS = 3;

    private DescriptionFormPage mPage;

    private Text mMinOOoTxt;
    private Text mMaxOOoTxt;
    private Text mPlatformTxt;

    /**
     * @param pParent
     *            the parent composite where to add the section
     * @param pPage
     *            the parent page
     */
    public IntegrationSection(Composite pParent, DescriptionFormPage pPage) {
        super(pParent, pPage, ExpandableComposite.TITLE_BAR);
        mPage = pPage;

        createContent();

        setModel(pPage.getModel());
    }

    /**
     * Loads the values from the model into the controls.
     */
    @Override
    public void loadData() {
        getModel().setSuspendEvent(true);
        mMinOOoTxt.setText(getModel().getMinOOo());
        mMaxOOoTxt.setText(getModel().getMaxOOo());
        mPlatformTxt.setText(getModel().getPlatforms());
        getModel().setSuspendEvent(false);
    }

    /**
     * Creates the sections controls.
     */
    private void createContent() {
        Section section = getSection();
        section.setText(Messages.getString("IntegrationSection.Title")); //$NON-NLS-1$

        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        FormToolkit toolkit = mPage.getManagedForm().getToolkit();
        Composite clientArea = toolkit.createComposite(section);
        clientArea.setLayout(new GridLayout(GRID_COLUMS, false));

        Label descrLbl = toolkit.createLabel(clientArea, Messages.getString("IntegrationSection.Description"), //$NON-NLS-1$
            SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = GRID_COLUMS;
        descrLbl.setLayoutData(gd);

        // Min OOo version controls
        toolkit.createLabel(clientArea, Messages.getString("IntegrationSection.MinOOoVersion")); //$NON-NLS-1$
        mMinOOoTxt = toolkit.createText(clientArea, new String());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = GRID_COLUMS - 1;
        mMinOOoTxt.setLayoutData(gd);
        mMinOOoTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent pE) {
                getModel().setMinOOo(mMinOOoTxt.getText());
                markDirty();
            }
        });

        // Max OOo version controls
        toolkit.createLabel(clientArea, Messages.getString("IntegrationSection.MaxOOoVersion")); //$NON-NLS-1$
        mMaxOOoTxt = toolkit.createText(clientArea, new String());
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = GRID_COLUMS - 1;
        mMaxOOoTxt.setLayoutData(gd);
        mMaxOOoTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent pE) {
                getModel().setMaxOOo(mMaxOOoTxt.getText());
                markDirty();
            }
        });

        // Platforms controls
        toolkit.createLabel(clientArea, Messages.getString("IntegrationSection.Platforms")); //$NON-NLS-1$
        mPlatformTxt = toolkit.createText(clientArea, "all"); //$NON-NLS-1$
        mPlatformTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mPlatformTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent pE) {
                getModel().setPlatforms(mPlatformTxt.getText());
                markDirty();
            }
        });

        Button platformBtn = toolkit.createButton(clientArea, "...", SWT.PUSH | SWT.FLAT); //$NON-NLS-1$
        platformBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
        platformBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent pE) {
                PlatformDialog dlg = new PlatformDialog();
                if (dlg.open() == Window.OK) {
                    mPlatformTxt.setText(dlg.getSelected());
                }
            }
        });

        toolkit.paintBordersFor(clientArea);

        section.setClient(clientArea);
    }

    /**
     * Dialog used to select platforms.
    */
    private class PlatformDialog extends Dialog {

        private CheckboxTableViewer mList;
        private ArrayList<String> mSelected;

        /**
         * Dialog constructor.
         */
        public PlatformDialog() {
            super(new Shell(Display.getDefault()));

            setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);

            String textValue = mPlatformTxt.getText().replace(" ", new String()); //$NON-NLS-1$
            String[] selection = textValue.split(SEPARATOR);
            mSelected = new ArrayList<String>(Arrays.asList(selection));
        }

        /**
         * @return the selected platforms in a comma-separated string.
         */
        public String getSelected() {
            String selection = new String();
            for (String selected : mSelected) {
                selection += selected + SEPARATOR;
            }
            return selection.substring(0, selection.length() - SEPARATOR.length());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Control createDialogArea(Composite pParent) {
            Composite body = (Composite) super.createDialogArea(pParent);
            body.setLayout(new GridLayout());
            body.setLayoutData(new GridData(GridData.FILL_BOTH));

            Table table = new Table(body, SWT.MULTI | SWT.CHECK);
            mList = new CheckboxTableViewer(table);
            mList.setContentProvider(new ArrayContentProvider());
            mList.setLabelProvider(new LabelProvider());

            mList.setInput(PLATFORMS);
            mList.setCheckedElements(mSelected.toArray());

            mList.addCheckStateListener(new ICheckStateListener() {

                @Override
                public void checkStateChanged(CheckStateChangedEvent pEvent) {
                    Object[] values = mList.getCheckedElements();

                    mSelected.clear();
                    for (Object value : values) {
                        mSelected.add(value.toString());
                    }
                };
            });

            return body;
        }
    }
}
