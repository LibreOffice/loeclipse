/*************************************************************************
 *
 * $RCSfile: SDKTable.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/26 21:33:42 $
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

import org.eclipse.core.runtime.Platform;
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
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.FileRow;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.model.SDK;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 *This class creates the whole SDK table with it's viewer and content provider.
 * This class encloses a SDK editor dialog.
 * 
 * @see AbstractTable for the basic table functions descriptions
 * 
 * @author cbosdonnat
 *
 */
public class SDKTable extends AbstractTable {

	/**
	 * Temporary SDK for storing the values fetched from the dialog
	 */
	private SDK mTmpSdk;
		
	/**
	 * Main constructor of the SDK Table. It's style can't be configured like
	 * other SWT composites. When using a SDK Table, you should add all the 
	 * necessary Layouts and Layout Data to display it correctly. 
	 * 
	 * @param parent Composite parent of the table.
	 */
	public SDKTable(Composite parent) {
		super(parent, 
				  Messages.getString("SDKTable.Title"), //$NON-NLS-1$
				  new String[] {
						Messages.getString("SDKTable.NameTitle"), //$NON-NLS-1$
						Messages.getString("SDKTable.PathTitle") //$NON-NLS-1$
					},
				  new int[] {100, 200},
				  new String[] {
					SDK.NAME,
					SDK.PATH
			      });
			
			mTableViewer.setInput(SDKContainer.getInstance());
			mTableViewer.setContentProvider(new SDKContentProvider());
	}
	
	/**
	 * Fill the table with the preferences from the SDKS_CONFIG file
	 */
	public void getPreferences(){
		SDKContainer.getInstance();
	}
	
