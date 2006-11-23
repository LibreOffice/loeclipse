/*************************************************************************
 *
 * $RCSfile: NewInterfaceWizardPage.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/23 18:27:17 $
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

import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.ITableElement;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;

public class NewInterfaceWizardPage extends NewScopedElementWizardPage 
									implements ISelectionChangedListener{

	public NewInterfaceWizardPage(String pageName, IUnoidlProject unoProject) {
		super(pageName, unoProject);
	}

	public NewInterfaceWizardPage(String pageName, IUnoidlProject project,
			String aRootName, String aElementName) {
		super(pageName, project, aRootName, aElementName);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		
		mInterfaceInheritances.removeSelectionChangedListener(this);
		mInterfaceInheritances = null;
		
		super.dispose();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getProvidedTypes()
	 */
	public int getProvidedTypes() {
		return IUnoFactoryConstants.INTERFACE;
	}

	
	//--------------------------------------------------- Page content managment

	private InterfacesTable mInterfaceInheritances;
	private InterfaceMembersTable mMembers;
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#createSpecificControl(org.eclipse.swt.widgets.Composite)
	 */
	protected void createSpecificControl(Composite parent) {
		
		// Pour avoir des tailles des tables correctes
		Point point = getShell().getSize();
		point.y = Math.max(point.y, 600);
		point.x = Math.min(point.x, 600);
		getShell().setSize(point);
		
		
		Composite tableParent = new Composite(parent, SWT.NORMAL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		tableParent.setLayoutData(gd);
		tableParent.setLayout(new GridLayout(1, false));
		
		UnoTypeProvider.getInstance().initialize(mUnoProject, 
				IUnoFactoryConstants.INTERFACE);
		
		mInterfaceInheritances = new InterfacesTable(tableParent);
		mInterfaceInheritances.setToolTipText(Messages.getString("NewInterfaceWizardPage.InheritancesTableTooltip")); //$NON-NLS-1$
		mInterfaceInheritances.addSelectionChangedListener(this);
		
		mMembers = new InterfaceMembersTable(tableParent);
		mMembers.setToolTipText(Messages.getString("NewInterfaceWizardPage.MembersTableTooltip")); //$NON-NLS-1$
		mMembers.addSelectionChangedListener(this);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		return Messages.getString("NewInterfaceWizardPage.Title"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		return Messages.getString("NewInterfaceWizardPage.InterfaceDescription"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getTypeLabel()
	 */
	protected String getTypeLabel() {
		return Messages.getString("NewInterfaceWizardPage.Label"); //$NON-NLS-1$
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.wizards.NewScopedElementWizardPage#getImageDescriptor()
	 */
	protected ImageDescriptor getImageDescriptor() {
		return OOEclipsePlugin.getImageDescriptor(
				ImagesConstants.NEW_INTERFACE_IMAGE);
	}

	/*
	 * When such an event is catch, this method reevaluate the page completeness
	 * 
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		setPageComplete(isPageComplete());
	}
	
	/**
	 * @return the given data with the completed properties, <code>null</code>
	 *   if the provided data is <code>null</code>
	 */
	public UnoFactoryData fillData(UnoFactoryData data) {
		data = super.fillData(data);
		if (data != null) {
			data.setProperty(IUnoFactoryConstants.TYPE_NATURE, 
					Integer.valueOf(IUnoFactoryConstants.INTERFACE));
			
			// Vector containing the interface inheritance paths "::" separated
			Vector<String> optionalIntf = new Vector<String>();
			Vector<String> mandatoryIntf = new Vector<String>();
			
			// Separate the optional and mandatory interface inheritances			
			Vector<ITableElement> lines = mInterfaceInheritances.getLines();
			for (ITableElement linei : lines) {
				InterfacesTable.InheritanceLine line = 
					(InterfacesTable.InheritanceLine)linei;
				
				if (line.isOptional()) {
					optionalIntf.add(line.getInterfaceName().replace(".", "::")); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					mandatoryIntf.add(line.getInterfaceName().replace(".", "::")); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
			lines.clear();
			
			// Get the mandatory inheritances
			String[] interfaces = new String[mandatoryIntf.size()];
			interfaces = mandatoryIntf.toArray(interfaces);
			data.setProperty(IUnoFactoryConstants.INHERITED_INTERFACES, 
					interfaces);
			
			// Get the optional inheritances
			String[] opt_interfaces = new String[optionalIntf.size()];
			opt_interfaces = optionalIntf.toArray(opt_interfaces);
			data.setProperty(IUnoFactoryConstants.OPT_INHERITED_INTERFACES, 
					opt_interfaces);
			
			optionalIntf.clear();
			mandatoryIntf.clear();
			
			// Get the interface members data
			UnoFactoryData[] membersData = mMembers.getUnoFactoryData();
			for (UnoFactoryData member : membersData) {
				data.addInnerData(member);
			}
		}
		return data;
	}
}
