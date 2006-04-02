/*************************************************************************
 *
 * $RCSfile: SDKTable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:06 $
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
package org.openoffice.ide.eclipse.core.gui;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.FileRow;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.model.SDK;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * This class creates the whole SDK table with it's viewer and content provider
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class SDKTable extends AbstractTable {

	/**
	 * Temporary SDK for storing the values fetched from the dialog
	 */
	private SDK tmpsdk;
		
	/**
	 * Main constructor of the SDK Table. It's style can't be configured like other
	 * SWT composites. When using a SDK Table, you should add all the necessary Layouts
	 * and Layout Data to display it correctly. 
	 * 
	 * @param parent Composite parent of the table.
	 */
	public SDKTable(Composite parent) {
		super(parent, 
				  OOEclipsePlugin.getTranslationString(I18nConstants.SDKS_LIST),
				  new String[] {
						OOEclipsePlugin.getTranslationString(I18nConstants.BUILID),
						OOEclipsePlugin.getTranslationString(I18nConstants.SDK_PATH)
					},
				  new int[] {100, 200},
				  new String[] {
					SDK.NAME,
					SDK.PATH
			      });
			
			tableViewer.setInput(SDKContainer.getSDKContainer());
			tableViewer.setContentProvider(new SDKContentProvider());
	}
	
	/**
	 * Fill the table with the preferences from the SDKS_CONFIG file
	 */
	public void getPreferences(){
		SDKContainer.getSDKContainer();
	}
	
	public void savePreferences(){
		
		SDKContainer.getSDKContainer().saveSDKs();
	}

	protected void handleDoubleClick(DoubleClickEvent event) {
		if (!event.getSelection().isEmpty()){
			
			// Get the double clicked SDK line
			SDK sdk = (SDK)((IStructuredSelection)event.getSelection()).getFirstElement();
			
			// Launch the dialog
			sdk = openDialog(sdk, true);
			SDKContainer.getSDKContainer().updateSDK(sdk.getId(), sdk);
		}
	}
	
	protected ITableElement addLine() {
		// Launch add SDK dialog
		SDK sdk = openDialog(null, false);
		SDKContainer.getSDKContainer().addSDK(sdk);
		return sdk;
	}
	
	protected ITableElement removeLine() {
		ITableElement o = super.removeLine();
		if (null != o && o instanceof SDK) {
			SDKContainer.getSDKContainer().delSDK((SDK)o);
		}
		
		return o;
	}
	
	/**
	 * This method create and calls the dialog box to be launched on SDK edition or SDK creation.
	 * The parameter <code>sdk</code> could be null: in this case, a new one will be created. 
	 * Otherwise the fields of the old one will be changed. This is useful for SDK editing: the 
	 * object reference is the same.
	 * 
	 * @param sdk
	 * @return
	 */
	protected SDK openDialog(SDK sdk, boolean editing){
		
		// Gets the shell of the active eclipse window
		Shell shell = OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		
		SDKDialog dialog = new SDKDialog(shell, sdk);
		if (SDKDialog.OK == dialog.open()){
			// The user validates his choice, perform the changes
			SDK newSDK = tmpsdk;
			tmpsdk = null;
			
			if (null != sdk){
				// Only an existing SDK modification
				try {
					sdk.setHome(newSDK.getHome());
				} catch (InvalidConfigException e) {
					OOEclipsePlugin.logError(e.getLocalizedMessage(), e); // localized in SDK class
				}
			} else {
				// Creation of a new SDK
				
				sdk = newSDK;
			}

		}
		
		return sdk;
	}
	
	/**
	 * The SDK content provider is a class which provides the SDKs objects to the viewer
	 * 
	 * @author cbosdonnat
	 *
	 */
	class SDKContentProvider implements IStructuredContentProvider, IConfigListener {
		
		public SDKContentProvider() {
			if (null == SDKContainer.getSDKContainer()){
				SDKContainer.getSDKContainer();
			}
		}

		public Object[] getElements(Object inputElement) {
			return SDKContainer.getSDKContainer().toArray();
		}

		public void dispose() {
			SDKContainer.getSDKContainer().removeListener(this);
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (null != oldInput){
				((SDKContainer)oldInput).removeListener(this);
			}
			
			if (null != newInput){
				((SDKContainer)newInput).addListener(this);
			}
		}

		public void ConfigAdded(Object element) {
			if (element instanceof SDK){
				tableViewer.add(element);
				
				// This redrawing order is necessary to avoid having strange columns
				table.redraw();
			}
		}

		public void ConfigRemoved(Object element) {
			if (null != element && element instanceof SDK){
				// Only one SDK to remove
				tableViewer.remove(element);
			} else {
				// All the SDK have been removed
				if (null != tableViewer){
					int i = 0;
					SDK sdki = (SDK)tableViewer.getElementAt(i);
					
					while (null != sdki){
						tableViewer.remove(sdki);
					}
				}
			}
			
			// This redrawing order is necessary to avoid having strange columns
			table.redraw();
		}

		public void ConfigUpdated(Object element) {
			if (element instanceof SDK) {
				// Note that we can do this only because the SDK Container guarantees
				// that the reference of the sdk will not change during an update
				tableViewer.update(element, null);
			}
		}
	}
	
	/**
	 * Class for the SDK add/edit dialog. 
	 * 
	 * @author cbosdonnat
	 *
	 */
	class SDKDialog extends StatusDialog implements IFieldChangedListener{
		
		private static final String P_SDK_PATH    = "__sdk_path";

		private FileRow sdkpathRow;
		
		private TextRow buidlidRow; 
		
		private SDK sdk;
		
		protected SDKDialog(Shell parentShell) {
			this(parentShell, null);
		}
		
		protected SDKDialog(Shell parentShell, SDK sdk) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.sdk = sdk;
			
			setBlockOnOpen(true); // This dialog is a modal one
			setTitle(OOEclipsePlugin.getTranslationString(I18nConstants.SDK_CONFIG_DIALOG_TITLE));
		}
		
		protected Control createDialogArea(Composite parent) {
			
			Composite body = new Composite(parent, SWT.None);
			body.setLayout(new GridLayout(3, false));
			body.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			Label image = new Label(body, SWT.RIGHT);
			image.setBackground(new Color(getDisplay(), 255, 255, 255)); // White background
			image.setImage(OOEclipsePlugin.getImage(ImagesConstants.SDK_DIALOG_IMAGE));
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			image.setLayoutData(gd);
			
			// Creates each line of the dialog
			sdkpathRow = new FileRow(body, P_SDK_PATH, 
					OOEclipsePlugin.getTranslationString(I18nConstants.SDK_PATH), true);
			sdkpathRow.setFieldChangedListener(this);
			
			// put the value of the edited SDK in the fields
			if (null != sdk){
				sdkpathRow.setValue(sdk.getHome());
			}
			
			buidlidRow = new TextRow(body, "", 
					OOEclipsePlugin.getTranslationString(I18nConstants.BUILID));
			buidlidRow.setEnabled(false);   // This line is only to show the value
			
			if (null != sdk && null != sdk.getId()){
				buidlidRow.setValue(sdk.getId());
			}
			
			// activate the OK button only if the SDK is correct
			Button okButton = getButton(IDialogConstants.OK_ID);
			if (null != okButton){
				okButton.setEnabled(isValid(null));
			}
			
			return body;
		}
		
		protected void okPressed() {
			// Perform data controls on the fields: they are all mandatory
			// If there is one field missing, print an error line at the bottom
			// of the dialog.
			
			if (!sdkpathRow.getValue().equals("")) {
				isValid(null);
				super.okPressed();
			} else {
				updateStatus(new Status(Status.ERROR, 
					     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						 Status.ERROR,
						 OOEclipsePlugin.getTranslationString(I18nConstants.ALL_FIELDS_FILLED),
						 null));
			}
		}
		
		protected void cancelPressed() {
			
			super.cancelPressed();
		}

		public void fieldChanged(FieldEvent e) {
			// The result doesn't matter: we only want to update the status of the windows

			Button okButton = getButton(IDialogConstants.OK_ID);
			if (null != okButton){
				okButton.setEnabled(isValid(e.getProperty()));
			}
		}
		
		private boolean isValid(String property){
			boolean result = false;
				
			// Try to create an SDK
			try {
				tmpsdk = new SDK (sdkpathRow.getValue()); 

				if (null != tmpsdk.getId()) {
					buidlidRow.setValue(tmpsdk.getId());
				}
				
				updateStatus(new Status(Status.OK,
			    		OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
			    		Status.OK, "", null));
				
				result = true;
				
			} catch (InvalidConfigException e) {
				if (property.equals(P_SDK_PATH) && InvalidConfigException.INVALID_SDK_HOME == e.getErrorCode()){
					updateStatus(new Status(Status.ERROR, 
						     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							 Status.ERROR,
							 OOEclipsePlugin.getTranslationString(I18nConstants.INVALID_SDK_PATH),
							 e));
				} else {
					updateStatus(new Status(Status.OK,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							Status.OK,
							"",
							e));
				}
			} 
			
			return result;
		}
	}
}
