/*************************************************************************
 *
 * $RCSfile: OOoTable.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/25 19:10:04 $
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
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.preferences.IConfigListener;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * This class creates the whole SDK table with it's viewer and content provider
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class OOoTable extends AbstractTable {
	
	/**
	 * Temporary OOo for storing the values fetched from the dialog
	 */
	private OOo tmpooo;
		
	/**
	 * Main constructor of the OOo Table. It's style can't be configured like other
	 * SWT composites. When using a OOo Table, you should add all the necessary Layouts
	 * and Layout Data to display it correctly. 
	 * 
	 * @param parent Composite parent of the table.
	 */
	public OOoTable(Composite parent) {
		super(parent, 
			  OOEclipsePlugin.getTranslationString(I18nConstants.OOOS_LIST),
			  new String[] {
					OOEclipsePlugin.getTranslationString(I18nConstants.NAME),
					OOEclipsePlugin.getTranslationString(I18nConstants.OOO_HOME_PATH)
				},
			  new int[] {100, 200},
			  new String[] {
				OOo.NAME,
				OOo.PATH
		      });
		
		tableViewer.setInput(OOoContainer.getOOoContainer());
		tableViewer.setContentProvider(new OOoContentProvider());
	}
	
	/**
	 * Fill the table with the preferences from the OOOS_CONFIG file
	 */
	public void getPreferences(){
		OOoContainer.getOOoContainer();
	}
	
	/**
	 * Saves the ooos in the OOOS_CONFIG file
	 * 
	 */
	public void savePreferences(){
		
		OOoContainer.getOOoContainer().saveOOos();
	}
	
	protected ITableElement addLine() {
		
		OOo ooo = openDialog(null, false);
		OOoContainer.getOOoContainer().addOOo(ooo);
		return ooo;
	}
	
	protected ITableElement removeLine() {
		
		ITableElement o = super.removeLine();
		if (null != o && o instanceof OOo) {
			OOoContainer.getOOoContainer().delOOo((OOo)o);
		}
		return o;
	}
	
	protected void handleDoubleClick(DoubleClickEvent event) {
		if (!event.getSelection().isEmpty()){
			
			// Get the double clicked OOo line
			OOo ooo = (OOo)((IStructuredSelection)event.getSelection()).getFirstElement();
			
			// Launch the dialog
			ooo = openDialog(ooo, true);
			OOoContainer.getOOoContainer().updateOOo(ooo.getId(), ooo);
		}
	}
	
	/**
	 * This method create and calls the dialog box to be launched on OOo edition or OOo creation.
	 * The parameter <code>ooo</code> could be null: in this case, a new one will be created. 
	 * Otherwise the fields of the old one will be changed. This is useful for OOo editing: the 
	 * object reference is the same.
	 * 
	 * @param ooo
	 * @return
	 */
	protected OOo openDialog(OOo ooo, boolean editing){
		
		// Gets the shell of the active eclipse window
		Shell shell = OOEclipsePlugin.getDefault().getWorkbench().
						getActiveWorkbenchWindow().getShell();
		
		OOoDialog dialog = new OOoDialog(shell, ooo);
		if (OOoDialog.OK == dialog.open()){
			// The user validates his choice, perform the changes
			OOo newOOo = tmpooo;
			tmpooo = null;
			
			if (null != ooo){
				// Only an existing OOo modification
				try {
					ooo.setHome(newOOo.getHome());
				} catch (InvalidConfigException e) {
					PluginLogger.getInstance().error(
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
	 * The OOo content provider is a class which provides the OOos objects to the viewer
	 * 
	 * @author cbosdonnat
	 *
	 */
	class OOoContentProvider implements IStructuredContentProvider, IConfigListener {
		
		public OOoContentProvider() {
			if (null == OOoContainer.getOOoContainer()){
				OOoContainer.getOOoContainer();
			}
		}

		public Object[] getElements(Object inputElement) {
			return OOoContainer.getOOoContainer().toArray();
		}

		public void dispose() {
			OOoContainer.getOOoContainer().removeListener(this);
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (null != oldInput){
				((OOoContainer)oldInput).removeListener(this);
			}
			
			if (null != newInput){
				((OOoContainer)newInput).addListener(this);
			}
		}

		public void ConfigAdded(Object element) {
			if (element instanceof OOo){
				tableViewer.add(element);
				
				// This redrawing order is necessary to avoid having strange columns
				table.redraw();
			}
		}

		public void ConfigRemoved(Object element) {
			if (null != element && element instanceof OOo){
				// Only one OOo to remove
				tableViewer.remove(element);
			} else {
				// All the OOo have been removed
				if (null != tableViewer){
					int i = 0;
					OOo oooi = (OOo)tableViewer.getElementAt(i);
					
					while (null != oooi){
						tableViewer.remove(oooi);
					}
				}
			}
			
			// This redrawing order is necessary to avoid having strange columns
			table.redraw();
		}

		public void ConfigUpdated(Object element) {
			if (element instanceof OOo) {
				// Note that we can do this only because the OOo Container guarantees
				// that the reference of the ooo will not change during an update
				tableViewer.update(element, null);
			}
		}
	}
	
	/**
	 * Class for the OOo add/edit dialog. 
	 * 
	 * @author cbosdonnat
	 *
	 */
	class OOoDialog extends StatusDialog implements IFieldChangedListener{
		
		private static final String P_OOO_PATH    = "__ooo_path";

		private FileRow ooopathRow;
		
		private TextRow nameRow;
		
		private OOo ooo;
		
		protected OOoDialog(Shell parentShell) {
			this(parentShell, null);
		}
		
		protected OOoDialog(Shell parentShell, OOo ooo) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			this.ooo = ooo;
			
			setBlockOnOpen(true); // This dialog is a modal one
			setTitle(OOEclipsePlugin.getTranslationString(
					I18nConstants.OOO_CONFIG_DIALOG_TITLE));
		}
		
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
			ooopathRow = new FileRow(body, P_OOO_PATH, 
					OOEclipsePlugin.getTranslationString(I18nConstants.OOO_HOME_PATH), true);
			ooopathRow.setFieldChangedListener(this);
			
			// put the value of the edited OOo in the fields
			if (null != ooo){
				ooopathRow.setValue(ooo.getHome());
			}
			
			nameRow = new TextRow(body, "", 
					OOEclipsePlugin.getTranslationString(I18nConstants.NAME));
			nameRow.setEnabled(false);   // This line is only to show the value
			
			if (null != ooo && null != ooo.getName()) {
				nameRow.setValue(ooo.getName());
			}
			
			// activate the OK button only if the OOo is correct
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
			
			if (!ooopathRow.getValue().equals("")) {
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
				
			// Try to create an OOo
			try {
				tmpooo = new OOo (ooopathRow.getValue()); 

				if (null != tmpooo.getName()) {
					nameRow.setValue(tmpooo.getName());
				}
				
				updateStatus(new Status(Status.OK,
			    		OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
			    		Status.OK, "", null));
				
				result = true;
				
			} catch (InvalidConfigException e) {
				if (property.equals(P_OOO_PATH) && 
						InvalidConfigException.INVALID_OOO_HOME == e.getErrorCode()){
					
					updateStatus(new Status(Status.ERROR, 
						     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							 Status.ERROR,
							 OOEclipsePlugin.getTranslationString(I18nConstants.INVALID_OOO_PATH),
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
