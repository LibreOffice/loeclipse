/*************************************************************************
 *
 * $RCSfile: MainPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:33 $
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

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;

/**
 * 
 * @author cbosdonnat
 *
 */
public class MainPage extends PreferencePage implements IWorkbenchPreferencePage {

	protected Control createContents(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(2, false));
		body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		// Creates the left image
		Label imgLabel = new Label(body, SWT.CENTER);
		imgLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | 
											GridData.VERTICAL_ALIGN_BEGINNING));
		imgLabel.setImage(OOEclipsePlugin.getImage(ImagesConstants.ABOUT_BANNER));
		
		Composite textBody = new Composite(body, SWT.NONE);
		textBody.setLayout(new GridLayout());
		textBody.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Creates the title
		Label titleLabel = new Label(textBody, SWT.CENTER);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 10;
		titleLabel.setLayoutData(gd);
		titleLabel.setText(OOEclipsePlugin.getTranslationString(
				I18nConstants.MAINPREF_TITLE));
		titleLabel.setFont(new Font(getShell().getDisplay(), 
				"Arial", 12, SWT.BOLD));
		
		// Creates the description
		Label descrLabel = new Label(textBody, SWT.LEFT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 30;
		descrLabel.setLayoutData(gd);
		descrLabel.setFont(new Font(getShell().getDisplay(), 
				"Arial", 9, SWT.NORMAL));
		descrLabel.setText(OOEclipsePlugin.getTranslationString(
				I18nConstants.MAINPREF_DESCRIPTION));
		
		// Create the Credits
		
		Label creditsLabel = new Label(textBody, SWT.LEFT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 30;
		creditsLabel.setLayoutData(gd);
		creditsLabel.setFont(new Font(getShell().getDisplay(), 
				"Arial", 9, SWT.NORMAL));
		creditsLabel.setText(OOEclipsePlugin.getTranslationString(
				I18nConstants.MAINPREF_CREDITS));
		
		// Create the copyright section
		Label copyLabel = new Label(textBody, SWT.LEFT);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalIndent = 30;
		copyLabel.setLayoutData(gd);
		copyLabel.setText(OOEclipsePlugin.getTranslationString(
				I18nConstants.MAINPREF_COPYRIGHT));
		copyLabel.setFont(new Font(getShell().getDisplay(), 
				"Arial", 7, SWT.ITALIC));
		return body;
	}

	public void init(IWorkbench workbench) {
		
	}
}
