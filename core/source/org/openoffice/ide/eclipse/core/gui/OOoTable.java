/*************************************************************************
 *
 * $RCSfile: OOoTable.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:59 $
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
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.FileRow;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.model.AbstractOOo;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.internal.model.URE;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * This class creates the whole OOo table with it's viewer and content provider.
 * This class encloses an OOo editor dialog.
 * 
 * @see AbstractTable for the basic table functions descriptions
 * 
 * @author cbosdonnat
 *
 */
public class OOoTable extends AbstractTable {
	
	/**
	 * Temporary OOo for storing the values fetched from the dialog
	 */
	private AbstractOOo mTmpOOo;
		
	/**
	 * Main constructor of the OOo Table. It's style can't be configured like 
	 * other SWT composites. When using a OOo Table, you should add all the 
	 * necessary Layouts and Layout Data to display it correctly. 
	 * 
	 * @param parent Composite parent of the table.
	 */
	public OOoTable(Composite parent) {
		super(parent, 
			  Messages.getString("OOoTable.Title"), //$NON-NLS-1$
			  new String[] {
					Messages.getString("OOoTable.NameTitle"), //$NON-NLS-1$
					Messages.getString("OOoTable.PathTitle") //$NON-NLS-1$
				},
			  new int[] {100, 200},
			  new String[] {
				OOo.NAME,
				OOo.PATH
		      });
		
		mTableViewer.setInput(OOoContainer.getInstance());
		mTableViewer.setContentProvider(new OOoContentProvider());
	}
	
	/**
	 * Fill the table with the preferences from the OOOS_CONFIG file
	 */
	public void getPreferences(){
		OOoContainer.getInstance();
	}
	
