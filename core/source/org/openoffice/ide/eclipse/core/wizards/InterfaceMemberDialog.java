/*************************************************************************
 *
 * $RCSfile: InterfaceMemberDialog.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/23 18:27:16 $
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

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.swt.widgets.TableItem;
import org.openoffice.ide.eclipse.core.gui.TypeCellEditor;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.gui.rows.TypeRow;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;

/**
 * Defines an interface member creation or edition dialog. To get the computed
 * data, use the {@link #getData()} method, even after disposing the dialog. 
 * 
 * This class shouldn't be subclassed.
 * 
 * @author cedricbosdo
 *
 */
public class InterfaceMemberDialog extends StatusDialog implements
		IFieldChangedListener {

	private static final String MEMBER_TYPE = "__member_type"; //$NON-NLS-1$
	private static final String NAME = "__name"; //$NON-NLS-1$
	private static final String TYPE = "__type"; //$NON-NLS-1$
	private static final String BOUND = "__bound"; //$NON-NLS-1$
	private static final String READONLY = "__readonly"; //$NON-NLS-1$
	private static final String PARAM_TYPE = "__param_type"; //$NON-NLS-1$
	private static final String PARAM_INOUT = "__param_inout"; //$NON-NLS-1$
	private static final String PARAM_NAME = "__param_name"; //$NON-NLS-1$
	
	private UnoFactoryData mData;
	
	private ChoiceRow mMemberTypeRow;
	private TextRow mNameRow;
	private TypeRow mTypeRow;
	private BooleanRow mBoundRow;
	private BooleanRow mReadonlyRow;
	private Button mAddButton;
	private Button mDelButton;
	private TableViewer mArgumentTableViewer;
	
	private Composite mSpecificPanel;
	private boolean mShowAttribute;
	
	/**
	 * Default constructor to use for member creation.
	 */
	public InterfaceMemberDialog() {
		super(Display.getDefault().getActiveShell());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		setBlockOnOpen(true); // This dialog is a modal one
		setTitle(Messages.getString("InterfaceMemberDialog.CreationDialogTitle")); //$NON-NLS-1$
		mData = new UnoFactoryData(); 
	}
	
	/**
	 * Constructor to use for member edition.
	 * 
	 * @param data the member's data to edit
	 */
	public InterfaceMemberDialog(UnoFactoryData data) {
		super(Display.getDefault().getActiveShell());
		setShellStyle(getShellStyle() | SWT.RESIZE);
		
		setBlockOnOpen(true); // This dialog is a modal one
		mData = data;
		
		try {
			int type = ((Integer)mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)).intValue();
			if (type == IUnoFactoryConstants.METHOD) {
				setTitle(Messages.getString("InterfaceMemberDialog.MethodDialogTitle")); //$NON-NLS-1$
			} else if (type == IUnoFactoryConstants.ATTRIBUTE) {
				setTitle(Messages.getString("InterfaceMemberDialog.AttributeDialogTitle")); //$NON-NLS-1$
			}
		} catch (NullPointerException e) {
			// No need to log this. 
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.StatusDialog#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		
		// Just set the correct size of the dialog and center it on the screen
		Rectangle bounds = Display.getDefault().getClientArea();
		shell.setBounds((bounds.width - 500)/2, (bounds.height - 450)/2, 500, 450);
	}
	
	/**
	 * Returns he filled data corresponding to the object 
	 */
	public UnoFactoryData getData() {
		return mData;
	}
	
	/**
	 * Disposes the unused data
	 */
	public void disposeData() {
		if (mData != null) mData.dispose();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		
		Composite body = new Composite(parent, SWT.None);
		body.setLayout(new GridLayout(3, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		String type = ""; //$NON-NLS-1$
		if (mData != null) {
			if (null != mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)) {
				Integer iType = (Integer)mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE);
				if (iType != null) {
					switch (iType.intValue()) {
						case IUnoFactoryConstants.METHOD:
							type = "method"; //$NON-NLS-1$
							mShowAttribute = true; // Has to be the opposite to show it the first time
							break;
						case IUnoFactoryConstants.ATTRIBUTE:
							type = "attribute"; //$NON-NLS-1$
							mShowAttribute = false; // Has to be the opposite to show it the first time
							break;
						default:
							type = ""; //$NON-NLS-1$
					}
				}
			}
		}
		
		// Common rows
		if (type.equals("")) { // $NON-NLS-1$ //$NON-NLS-1$
			
			Composite typeComposite = new Composite(body, SWT.NONE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			typeComposite.setLayoutData(gd);
			typeComposite.setLayout(new GridLayout(3, false));
			
			mMemberTypeRow = new ChoiceRow(typeComposite, MEMBER_TYPE);
			mMemberTypeRow.add(Messages.getString("InterfaceMemberDialog.MethodChoice"), "method"); //$NON-NLS-1$ //$NON-NLS-2$
			mMemberTypeRow.add(Messages.getString("InterfaceMemberDialog.AttributeChoice"), "attribute"); //$NON-NLS-1$ //$NON-NLS-2$
			mMemberTypeRow.select(0);
			mMemberTypeRow.setFieldChangedListener(this);
			mData.setProperty(IUnoFactoryConstants.MEMBER_TYPE, 
					Integer.valueOf(IUnoFactoryConstants.METHOD));
			mShowAttribute = true; // Has to be the opposite to show it the first time
			
			Label sep = new Label(typeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			sep.setLayoutData(gd);
		}
		
		mNameRow = new TextRow(body, NAME, Messages.getString("InterfaceMemberDialog.Name")); //$NON-NLS-1$
		if (mData != null) {
			String name = (String)mData.getProperty(IUnoFactoryConstants.NAME);
			if (name != null) {
				mNameRow.setValue(name);
			}
		}
		mNameRow.setFieldChangedListener(this);
		
		mTypeRow = new TypeRow(body, TYPE, Messages.getString("InterfaceMemberDialog.Type"), InternalUnoType.ALL_TYPES); //$NON-NLS-1$
		mTypeRow.includeSequences(true);
		mTypeRow.includeSimpleTypes(true);
		mTypeRow.setFieldChangedListener(this);
		if (mData != null) {
			Object o = mData.getProperty(IUnoFactoryConstants.TYPE);
			if (o instanceof String) {
				String memberType = (String)o;
				mTypeRow.setValue(memberType);
			}
		}
		
		mSpecificPanel = new Composite(body, SWT.NONE);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		mSpecificPanel.setLayoutData(gd);
		mSpecificPanel.setLayout(new GridLayout(3, false));
		
		// Method or Attribute specific fields
		if (type.equals("")) { //$NON-NLS-1$
			type = mMemberTypeRow.getValue();
		}
		
		showSpecificControls(type.equals("attribute")); //$NON-NLS-1$
		
		return body;
	}
	
	/**
	 * This method cleans up the specific composite of all its children and 
	 * recreate the controls for the new type (attribute or method).
	 *
	 * @param isAttribute flag defining whether to show the method or attribute
	 * 		controls.
	 */
	protected void showSpecificControls(boolean isAttribute) {
		
		if (mShowAttribute != isAttribute) {
		
			// Cleans up the previous controls
			Control[] children = mSpecificPanel.getChildren();
			for (int i=0; i<children.length; i++) {
				children[i].dispose();
			}
			
			// Creates the new controls
			if (isAttribute) {
				mShowAttribute = true;
				createAttributeControls(mSpecificPanel);
			} else {
				mShowAttribute = false;
				createMethodControls(mSpecificPanel);
			}
		}
		
		// redraw the control
		mSpecificPanel.layout();
	}
	
	/**
	 * Creates the field rows specific to the Attributes
	 * 
	 * @param parent the composite parent in which to create the controls.
	 */
	protected void createAttributeControls(Composite parent) {		
		mReadonlyRow = new BooleanRow(parent, READONLY, Messages.getString("InterfaceMemberDialog.Readonly")); //$NON-NLS-1$
		mReadonlyRow.setTooltip(Messages.getString("InterfaceMemberDialog.ReadonlyTooltip")); //$NON-NLS-1$
		mReadonlyRow.setFieldChangedListener(this);
		
		mBoundRow = new BooleanRow(parent, BOUND, Messages.getString("InterfaceMemberDialog.Bound")); //$NON-NLS-1$
		mBoundRow.setTooltip(Messages.getString("InterfaceMemberDialog.BoundTooltip")); //$NON-NLS-1$
		mBoundRow.setFieldChangedListener(this);
		
		// loads the data from the model
		if (mData != null) {
			Object o = mData.getProperty(IUnoFactoryConstants.FLAGS);
			if (o instanceof String) {
				String flags = (String)o;
				mReadonlyRow.setValue(flags.contains("readonly")); //$NON-NLS-1$
				mBoundRow.setValue(flags.contains("bound")); //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * Creates the field rows specific to the Methods
	 * 
	 * @param parent the composite parent in which to create the controls.
	 * 
	 */
	protected void createMethodControls(Composite parent) {
		// create an arguments table
		Table table = new Table(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 3;
		table.setLayoutData(gd);
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
		table.setToolTipText(Messages.getString("InterfaceMemberDialog.ArgumentTableTooltip")); //$NON-NLS-1$
		
		// Create the columns
		TableColumn column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
		column.setText(Messages.getString("InterfaceMemberDialog.ArgumentNameColumnTitle")); //$NON-NLS-1$
		column.setWidth(200);
		column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
		column.setText(Messages.getString("InterfaceMemberDialog.ArgumentTypeColumnTitle")); //$NON-NLS-1$
		column.setWidth(200);
		column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
		column.setWidth(60);
		column.setText(Messages.getString("InterfaceMemberDialog.ArgumentDirectionColumnTitle")); //$NON-NLS-1$
		
		mArgumentTableViewer = new TableViewer(table);
		mArgumentTableViewer.setLabelProvider(new ParamLabelProvider());
		mArgumentTableViewer.setContentProvider(new ParamContentProvider());
		mArgumentTableViewer.setColumnProperties(new String[]{
			PARAM_NAME,
			PARAM_TYPE,
			PARAM_INOUT
		});
		TypeCellEditor typeCellEditor = new TypeCellEditor(table, InternalUnoType.ALL_TYPES);
		typeCellEditor.includeSequences(true);
		typeCellEditor.includeSimpleTypes(true);
		typeCellEditor.includeVoid(false);
		mArgumentTableViewer.setCellEditors(new CellEditor[]{
			new TextCellEditor(table),
			typeCellEditor,
			new ComboBoxCellEditor(table,
					new String[]{"inout", "in", "out"}) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		});
		mArgumentTableViewer.setCellModifier(new ParamCellModifier());
		mArgumentTableViewer.setInput(mData);
		
		// Create the Add-Edit / Remove buttons
		Composite buttonComposite = new Composite(parent, SWT.None);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
		gd.horizontalSpan = 3;
		buttonComposite.setLayoutData(gd);
		buttonComposite.setLayout(new GridLayout(3, false));
		
		mAddButton = new Button(buttonComposite, SWT.NORMAL);
		mAddButton.setText(Messages.getString("InterfaceMemberDialog.New")); //$NON-NLS-1$
		mAddButton.setLayoutData(new GridData());
		mAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				UnoFactoryData data = new UnoFactoryData();
				data.setProperty(IUnoFactoryConstants.NAME, "arg"); //$NON-NLS-1$
				data.setProperty(IUnoFactoryConstants.TYPE, "short"); //$NON-NLS-1$
				data.setProperty(IUnoFactoryConstants.ARGUMENT_INOUT, "inout"); //$NON-NLS-1$
				mData.addInnerData(data);
				mArgumentTableViewer.add(data);
			}
		});

		mDelButton = new Button(buttonComposite, SWT.NORMAL);
		mDelButton.setText(Messages.getString("InterfaceMemberDialog.Remove")); //$NON-NLS-1$
		mDelButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
		mDelButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				// Remove the selected attribute
				ISelection sel = mArgumentTableViewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					Object element = ((IStructuredSelection)sel).getFirstElement();
					mData.removeInnerData((UnoFactoryData)element);
					mArgumentTableViewer.remove(element);
				}
			}
		});
	}
	
	/* (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener#fieldChanged(org.openoffice.ide.eclipse.core.gui.rows.FieldEvent)
	 */
	public void fieldChanged(FieldEvent e) {
		if (e.getProperty().equals(MEMBER_TYPE)) {
			String type = mMemberTypeRow.getValue();
			if (type.equals("method")) { //$NON-NLS-1$
				mData.setProperty(IUnoFactoryConstants.MEMBER_TYPE, 
						Integer.valueOf(IUnoFactoryConstants.METHOD));
				showSpecificControls(false);
			} else {
				mData.setProperty(IUnoFactoryConstants.MEMBER_TYPE, 
						Integer.valueOf(IUnoFactoryConstants.ATTRIBUTE));
				showSpecificControls(true);
			}
		} else if (e.getProperty().equals(NAME)) {
			mData.setProperty(IUnoFactoryConstants.NAME, e.getValue().trim());
		} else if (e.getProperty().equals(TYPE)) {
			mData.setProperty(IUnoFactoryConstants.TYPE, e.getValue().trim());
		} else if (e.getProperty().equals(BOUND)) {
			String flags = (String)mData.getProperty(IUnoFactoryConstants.FLAGS);
			if (flags != null && flags.contains("bound")) { //$NON-NLS-1$
				if (! mBoundRow.getBooleanValue()) {
					// remove the bound flag
					flags = flags.replace("bound", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					// Flag already present: nothing to do
				}
			} else {
				if (mBoundRow.getBooleanValue()) {
					// Set the bound flag
					if (flags == null) flags = ""; //$NON-NLS-1$
					flags += " bound"; //$NON-NLS-1$
					flags = flags.trim();
				}
			}
			mData.setProperty(IUnoFactoryConstants.FLAGS, flags);
		} else if (e.getProperty().equals(READONLY)) {
			String flags = (String)mData.getProperty(IUnoFactoryConstants.FLAGS);
			if (flags != null && flags.contains("readonly")) { //$NON-NLS-1$
				if (! mReadonlyRow.getBooleanValue()) {
					// remove the bound flag
					flags = flags.replace("readonly", "").trim(); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					// Flag already present: nothing to do
				}
			} else {
				if (mReadonlyRow.getBooleanValue()) {
					// Set the bound flag
					if (flags == null) flags = ""; //$NON-NLS-1$
					flags += " readonly"; //$NON-NLS-1$
					flags = flags.trim();
				}
			}
			mData.setProperty(IUnoFactoryConstants.FLAGS, flags);
		}
	}
	
	/**
	 * Class providing an access to the inner data of the uno factory data for
	 * the method arguments table.
	 * 
	 * @author cedricbosdo
	 */
	class ParamContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return mData.getInnerData();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do here
		}
		
	}
	
	/**
	 * Simply provides the values access for the cell editors of the method
	 * arguments table.
	 * 
	 * @author cedricbosdo
	 */
	class ParamCellModifier implements ICellModifier {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			return (element instanceof UnoFactoryData && (property.equals(PARAM_TYPE) ||
					property.equals(PARAM_NAME) || property.equals(PARAM_INOUT)));
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			Object value = null;
			if (element instanceof UnoFactoryData) {
				UnoFactoryData data = (UnoFactoryData)element;
				
				if (property.equals(PARAM_NAME)) {
					// get the value of the name
					value = (String)data.getProperty(IUnoFactoryConstants.NAME);
				} else if (property.equals(PARAM_TYPE)) {
					// get the value of the type
					value = data.getProperty(IUnoFactoryConstants.TYPE);
				} else if (property.equals(PARAM_INOUT)) {
					// get the value of the direction
					String text = (String)data.getProperty(IUnoFactoryConstants.ARGUMENT_INOUT);
					if ("in".equals(text)) { //$NON-NLS-1$
						value = Integer.valueOf(1);
					} else if ("out".equals(text)) { //$NON-NLS-1$
						value = Integer.valueOf(2);
					} else if ("inout".equals(text)) { //$NON-NLS-1$
						value = Integer.valueOf(0);
					}
				}
			}
			if (value == null) value = ""; //$NON-NLS-1$
			
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			if (((TableItem)element).getData() instanceof UnoFactoryData) {
				UnoFactoryData data = (UnoFactoryData)((TableItem)element).getData();
				if (property.equals(PARAM_NAME) && value instanceof String) {
					// set the value of the name
					data.setProperty(IUnoFactoryConstants.NAME, value);
					mArgumentTableViewer.setInput(mData);
				} else if (property.equals(PARAM_TYPE) && value instanceof String) {
					// set the value of the type
					data.setProperty(IUnoFactoryConstants.TYPE, value);
					mArgumentTableViewer.setInput(mData);
				} else if (property.equals(PARAM_INOUT) && value instanceof Integer) {
					// set the value of the direction
					String direction = null;
					switch (((Integer)value).intValue()) {
					case 0:
						direction = "inout"; //$NON-NLS-1$
						break;
					case 1:
						direction = "in"; //$NON-NLS-1$
						break;
					case 2:
						direction = "out"; //$NON-NLS-1$
						break;
					}
					data.setProperty(IUnoFactoryConstants.ARGUMENT_INOUT, direction);
					mArgumentTableViewer.setInput(mData);
				}
			}
		}
	}
	
	/**
	 * Simply provides the label for the method arguments table
	 * 
	 * @author cedricbosdo
	 */
	class ParamLabelProvider implements ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			String label = null;
			UnoFactoryData data = (UnoFactoryData)element;
			
			switch (columnIndex) {
				case 0:
					// Get the Argument Name
					label = (String)data.getProperty(IUnoFactoryConstants.NAME);
					break;
				case 1:
					// Get the Argument Type
					label = (String)data.getProperty(IUnoFactoryConstants.TYPE);
					break;
				case 2:
					// Get the Argument IN/OUT property
					label = (String)data.getProperty(IUnoFactoryConstants.ARGUMENT_INOUT);
					break;
			}
			
			return label;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener(ILabelProviderListener listener) {
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty(Object element, String property) {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener(ILabelProviderListener listener) {			
		}
	}
}
