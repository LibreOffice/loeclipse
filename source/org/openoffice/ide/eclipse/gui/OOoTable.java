/*************************************************************************
 *
 * $RCSfile: OOoTable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:28 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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
package org.openoffice.ide.eclipse.gui;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.gui.rows.FileRow;
import org.openoffice.ide.eclipse.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.gui.rows.TextRow;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.preferences.ConfigListener;
import org.openoffice.ide.eclipse.preferences.InvalidConfigException;
import org.openoffice.ide.eclipse.preferences.ooo.OOo;
import org.openoffice.ide.eclipse.preferences.ooo.OOoContainer;

/**
 * This class creates the whole SDK table with it's viewer and content provider
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class OOoTable extends Composite{

	/** Column properties */
	private static final String OOO_NAME = "OOO_NAME";
	private static final String OOO_PATH = "OOO_PATH";
	
	private TableColumn oooname;
	private TableColumn ooopath;
	
	/**
	 * Table object
	 */
	private Table table;
	
	/**
	 * Reference to the add button, used for graphical rendering
	 */
	private Button add;
	
	/**
	 * Reference to the del button, used for graphical rendering
	 */
	private Button del;
	
	/**
	 * Table Viewer object to make table access easier
	 */
	private TableViewer tableViewer;
	
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
		super(parent, SWT.NONE);
		
		OOoContainer.getOOoContainer();
		createContent();
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
	
	/**
	 * Removes all the elements of the composite
	 */
	public void dispose() {
		super.dispose();
		
		add.dispose();
		del.dispose();
		table.dispose();
	}
	
	/**
	 * Method used by the constructor to create the graphic components of the table
	 * This method could be overridden by sub classes to adapt their look.
	 *
	 */
	protected void createContent(){
		// Creates the layout of the composite with 2 columns and extended at it's maximum size
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label oooLabel = new Label(this, SWT.NONE);
		oooLabel.setText(OOEclipsePlugin.getTranslationString(
				I18nConstants.OOOS_LIST));
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		oooLabel.setLayoutData(gd);
		
		createTable();
		createTableViewer();
		createButtons();
		
		tableViewer.setInput(OOoContainer.getOOoContainer());
	}


	private void createTable() {
		// Creates a table, with a single full line selection and borders in this composite
		table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		
		// The table uses two lines of the layout because of the two buttons Add and Del
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		table.setLayoutData(gd);
		
		// Sets the graphical properties of the line
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		
		// Creates the two columns: OOo Name+Version, OOo Path
		oooname = new TableColumn(table, SWT.LEFT | SWT.RESIZE);
		oooname.setText(OOEclipsePlugin.getTranslationString(I18nConstants.NAME));
		oooname.setWidth(100); // Used to 'fix' the eclipse-GTK+ painting bug
		
		ooopath = new TableColumn(table, SWT.LEFT | SWT.RESIZE);
		ooopath.setText(OOEclipsePlugin.getTranslationString(I18nConstants.OOO_HOME_PATH));
		ooopath.setWidth(200); // Used to 'fix' the eclipse-GTK+ painting bug

	}
	
	private void createTableViewer() {
		// Creates the table viewer
		tableViewer = new TableViewer(table);
		
		// Sets the column properties to know which column is edited afterwards
		tableViewer.setColumnProperties(new String[]{
			OOO_NAME,
			OOO_PATH
		});
		
		// Manages the label to print in the cells from the model
		tableViewer.setLabelProvider(new OOoLabelProvider());
		
		tableViewer.setContentProvider(new OOoContentProvider());
		
		// Listen to a double clic to popup an edition dialog
		tableViewer.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()){
					
					// Get the double clicked OOo line
					OOo ooo = (OOo)((IStructuredSelection)event.getSelection()).getFirstElement();
					
					// Launch the dialog
					ooo = openDialog(ooo, true);
					OOoContainer.getOOoContainer().updateOOo(ooo.getId(), ooo);
				}
			}
			
		});
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
					ooo.setOOoHome(newOOo.getOOoHome());
				} catch (InvalidConfigException e) {
					OOEclipsePlugin.logError(
							e.getLocalizedMessage(), e); // localized in OOo class
				}
			} else {
				// Creation of a new OOo
				ooo = newOOo;
			}

		}
		
		return ooo;
	}
	
	private void createButtons() {
		// Creates the two buttons ADD and DEL
		add = new Button(this, SWT.NONE);
		add.setText(OOEclipsePlugin.getTranslationString(I18nConstants.ADD));
		GridData gdAdd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING |
				                      GridData.HORIZONTAL_ALIGN_FILL);
		add.setLayoutData(gdAdd);
		add.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				// Launch add OOodialog
				OOo ooo = openDialog(null, false);
				OOoContainer.getOOoContainer().addOOo(ooo);
			}
		});
		
		
		del = new Button(this, SWT.NONE);
		del.setText(OOEclipsePlugin.getTranslationString(I18nConstants.DEL));
		GridData gdDel = new GridData(GridData.VERTICAL_ALIGN_BEGINNING |
                					  GridData.HORIZONTAL_ALIGN_FILL);
		del.setLayoutData(gdDel);
		del.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection != null) {
					OOo ooo = (OOo)selection.getFirstElement();
					OOoContainer.getOOoContainer().delOOo(ooo);
				}
			}
		});
		
	}
	
	/**
	 * The OOo content provider is a class which provides the OOos objects to the viewer
	 * 
	 * @author cbosdonnat
	 *
	 */
	class OOoContentProvider implements IStructuredContentProvider, ConfigListener {
		
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
	 * Internal class used to get the label to be put in the table cell from an ooo
	 * 
	 * @author cbosdonnat
	 *
	 */
	class OOoLabelProvider extends LabelProvider implements ITableLabelProvider {

		// Aucune image pour le OOo
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			OOo ooo = (OOo)element;
			String text = new String();
			
			if (0 == columnIndex){  // name column
				text = ooo.getName()+" - "+ooo.getBuildId();
			} else if (1 == columnIndex) { // ooo path column
				text = ooo.getOOoHome();
			}
			
			return text;
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
		private TextRow buidlidRow; 
		
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
			image.setImage(OOEclipsePlugin.getImage(ImagesConstants.SDK_DIALOG_IMAGE));
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			image.setLayoutData(gd);
			
			// Creates each line of the dialog
			ooopathRow = new FileRow(body, P_OOO_PATH, 
					OOEclipsePlugin.getTranslationString(I18nConstants.OOO_HOME_PATH), true);
			ooopathRow.setFieldChangedListener(this);
			
			// put the value of the edited OOo in the fields
			if (null != ooo){
				ooopathRow.setFile(ooo.getOOoHome());
			}
			
			nameRow = new TextRow(body, "", 
					OOEclipsePlugin.getTranslationString(I18nConstants.NAME));
			nameRow.setEnabled(false);   // This line is only to show the value
			
			buidlidRow = new TextRow(body, "", 
					OOEclipsePlugin.getTranslationString(I18nConstants.BUILID));
			buidlidRow.setEnabled(false);   // This line is only to show the value
			
			if (null != ooo && null != ooo.getName() && null != ooo.getBuildId()){
				nameRow.setText(ooo.getName());
				buidlidRow.setText(ooo.getBuildId());
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
			
			if (!ooopathRow.getFile().equals("")) {
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
				tmpooo = new OOo (ooopathRow.getFile()); 

				if (null != tmpooo.getName() && null != tmpooo.getBuildId()) {
					nameRow.setText(tmpooo.getName());
					buidlidRow.setText(tmpooo.getBuildId());
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
