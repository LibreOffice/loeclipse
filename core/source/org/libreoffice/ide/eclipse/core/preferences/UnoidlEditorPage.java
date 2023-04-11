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
package org.libreoffice.ide.eclipse.core.preferences;

import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.editors.idl.Colors;

/**
 * Preference page to change the UNO-IDL editor colors.
 *
 */
public class UnoidlEditorPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    /**
     * Default constructor loading the preferences.
     */
    public UnoidlEditorPage() {
        super(GRID);
        setPreferenceStore(OOEclipsePlugin.getDefault().getPreferenceStore());
        setDescription(Messages.getString("UnoidlEditorPage.Title")); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createFieldEditors() {
        addField(new ColorFieldEditor(Colors.C_TEXT, Messages.getString("UnoidlEditorPage.Text"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_KEYWORD, Messages.getString("UnoidlEditorPage.Keyword"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_MODIFIER, Messages.getString("UnoidlEditorPage.Modifier"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_STRING, Messages.getString("UnoidlEditorPage.String"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_TYPE, Messages.getString("UnoidlEditorPage.Type"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_COMMENT, Messages.getString("UnoidlEditorPage.Comment"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_AUTODOC_COMMENT, Messages.getString("UnoidlEditorPage.AutodocComment"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_XML_TAG, Messages.getString("UnoidlEditorPage.XmlTag"), //$NON-NLS-1$
            getFieldEditorParent()));
        addField(new ColorFieldEditor(Colors.C_PREPROCESSOR, Messages.getString("UnoidlEditorPage.PreprocessorCommand"), //$NON-NLS-1$
            getFieldEditorParent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IWorkbench pWorkbench) {
    }
}