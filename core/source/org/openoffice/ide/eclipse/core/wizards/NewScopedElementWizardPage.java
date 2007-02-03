/*************************************************************************
 *
 * $RCSfile: NewScopedElementWizardPage.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/03 21:42:12 $
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
package org.openoffice.ide.eclipse.core.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;

/**
 * Astract class for a wizard page to create a scoped element
 * such as a service or an interface.
 * 
 * @author cbosdonnat
 *
 */
public abstract class NewScopedElementWizardPage extends WizardPage
												 implements IFieldChangedListener{

	protected IUnoidlProject mUnoProject;
	private String mRootName;
	private String mElementName;
	
	/**
	 * Default constructor to use when neither the project nor the
	 * OOo instance is known.
	 * 
	 * @param aName wizard page name
	 */
	public NewScopedElementWizardPage(String aName) {
		this (aName, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Constructor to use when the uno project is already created
	 * 
	 * @param pageName name of the page
	 * @param unoProject uno project in which to create a scoped type
	 */
	public NewScopedElementWizardPage(
			String pageName, IUnoidlProject unoProject) {
		this(pageName, unoProject, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
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
			String pageName, IUnoidlProject project, 
			String aRootName, String aElementName) {
		
		this(pageName, aRootName, aElementName);
		setUnoidlProject(project);
	}
	
	/**
	 * Creates a default scoped name type wizard page with blank container
	 * path and type name.
	 */
	public NewScopedElementWizardPage(String aPageName, IOOo aOOoInstance) {
		this(aPageName, "", "", aOOoInstance); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * Constructor to use when the uno project is already created, the 
	 * scoped type name and it's path already known
	 * 
	 * @param aPageName name of the wizard page
	 * @param aRootName scoped name of the module containing the type 
	 * @param aElementName name of the type, without any '.' or '::'
	 * @param aOOoInstance the reference to the OOo to use for type selection
	 */
	public NewScopedElementWizardPage(String aPageName,
			String aRootName, String aElementName, IOOo aOOoInstance) {
		
		this(aPageName, aRootName, aElementName);
		setOOoInstance(aOOoInstance);
	}
	
	/**
	 * Creates a default page for a scoped element like an interface or a 
	 * service. This constructor let provide default values for the container
	 * path and the type name. 
	 * 
	 * @param pageName name of the wizard page
	 * @param aRootName scoped name of the module containing the type 
	 * @param aElementName name of the type, without any '.' or '::'
	 */
	private NewScopedElementWizardPage(
			String pageName, String aRootName, String aElementName) {
		
		super(pageName);
		
		setTitle(getTitle());
		setDescription(getDescription());
		setImageDescriptor(getImageDescriptor());
		
		mRootName = null != aRootName ? aRootName: ""; //$NON-NLS-1$
		mElementName = null != aElementName ? aElementName : ""; //$NON-NLS-1$
	}
	
	/**
	 * Return the string corresponding to the type name, eg "interface"
	 */
	protected abstract String getTypeLabel();
	
	/**
	 * Return the image descriptor to put on the top-right of the page
	 */
	protected abstract ImageDescriptor getImageDescriptor();

	/**
	 * Implement this method to add specific controls for the subclassing 
	 * wizard page.
	 * 
	 * @param parent the composite parent where to put the controls
	 */
	protected abstract void createSpecificControl(Composite parent);
	
	/**
	 * <p>Returns the types to get in the UNO types provider. The returned integer
	 * is a <pre>bit or</pre> of the types defined in the {@link UnoTypeProvider} class.</p>
	 */
	public abstract int getProvidedTypes();
	
	/**
	 * Launch or relaunch the type provider by setting 
	 * the used OOo instance
	 * 
	 * @param aOOoInstance OOo instance to use.
	 */
	public void setOOoInstance(IOOo aOOoInstance) {
		if (aOOoInstance != null) {
			UnoTypeProvider.getInstance().initialize(aOOoInstance, getProvidedTypes());
		}
	}
	
	/**
	 * Sets the Uno project in which to create the scoped name type
	 */
	public void setUnoidlProject(IUnoidlProject aUnoProject) {
		mUnoProject = aUnoProject;
		UnoTypeProvider.getInstance().initialize(mUnoProject, getProvidedTypes());
	}
	
	/**
	 * the container name of the type to create is composed of two parts: the
	 * package root and the package. This method returns the second part.
	 */
	public String getPackage() {
		String packageName = mUnoProject != null ? mUnoProject.getRootModule() : ""; //$NON-NLS-1$
		

		if (!mRootName.equals("")) { //$NON-NLS-1$
			if (!packageName.equals("")) { //$NON-NLS-1$
				packageName += "::"; //$NON-NLS-1$
			}
			packageName += mRootName.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		if (!mPackageRow.getValue().equals("")) { //$NON-NLS-1$
			if (!packageName.equals("")) { //$NON-NLS-1$
				packageName += "::"; //$NON-NLS-1$
			}
			packageName += mPackageRow.getValue();
		}
		return packageName;
	}
	
	/**
	 * Get the name of the element to create
	 */
	public String getElementName() {
		return mNameRow.getValue();
	}
	
	/**
	 * the container name of the type to create is composed of two parts: the
	 * package root and the package. This method sets the first part.
	 */
	public void setPackageRoot(String value) {
		String packageLabel = Messages.getString("NewScopedElementWizardPage.Package") + value; //$NON-NLS-1$
		mRootName = value;
		
		if (mPackageRow != null) mPackageRow.setLabel(packageLabel);
	}
	
	/**
	 * the container name of the type to create is composed of two parts: the
	 * package root and the package. This method sets the second part.
	 * 
	 * @param value the new package value
	 * @param forced <code>true</code> will replace the current value, 
	 * 			<code>false</code> will set the value only if the current
	 * 			package is empty or <code>null</code>. 
	 */
	public void setPackage(String value, boolean forced) {
		if (mPackageRow != null) {
			mPackageRow.setValue(value);
			mPackageRow.setEnabled(!forced);
		}
	}
	
	/**
	 * Sets the name of the element to create
	 * 
	 * @param value the new package value
	 * @param forced <code>true</code> will replace the current value, 
	 * 			<code>false</code> will set the value only if the current
	 * 			package is empty or <code>null</code>. 
	 */
	public void setName(String value, boolean forced) {
		
		mElementName = value;
		if (mNameRow != null) {
			
			value = value.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			mNameRow.setValue(value);
			mNameRow.setEnabled(!forced);
		}
	}
	
	/**
	 * Returns whether the service is published or not
	 */
	public boolean isPublished() {
		return mPublishedRow.getBooleanValue();
	}
	
	/**
	 * Sets whether the service is published or not
	 */
	public void setPublished(boolean value, boolean forced) {
		
		mPublishedRow.setValue(value);
		mPublishedRow.setEnabled(!forced);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		try {
			mPackageRow.removeFieldChangedlistener();
			mNameRow.removeFieldChangedlistener();
			mPublishedRow.removeFieldChangedlistener();
			UnoTypeProvider.getInstance().stopProvider();
		} catch (NullPointerException e) {
			PluginLogger.debug(e.getMessage());
		}
		
		super.dispose();
	}
	
	//--------------------------------------------------- Page content managment
	
	private final static String P_PACKAGE           = "__package"; //$NON-NLS-1$
	private final static String P_NAME              = "__name"; //$NON-NLS-1$
	private final static String P_PUBLISHED			= "__published"; //$NON-NLS-1$
	
	private TextRow mPackageRow;
	private TextRow mNameRow;
	private BooleanRow mPublishedRow;
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(3, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Creates the package row
		String packageLabel = Messages.getString("NewScopedElementWizardPage.Package"); //$NON-NLS-1$
		if (null != mUnoProject) {
			packageLabel = packageLabel + mUnoProject.getRootModule();
		}
		
		mPackageRow = new TextRow(body, P_PACKAGE, packageLabel);
		mPackageRow.setFieldChangedListener(this);
		mPackageRow.setValue(mRootName);
		mPackageRow.setTooltip(Messages.getString("NewScopedElementWizardPage.PackageTooltip")); //$NON-NLS-1$
		
		mNameRow = new TextRow(body, P_NAME, getTypeLabel());
		mNameRow.setFieldChangedListener(this);
		mNameRow.setValue(mElementName);
		mNameRow.setTooltip(Messages.getString("NewScopedElementWizardPage.TypeNameTooltip")); //$NON-NLS-1$
		
		createSpecificControl(body);
		
		Composite publishedParent = new Composite(body, SWT.NONE);
		publishedParent.setLayout(new GridLayout(2, false));
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		publishedParent.setLayoutData(gd);
		
		mPublishedRow = new BooleanRow(publishedParent, P_PUBLISHED,
				Messages.getString("NewScopedElementWizardPage.Published")); //$NON-NLS-1$
		mPublishedRow.setFieldChangedListener(this);
		
		setPageComplete(isPageComplete());
		
		setControl(body);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		super.setVisible(visible);
	}
	
	/**
	 * @return the given data with the completed properties, <code>null</code>
	 *   if the provided data is <code>null</code>
	 */
	public UnoFactoryData fillData(UnoFactoryData data) {
		if (data != null) {
			data.setProperty(IUnoFactoryConstants.PACKAGE_NAME, getPackage());
			data.setProperty(IUnoFactoryConstants.TYPE_NAME, getElementName());
			data.setProperty(IUnoFactoryConstants.TYPE_PUBLISHED, 
					Boolean.valueOf(isPublished()));
		}
		return data;
	}
	
	public static UnoFactoryData getTypeData(UnoFactoryData data) {
		UnoFactoryData typeData = new UnoFactoryData();
		
		if (data != null) {
			try {
			String name = (String)data.getProperty(
					IUnoFactoryConstants.PROJECT_NAME);
			name = name.substring(0, 1).toUpperCase() + name.substring(1);
			name = name.replace(" ", ""); //$NON-NLS-1$ //$NON-NLS-2$
			
			String packageName = (String)data.getProperty(
					IUnoFactoryConstants.PROJECT_PREFIX);
			packageName = packageName.replace(".", "::"); //$NON-NLS-1$ //$NON-NLS-2$
			
			// put the properties in the data
			typeData.setProperty(IUnoFactoryConstants.TYPE_NAME, name);
			typeData.setProperty(IUnoFactoryConstants.PACKAGE_NAME, packageName);
			typeData.setProperty(IUnoFactoryConstants.TYPE_PUBLISHED, 
					Boolean.FALSE);
			} catch (Exception e) {
				typeData = null;
			}
		}
		return typeData;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener#fieldChanged(org.openoffice.ide.eclipse.core.gui.rows.FieldEvent)
	 */
	public void fieldChanged(FieldEvent e) {
		try {
			if (e.getProperty().equals(P_PACKAGE)) {
				// Change the label of the package row
				String text = Messages.getString("NewScopedElementWizardPage.Package")+ mUnoProject.getRootModule(); //$NON-NLS-1$
				
				if (null != e.getValue() && !e.getValue().equals("")){ //$NON-NLS-1$
					text = text + "."; //$NON-NLS-1$
				}
				mPackageRow.setLabel(text);
	
			} else if (e.getProperty().equals(P_NAME)) {
				// Test if there is the scoped name already exists
				boolean exists = UnoTypeProvider.getInstance().
					contains(e.getValue());
				if (exists) {
					setErrorMessage(Messages.getString("NewScopedElementWizardPage.NameExistsError")); //$NON-NLS-1$
				} else {
					setErrorMessage(null);
				}
				
			}
		} catch (NullPointerException ex) {
			// Nothing to do... this is sometimes normal
		}
		
	
		setPageComplete(isPageComplete());
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#isPageComplete()
	 */
	public boolean isPageComplete() {
		boolean result = true; 
		
		try {
			if (mNameRow.getValue().equals("")) { //$NON-NLS-1$
				result = false;
			}
		} catch (NullPointerException e) {
			result = false;
		}
		
		return result;
	}
}
