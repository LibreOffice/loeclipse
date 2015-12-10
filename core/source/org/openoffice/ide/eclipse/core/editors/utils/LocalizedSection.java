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
package org.openoffice.ide.eclipse.core.editors.utils;

import java.util.Locale;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openoffice.ide.eclipse.core.model.utils.IModel;

/**
 *
 * @param <ModelType>
 *            the type of the model object for the section
 *
 * @author Cédric Bosdonnat
 *
 */
public abstract class LocalizedSection<ModelType extends IModel> extends AbstractSection<ModelType>
implements ILocaleListener {

    protected Locale mCurrentLocale;

    private FormPage mPage;

    /**
     * @param pParent
     *            the parent composite where to add the section
     * @param pPage
     *            the page page of the section
     * @param pStyle
     *            a bit-or of the styles defined in Section class
     */
    public LocalizedSection(Composite pParent, FormPage pPage, int pStyle) {
        super(pParent, pPage, pStyle);

        mPage = pPage;

        createContent();
    }

    /**
     * Create the localized controls in the given parent.
     *
     * @param pToolkit
     *            the toolkit to use for the controls creation
     * @param pParent
     *            the parent to use for the new controls.
     */
    protected abstract void createControls(FormToolkit pToolkit, Composite pParent);

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(final Locale pLocale) {
        mCurrentLocale = pLocale;
    }

    /**
     * Creates the dialog content.
     */
    private void createContent() {
        // Create the Language selection tools
        Section section = getSection();
        section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        FormToolkit toolkit = mPage.getManagedForm().getToolkit();

        Composite clientArea = toolkit.createComposite(section);
        clientArea.setLayout(new GridLayout());
        clientArea.setLayoutData(new GridData(GridData.FILL_BOTH));
        createControls(toolkit, clientArea);

        toolkit.paintBordersFor(clientArea);

        section.setClient(clientArea);
    }
}
