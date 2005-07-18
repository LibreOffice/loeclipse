/*************************************************************************
 *
 * $RCSfile: SDKTable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/18 19:36:06 $
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.runtime.Status;
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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
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
import org.openoffice.ide.eclipse.i18n.Translator;
import org.openoffice.ide.eclipse.preferences.sdk.SDK;
import org.openoffice.ide.eclipse.preferences.sdk.SDKContainer;
import org.openoffice.ide.eclipse.preferences.sdk.SDKListener;

/**
 * This class creates the whole SDK table with it's viewer and content provider
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class SDKTable extends Composite{

	/** Column properties */
	private static final String SDK_NAME = "SDK_NAME";
	private static final String SDK_PATH = "SDK_PATH";
	private static final String OOO_PATH = "OOO_PATH";
	
	private TableColumn sdkname;
	private TableColumn sdkpath;
	private TableColumn ooopath;
	
	/**
	 * Model of the table
	 */
	private SDKContainer sdks = new SDKContainer();
	
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
	 * Temporary SDK for storing the values fetched from the dialog
	 */
	private SDK tmpsdk;
	
	/**
	 * Plugin home relative path for the sdks configuration file
	 */
	private final static String SDKS_CONFIG = ".sdks_config";
		
	/**
	 * Main constructor of the SDK Table. It's style can't be configured like other
	 * SWT composites. When using a SDK Table, you should add all the necessary Layouts
	 * and Layout Data to display it correctly. 
	 * 
	 * @param parent Composite parent of the table.
	 */
	public SDKTable(Composite parent) {
		super(parent, SWT.NONE);
		createContent();
	}
	
	/**
	 * Fill the table with the preferences from the SDKS_CONFIG file
	 */
	public void getPreferences(){
		
		try {
			// Loads the sdks config file into a properties object
			String sdks_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
			File file = new File(sdks_config_url+"/"+SDKS_CONFIG);
			if (!file.exists()){
				file.createNewFile();
			}
			
			Properties sdksProperties = new Properties();
		
			sdksProperties.load(new FileInputStream(file));
			
			int i=0;
			boolean found = false;
			
			do {
				String name = sdksProperties.getProperty(OOEclipsePlugin.SDKNAME_PREFERENCE_KEY+i);
				String version = sdksProperties.getProperty(OOEclipsePlugin.SDKVERSION_PREFERENCE_KEY+i);
				String path = sdksProperties.getProperty(OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+i);
				String ooopath = sdksProperties.getProperty(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+i);
				
				found = !(null == name || null == version || null == path || null == ooopath);
				i++;
				
				if (found){
					SDK sdk = new SDK(name, version, path, ooopath);
					sdks.addSDK(sdk);
				}				
			} while (found);
			
		} catch (IOException e) {
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e); // TODO i18n
		}
	}
	
	/**
	 * Saves the sdks in the SDKS_CONFIG file
	 * 
	 */
	public void savePreferences(){
		
		Properties sdksProperties = new Properties();
				
		// Saving the new SDKs 
		for (int j=0, length=sdks.getSDKCount(); j<length; j++){
			SDK sdkj = sdks.getSDK(j);
			sdksProperties.put(OOEclipsePlugin.SDKNAME_PREFERENCE_KEY+j, sdkj.name);
			sdksProperties.put(OOEclipsePlugin.SDKVERSION_PREFERENCE_KEY+j, sdkj.version);
			sdksProperties.put(OOEclipsePlugin.SDKPATH_PREFERENCE_KEY+j, sdkj.path);
			sdksProperties.put(OOEclipsePlugin.OOOPATH_PREFERENCE_KEY+j, sdkj.oooProgramPath);
		}
		
		try {
			String sdks_config_url = OOEclipsePlugin.getDefault().getStateLocation().toString();
			File file = new File(sdks_config_url+"/"+SDKS_CONFIG);
			if (!file.exists()){
				file.createNewFile();
			}
			
			sdksProperties.store(new FileOutputStream(file), "");
		} catch (FileNotFoundException e) {
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		} catch (IOException e){
			OOEclipsePlugin.logError(e.getLocalizedMessage(), e);
		}
	}
	
	/**
	 * Removes all the elements of the composite
	 */
	public void dispose() {
		super.dispose();
		
		add.dispose();
		del.dispose();
		table.dispose();
		sdks.dispose();
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
		
		createTable();
		createTableViewer();
		createButtons();
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
		
		// Creates the three columns: SDK Name+Version, SDK Path, OOo Program path
		sdkname = new TableColumn(table, SWT.LEFT);
		sdkname.setText(OOEclipsePlugin.getTranslationString(I18nConstants.SDK_NAME));
		sdkname.setResizable(false);
		sdkname.setWidth(50); // Used to 'fix' the eclipse-GTK+ painting bug
		
		sdkpath = new TableColumn(table, SWT.LEFT);
		sdkpath.setText(OOEclipsePlugin.getTranslationString(I18nConstants.SDK_PATH));
		sdkpath.setResizable(false);
		sdkpath.setWidth(100); // Used to 'fix' the eclipse-GTK+ painting bug
		
		ooopath = new TableColumn(table, SWT.LEFT); 
		ooopath.setText(OOEclipsePlugin.getTranslationString(I18nConstants.OOO_PROGRAM_PATH));
		ooopath.setResizable(false);
		ooopath.setWidth(100); // Used to 'fix' the eclipse-GTK+ painting bug
		
		// TODO Bug found: works on Win32 platform, however, there is no event
		//      Recieved on a Linux one.
		
		// Add a listener for each painting to get the computed width of the table
		// and thus resize all the columns at a constant rate
		table.addPaintListener(new PaintListener(){
			
			// This boolean avoid the paint listener to be recalled 
			// after the width changed. Indeed the setWidth let the table be
			// repainted and thus there is a infinite loop. This simple
			// mechanism is made to avoid the loop after the width change.
			private boolean changeWidth = false;
			
			public void paintControl(PaintEvent e) {
				// If the width aren't changing, change them.
				if (!changeWidth){
					changeWidth = true; 
					
					int width = e.width;
					sdkname.setWidth((int)(0.2*width));
					sdkpath.setWidth((int)(0.4*width));
					ooopath.setWidth((int)(0.4*width));
					
					changeWidth = false;
				}
			};
		});
	}
	
	private void createTableViewer() {
		// Creates the table viewer
		tableViewer = new TableViewer(table);
		
		// Sets the column properties to know which column is edited afterwards
		tableViewer.setColumnProperties(new String[]{
			SDK_NAME,
			SDK_PATH,
			OOO_PATH
		});
		
		// Manages the label to print in the cells from the model
		tableViewer.setLabelProvider(new SDKLabelProvider());
		
		tableViewer.setContentProvider(new SDKContentProvider());
		
		// Listen to a double clic to popup an edition dialog
		tableViewer.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event) {
				if (!event.getSelection().isEmpty()){
					
					// Get the double clicked SDK line
					SDK sdk = (SDK)((IStructuredSelection)event.getSelection()).getFirstElement();
					
					// fetches the sdk position for later update
					int i = sdks.indexOf(sdk);
					
					// Launch the dialog
					sdk = openDialog(sdk);
					sdks.updateSDK(i, sdk);
				}
			}
			
		});
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
	protected SDK openDialog(SDK sdk){
		
		// Gets the shell of the active eclipse window
		Shell shell = OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();
		
		SDKDialog dialog = new SDKDialog(shell, sdk);
		if (SDKDialog.OK == dialog.open()){
			// The user validates his choice, perform the changes
			SDK newSDK = tmpsdk;
			tmpsdk = null;
			
			if (null != sdk){
				// Only an existing SDK modification
				
				sdk.name = newSDK.name;
				sdk.version = newSDK.version;
				sdk.path = newSDK.path;
				sdk.oooProgramPath = newSDK.oooProgramPath;
			} else {
				// Creation of a new SDK
				
				sdk = newSDK;
			}

		}
		
		return sdk;
	}
	
	private void createButtons() {
		// Creates the two buttons ADD and DEL
		Translator translator = OOEclipsePlugin.getDefault().getTranslator();
		add = new Button(this, SWT.FLAT);
		add.setText(translator.getString(I18nConstants.ADD));
		GridData gdAdd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING |
				                      GridData.HORIZONTAL_ALIGN_FILL);
		add.setLayoutData(gdAdd);
		add.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				// Launch add SDK dialog
				SDK sdk = openDialog(null);
				sdks.addSDK(sdk);
			}
		});
		
		
		del = new Button(this, SWT.FLAT);
		del.setText(translator.getString(I18nConstants.DEL));
		GridData gdDel = new GridData(GridData.VERTICAL_ALIGN_BEGINNING |
                					  GridData.HORIZONTAL_ALIGN_FILL);
		del.setLayoutData(gdDel);
		del.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
				if (selection != null) {
					SDK sdk = (SDK)selection.getFirstElement();
					sdks.delSDK(sdk);
				}
			}
		});
		
	}
	
	/**
	 * The SDK content provider is a class which provides the SDKs objects to the viewer
	 * 
	 * @author cbosdonnat
	 *
	 */
	class SDKContentProvider implements IStructuredContentProvider, SDKListener {
		
		public SDKContentProvider() {
			sdks.addListener(this);
		}

		public Object[] getElements(Object inputElement) {
			return sdks.toArray();
		}

		public void dispose() {
			sdks.removeListener(this);
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (null != oldInput){
				((SDKContainer)oldInput).removeListener(this);
			}
			
			if (null != newInput){
				((SDKContainer)newInput).addListener(this);
			}
		}

		public void SDKAdded(SDK sdk) {
			tableViewer.add(sdk);
			
			// This redrawing order is necessary to avoid having strange columns
			table.redraw();
		}

		public void SDKRemoved(SDK sdk) {
			if (null != sdk){
				// Only one SDK to remove
				tableViewer.remove(sdk);
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

		public void SDKUpdated(int i, SDK sdk) {
			SDK oldSDK = (SDK)tableViewer.getElementAt(i);
			if (null != oldSDK){
				
				oldSDK.name = sdk.name;
				oldSDK.version = sdk.version;
				oldSDK.path = sdk.path;
				oldSDK.oooProgramPath = sdk.oooProgramPath;
				
				tableViewer.update(oldSDK, null);
			}
		}
	}
	
	/**
	 * Internal class used to get the label to be put in the table cell from an sdk
	 * 
	 * @author cbosdonnat
	 *
	 */
	class SDKLabelProvider extends LabelProvider implements ITableLabelProvider {

		// Aucune image pour le SDK
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			SDK sdk = (SDK)element;
			String text = new String();
			
			if (0 == columnIndex){  // name column
				text = sdk.name+"-"+sdk.version;
			} else if (1 == columnIndex) { // sdk path column
				text = sdk.path;
			} else if (2 == columnIndex) { // ooo path column
				text = sdk.oooProgramPath;
			}
			
			return text;
		}	
	}
	
	/**
	 * Class for the SDK add/edit dialog. 
	 * 
	 * @author cbosdonnat
	 *
	 */
	class SDKDialog extends StatusDialog implements IFieldChangedListener{
		
		private static final String P_SDK_NAME    = "__sdk_name";
		private static final String P_SDK_VERSION = "__sdk_version";
		private static final String P_SDK_PATH    = "__sdk_path";
		private static final String P_OOO_PATH    = "__ooo_path";

		private TextRow sdknameRow;
		private TextRow sdkversionRow;
		private FileRow sdkpathRow;
		private FileRow ooopathRow;
		
		private SDK sdk;
		
		protected SDKDialog(Shell parentShell) {
			this(parentShell, null);
		}
		
		protected SDKDialog(Shell parentShell, SDK sdk) {
			super(parentShell);
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
			image.setImage(OOEclipsePlugin.getImageDescriptor("icons/OOoSDK.png").createImage());
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			image.setLayoutData(gd);
			
			// Creates each line of the dialog
			sdknameRow = new TextRow(body, P_SDK_NAME, 
					OOEclipsePlugin.getTranslationString(I18nConstants.SDK_NAME));
			sdknameRow.setFieldChangedListener(this);
			
			sdkversionRow = new TextRow(body, P_SDK_VERSION, 
					OOEclipsePlugin.getTranslationString(I18nConstants.SDK_VERSION));
			sdkversionRow.setFieldChangedListener(this);
			
			sdkpathRow = new FileRow(body, P_SDK_PATH, 
					OOEclipsePlugin.getTranslationString(I18nConstants.SDK_PATH), true);
			sdkpathRow.setFieldChangedListener(this);
			
			ooopathRow = new FileRow(body, P_OOO_PATH,
					OOEclipsePlugin.getTranslationString(I18nConstants.OOO_PROGRAM_PATH), true);
			ooopathRow.setFieldChangedListener(this);
			
			// put the value of the edited SDK in the fields
			if (null != sdk){
				sdknameRow.setText(sdk.name);
				sdkversionRow.setText(sdk.version);
				sdkpathRow.setFile(sdk.path);
				ooopathRow.setFile(sdk.oooProgramPath);
			}
			
			return body;
		}

		/**
		 * This method is overridden in order to set the appropriate title for
		 * the dialog.
		 */
		protected void configureShell(Shell newShell) {

			super.configureShell(newShell);
			newShell.setSize(400, 270);
			newShell.setText(OOEclipsePlugin.getTranslationString(I18nConstants.SDK_CONFIG_DIALOG_TITLE));
		}
		
		protected void okPressed() {
			// Perform data controls on the fields: they are all mandatory
			// If there is one field missing, print an error line at the bottom
			// of the dialog.
			
			if (sdknameRow.getText().equals("")|| sdkversionRow.getText().equals("") ||
				  sdkpathRow.getFile().equals("") || ooopathRow.getFile().equals("")){
		
				updateStatus(new Status(Status.ERROR, 
					     OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						 Status.ERROR,
						 OOEclipsePlugin.getTranslationString(I18nConstants.ALL_FIELDS_FILLED),
						 null));
			
			} else {
			
				// Store the SDK in tmpsdk
				tmpsdk = new SDK(sdknameRow.getText(),
						         sdkversionRow.getText(),
						         sdkpathRow.getFile(),
						         ooopathRow.getFile());
				super.okPressed();
			}
		}

		public void fieldChanged(FieldEvent e) {
			if (!(sdknameRow.getText().equals("")|| sdkversionRow.getText().equals("") ||
					  sdkpathRow.getFile().equals("") || ooopathRow.getFile().equals(""))){
				
				updateStatus(new Status(Status.OK,
			    		OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
			    		Status.OK, "", null));
			}
		}
	}
}
