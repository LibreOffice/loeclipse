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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.editors.utils.ILocaleListener;
import org.libreoffice.ide.eclipse.core.editors.utils.LocalizedSection;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * Section displaying the release notes part of the descriptions.xml file.
 */
public class ReleaseNotesSection extends LocalizedSection<DescriptionModel> implements ILocaleListener {

    private static final int LAYOUT_COLS = 2;
    private Text mUrlTxt;

    /**
     * @param parent
     *            the parent composite where to add the section
     * @param page
     *            the parent page
     */
    public ReleaseNotesSection(Composite parent, DescriptionFormPage page) {
        super(parent, page, ExpandableComposite.TITLE_BAR);

        getSection().setText(Messages.getString("ReleaseNotesSection.Title")); //$NON-NLS-1$

        setModel(page.getModel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData() {
        getModel().setSuspendEvent(true);
        if (!getModel().getReleaseNotes().isEmpty()) {
            mUrlTxt.setText(getModel().getReleaseNotes().get(mCurrentLocale));
        }
        getModel().setSuspendEvent(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls(FormToolkit pToolkit, Composite pParent) {
        pParent.setLayout(new GridLayout(LAYOUT_COLS, false));

        String msg = Messages.getString("ReleaseNotesSection.Description"); //$NON-NLS-1$
        Label descrLbl = pToolkit.createLabel(pParent, msg, SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = LAYOUT_COLS;
        descrLbl.setLayoutData(gd);

        // Url controls
        pToolkit.createLabel(pParent, Messages.getString("ReleaseNotesSection.Url")); //$NON-NLS-1$
        mUrlTxt = pToolkit.createText(pParent, new String());
        mUrlTxt.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        mUrlTxt.setEnabled(false);
        mUrlTxt.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                getModel().addReleaseNote(mCurrentLocale, mUrlTxt.getText());
                markDirty();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLocale(Locale locale) {
        if (!getModel().getReleaseNotes().containsKey(locale)) {
            getModel().addReleaseNote(locale, new String());
        }
        mUrlTxt.setEnabled(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteLocale(Locale locale) {
        getModel().removeReleaseNote(locale);
        if (getModel().getReleaseNotes().isEmpty()) {
            mUrlTxt.setEnabled(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale locale) {

        if (mCurrentLocale != null) {
            getModel().addReleaseNote(mCurrentLocale, mUrlTxt.getText());
        }
        super.selectLocale(locale);
        mUrlTxt.setText(getModel().getReleaseNotes().get(locale));
    }
}
