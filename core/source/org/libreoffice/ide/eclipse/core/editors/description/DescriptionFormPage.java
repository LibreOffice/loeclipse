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
package org.libreoffice.ide.eclipse.core.editors.description;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.editors.utils.AbstractSection;
import org.libreoffice.ide.eclipse.core.editors.utils.LocaleSelector;
import org.libreoffice.ide.eclipse.core.editors.utils.LocalizedSection;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * The form page of the package editor helping to configure the project's description and main properties.
 */
public class DescriptionFormPage extends FormPage {

    private LocaleSelector mLocaleSel;
    private DescriptionModel mModel;

    private ArrayList<AbstractSection<DescriptionModel>> mSections;

    /**
     * Constructor.
     *
     * @param pEditor
     *            the editor where to add the page
     * @param pId
     *            the page identifier
     */
    public DescriptionFormPage(FormEditor pEditor, String pId) {
        super(pEditor, pId, Messages.getString("PackageOverviewFormPage.Title")); //$NON-NLS-1$
        mSections = new ArrayList<AbstractSection<DescriptionModel>>();
    }

    /**
     * @param pModel
     *            the description.xml model to set
     */
    public void setModel(DescriptionModel pModel) {
        mModel = pModel;
        for (AbstractSection<DescriptionModel> section : mSections) {
            section.setModel(pModel);
        }
    }

    /**
     * @return the description model for the page.
     */
    public DescriptionModel getModel() {
        return mModel;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFormContent(IManagedForm pManagedForm) {
        super.createFormContent(pManagedForm);

        ScrolledForm form = pManagedForm.getForm();
        form.setText(Messages.getString("PackageOverviewFormPage.Title")); //$NON-NLS-1$
        Composite body = form.getBody();

        FormToolkit toolkit = getManagedForm().getToolkit();
        toolkit.decorateFormHeading(form.getForm());

        String msg = Messages.getString("PackageOverviewFormPage.Description"); //$NON-NLS-1$
        Label descrLbl = toolkit.createLabel(body, msg, SWT.WRAP);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData(gd);

        body.setLayout(new GridLayout(2, false));

        ArrayList<LocalizedSection<DescriptionModel>> sections = createMainPage(toolkit, body);

        // Create the locale selector line
        Composite bottomLine = toolkit.createComposite(body);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        bottomLine.setLayoutData(gd);
        bottomLine.setLayout(new GridLayout());

        mLocaleSel = new LocaleSelector(toolkit, bottomLine);

        // Set the locale listeners
        for (LocalizedSection<DescriptionModel> section : sections) {
            mLocaleSel.addListener(section);
        }

        mLocaleSel.loadLocales(mModel.getAllLocales());

        // Set the model listeners
        for (AbstractSection<DescriptionModel> section : mSections) {
            section.loadData();
            mModel.addListener(section);
        }
        mModel.setSuspendEvent(false);
    }

    /**
     * Creates the main tab page.
     *
     * @param pToolkit
     *            the toolkit used to create the page
     * @param pParent
     *            the parent composite where to create the page.
     *
     * @return the localized sections of the page
     */
    private ArrayList<LocalizedSection<DescriptionModel>> createMainPage(FormToolkit pToolkit, Composite pParent) {

        ArrayList<LocalizedSection<DescriptionModel>> localized = new ArrayList<LocalizedSection<DescriptionModel>>();

        Composite leftColumn = pToolkit.createComposite(pParent);
        leftColumn.setLayoutData(new GridData(GridData.FILL_BOTH));
        leftColumn.setLayout(new GridLayout());

        Composite rightColumn = pToolkit.createComposite(pParent);
        rightColumn.setLayoutData(new GridData(GridData.FILL_BOTH));
        rightColumn.setLayout(new GridLayout());

        IFileEditorInput input = (IFileEditorInput) getEditorInput();
        IProject project = input.getFile().getProject();

        /*
         * Left column: Right column: + Section "General" + Section "Update mirrors" + Section "Integration" + Section
         * "License" + Section "Publisher" + Section "Release notes"
         */
        GeneralSection generalSection = new GeneralSection(leftColumn, this, project);
        localized.add(generalSection);
        mSections.add(generalSection);

        IntegrationSection integrationSection = new IntegrationSection(leftColumn, this);
        mSections.add(integrationSection);

        PublisherSection publisherSection = new PublisherSection(leftColumn, this);
        localized.add(publisherSection);
        mSections.add(publisherSection);

        ReleaseNotesSection releaseNotesSection = new ReleaseNotesSection(leftColumn, this);
        localized.add(releaseNotesSection);
        mSections.add(releaseNotesSection);

        MirrorsSection mirrorSection = new MirrorsSection(rightColumn, this);
        mSections.add(mirrorSection);

        LicenseSection licenseSection = new LicenseSection(rightColumn, this, project);
        mSections.add(licenseSection);
        localized.add(licenseSection);

        return localized;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canLeaveThePage() {
        DescriptionEditor editor = (DescriptionEditor) getEditor();
        editor.writeDescrToSource();

        return super.canLeaveThePage();
    }

    /**
     * Reload the data from the model in the sections.
     */
    public void reloadData() {
        getModel().setSuspendEvent(true);
        for (AbstractSection<DescriptionModel> section : mSections) {
            section.loadData();
        }

        getModel().setSuspendEvent(false);
    }
}
