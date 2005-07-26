/*************************************************************************
 *
 * $RCSfile: NewUnoProjectPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/26 06:23:58 $
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
package org.openoffice.ide.eclipse.wizards;

import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.gui.rows.TextRow;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.preferences.sdk.SDKContainer;

/**
 * Uses the default Project wizard page and add some UNO-IDL special
 * fields: SDK choice company prefix and Output path
 * 
 * @author cbosdonnat
 *
 */
public class NewUnoProjectPage extends WizardNewProjectCreationPage 
							   implements IFieldChangedListener{
	
	/* Constants defining the field properties used to react to field change events */
	private static final String PREFIX = "__prefix";
	private static final String OUTPUT_EXT = "__output_ext";
	private static final String SDK = "__sdk";
	private static final String LANGUAGE = "__language";
	
	/**
	 * Prefix field object
	 */
	private TextRow prefixRow;
	
	/**
	 * Implementation extension field object
	 */
	private TextRow outputExt;
	
	/**
	 * SDK used for the project selection row
	 */
	private ChoiceRow sdkRow;
	
	/**
	 * Programming language to use for code generation 
	 */
	private ChoiceRow languageRow;
	
	/**
	 * Specific error message label. <code>setErrorMessage()</code> will
	 * use this row instead of the standard one.
	 */
	private Label messageLabel;
	private Label messageIcon;
	
	public NewUnoProjectPage() {
		super(OOEclipsePlugin.getTranslationString(
				I18nConstants.NEW_PROJECT_TITLE));
		setTitle(OOEclipsePlugin.getTranslationString(
				I18nConstants.NEW_PROJECT_TITLE));
		
		setDescription(OOEclipsePlugin.getTranslationString(
				I18nConstants.NEW_PROJECT_MESSAGE));
		
		setImageDescriptor(OOEclipsePlugin.getImageDescriptor(
				ImagesConstants.NEWPROJECT_WIZ));
	}
	
	/**
	 * Returns the entered company prefix
	 * 
	 * @return company prefix entered
	 */
	public String getPrefix(){
		return prefixRow.getText();
	}
	
	/**
	 * Returns the entered ouput extension
	 * 
	 * @return ouput extension entered
	 */
	public String getOutputExt(){
		return outputExt.getText();
	}
	
	/**
	 * Returns the selected SDK Name
	 * 
	 * @return SDK name selected
	 */
	public String getSDKName(){
		return sdkRow.getValue();
	}
	
	public int getChosenLanguage(){
		 String value = languageRow.getValue();
		 int result = UnoidlProject.JAVA_LANGUAGE;
		 
		 if (value.equals(OOEclipsePlugin.getTranslationString(
				 I18nConstants.JAVA))){
			 result = UnoidlProject.JAVA_LANGUAGE;
			 
		 } else if (value.equals(OOEclipsePlugin.getTranslationString(
				 I18nConstants.CPP))){
			 result = UnoidlProject.CPP_LANGUAGE;
			 
		 } else if (value.equals(OOEclipsePlugin.getTranslationString(
				 I18nConstants.PYTHON))){
			 result = UnoidlProject.PYTHON_LANGUAGE;
		 }
		 
		 return result;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Inherits the parents control
		super.createControl(parent);
		Composite control = (Composite)getControl();
		
		Composite body = new Composite(control, SWT.NONE);
		body.setLayout(new GridLayout(2, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Add the company prefix field
		prefixRow = new TextRow(body, PREFIX, 
						OOEclipsePlugin.getTranslationString(I18nConstants.COMPANY_PREFIX));
		prefixRow.setFieldChangedListener(this);
		
		// Add the output directory field
		outputExt = new TextRow(body, OUTPUT_EXT,
						OOEclipsePlugin.getTranslationString(I18nConstants.OUTPUT_EXT));
		outputExt.setFieldChangedListener(this);
		
		// Add the SDK choice field
		sdkRow = new ChoiceRow(body, SDK,
						OOEclipsePlugin.getTranslationString(I18nConstants.USED_SDK));
		sdkRow.setFieldChangedListener(this);
		
		// Adding the SDK names to the combo box 
		SDKContainer container = SDKContainer.getSDKContainer();
		String[] sdks = new String[container.getSDKCount()];
		Vector keys = container.getSDKKeys();
		for (int i=0, length=container.getSDKCount(); i<length; i++){
			sdks[i] = (String)keys.get(i);
		}
		
		sdkRow.addAll(sdks);
		sdkRow.select(0);   // The default SDK is randomly the first one
		
		// Adding the programming language row 
		languageRow = new ChoiceRow(body, LANGUAGE,
						OOEclipsePlugin.getTranslationString(I18nConstants.PROG_LANGUAGE));
		
		// Sets the available programming languages
		// TODO Add CPP and PYTHON when they will be ready
		languageRow.add(OOEclipsePlugin.getTranslationString(I18nConstants.JAVA));
		languageRow.select(0);
		languageRow.setFieldChangedListener(this);
		
		// Add an error message label
		Composite messageComposite = new Composite(control, SWT.NONE);
		messageComposite.setLayout(new GridLayout(2, false));
		messageComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		messageIcon = new Label(messageComposite, SWT.LEFT);
		messageIcon.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING |
											   GridData.VERTICAL_ALIGN_END));
		messageIcon.setImage(OOEclipsePlugin.getImage(ImagesConstants.ERROR));
		messageIcon.setVisible(false);
		
		messageLabel = new Label(messageComposite, SWT.LEFT);
		messageLabel.setLayoutData(new GridData(GridData.FILL_BOTH |
				                                GridData.VERTICAL_ALIGN_END));
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.gui.rows.IFieldChangedListener#fieldChanged(org.openoffice.ide.eclipse.gui.rows.FieldEvent)
	 */
	public void fieldChanged(FieldEvent e) {
		// Check the prefix correctness
		if (e.getProperty().equals(PREFIX)){
			String newCompanyPrefix = e.getValue();
			/**
			 * <p>The company prefix is a package like name used by the project
			 * to build the idl file path and the implementation path.</p>
			 */
			
			if (!newCompanyPrefix.matches(
					"([a-zA-Z][a-zA-Z0-9]*)(.[a-zA-Z][a-zA-Z0-9]*)*")){
				/**
				 * <p>If the new company prefix is invalid, it is set to
				 * the empty string with an error message.</p>
				 */
				
				prefixRow.setText("");
				setErrorMessage(OOEclipsePlugin.getTranslationString(
						I18nConstants.COMPANY_PREFIX_ERROR));
			} else {
				setErrorMessage(null);
			}
			
		}
		
		// Check the implementation extension correctness
		if (e.getProperty().equals(OUTPUT_EXT)){
			String newOuputExt = e.getValue();
			/**
			 * <p>The implementation extension is a single word which could 
			 * contain numbers. It have to begin with a letter.</p> 
			 */
			
			if (!newOuputExt.matches("[a-zA-Z][a-zA-Z0-9]*")){
				/**
				 * <p>If the new implementation extension is invalid, it is set to
				 * the empty string with an error message.</p>
				 */
				outputExt.setText("");
				setErrorMessage(OOEclipsePlugin.getTranslationString(
									I18nConstants.OUTPUT_EXT_ERROR));
			} else {
				setErrorMessage(null);
			}
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String newMessage) {
		if (null != messageLabel){
			if (null == newMessage){
				messageLabel.setText("");
				messageIcon.setVisible(false);
				messageLabel.setVisible(false);
			} else {
				messageLabel.setText(newMessage);
				messageIcon.setVisible(true);
				messageLabel.setVisible(true);
			}
		}
	}

}
