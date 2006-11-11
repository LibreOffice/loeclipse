/*************************************************************************
 *
 * $RCSfile: JavaWizardPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/11 18:39:35 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.java;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.model.language.LanguageWizardPage;

public class JavaWizardPage extends LanguageWizardPage {

	public static final String REGISTRATION_CLASS_NAME = "registration_class_name"; //$NON-NLS-1$
	public static final String JAVA_VERSION = "java_version"; //$NON-NLS-1$
	
	private TextRow mRegclassRow;
	private ChoiceRow mJavaVersionRow;
	
	private String mRegclass;
	private String mJavaVersion;
	
	private IFieldChangedListener mListener = new IFieldChangedListener() {

		public void fieldChanged(FieldEvent e) {
			if (!e.getValue().matches("(\\w+\\.)*\\w+") &&  //$NON-NLS-1$
					e.getProperty().equals(REGISTRATION_CLASS_NAME)) {
				setErrorMessage(Messages.getString("JavaWizardPage.InvalidClassNameError")); //$NON-NLS-1$
			} else {
				mRegclass = mRegclassRow.getValue();
				mJavaVersion = mJavaVersionRow.getValue();
				setMessage(null);
			}
		}
	};
	
	public JavaWizardPage(UnoFactoryData data) {
		super();
		setProjectInfos(data);
		setImageDescriptor(OOoJavaPlugin.getDefault().getImageRegistry().
				getDescriptor(OOoJavaPlugin.WIZBAN));
		setTitle(Messages.getString("JavaWizardPage.PageTitle")); //$NON-NLS-1$
		setDescription(Messages.getString("JavaWizardPage.PageDescription")); //$NON-NLS-1$
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.LanguageWizardPage#setProjectInfos(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setProjectInfos(UnoFactoryData data) {
		try {
			String projectPrefix = (String)data.getProperty(
					IUnoFactoryConstants.PROJECT_PREFIX);
			String projectName = (String)data.getProperty(
					IUnoFactoryConstants.PROJECT_NAME);
			String projectComp = (String)data.getProperty(
					IUnoFactoryConstants.PROJECT_COMP);

			if (projectPrefix != null && projectName != null) {
				String classname = projectName.substring(0, 1).toUpperCase() + 
				projectName.substring(1) + "Impl"; //$NON-NLS-1$
				String regclass = projectPrefix + "." + //$NON-NLS-1$
					projectComp + "." + classname; //$NON-NLS-1$
				
				// Strips any whitespace in the implementation name
				mRegclass = regclass.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			// default value
			mJavaVersion = "java4"; //$NON-NLS-1$
			
		} catch (Exception e) {	}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.LanguageWizardPage#fillData(org.openoffice.ide.eclipse.core.model.UnoFactoryData)
	 */
	public UnoFactoryData fillData(UnoFactoryData data) {
		
		if (data != null) {
			data.setProperty(REGISTRATION_CLASS_NAME, mRegclass);
			data.setProperty(JAVA_VERSION, mJavaVersion);
		}
		
		return data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(2, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		mRegclassRow = new TextRow(body, REGISTRATION_CLASS_NAME, Messages.getString("JavaWizardPage.RegistrationClassName")); //$NON-NLS-1$
		mRegclassRow.setValue(mRegclass);
		mRegclassRow.setFieldChangedListener(mListener);
		mRegclassRow.setTooltip("Defines the implementation name of the service.");
		
		mJavaVersionRow = new ChoiceRow(body, JAVA_VERSION, Messages.getString("JavaWizardPage.JavaVersion")); //$NON-NLS-1$
		mJavaVersionRow.add(Messages.getString("JavaWizardPage.Java4"), "java4"); //$NON-NLS-1$ //$NON-NLS-2$
		mJavaVersionRow.add(Messages.getString("JavaWizardPage.Java5"), "java5"); //$NON-NLS-1$ //$NON-NLS-2$
		mJavaVersionRow.setFieldChangedListener(mListener);
		mJavaVersionRow.select(0);
		mJavaVersionRow.setTooltip("Defines the minimal required java version of the project.");
		
		setControl(body);
	}
}
