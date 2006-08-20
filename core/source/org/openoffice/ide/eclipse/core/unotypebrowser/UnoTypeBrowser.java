/*************************************************************************
 *
 * $RCSfile: UnoTypeBrowser.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:55:53 $
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;

/**
 * A dialog to browse UNO types. This class doesn't launch the types query:
 * this job is performed by the uno type provider to avoid very slow window
 * rendering.
 * 
 * @author cbosdonnat
 *
 */
public class UnoTypeBrowser extends StatusDialog
							implements IFieldChangedListener,
							 		   IInitListener {

	private UnoTypeProvider mTypesProvider;
	private InternalUnoType mSelectedType;
	
	/**
	 * Creates a new browser dialog. The browser, waits for the type provider
	 * to finish its work if it's not already over.
	 * 
	 * @param parentShell the shell where to create the dialog
	 * @param aUnoTypesProvider the uno type provider
	 */
	public UnoTypeBrowser(Shell parentShell, UnoTypeProvider aUnoTypesProvider) {
		super(parentShell);
		
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setBlockOnOpen(true);
		setTitle(Messages.getString("UnoTypeBrowser.Title")); //$NON-NLS-1$
		
		// Initialize the Type Browser
		if (null != aUnoTypesProvider) {
			mTypesProvider = aUnoTypesProvider;
			mTypesProvider.addInitListener(this);
			
			if (!mTypesProvider.isInitialized()){
				// changes the status to warn the user
				
				updateStatus(new Status(IStatus.INFO,
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
						IStatus.INFO,
						Messages.getString("UnoTypeBrowser.WaitTypes"), //$NON-NLS-1$
						null));
			}
		}
	}
	
	//---------------------------------------------------- UI creation & control 
	
	private TextRow mInputRow;
	private TableViewer mTypesList;
	
	private BooleanRow mModuleFilterRow;
	private BooleanRow mInterfaceFilterRow;
	private BooleanRow mServiceFilterRow;
	private BooleanRow mStructFilterRow;
	private BooleanRow mEnumFilterRow;
	private BooleanRow mExceptionFilterRow;
	private BooleanRow mTypedefFilterRow;
	private BooleanRow mConstantFilterRow;
	private BooleanRow mConstantsFilterRow;
	private BooleanRow mSingletonFilterRow;
	
	private static final String F_INPUT = "__input"; //$NON-NLS-1$
	
	private static final String F_MODULE = "__module"; //$NON-NLS-1$
	
	private static final String F_INTERFACE = "__interface"; //$NON-NLS-1$
	
	private final static String F_SERVICE = "__service"; //$NON-NLS-1$
	
	private final static String F_STRUCT = "__struct"; //$NON-NLS-1$
	
	private final static String F_ENUM = "__enum"; //$NON-NLS-1$
	
	private final static String F_EXCEPTION = "__exception"; //$NON-NLS-1$
	
	private final static String F_TYPEDEF = "__typedef"; //$NON-NLS-1$
	
	private final static String F_CONSTANT = "__contant"; //$NON-NLS-1$
	
	private final static String F_CONSTANTS = "__constants"; //$NON-NLS-1$
	
	private final static String F_SINGLETON = "__singleton"; //$NON-NLS-1$
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
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
		titleLabel.setText(Messages.getString("UnoTypeBrowser.TitleTitle")); //$NON-NLS-1$
		
		// create the input text row
		mInputRow = new TextRow(body, F_INPUT, 
				Messages.getString("UnoTypeBrowser.TypeName")); //$NON-NLS-1$
		mInputRow.setFieldChangedListener(this);
		
		createList(body);
		createFilterRows(body);
		mFilter = mTypesProvider.getTypes();

		mTypesList.setInput(mTypesProvider);
		if (!mTypesProvider.isInitialized()) {
			activateFields(false);
		}
		
		mInputRow.setFocus();
		
		return body;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.unotypebrowser.IInitListener#initialized()
	 */
	public void initialized() {
	
		Runnable run = new Runnable(){

			public void run() {
				if (!mTypesList.getTable().isDisposed()) {
					mTypesList.refresh();
					activateFields(true);

					updateStatus(new Status(IStatus.INFO,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							IStatus.INFO,
							"", //$NON-NLS-1$
							null));
				}
			}
		};
	
		Display.getDefault().asyncExec(run);
	}
	
	/**
	 * Create and configure the types list
	 * 
	 * @param parent the parent composite where to create the list
	 */
	private void createList(Composite parent){
		
		Table table = new Table(parent, SWT.BORDER | SWT.V_SCROLL| SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 2;
		gd.heightHint = 150;
		table.setLayoutData(gd);
		
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setWidth(300);
		
		mTypesList = new TableViewer(table);
		mTypesList.setUseHashlookup(true);
		mTypesList.setLabelProvider(new TypeLabelProvider());
		mTypesList.setContentProvider(new InternalTypesProvider());
		mTypesList.addFilter(new UnoTypesFilter());
		mTypesList.setSorter(new ViewerSorter());
		mTypesList.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection().isEmpty()){
					updateStatus(new Status(Status.ERROR,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							Status.ERROR,
							Messages.getString("UnoTypeBrowser.EmptySelectionError"), //$NON-NLS-1$
							null));
					mSelectedType = null;
					getButton(IDialogConstants.OK_ID).setEnabled(false);
					
				} else {
					updateStatus(new Status(Status.OK,
							OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
							Status.OK,
							"", //$NON-NLS-1$
							null));
					getButton(IDialogConstants.OK_ID).setEnabled(true);
					
					IStructuredSelection selection = (IStructuredSelection)event.getSelection();
					mSelectedType = (InternalUnoType)selection.getFirstElement();
					
					mTypesList.refresh(mSelectedType, true);
				}
			}			
		});
	}
	
	/**
	 * Creates the type filter checkboxes
	 * 
	 * @param parent the parent composite where to create the boxes
	 */
	private void createFilterRows(Composite parent){
		
		// Create the necessary filter rows depending on the needed types
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.MODULE) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.MODULE){
			mModuleFilterRow = new BooleanRow(parent, F_MODULE, 
					Messages.getString("UnoTypeBrowser.FilterModule")); //$NON-NLS-1$
			mModuleFilterRow.setValue(true);
			mModuleFilterRow.setFieldChangedListener(this);
		}

		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.INTERFACE) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.INTERFACE) {
			mInterfaceFilterRow = new BooleanRow(parent, F_INTERFACE, 
					Messages.getString("UnoTypeBrowser.FilterInterface")); //$NON-NLS-1$
			mInterfaceFilterRow.setValue(true);
			mInterfaceFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.SERVICE) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.SERVICE){
			mServiceFilterRow = new BooleanRow(parent, F_SERVICE, 
					Messages.getString("UnoTypeBrowser.FilterService")); //$NON-NLS-1$
			mServiceFilterRow.setValue(true);
			mServiceFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.STRUCT) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.STRUCT){
			mStructFilterRow = new BooleanRow(parent, F_STRUCT, 
					Messages.getString("UnoTypeBrowser.FilterStruct")); //$NON-NLS-1$
			mStructFilterRow.setValue(true);
			mStructFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.ENUM) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.ENUM){
			mEnumFilterRow = new BooleanRow(parent, F_ENUM, 
					Messages.getString("UnoTypeBrowser.FilterEnum")); //$NON-NLS-1$
			mEnumFilterRow.setValue(true);
			mEnumFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.EXCEPTION) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.EXCEPTION){
			mExceptionFilterRow = new BooleanRow(parent, F_EXCEPTION, 
					Messages.getString("UnoTypeBrowser.FilterException")); //$NON-NLS-1$
			mExceptionFilterRow.setValue(true);
			mExceptionFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.TYPEDEF) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.TYPEDEF){
			mTypedefFilterRow = new BooleanRow(parent, F_TYPEDEF, 
					Messages.getString("UnoTypeBrowser.FilterTypedef")); //$NON-NLS-1$
			mTypedefFilterRow.setValue(true);
			mTypedefFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.CONSTANT) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.CONSTANT){
			mConstantFilterRow = new BooleanRow(parent, F_CONSTANT, 
					Messages.getString("UnoTypeBrowser.FilterConstant")); //$NON-NLS-1$
			mConstantFilterRow.setValue(true);
			mConstantFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.CONSTANTS) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.CONSTANTS){
			mConstantsFilterRow = new BooleanRow(parent, F_CONSTANTS, 
					Messages.getString("UnoTypeBrowser.FilterConstants")); //$NON-NLS-1$
			mConstantsFilterRow.setValue(true);
			mConstantsFilterRow.setFieldChangedListener(this);
		}
		
		if (mTypesProvider.isTypeSet(IUnoFactoryConstants.SINGLETON) &&
				mTypesProvider.getTypes() != IUnoFactoryConstants.SINGLETON){
			mSingletonFilterRow = new BooleanRow(parent, F_SINGLETON, 
					Messages.getString("UnoTypeBrowser.FilterSingleton")); //$NON-NLS-1$
			mSingletonFilterRow.setValue(true);
			mSingletonFilterRow.setFieldChangedListener(this);
		}
	}
	
	/**
	 * Method to activate or unactivate the dialog fields
	 */
	public void activateFields(boolean activate) {
		mInputRow.setEnabled(activate);
		mTypesList.getTable().setEnabled(activate);
		
		if (null != mModuleFilterRow) {
			mModuleFilterRow.setEnabled(activate);
		}
		
		if (null != mInterfaceFilterRow) {
			mInterfaceFilterRow.setEnabled(activate);
		}
		
		if (null != mServiceFilterRow) {
			mServiceFilterRow.setEnabled(activate);
		}
		
		if (null != mStructFilterRow) {
			mStructFilterRow.setEnabled(activate);
		}
		
		if (null != mEnumFilterRow) {
			mEnumFilterRow.setEnabled(activate);
		}
		
		if (null != mExceptionFilterRow) {
			mExceptionFilterRow.setEnabled(activate);
		}
		
		if (null != mTypedefFilterRow) {
			mTypedefFilterRow.setEnabled(activate);
		}
		
		if (null != mConstantFilterRow) {
			mConstantFilterRow.setEnabled(activate);
		}
		
		if (null != mConstantsFilterRow) {
			mConstantsFilterRow.setEnabled(activate);
		}
		
		if (null != mSingletonFilterRow) {
			mSingletonFilterRow.setEnabled(activate);
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
	
	/**
	 * Provides the label and image for the list items
	 * 
	 * @author cbosdonnat
	 */
	class TypeLabelProvider extends LabelProvider {
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			Image result = null;
			
			if (element instanceof InternalUnoType) {
				int type = ((InternalUnoType)element).getType();
				
				if (IUnoFactoryConstants.SERVICE == type) {
					result = OOEclipsePlugin.getImage(ImagesConstants.SERVICE);
				} else if (IUnoFactoryConstants.INTERFACE == type) {
					result = OOEclipsePlugin.getImage(ImagesConstants.INTERFACE);
				}
				
			}
			return result;
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			String result = ""; //$NON-NLS-1$
			
			if (element instanceof InternalUnoType){
				InternalUnoType type = (InternalUnoType)element;
				result = type.getName();
				
				if (!mTypesList.getSelection().isEmpty()){
					IStructuredSelection selection = (IStructuredSelection)
								mTypesList.getSelection();
					
					if (selection.getFirstElement().equals(type)){
						result = result + " - " + type.getFullName(); //$NON-NLS-1$
					}
				}
			}
			return result;
		}
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		
		/**
		 * The calling method have to close the dialog window after having get the 
		 * resulting data.
		 */
		
		if (mTypesList.getSelection().isEmpty()) {
			updateStatus(new Status(Status.ERROR,
					OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
					Status.ERROR,
					Messages.getString("UnoTypeBrowser.EmptySelectionError"),  //$NON-NLS-1$
					null));
			getButton(IDialogConstants.OK_ID).setEnabled(false);
			mTypesProvider.removeInitListener(this);
		} else {
			super.okPressed();
		}
	}

	//------------------------------------------------- React on field changing
	
	private int mFilter;
	
	/**
	 * Refreshes the dialog
	 */
	private void refresh(){
		
		activateFields(false);
		mTypesList.refresh();
		activateFields(true);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener#fieldChanged(org.openoffice.ide.eclipse.core.gui.rows.FieldEvent)
	 */
	public void fieldChanged(FieldEvent e) {
		
		if (e.getProperty().equals(F_INPUT)) {
			
			refresh();
			
		} else if (e.getProperty().equals(F_MODULE)) {
			boolean newValue = mModuleFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.MODULE);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.MODULE : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_INTERFACE)) {
			boolean newValue = mInterfaceFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.INTERFACE);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.INTERFACE : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_SERVICE)) {
			boolean newValue = mServiceFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.SERVICE);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.SERVICE : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_STRUCT)) {
			boolean newValue = mStructFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.STRUCT);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.STRUCT : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_ENUM)) {
			boolean newValue = mEnumFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.ENUM);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.ENUM : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_EXCEPTION)) {
			boolean newValue = mExceptionFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.EXCEPTION);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.EXCEPTION : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_TYPEDEF)) {
			boolean newValue = mTypedefFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.TYPEDEF);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.TYPEDEF : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_CONSTANT)) {
			boolean newValue = mConstantFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.CONSTANT);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.CONSTANT : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_CONSTANTS)) {
			boolean newValue = mConstantsFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.CONSTANTS);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.CONSTANTS : mFilter & inv;
			
			refresh();
			
		} else if (e.getProperty().equals(F_SINGLETON)) {
			boolean newValue = mSingletonFilterRow.getBooleanValue();
			int inv = UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.SINGLETON);
			mFilter = newValue ? mFilter & inv | IUnoFactoryConstants.SINGLETON : mFilter & inv;
			
			refresh();
			
		}
		
		mInputRow.setFocus();
	}
	
	//---------------------------------------- Filters the elements in the list
	
	/**
	 * List items filter class 
	 * 
	 * @author cbosdonnat
	 */
	private class UnoTypesFilter extends ViewerFilter {

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			boolean select = false;
			
			if (element instanceof InternalUnoType){
				InternalUnoType type = (InternalUnoType)element;
				if ((mFilter & type.getType()) != 0) {
					// The type is correct, check the name
					if (type.getName().startsWith(mInputRow.getValue())){
						select = true;
					}
				}
			}
			return select;
		}
	}
	
	//----------------------------------------- Manages the content of the list
	
	/**
	 * Returns the selected {@link InternalUnoType}
	 */
	public InternalUnoType getSelectedType(){
		return mSelectedType;
	}
	
	public void setSelectedType(InternalUnoType type) {
		mSelectedType = type;
		if (null != mTypesList){
			IStructuredSelection selection = StructuredSelection.EMPTY;
			if (null != mSelectedType) {
				selection = new StructuredSelection(mSelectedType);
			}
			
			mTypesList.setSelection(selection);
		}
	}
	
	/**
	 * Provides the content to the list viewer
	 * 
	 * @author cbosdonnat
	 *
	 */
	private class InternalTypesProvider implements IStructuredContentProvider {

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return mTypesProvider.toArray();
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// Nothing to do
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Should never happen
		}
	}
}
