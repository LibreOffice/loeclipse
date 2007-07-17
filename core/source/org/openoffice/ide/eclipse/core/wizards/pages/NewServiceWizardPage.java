/*************************************************************************
 *
 * $RCSfile: NewServiceWizardPage.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/07/17 21:01:01 $
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
package org.openoffice.ide.eclipse.core.wizards.pages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.TypeRow;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.wizards.Messages;

/**
 * Service creation wizard page. This page is based on the 
 * {@link NewScopedElementWizardPage}.
 * 
 * @author cbosdonnat
 */
public class NewServiceWizardPage extends NewScopedElementWizardPage {
	
	/**
	 * Simple constructor setting the package root and element name to 
	 * blank values.
	 * 
	 * @param pageName the page name
	 * @param project the project where to create the service
	 */
	public NewServiceWizardPage(String pageName, IUnoidlProject project) {
		super(pageName, project);
	}
	
	/**
	 * Constructor setting Allowing to set custom root package and service
	 * name
	 * 
	 * @param pageName the page name
	 * @param project the project where to create the service
	 * @param aRootName the project root namespace
	 * @param aServiceName the default service name 
	 */
	public NewServiceWizardPage(String pageName, IUnoidlProject project, 
								String aRootName, String aServiceName){
		super(pageName, project, aRootName, aServiceName);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getProvidedTypes()
	 */
	public int getProvidedTypes() {
		return IUnoFactoryConstants.INTERFACE;
	}
	
	//--------------------------------------------------- Page content managment
	
	private final static String P_IFACE_INHERITANCE = "__iface_inheritance"; //$NON-NLS-1$
	
	private TypeRow mIfaceInheritanceRow;
	private String mInheritedInterface;
	
	/**
	 * Variable indicating that the inherited interface field value is being 
	 * changed by the page API. 
	 */
	private boolean mChanging = false;
	
	/**
	 * Variable indicating that the inherited interface has been changed by the
	 * user since the last definition using the page API.
	 */
	private boolean mInheritanceChanged = false;
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#createSpecificControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createSpecificControl(Composite parent) {
		
		mIfaceInheritanceRow = new TypeRow(parent, 
				P_IFACE_INHERITANCE, 
				Messages.getString("NewServiceWizardPage.InheritedInterface"), //$NON-NLS-1$
				IUnoFactoryConstants.INTERFACE);
		if (mInheritedInterface != null) {
			mIfaceInheritanceRow.setValue(mInheritedInterface);
		}
		mIfaceInheritanceRow.setFieldChangedListener(this);
		mIfaceInheritanceRow.setTooltip(Messages.getString("NewServiceWizardPage.InheritanceTooltip")); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		return Messages.getString("NewServiceWizardPage.Title"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		return Messages.getString("NewServiceWizardPage.ServiceDescription"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getTypeLabel()
	 */
	protected String getTypeLabel() {
		return Messages.getString("NewServiceWizardPage.Type"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getImageDescriptor()
	 */
	protected ImageDescriptor getImageDescriptor() {
		return OOEclipsePlugin.getImageDescriptor(
				ImagesConstants.NEW_SERVICE_IMAGE);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#fieldChanged(org.openoffice.ide.eclipse.core.gui.rows.FieldEvent)
	 */
	public void fieldChanged(FieldEvent e) {
		super.fieldChanged(e);
		
		if (e.getProperty().equals(P_IFACE_INHERITANCE) && !mChanging) {
			mInheritanceChanged = true;
		}
	}
	
	/**
	 * Gets the name of the exported interface
	 * 
	 * @return the fully qualified name of the exported interface separated with "::"  
	 */
	public String getInheritanceName() {
		return (mIfaceInheritanceRow != null) ? mIfaceInheritanceRow.getValue() : "";
	}
	
	/**
	 * Sets the name of the exported interface.
	 * 
	 * <p>Use thie method to impose the service to implement a particular
	 * interface. This is the case for an URE application.</p>
	 * 
	 * @param value the interface fully qualified name
	 * @param forced disables the field if <code>true</code>
	 */
	public void setInheritanceName(String value, boolean forced) {
		
		if (value.matches("([a-zA-Z][a-zA-Z0-9]*)(::[a-zA-Z][a-zA-Z0-9]*)*")) { //$NON-NLS-1$
			
			if (mIfaceInheritanceRow != null) {
				mChanging = true;
				
				mIfaceInheritanceRow.setValue(value);
				mIfaceInheritanceRow.setEnabled(!forced);
				mInheritanceChanged = false;
				
				mChanging = false;
			} else {
				mInheritedInterface = value;
			}
		}
	}
	
	/**
	 * Tells whether the user has changed the exported interface since it has last
	 * been set using the APIs.
	 * 
	 * @return <code>true</code> is the has changed the exported interface.
	 */
	public boolean isInheritanceChanged() {
		return mInheritanceChanged;
	}
	
	/**
	 * @return the given data with the completed properties, <code>null</code>
	 *   if the provided data is <code>null</code>
	 */
	public UnoFactoryData fillData(UnoFactoryData data) {
		
		data = super.fillData(data);
		
		if (data != null) {
			data.setProperty(IUnoFactoryConstants.TYPE_NATURE, 
					Integer.valueOf(IUnoFactoryConstants.SERVICE));
			data.setProperty(IUnoFactoryConstants.INHERITED_INTERFACES, 
					new String[]{getInheritanceName().replace(".", "::")}); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		return data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.pages.NewScopedElementWizardPage#getEmptyTypeData()
	 */
	public UnoFactoryData getEmptyTypeData() {
		UnoFactoryData typeData = new UnoFactoryData();
		
		if (typeData != null) {
			typeData.setProperty(IUnoFactoryConstants.TYPE_NATURE, 
					Integer.valueOf(IUnoFactoryConstants.SERVICE));
		}
		return typeData;
	}
}