	/**
	 * Saves the ooos in the OOOS_CONFIG file
	 * 
	 */
	public void savePreferences(){
		
		OOoContainer.getInstance().saveOOos();
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#addLine()
	 */
	protected ITableElement addLine() {
		
		AbstractOOo ooo = openDialog(null);
		OOoContainer.getInstance().addOOo(ooo);
		return ooo;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#removeLine()
	 */
	protected ITableElement removeLine() {
		
		ITableElement o = super.removeLine();
		if (null != o && o instanceof IOOo) {
			OOoContainer.getInstance().delOOo((IOOo)o);
		}
		return o;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#handleDoubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		if (!event.getSelection().isEmpty()){
			
			// Get the double clicked OOo line
			AbstractOOo ooo = (AbstractOOo)((IStructuredSelection)event.getSelection()).getFirstElement();
			
			// Launch the dialog
			ooo = openDialog(ooo);
			OOoContainer.getInstance().updateOOo(ooo.getName(), ooo);
		}
	}
	
	/**
	 * This method create and calls the dialog box to be launched on OOo edition
	 * or OOo creation. The parameter <code>ooo</code> could be null: in this 
	 * case, a new one will be created. Otherwise the fields of the old one will
	 * be changed. This is useful for OOo editing: the object reference is the
	 * same.
	 * 
	 * @param ooo the OpenOffice.org instance to show in the dialog
	 * @return the modified or created OpenOffice.org instance
	 */
	protected AbstractOOo openDialog(AbstractOOo ooo){
		
		// Gets the shell of the active eclipse window
		Shell shell = OOEclipsePlugin.getDefault().getWorkbench().
						getActiveWorkbenchWindow().getShell();
		
		OOoDialog dialog = new OOoDialog(shell, ooo);
		if (OOoDialog.OK == dialog.open()){
			// The user validates his choice, perform the changes
			AbstractOOo newOOo = mTmpOOo;
			mTmpOOo = null;
			
			if (null != ooo){
				// Only an existing OOo modification
				try {
					ooo.setHome(newOOo.getHome());
				} catch (InvalidConfigException e) {
					PluginLogger.error(
							e.getLocalizedMessage(), e); 
					// localized in OOo class
				}
			} else {
				// Creation of a new OOo
				ooo = newOOo;
			}

		}
		return ooo;
	}
	
	/**
	 * The OOo content provider is a class which provides the OOos objects to 
	 * the viewer.
	 * 
	 * @author cbosdonnat
	 *
	 */
	class OOoContentProvider implements IStructuredContentProvider, IConfigListener {
		
		/**
		 * Crates a content provider using the OOo container
		 */
		public OOoContentProvider() {
			if (null == OOoContainer.getInstance()){
				OOoContainer.getInstance();
			}
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return OOoContainer.getInstance().toArray();
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			OOoContainer.getInstance().removeListener(this);
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (null != oldInput){
				((OOoContainer)oldInput).removeListener(this);
			}
			
			if (null != newInput){
				((OOoContainer)newInput).addListener(this);
			}
		}

		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.preferences.IConfigListener#ConfigAdded(java.lang.Object)
		 */
		public void ConfigAdded(Object element) {
			if (element instanceof OOo){
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
			if (null != element && element instanceof IOOo){
				// Only one OOo to remove
				mTableViewer.remove(element);
			} else {
				// All the OOo have been removed
				if (null != mTableViewer){
					int i = 0;
					IOOo oooi = (IOOo)mTableViewer.getElementAt(i);
					
					while (null != oooi){
						mTableViewer.remove(oooi);
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
			if (element instanceof OOo) {
				// Note that we can do this only because the OOo Container guarantees
				// that the reference of the ooo will not change during an update
				mTableViewer.update(element, null);
			}
		}
	}
	
	/**
	 * Class for the OOo add/edit dialog. 
	 * 
	 * @author cbosdonnat
	 */
	class OOoDialog extends StatusDialog implements IFieldChangedListener{
		
		private static final String P_OOO_PATH    = "__ooo_path"; //$NON-NLS-1$
		private static final String P_OOO_NAME    = "__ooo_name"; //$NON-NLS-1$

		private FileRow mOOopathRow;
		
		private TextRow mNameRow;
		
		private AbstractOOo mOOo;
		
		/**
		 * Create the OOo dialog without any OOo instance
		 * 
		 * @param parentShell the shell where to put the dialog 
		 */
		protected OOoDialog(Shell parentShell) {
			this(parentShell, null);
		}
		
		/**
		 * Create the OOo dialog with an OOo instance to edit
		 * 
		 * @param parentShell the shell where to put the dialog 
		 * @param ooo the OOo instance to edit
		 */
		protected OOoDialog(Shell parentShell, AbstractOOo ooo) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.mOOo = ooo;
			
			setBlockOnOpen(true); // This dialog is a modal one
			setTitle(Messages.getString("OOoTable.DialogTitle")); //$NON-NLS-1$
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
			image.setImage(OOEclipsePlugin.getImage(ImagesConstants.OOO_DIALOG_IMAGE));
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			image.setLayoutData(gd);
			
			// Creates each line of the dialog
			mOOopathRow = new FileRow(body, P_OOO_PATH, 
					Messages.getString("OOoTable.PathTitle"), true); //$NON-NLS-1$
			mOOopathRow.setFieldChangedListener(this);
			
			// put the value of the edited OOo in the fields
			if (null != mOOo){
				mOOopathRow.setValue(mOOo.getHome());
			}
			
			mNameRow = new TextRow(body, P_OOO_NAME, 
					Messages.getString("OOoTable.NameTitle")); //$NON-NLS-1$
			mNameRow.setFieldChangedListener(this);
			
			if (null != mOOo && null != mOOo.getName()) {
				mNameRow.setValue(mOOo.getName());
				mNameRow.setEnabled(false);
			}
			
			// activate the OK button only if the OOo is correct
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
			
			if (!mOOopathRow.getValue().equals("")) { //$NON-NLS-1$
				isValid(null);
				super.okPressed();
			} else {
				updateStatus(new Status(Status.ERROR, 
					     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						 Status.ERROR,
						 Messages.getString("OOoTable.MissingFieldError"), //$NON-NLS-1$
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
				
				if (e.getProperty().equals(P_OOO_PATH)) { 
					okButton.setEnabled(isValid(e.getProperty()));
				}
				
				// checks if the name is unique and toggle a warning
				if (e.getProperty().equals(P_OOO_NAME)) {
					boolean unique = !OOoContainer.getInstance().
						containsName(e.getValue());
					
					if (unique) {
						updateStatus(new Status(Status.OK,
					    		OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					    		Status.OK, "", null)); //$NON-NLS-1$
					} else {
						updateStatus(new Status(Status.WARNING, 
							     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
								 Status.WARNING,
								 Messages.getString("OOoTable.NameExistsError"), //$NON-NLS-1$
								 null));
					}
				}
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
				
			// Try to create an OOo
			try {
				mTmpOOo = new OOo (mOOopathRow.getValue(), mNameRow.getValue()); 

				if (null != mTmpOOo.getName()) {
					mNameRow.setValue(mTmpOOo.getName());
				}
				
				updateStatus(new Status(Status.OK,
			    		OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
			    		Status.OK, "", null)); //$NON-NLS-1$
				
				result = true;
				
			} catch (InvalidConfigException e) {

				try {
					
					mTmpOOo = new URE(mOOopathRow.getValue(), mNameRow.getValue());
					if (null != mTmpOOo.getName()) {
						mNameRow.setValue(mTmpOOo.getName());
					}
					
					updateStatus(new Status(Status.OK,
				    		OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
				    		Status.OK, "", null)); //$NON-NLS-1$
					
					result = true;
					
				} catch (InvalidConfigException ex) {
					updateStatus(new Status(Status.ERROR, 
						     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							 Status.ERROR,
							 Messages.getString("OOoTable.InvalidPathError"), //$NON-NLS-1$
							 ex));
				}
			} 
			
			return result;
		}
	}
}
