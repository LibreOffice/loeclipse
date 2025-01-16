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
package org.libreoffice.ide.eclipse.core.editors.pack;


import org.eclipse.core.resources.IProject;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.model.pack.PackagePropertiesModel;

/**
 * Page displaying the Package properties in a more user friendly way.
 */
public class PackageFormPage extends FormPage {

    private ContentsSection mContents;
    private LibsSection mLibs;
    private PackageDescriptionSection mDescriptions;

    /**
     * Content form page constructor.
     *
     * @param editor
     *            the editor where to create the form page
     * @param pageId
     *            the page identifier
     */
    public PackageFormPage(FormEditor editor, String pageId) {
        super(editor, pageId, Messages.getString("PackagePropertiesFormPage.PackagePropertiesText")); //$NON-NLS-1$
    }

    /**
     * @return the project for which the contents are shown
     */
    public IProject getProject() {
        IProject prj = null;
        if (getEditorInput() instanceof IFileEditorInput) {
            prj = ((IFileEditorInput) getEditorInput()).getFile().getProject();
        }
        return prj;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFormContent(IManagedForm managedForm) {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();

        FormToolkit toolkit = managedForm.getToolkit();
        toolkit.decorateFormHeading(form.getForm());

        form.setText(Messages.getString("PackagePropertiesFormPage.PackagePropertiesText")); //$NON-NLS-1$

        // Create the only section with a tree representing the files
        // and dirs in its client area
        form.getBody().setLayout(new GridLayout(2, true));

        mContents = new ContentsSection(this);
        mLibs = new LibsSection(this);
        mDescriptions = new PackageDescriptionSection(this);


        // Get the Libs and Descriptions properties from the document
        PackagePropertiesModel model = getModel();
        model.setQuiet(true);
        mContents.setContents();
        mLibs.setLibraries(model);
        mDescriptions.setDescriptions(model.getDescriptionFiles());
        model.setQuiet(false);
    }

    private PackagePropertiesModel getModel() {
        return ((PackagePropertiesEditor) getEditor()).getModel();
    }

}
