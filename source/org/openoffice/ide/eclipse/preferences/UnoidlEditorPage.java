/*************************************************************************
 *
 * $RCSfile: UnoidlEditorPage.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/18 19:35:57 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.preferences;


import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.editors.Colors;
import org.openoffice.ide.eclipse.i18n.I18nConstants;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlEditorPage
	extends FieldEditorPreferencePage
	implements IWorkbenchPreferencePage {

	public UnoidlEditorPage() {
		super(GRID);
		setPreferenceStore(OOEclipsePlugin.getDefault().getPreferenceStore());
		setDescription(OOEclipsePlugin.getTranslationString(I18nConstants.EDITOR_COLORS_PREF));
	}

	
	public void createFieldEditors() {
		addField(new ColorFieldEditor(Colors.C_TEXT, 
				 OOEclipsePlugin.getTranslationString(I18nConstants.TEXT), getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_KEYWORD, 
				 OOEclipsePlugin.getTranslationString(I18nConstants.KEYWORD), getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_MODIFIER,
				 OOEclipsePlugin.getTranslationString(I18nConstants.MODIFIER),
				 getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_STRING, 
				 OOEclipsePlugin.getTranslationString(I18nConstants.STRING), getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_TYPE, 
				 OOEclipsePlugin.getTranslationString(I18nConstants.TYPE), getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_COMMENT, 
				 OOEclipsePlugin.getTranslationString(I18nConstants.COMMENT), getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_AUTODOC_COMMENT,
				 OOEclipsePlugin.getTranslationString(I18nConstants.AUTODOC_COMMENT), 
				 getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_XML_TAG,
				 OOEclipsePlugin.getTranslationString(I18nConstants.XML_TAG),
				 getFieldEditorParent()));
		addField(new ColorFieldEditor(Colors.C_PREPROCESSOR, 
				 OOEclipsePlugin.getTranslationString(I18nConstants.PREPROCESSOR_COMMAND), 
				 getFieldEditorParent()));
	}
	
	public void init(IWorkbench workbench) {
	}
}