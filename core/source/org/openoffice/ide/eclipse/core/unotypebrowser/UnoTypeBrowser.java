/*************************************************************************
 *
 * $RCSfile: UnoTypeBrowser.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:12 $
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
package org.openoffice.ide.eclipse.core.unotypebrowser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
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
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoTypeBrowser extends StatusDialog
							implements IFieldChangedListener,
							 		   IInitListener {

	private UnoTypeProvider typesProvider;
	private InternalUnoType selectedType;
	
	public UnoTypeBrowser(Shell parentShell, UnoTypeProvider aUnoTypesProvider) {
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setBlockOnOpen(true);
		setTitle(OOEclipsePlugin.getTranslationString(
				I18nConstants.UNO_BROWSER_TITLE));
		
		// Initialize the Type Browser
		if (null != aUnoTypesProvider) {
			typesProvider = aUnoTypesProvider;
			typesProvider.addInitListener(this);
			
			if (!typesProvider.isInitialized()){
				// changes the status to warn the user
				
				updateStatus(new Status(IStatus.INFO,
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						IStatus.INFO,
						OOEclipsePlugin.getTranslationString(
								I18nConstants.WAITING_4_UNOTYPES),
						null));
			}
		}
	}
	
	//---------------------------------------------------- UI creation & control 
	
	private TextRow inputRow;
	private TableViewer typesList;
	
	private BooleanRow moduleFilterRow;
	private BooleanRow interfaceFilterRow;
	private BooleanRow serviceFilterRow;
	private BooleanRow structFilterRow;
	private BooleanRow enumFilterRow;
	private BooleanRow exceptionFilterRow;
	private BooleanRow typedefFilterRow;
	private BooleanRow constantFilterRow;
	private BooleanRow constantsFilterRow;
	private BooleanRow singletonFilterRow;
	
	private static final String F_INPUT = "__input";
	
	private static final String F_MODULE = "__module";
	
	private static final String F_INTERFACE = "__interface";
	
	private final static String F_SERVICE = "__service";
	
	private final static String F_STRUCT = "__struct";
	
	private final static String F_ENUM = "__enum";
	
	private final static String F_EXCEPTION = "__exception";
	
	private final static String F_TYPEDEF = "__typedef";
	
	private final static String F_CONSTANT = "__contant";
	
	private final static String F_CONSTANTS = "__constants";
	
	private final static String F_SINGLETON = "__singleton";
	
	
	protected Control createDialogArea(Composite parent) {
		
		// Create the control that contains all the UI components
		Composite body = (Composite)super.createDialogArea(parent);
		body.setLayout(new GridLayout(2, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		// Create the title label
		Label titleLabel = new Label(body, SWT.NONE);
		GridData gdLabel = new GridData(GridData.FILL_BOTH | 
										GridData.VERTICAL_ALIGN_BEGINNING);
		gdLabel.horizontalSpan = 2;
		gdLabel.heightHint = 20;
		titleLabel.setLayoutData(gdLabel);
		titleLabel.setText(OOEclipsePlugin.getTranslationString(
				I18nConstants.SELECT_TYPE_MESSAGE));
		
		// create the input text row
		inputRow = new TextRow(body, F_INPUT, 
				OOEclipsePlugin.getTranslationString(I18nConstants.TYPE_NAME));
		inputRow.setFieldChangedListener(this);
		
		createList(body);
		createFilterRows(body);
		filter = typesProvider.getTypes();

		typesList.setInput(typesProvider);
		if (!typesProvider.isInitialized()) {
			activateFields(false);
		}
		
		inputRow.setFocus();
		
		return body;
	}
	
	public void initialized() {
	
		Runnable run = new Runnable(){

			public void run() {
				
				typesList.refresh();
				activateFields(true);
				
				updateStatus(new Status(IStatus.INFO,
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						IStatus.INFO,
						"",
						null));
			}
		};
	
		getContents().getDisplay().asyncExec(run);
	}
	
	private void createList(Composite parent){
		
		Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL| SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.heightHint = 150;
		table.setLayoutData(gd);
		
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(300);
		
		typesList = new TableViewer(table);
		typesList.setUseHashlookup(true);
		typesList.setLabelProvider(new TypeLabelProvider());
		typesList.setContentProvider(new InternalTypesProvider());
		typesList.addFilter(new UnoTypesFilter());
		typesList.setSorter(new ViewerSorter());
		typesList.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()){
					updateStatus(new Status(Status.ERROR,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							Status.ERROR,
							OOEclipsePlugin.getTranslationString(
									I18nConstants.EMPTY_SELECTION),
							null));
					selectedType = null;
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					
				} else {
					updateStatus(new Status(Status.OK,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							Status.OK,
							"",
							null));
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					selectedType = (InternalUnoType)selection.getFirstElement();
					
					typesList.refresh(selectedType, true);
				}
			}			
		});
	}
	
	private void createFilterRows(Composite parent){
		
		// Create the necessary filter rows depending on the needed types
		
		if (typesProvider.isTypeSet(UnoTypeProvider.MODULE) &&
				typesProvider.getTypes() != UnoTypeProvider.MODULE){
			moduleFilterRow = new BooleanRow(parent, F_MODULE, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_MODULES));
			moduleFilterRow.setValue(true);
			moduleFilterRow.setFieldChangedListener(this);
		}

		if (typesProvider.isTypeSet(UnoTypeProvider.INTERFACE) &&
				typesProvider.getTypes() != UnoTypeProvider.INTERFACE) {
			interfaceFilterRow = new BooleanRow(parent, F_INTERFACE, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_INTERFACES));
			interfaceFilterRow.setValue(true);
			interfaceFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.SERVICE) &&
				typesProvider.getTypes() != UnoTypeProvider.SERVICE){
			serviceFilterRow = new BooleanRow(parent, F_SERVICE, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_SERVICES));
			serviceFilterRow.setValue(true);
			serviceFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.STRUCT) &&
				typesProvider.getTypes() != UnoTypeProvider.STRUCT){
			structFilterRow = new BooleanRow(parent, F_STRUCT, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_STRUCTS));
			structFilterRow.setValue(true);
			structFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.ENUM) &&
				typesProvider.getTypes() != UnoTypeProvider.ENUM){
			enumFilterRow = new BooleanRow(parent, F_ENUM, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_ENUMS));
			enumFilterRow.setValue(true);
			enumFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.EXCEPTION) &&
				typesProvider.getTypes() != UnoTypeProvider.EXCEPTION){
			exceptionFilterRow = new BooleanRow(parent, F_EXCEPTION, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_EXCEPTIONS));
			exceptionFilterRow.setValue(true);
			exceptionFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.TYPEDEF) &&
				typesProvider.getTypes() != UnoTypeProvider.TYPEDEF){
			typedefFilterRow = new BooleanRow(parent, F_TYPEDEF, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_TYPEDEFS));
			typedefFilterRow.setValue(true);
			typedefFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.CONSTANT) &&
				typesProvider.getTypes() != UnoTypeProvider.CONSTANT){
			constantFilterRow = new BooleanRow(parent, F_CONSTANT, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_CONSTANTS));
			constantFilterRow.setValue(true);
			constantFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.CONSTANTS) &&
				typesProvider.getTypes() != UnoTypeProvider.CONSTANTS){
			constantsFilterRow = new BooleanRow(parent, F_CONSTANTS, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_CONSTANTSS));
			constantsFilterRow.setValue(true);
			constantsFilterRow.setFieldChangedListener(this);
		}
		
		if (typesProvider.isTypeSet(UnoTypeProvider.SINGLETON) &&
				typesProvider.getTypes() != UnoTypeProvider.SINGLETON){
			singletonFilterRow = new BooleanRow(parent, F_SINGLETON, 
					OOEclipsePlugin.getTranslationString(
							I18nConstants.FILTER_SINGLETONS));
			singletonFilterRow.setValue(true);
			singletonFilterRow.setFieldChangedListener(this);
		}
	}
	
	public void activateFields(boolean activate) {
		inputRow.setEnabled(activate);
		typesList.getTable().setEnabled(activate);
		
		if (null != moduleFilterRow) {
			moduleFilterRow.setEnabled(activate);
		}
		
		if (null != interfaceFilterRow) {
			interfaceFilterRow.setEnabled(activate);
		}
		
		if (null != serviceFilterRow) {
			serviceFilterRow.setEnabled(activate);
		}
		
		if (null != structFilterRow) {
			structFilterRow.setEnabled(activate);
		}
		
		if (null != enumFilterRow) {
			enumFilterRow.setEnabled(activate);
		}
		
		if (null != exceptionFilterRow) {
			exceptionFilterRow.setEnabled(activate);
		}
		
		if (null != typedefFilterRow) {
			typedefFilterRow.setEnabled(activate);
		}
		
		if (null != constantFilterRow) {
			constantFilterRow.setEnabled(activate);
		}
		
		if (null != constantsFilterRow) {
			constantsFilterRow.setEnabled(activate);
		}
		
		if (null != singletonFilterRow) {
			singletonFilterRow.setEnabled(activate);
		}
		
		Button okButton = getButton(IDialogConstants.OK_ID);
		if (null != okButton){
			okButton.setEnabled(activate);
		}
		
		Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
		if (null != cancelButton){
			cancelButton.setEnabled(activate);
		}
	}
	
	class TypeLabelProvider extends LabelProvider {
		
		public Image getImage(Object element) {
			Image result = null;
			
			if (element instanceof InternalUnoType) {
				int type = ((InternalUnoType)element).getType();
				
				if (UnoTypeProvider.SERVICE == type) {
					result = OOEclipsePlugin.getImage(ImagesConstants.SERVICE);
				} else if (UnoTypeProvider.INTERFACE == type) {
					result = OOEclipsePlugin.getImage(ImagesConstants.INTERFACE);
				}
				
			}
			return result;
		}
		
		public String getText(Object element) {
			String result = "";
			
			if (element instanceof InternalUnoType){
				InternalUnoType type = (InternalUnoType)element;
				result = type.getName();
				
				if (!typesList.getSelection().isEmpty()){
					IStructuredSelection selection = (IStructuredSelection)
								typesList.getSelection();
					
					if (selection.getFirstElement().equals(type)){
						result = result + " - " + type.getPath();
					}
				}
			}
			return result;
		}
		
	}
	
	/**
	 * The calling method have to close the dialog window after having get the 
	 * resulting data.
	 */
	protected void okPressed() {
		if (typesList.getSelection().isEmpty()) {
			updateStatus(new Status(Status.ERROR,
					OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					Status.ERROR,
					OOEclipsePlugin.getTranslationString(
							I18nConstants.EMPTY_SELECTION), 
					null));
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			typesProvider.removeInitListener(this);
		} else {
			super.okPressed();
		}
	}

	//------------------------------------------------- React on field changing
	
	private int filter;
	
	private void refresh(){
		
		activateFields(false);
		typesList.refresh();
		activateFields(true);
	}
	
	public void fieldChanged(FieldEvent e) {
		
		if (e.getProperty().equals(F_INPUT)) {
			
			refresh();
			
		} else if (e.getProperty().equals(F_MODULE)) {
			boolean newValue = moduleFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.MODULE);
			filter = newValue ? filter & inv | UnoTypeProvider.MODULE : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_INTERFACE)) {
			boolean newValue = interfaceFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.INTERFACE);
			filter = newValue ? filter & inv | UnoTypeProvider.INTERFACE : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_SERVICE)) {
			boolean newValue = serviceFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.SERVICE);
			filter = newValue ? filter & inv | UnoTypeProvider.SERVICE : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_STRUCT)) {
			boolean newValue = structFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.STRUCT);
			filter = newValue ? filter & inv | UnoTypeProvider.STRUCT : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_ENUM)) {
			boolean newValue = enumFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.ENUM);
			filter = newValue ? filter & inv | UnoTypeProvider.ENUM : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_EXCEPTION)) {
			boolean newValue = exceptionFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.EXCEPTION);
			filter = newValue ? filter & inv | UnoTypeProvider.EXCEPTION : filter & inv;
			
			refresh();
			
		}else if (e.getProperty().equals(F_TYPEDEF)) {
			boolean newValue = typedefFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.TYPEDEF);
			filter = newValue ? filter & inv | UnoTypeProvider.TYPEDEF : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_CONSTANT)) {
			boolean newValue = constantFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.CONSTANT);
			filter = newValue ? filter & inv | UnoTypeProvider.CONSTANT : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_CONSTANTS)) {
			boolean newValue = constantsFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.CONSTANTS);
			filter = newValue ? filter & inv | UnoTypeProvider.CONSTANTS : filter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_SINGLETON)) {
			boolean newValue = singletonFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(UnoTypeProvider.SINGLETON);
			filter = newValue ? filter & inv | UnoTypeProvider.SINGLETON : filter & inv;
			
			refresh();
			
		}
		
		inputRow.setFocus();
	}
	
	//---------------------------------------- Filters the elements in the list
	
	private class UnoTypesFilter extends ViewerFilter {

		public boolean select(Viewer viewer, Object parentElement, Object element) {
			boolean select = false;
			
			if (element instanceof InternalUnoType){
				InternalUnoType type = (InternalUnoType)element;
				if ((filter & type.getType()) != 0) {
					// The type is correct, check the name
					if (type.getName().startsWith(inputRow.getValue())){
						select = true;
					}
				}
			}
			return select;
		}
	}
	
	//----------------------------------------- Manages the content of the list
	
	
	
	/**
	 * Returns the type path and it's type in the following way:
	 * <code>&lt;path&gt; &lt;type&gt;</code>, where the type is a numeric
	 * value among those defined in UnoTypeProvider class. If the selection
	 * is empty, the returned value is <code>null</code>.
	 * 
	 * @see UnoTypeProvider
	 * 
	 * @return the exact path and type of the chosen UNO type. For example
	 *       this could be "com::sun::star::uno::XInterface 2"
	 */
	public String getSelectedType(){
		String typePath = null;
		
		if (null != selectedType){
			typePath = selectedType.getPath() + " " + selectedType.getType();
		}
		
		return typePath;
	}
	
	public void setSelectedType(String type) {
		
		selectedType = new InternalUnoType(type);
		typesList.setSelection(new StructuredSelection(selectedType));
	}
	
	private class InternalTypesProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return typesProvider.toArray();
		}

		public void dispose() {
			// Nothing to do
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Should never happen
		}
		
	}
}