	/**
	 * Saves the SDK preferences
	 *
	 */
	public void savePreferences(){
		
		SDKContainer.getInstance().saveSDKs();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#handleDoubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		if (!event.getSelection().isEmpty()){
			
			// Get the double clicked SDK line
			SDK sdk = (SDK)((IStructuredSelection)event.getSelection()).getFirstElement();
			
			// Launch the dialog
			sdk = openDialog(sdk, true);
			SDKContainer.getInstance().updateSDK(sdk.getId(), sdk);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#addLine()
	 */
	protected ITableElement addLine() {
		// Launch add SDK dialog
		SDK sdk = openDialog(null, false);
		SDKContainer.getInstance().addSDK(sdk);
		return sdk;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#removeLine()
	 */
	protected ITableElement removeLine() {
		ITableElement o = super.removeLine();
		if (null != o && o instanceof SDK) {
			SDKContainer.getInstance().delSDK((SDK)o);
		}
		
		return o;
	}
	
	/**
	 * This method create and calls the dialog box to be launched on SDK 
	 * edition or SDK creation. The parameter <code>sdk</code> could be null: 
	 * in this case, a new one will be created. Otherwise the fields of the 
	 * old one will be changed. This is useful for SDK editing: the object 
	 * reference is the same.
	 * 
	 * @param sdk the SDK instance to edit if any
	 * @param editing <code>true</code> if about to edit a SDK instance
	 * 
	 * @return the modified or created SDK instance
	 */
	protected SDK openDialog(SDK sdk, boolean editing){
		
		// Gets the shell of the active eclipse window
		Shell shell = OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		
		SDKDialog dialog = new SDKDialog(shell, sdk);
		if (SDKDialog.OK == dialog.open()){
			// The user validates his choice, perform the changes
			SDK newSDK = mTmpSdk;
			mTmpSdk = null;
			
			if (null != sdk){
				// Only an existing SDK modification
				try {
					sdk.setHome(newSDK.getHome());
				} catch (InvalidConfigException e) {
					PluginLogger.error(
							e.getLocalizedMessage(), e); 
					// localized in SDK class
				}
			} else {
				// Creation of a new SDK
				
				sdk = newSDK;
			}

		}
		
		return sdk;
	}
	
	/**
	 * The SDK content provider is a class which provides the SDKs objects to 
	 * the viewer.
	 * 
	 * @author cbosdonnat
	 *
	 */
	class SDKContentProvider implements IStructuredContentProvider, IConfigListener {
		
		public SDKContentProvider() {
			if (null == SDKContainer.getInstance()){
				SDKContainer.getInstance();
			}
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return SDKContainer.getInstance().toArray();
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			SDKContainer.getInstance().removeListener(this);
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (null != oldInput){
				((SDKContainer)oldInput).removeListener(this);
			}
			
			if (null != newInput){
				((SDKContainer)newInput).addListener(this);
			}
		}

		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.preferences.IConfigListener#ConfigAdded(java.lang.Object)
		 */
		public void ConfigAdded(Object element) {
			if (element instanceof SDK){
				mTableViewer.add(element);
				
				// This redrawing order is necessary to avoid having strange columns
				mTable.redraw();
			}
		}

		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.preferences.IConfigListener#ConfigRemoved(java.lang.Object)
		 */
		public void ConfigRemoved(Object element) {
			if (null != element && element instanceof SDK){
				// Only one SDK to remove
				mTableViewer.remove(element);
			} else {
				// All the SDK have been removed
				if (null != mTableViewer){
					int i = 0;
					SDK sdki = (SDK)mTableViewer.getElementAt(i);
					
					while (null != sdki){
						mTableViewer.remove(sdki);
					}
				}
			}
			
			// This redrawing order is necessary to avoid having strange columns
			mTable.redraw();
		}

		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.preferences.IConfigListener#ConfigUpdated(java.lang.Object)
		 */
		public void ConfigUpdated(Object element) {
			if (element instanceof SDK) {
				// Note that we can do this only because the SDK Container guarantees
				// that the reference of the sdk will not change during an update
				mTableViewer.update(element, null);
			}
		}
	}
	
	/**
	 * Class for the SDK add/edit dialog. 
	 * 
	 * @author cbosdonnat
	 */
	class SDKDialog extends StatusDialog implements IFieldChangedListener{
		
		private static final String P_SDK_PATH    = "__sdk_path"; //$NON-NLS-1$

		private FileRow mSdkpathRow;
		
		private TextRow mBuidlidRow; 
		
		private SDK mSdk;
		
		/**
		 * Create the SDK dialog without any SDK instance
		 * 
		 * @param parentShell the shell where to put the dialog 
		 */
		protected SDKDialog(Shell parentShell) {
			this(parentShell, null);
		}
		
		/**
		 * Create the SDK dialog with an SDK instance to edit
		 * 
		 * @param parentShell the shell where to put the dialog 
		 * @param sdk the SDK instance to edit
		 */
		protected SDKDialog(Shell parentShell, SDK sdk) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.mSdk = sdk;
			
			setBlockOnOpen(true); // This dialog is a modal one
			setTitle(Messages.getString("SDKTable.DialogTitle")); //$NON-NLS-1$
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
		 */
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
			mSdkpathRow = new FileRow(body, P_SDK_PATH, 
					Messages.getString("SDKTable.PathTitle"), true); //$NON-NLS-1$
			mSdkpathRow.setFieldChangedListener(this);
			
			// put the value of the edited SDK in the fields
			if (null != mSdk){
				mSdkpathRow.setValue(mSdk.getHome());
			}
			
			mBuidlidRow = new TextRow(body, "",  //$NON-NLS-1$
					Messages.getString("SDKTable.NameTitle")); //$NON-NLS-1$
			mBuidlidRow.setEnabled(false);   // This line is only to show the value
			
			if (null != mSdk && null != mSdk.getId()){
				mBuidlidRow.setValue(mSdk.getId());
			}
			
			// activate the OK button only if the SDK is correct
			Button okButton = getButton(IDialogConstants.OK_ID);
			if (null != okButton){
				okButton.setEnabled(isValid(null));
			}
			
			return body;
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
		 */
		protected void okPressed() {
			// Perform data controls on the fields: they are all mandatory
			// If there is one field missing, print an error line at the bottom
			// of the dialog.
			
			if (!mSdkpathRow.getValue().equals("")) { //$NON-NLS-1$
				isValid(null);
				super.okPressed();
			} else {
				updateStatus(new Status(Status.ERROR, 
					     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						 Status.ERROR,
						 Messages.getString("SDKTable.MissingFieldError"), //$NON-NLS-1$
						 null));
			}
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#cancelPressed()
		 */
		protected void cancelPressed() {
			
			super.cancelPressed();
		}

		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener#fieldChanged(org.openoffice.ide.eclipse.core.gui.rows.FieldEvent)
		 */
		public void fieldChanged(FieldEvent e) {
			// The result doesn't matter: we only want to update the status of the windows
			
			Button okButton = getButton(IDialogConstants.OK_ID);
			if (null != okButton){
				okButton.setEnabled(isValid(e.getProperty()));
			}
		}
		
		/**
		 * Checks if the property is valid
		 * 
		 * @param property the property to check
		 * @return <code>true</code> if the property is valid, <code>false</code>
		 * 			otherwise.
		 */
		private boolean isValid(String property){
			boolean result = false;
				
			// Try to create an SDK
			try {
				mTmpSdk = new SDK (mSdkpathRow.getValue()); 

				if (null != mTmpSdk.getId()) {
					mBuidlidRow.setValue(mTmpSdk.getId());
				}
				
				if (Platform.getOS().equals(Platform.OS_WIN32) && 
						mSdkpathRow.getValue().contains(" ")) { //$NON-NLS-1$
					
					updateStatus(new Status(Status.WARNING,
				    		OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
				    		Status.WARNING, 
				    		Messages.getString("SDKTable.SpacesSdkPathWarning"),  //$NON-NLS-1$
				    		null));
				} else {
					updateStatus(new Status(Status.OK,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							Status.OK, "", null)); //$NON-NLS-1$
				}
				
				result = true;
				
			} catch (InvalidConfigException e) {
				if (property.equals(P_SDK_PATH) && InvalidConfigException.INVALID_SDK_HOME == e.getErrorCode()){
					updateStatus(new Status(Status.ERROR, 
						     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							 Status.ERROR,
							 e.getMessage(),
							 e));
				} else {
					updateStatus(new Status(Status.OK,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							Status.OK,
							"", //$NON-NLS-1$
							e));
				}
			} 
			
			return result;
		}
	}
}
