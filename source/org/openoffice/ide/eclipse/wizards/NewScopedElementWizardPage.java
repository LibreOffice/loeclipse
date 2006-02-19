/*************************************************************************
 *
 * $RCSfile: NewScopedElementWizardPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/02/19 11:32:41 $
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
package org.openoffice.ide.eclipse.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.gui.rows.TextRow;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.preferences.ooo.OOo;
import org.openoffice.ide.eclipse.unotypebrowser.UnoTypeProvider;

/**
 * Basic class for a wizard page to create a scoped element
 * such as a service or an interface.
 * 
 * @author cbosdonnat
 *
 */
public abstract class NewScopedElementWizardPage extends WizardPage
												 implements IFieldChangedListener{

	protected UnoidlProject unoProject;
	private String rootName;
	private String elementName;
	
	protected UnoTypeProvider typesProvider;
	
	/**
	 * Default constructor to use when neither the project nor the
	 * OOo instance is known.
	 * 
	 * @param aName wizard page name
	 */
	public NewScopedElementWizardPage(String aName) {
		this (aName, "", "");
	}
	
	/**
	 * Constructor to use when the uno project is already created
	 * 
	 * @param pageName name of the page
	 * @param unoProject uno project in which to create a scoped type
	 */
	public NewScopedElementWizardPage(
			String pageName, UnoidlProject unoProject) {
		this(pageName, unoProject, "", "");
	}
	
	/**
	 * Constructor to use when the uno project is already created, the 
	 * scoped type name and it's path already known
	 * 
	 * @param pageName name of the wizard page
	 * @param project uno project in which to create a scoped type
	 * @param aRootName scoped name of the module containing the type 
	 * @param aElementName name of the type, without any '.' or '::'
	 */
	public NewScopedElementWizardPage(
			String pageName, UnoidlProject project, 
			String aRootName, String aElementName) {
		
		this(pageName, aRootName, aElementName);
		
		unoProject = project;
		typesProvider = new UnoTypeProvider(project, getProvidedTypes());

	}
	
	public NewScopedElementWizardPage(String aPageName, OOo aOOoInstance) {
		this(aPageName, "", "", aOOoInstance);
	}
	
	public NewScopedElementWizardPage(String aPageName,
			String aRootName, String aElementName, OOo aOOoInstance) {
		
		this(aPageName, aRootName, aElementName);
		typesProvider = new UnoTypeProvider(aOOoInstance, getProvidedTypes());
		
	}
	
	private NewScopedElementWizardPage(
			String pageName, String aRootName, String aElementName) {
		
		super(pageName);
		
		setTitle(getTitle());
		setDescription(getDescription());
		setImageDescriptor(getImageDescriptor());
		
		rootName = null != aRootName ? aRootName: "";
		elementName = null != aElementName ? aElementName : "";
	}
	
	protected abstract String getTypeLabel();
	
	protected abstract ImageDescriptor getImageDescriptor();

	public void dispose() {
		try {
			packageRow.removeFieldChangedlistener();
			nameRow.removeFieldChangedlistener();
			typesProvider.dispose();
		} catch (NullPointerException e) {
			if (null != System.getProperty("DEBUG")) {
				e.printStackTrace();
			}
		}
		
		super.dispose();
	}
	
	//--------------------------------------------------- Page content managment
	
	private final static String P_PACKAGE           = "__package";
	private final static String P_NAME              = "__name";
	
	private TextRow packageRow;
	private TextRow nameRow;
	
	/**
	 * Specific error message label. <code>setErrorMessage()</code> will
	 * use this row instead of the standard one.
	 */
	private Label messageLabel;
	private Label messageIcon;
	
	public void createControl(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(3, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Creates the package row
		String packageLabel = OOEclipsePlugin.getTranslationString(
				I18nConstants.PACKAGE_COLON);
		if (null != unoProject) {
			packageLabel = packageLabel + unoProject.getRootScopedName();
		}
		
		packageRow = new TextRow(body, P_PACKAGE, packageLabel);
		packageRow.setFieldChangedListener(this);
		packageRow.setValue(rootName);
		
		nameRow = new TextRow(body, P_NAME, getTypeLabel());
		nameRow.setFieldChangedListener(this);
		nameRow.setValue(elementName);

		createSpecificControl(body);
		
		// Message controls
		Composite messageComposite = new Composite(body, SWT.NONE);
		messageComposite.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		messageComposite.setLayoutData(gd);
		
		
		messageIcon = new Label(messageComposite, SWT.LEFT);
		messageIcon.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING |
											   GridData.VERTICAL_ALIGN_END));
		messageIcon.setImage(OOEclipsePlugin.getImage(ImagesConstants.ERROR));
		messageIcon.setVisible(false);
		
		messageLabel = new Label(messageComposite, SWT.LEFT);
		messageLabel.setLayoutData(new GridData(GridData.FILL_BOTH |
				                                GridData.VERTICAL_ALIGN_END));
		
		setPageComplete(isPageComplete());
		
		setControl(body);
	}
	
	protected abstract void createSpecificControl(Composite parent);
	
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

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			typesProvider.askUnoTypes();
		}
	}
	
	/**
	 * Launch or relaunch the type provider by setting 
	 * the used OOo instance
	 * 
	 * @param aOOoInstance OOo instance to use.
	 */
	public void setOOoInstance(OOo aOOoInstance) {
		
		if (null == typesProvider) {
			typesProvider = new UnoTypeProvider(aOOoInstance, getProvidedTypes());
		} else {
			typesProvider.setOOoInstance(aOOoInstance);
		}
	}
	
	public void setUnoidlProject(UnoidlProject aUnoProject) {
		unoProject = aUnoProject;
	}
	
	public String getPackage() {
		return packageRow.getValue();
	}
	
	public String getElementName() {
		return nameRow.getValue();
	}
	
	public void setPackageRoot(String value) {
		String packageLabel = OOEclipsePlugin.getTranslationString(
				I18nConstants.PACKAGE_COLON) + value;
		
		packageRow.setLabel(packageLabel);
	}
	
	public void setPackage(String value, boolean forced) {
		
		packageRow.setValue(value);
		packageRow.setEnabled(!forced);	
	}
	
	public void setName(String value, boolean forced) {
		
		nameRow.setValue(value);
		nameRow.setEnabled(!forced);
	}
	
	public void fieldChanged(FieldEvent e) {
		try {
			if (e.getProperty().equals(P_PACKAGE)) {
				// Change the label of the package row
				String text = OOEclipsePlugin.getTranslationString(
								I18nConstants.PACKAGE_COLON)+
							  unoProject.getRootScopedName();
				
				if (null != e.getValue() && !e.getValue().equals("")){
					text = text + ".";
				}
				packageRow.setLabel(text);
				((Composite)getControl()).layout();
	
			} else if (e.getProperty().equals(P_NAME)) {
				// Test if there is the scoped name already exists
				boolean exists = typesProvider.contains(e.getValue());
				if (exists) {
					setErrorMessage(OOEclipsePlugin.getTranslationString(
							I18nConstants.NAME_EXISTS));
				} else {
					setErrorMessage(null);
				}
				
			}
		} catch (NullPointerException ex) {
			// Nothing to do... this is sometimes normal
		}
		
	
		setPageComplete(isPageComplete());
	}
	
	public boolean isPageComplete() {
		boolean result = false; 
		
		try {
			result = messageLabel.getText().equals("");
			
			if (nameRow.getValue().equals("")) {
				result = false;
			}
		} catch (NullPointerException e) {
			result = false;
		}
		
		return result;
	}
	
	/**
	 * <p>Returns the types to get in the UNO types provider. The returned integer
	 * is a <pre>bit or</pre> of the types defined in the {@link UnoTypeProvider} class.</p>
	 * 
	 */
	public abstract int getProvidedTypes();

}
