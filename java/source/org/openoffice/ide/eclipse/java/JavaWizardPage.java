package org.openoffice.ide.eclipse.java;

import org.eclipse.core.resources.IProject;
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
import org.openoffice.ide.eclipse.core.wizards.LanguageWizardPage;

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
				setMessage(Messages.getString("JavaWizardPage.InvalidClassNameError")); //$NON-NLS-1$
			} else {
				mRegclass = mRegclassRow.getValue();
				mJavaVersion = mJavaVersionRow.getValue();
			}
		}
	};
	
	public JavaWizardPage(UnoFactoryData data) {
		super();
		setProjectInfos(data);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.LanguageWizardPage#setProjectInfos(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void setProjectInfos(UnoFactoryData data) {
		try {
			String projectPrefix = (String)data.getProperty(
					IUnoFactoryConstants.PROJECT_PREFIX);
			IProject projectHandle = (IProject)data.getProperty(
					IUnoFactoryConstants.PROJECT_HANDLE);
			String projectName = projectHandle.getName();
			String projectComp = (String)data.getProperty(
					IUnoFactoryConstants.PROJECT_COMP);

			if (projectPrefix != null && projectName != null) {
				String classname = projectName.substring(0, 1).toUpperCase() + 
				projectName.substring(1) + "Impl"; //$NON-NLS-1$
				mRegclass = projectPrefix + "." + //$NON-NLS-1$
				projectComp + "." + classname; //$NON-NLS-1$
			}
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
		
		mJavaVersionRow = new ChoiceRow(body, JAVA_VERSION, Messages.getString("JavaWizardPage.JavaVersion")); //$NON-NLS-1$
		mJavaVersionRow.add(Messages.getString("JavaWizardPage.Java4"), "java4"); //$NON-NLS-1$ //$NON-NLS-2$
		mJavaVersionRow.add(Messages.getString("JavaWizardPage.Java5"), "java5"); //$NON-NLS-1$ //$NON-NLS-2$
		mJavaVersionRow.select(0);
		mJavaVersionRow.setFieldChangedListener(mListener);
		
		setControl(body);
	}
}
