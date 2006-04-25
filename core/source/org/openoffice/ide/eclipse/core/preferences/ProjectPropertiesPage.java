/*************************************************************************
 *
 * $RCSfile: ProjectPropertiesPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:10:01 $
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
package org.openoffice.ide.eclipse.core.preferences;

import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.OOoTable;
import org.openoffice.ide.eclipse.core.gui.SDKTable;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;

public class ProjectPropertiesPage extends PropertyPage 
								   implements IWorkbenchPropertyPage, 
								   		   IConfigListener {

	private UnoidlProject project;
	
	public ProjectPropertiesPage() {
		super();
		
		noDefaultAndApplyButton();
		SDKContainer.getSDKContainer().addListener(this);
		OOoContainer.getOOoContainer().addListener(this);
	}
	
	public void dispose() {
		
		SDKContainer.getSDKContainer().removeListener(this);
		OOoContainer.getOOoContainer().removeListener(this);
		
		super.dispose();
	}

	//------------------------------------------------------- Content managment
	
	private ChoiceRow sdkRow;
	private ChoiceRow oooRow;
	
	private static final String SDK = "__sdk";
	private static final String OOO = "__ooo";
	
	
	public void setElement(IAdaptable element) {
		super.setElement(element);
		
		try {
			project = (UnoidlProject)((IProject)getElement()).
								getNature(OOEclipsePlugin.UNO_NATURE_ID);
			
		} catch (CoreException e) {
			PluginLogger.getInstance().debug(e.getMessage());
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(3, false));
		
		// Add the SDK choice field
		sdkRow = new ChoiceRow(body, SDK,
						OOEclipsePlugin.getTranslationString(I18nConstants.USED_SDK),
						OOEclipsePlugin.getTranslationString(I18nConstants.SDKS));
		sdkRow.setBrowseSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				// Open the SDK Configuration page
				TableDialog dialog = new TableDialog(getShell(), true);
				dialog.create();
				dialog.open();
				
			}
		});
		
		fillSDKRow();
		
		
		// Add the OOo choice field
		oooRow = new ChoiceRow(body, OOO,
						OOEclipsePlugin.getTranslationString(I18nConstants.USED_OOO),
						OOEclipsePlugin.getTranslationString(I18nConstants.OOOS));
		oooRow.setBrowseSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				// Open the OOo Configuration page
				TableDialog dialog = new TableDialog(getShell(), false);
				dialog.create();
				dialog.open();
			}
		});
		
		fillOOoRow();
		
		return body;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		saveValues();
		return true;
	}
	
	private void saveValues(){
		if (!sdkRow.getValue().equals("")) {
			ISdk sdk = SDKContainer.getSDKContainer().getSDK(sdkRow.getValue());
			project.setSdk(sdk);
		}
		
		if (!oooRow.getValue().equals("")){
			IOOo ooo = OOoContainer.getOOoContainer().getOOo(oooRow.getValue());
			project.setOOo(ooo);
		}
	}
	
	private void fillSDKRow (){
		
		if (null != sdkRow){
			// Adding the SDK names to the combo box 
			SDKContainer sdkContainer = SDKContainer.getSDKContainer();
			String[] sdks = new String[sdkContainer.getSDKCount()];
			Vector sdkKeys = sdkContainer.getSDKKeys();
			for (int i=0, length=sdkContainer.getSDKCount(); i<length; i++){
				sdks[i] = (String)sdkKeys.get(i);
			}
			
			sdkRow.removeAll();
			sdkRow.addAll(sdks);
			
			if (null != project.getSdk()){
				sdkRow.select(project.getSdk().getId());
			} else {
				sdkRow.select(0);
			}
		}
	}

	private void fillOOoRow(){
		
		if (null != oooRow){
			
			// Adding the OOo names to the combo box 
			OOoContainer oooContainer = OOoContainer.getOOoContainer();
			String[] ooos = new String[oooContainer.getOOoCount()];
			Vector oooKeys = oooContainer.getOOoKeys();
			for (int i=0, length=oooContainer.getOOoCount(); i<length; i++){
				ooos[i] = (String)oooKeys.get(i);
			}
			
			oooRow.removeAll();
			oooRow.addAll(ooos);
			if (null != project.getOOo()){
				oooRow.select(project.getOOo().getId());
			} else {
				oooRow.select(0);
			}
		}
	}
	
	private class TableDialog extends Dialog {
		
		private boolean editSDK = true;
		
		private Object table;
		
		TableDialog (Shell parentShell, boolean editSDK){
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.editSDK = editSDK;
			
			setBlockOnOpen(true); // This dialog is a modal one
			if (editSDK) {
				setTitle(OOEclipsePlugin.getTranslationString(I18nConstants.SDKS));
			} else {
				setTitle(OOEclipsePlugin.getTranslationString(I18nConstants.OOOS));
			}
		}
		
		protected Control createDialogArea(Composite parent) {
			
			if (editSDK){
				table = new SDKTable(parent);
				((SDKTable)table).getPreferences();
			} else {
				table = new OOoTable(parent);
				((OOoTable)table).getPreferences();
			}
				
			return parent;
		}
		
		protected void okPressed() {
			super.okPressed();
			
			if (editSDK){
				((SDKTable)table).savePreferences();
			} else {
				((OOoTable)table).savePreferences();
			}
		}
	}

	//-----------------------------------------Implementation of ConfigListener
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ConfigListener#ConfigAdded(java.lang.Object)
	 */
	public void ConfigAdded(Object element) {
		fillSDKRow();
		fillOOoRow();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ConfigListener#ConfigRemoved(java.lang.Object)
	 */
	public void ConfigRemoved(Object element) {
		fillSDKRow();
		fillOOoRow();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.preferences.ConfigListener#ConfigUpdated(java.lang.Object)
	 */
	public void ConfigUpdated(Object element) {
		fillSDKRow();
		fillOOoRow();
	}	

}
