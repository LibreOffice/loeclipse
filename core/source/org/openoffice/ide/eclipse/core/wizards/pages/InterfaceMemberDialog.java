/*************************************************************************
 *
 * $RCSfile: InterfaceMemberDialog.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:29 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.wizards.pages;

import org.eclipse.jface.dialogs.TitleAreaDialog;
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
import org.eclipse.swt.widgets.Group;
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
import org.openoffice.ide.eclipse.core.gui.rows.LabeledRow;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.gui.rows.TypeRow;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;
import org.openoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;
import org.openoffice.ide.eclipse.core.wizards.Messages;

/**
 * Defines an interface member creation or edition dialog. To get the computed
 * data, use the {@link #getData()} method, even after disposing the dialog. 
 * 
 * This class shouldn't be sub-classed.
 * 
 * @author cedricbosdo
 *
 */
public class InterfaceMemberDialog extends TitleAreaDialog implements
        IFieldChangedListener {

    private static final String MEMBER_TYPE = "__member_type"; //$NON-NLS-1$
    private static final String NAME = "__name"; //$NON-NLS-1$
    private static final String TYPE = "__type"; //$NON-NLS-1$
    private static final String BOUND = "__bound"; //$NON-NLS-1$
    private static final String READONLY = "__readonly"; //$NON-NLS-1$
    private static final String PARAM_TYPE = "__param_type"; //$NON-NLS-1$
    private static final String PARAM_INOUT = "__param_inout"; //$NON-NLS-1$
    private static final String PARAM_NAME = "__param_name"; //$NON-NLS-1$
    private static final int WIDTH = 500;
    private static final int HEIGHT = 480;
    private static final int NAME_WITH = 200;
    private static final int TYPE_WIDTH = 170;
    private static final int DIRECTION_WIDTH = 70;
    
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
    
    private String mTitle = ""; //$NON-NLS-1$
    private String mMessage = ""; //$NON-NLS-1$
    
    /**
     * Default constructor to use for member creation.
     */
    public InterfaceMemberDialog() {
        super(Display.getDefault().getActiveShell());
    
        setShellStyle(getShellStyle() | SWT.RESIZE);
        
        // This dialog is a modal one
        setBlockOnOpen(true);
        mTitle = Messages.getString("InterfaceMemberDialog.CreationDialogTitle"); //$NON-NLS-1$
        mMessage = Messages.getString("InterfaceMemberDialog.NewMemberDescription"); //$NON-NLS-1$
        mData = new UnoFactoryData(); 
    }
    
    /**
     * Constructor to use for member edition.
     * 
     * @param pData the member's data to edit
     */
    public InterfaceMemberDialog(UnoFactoryData pData) {
        super(Display.getDefault().getActiveShell());
        setShellStyle(getShellStyle() | SWT.RESIZE);
        
        // This dialog is a modal one
        setBlockOnOpen(true);
        mData = pData;
        
        try {
            int type = ((Integer)mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)).intValue();
            if (type == IUnoFactoryConstants.METHOD) {
                mTitle = Messages.getString("InterfaceMemberDialog.MethodDialogTitle"); //$NON-NLS-1$
                mMessage = Messages.getString("InterfaceMemberDialog.EditMethodDescription"); //$NON-NLS-1$
            } else if (type == IUnoFactoryConstants.ATTRIBUTE) {
                mTitle = Messages.getString("InterfaceMemberDialog.AttributeDialogTitle"); //$NON-NLS-1$
                mMessage = Messages.getString("InterfaceMemberDialog.EditAttributeDescription"); //$NON-NLS-1$
            }
        } catch (NullPointerException e) {
            // No need to log this. 
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void configureShell(Shell pShell) {
        super.configureShell(pShell);
        
        // Just set the correct size of the dialog and center it on the screen
        Rectangle bounds = Display.getDefault().getClientArea();
        pShell.setBounds((bounds.width - WIDTH) / 2, (bounds.height - HEIGHT) / 2, WIDTH, HEIGHT);
    }
    
    /**
     * @return he filled data corresponding to the object.
     */
    public UnoFactoryData getData() {
        return mData;
    }
    
    /**
     * Disposes the unused data.
     */
    public void disposeData() {
        if (mData != null) {
            mData.dispose();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected Control createDialogArea(Composite pParent) {
        
        setTitle(mTitle);
        setMessage(mMessage);
        
        Composite body = new Composite(pParent, SWT.None);
        body.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
        body.setLayoutData(new GridData(GridData.FILL_BOTH));
        
        String type = ""; //$NON-NLS-1$
        if (mData != null) {
            if (null != mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)) {
                Integer iType = (Integer)mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE);
                if (iType != null) {
                    switch (iType.intValue()) {
                        case IUnoFactoryConstants.METHOD:
                            type = "method"; //$NON-NLS-1$
                            // Has to be the opposite to show it the first time
                            mShowAttribute = true;
                            break;
                        case IUnoFactoryConstants.ATTRIBUTE:
                            type = "attribute"; //$NON-NLS-1$
                            // Has to be the opposite to show it the first time
                            mShowAttribute = false;
                            break;
                        default:
                            type = ""; //$NON-NLS-1$
                    }
                }
            }
        }
        
        createCommonRows(body, type.equals(""));
        
        mSpecificPanel = new Composite(body, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = LabeledRow.LAYOUT_COLUMNS;
        mSpecificPanel.setLayoutData(gd);
        mSpecificPanel.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
        
        // Method or Attribute specific fields
        if (type.equals("")) { //$NON-NLS-1$
            type = mMemberTypeRow.getValue();
        }
        
        showSpecificControls(type.equals("attribute")); //$NON-NLS-1$
        
        return body;
    }
    
    /**
     * Create the dialog fields which are common to the attribute and method inputs.
     * 
     * @param pParent the composite parent where to create the fields
     * @param pCreateTypeSelector <code>true</code> if the type selector should be created, 
     *          <code>false</code> otherwise.
     */
    private void createCommonRows(Composite pParent, boolean pCreateTypeSelector) {
        // Common rows
        if (pCreateTypeSelector) { // $NON-NLS-1$ //$NON-NLS-1$
            
            Composite typeComposite = new Composite(pParent, SWT.NONE);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = LabeledRow.LAYOUT_COLUMNS;
            typeComposite.setLayoutData(gd);
            typeComposite.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
            
            mMemberTypeRow = new ChoiceRow(typeComposite, MEMBER_TYPE);
            mMemberTypeRow.add(
                    Messages.getString("InterfaceMemberDialog.MethodChoice"), "method"); //$NON-NLS-1$ //$NON-NLS-2$
            mMemberTypeRow.add(
                    Messages.getString("InterfaceMemberDialog.AttributeChoice"), //$NON-NLS-1$ 
                    "attribute"); //$NON-NLS-1$
            mMemberTypeRow.select(0);
            mMemberTypeRow.setFieldChangedListener(this);
            mData.setProperty(IUnoFactoryConstants.MEMBER_TYPE, 
                    Integer.valueOf(IUnoFactoryConstants.METHOD));
            // Has to be the opposite to show it the first time
            mShowAttribute = true;
            
            Label sep = new Label(typeComposite, SWT.SEPARATOR | SWT.HORIZONTAL);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = LabeledRow.LAYOUT_COLUMNS;
            sep.setLayoutData(gd);
        }
        
        mNameRow = new TextRow(pParent, NAME, Messages.getString("InterfaceMemberDialog.Name")); //$NON-NLS-1$
        if (mData != null) {
            String name = (String)mData.getProperty(IUnoFactoryConstants.NAME);
            if (name != null) {
                mNameRow.setValue(name);
            }
        }
        mNameRow.setFieldChangedListener(this);
        
        String typeLabel = Messages.getString("InterfaceMemberDialog.Type"); //$NON-NLS-1$
        if (mShowAttribute) {
            typeLabel = Messages.getString("InterfaceMemberDialog.ReturnType"); //$NON-NLS-1$
        }
        
        mTypeRow = new TypeRow(pParent, TYPE, typeLabel,
                InternalUnoType.ALL_TYPES);
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
    }

    /**
     * This method cleans up the specific composite of all its children and 
     * recreate the controls for the new type (attribute or method).
     *
     * @param pIsAttribute flag defining whether to show the method or attribute
     *         controls.
     */
    protected void showSpecificControls(boolean pIsAttribute) {
        
        if (mShowAttribute != pIsAttribute) {
        
            // Cleans up the previous controls
            Control[] children = mSpecificPanel.getChildren();
            for (int i = 0; i < children.length; i++) {
                children[i].dispose();
            }
            
            // Creates the new controls
            if (pIsAttribute) {
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
     * Creates the field rows specific to the attributes.
     * 
     * @param pParent the composite parent in which to create the controls.
     */
    protected void createAttributeControls(Composite pParent) {
        
        mTypeRow.setLabel(Messages.getString("InterfaceMemberDialog.Type")); //$NON-NLS-1$
        
        mReadonlyRow = new BooleanRow(pParent, READONLY, 
                Messages.getString("InterfaceMemberDialog.Readonly")); //$NON-NLS-1$
        mReadonlyRow.setTooltip(Messages.getString("InterfaceMemberDialog.ReadonlyTooltip")); //$NON-NLS-1$
        mReadonlyRow.setFieldChangedListener(this);
        
        mBoundRow = new BooleanRow(pParent, BOUND, Messages.getString("InterfaceMemberDialog.Bound")); //$NON-NLS-1$
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
     * Creates the field rows specific to the Methods.
     * 
     * @param pParent the composite parent in which to create the controls.
     * 
     */
    protected void createMethodControls(Composite pParent) {
        
        mTypeRow.setLabel(Messages.getString("InterfaceMemberDialog.ReturnType")); //$NON-NLS-1$
        
        Group group = new Group(pParent, SWT.SHADOW_NONE);
        group.setText(Messages.getString("InterfaceMemberDialog.ArgumentsTitle")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = LabeledRow.LAYOUT_COLUMNS;
        group.setLayoutData(gd);
        group.setLayout(new GridLayout());
        
        // create an arguments table
        Table table = new Table(group, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setLinesVisible(false);
        table.setHeaderVisible(true);
        table.setToolTipText(Messages.getString("InterfaceMemberDialog.ArgumentTableTooltip")); //$NON-NLS-1$
        
        // Create the columns
        TableColumn column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
        column.setText(Messages.getString("InterfaceMemberDialog.ArgumentNameColumnTitle")); //$NON-NLS-1$
        column.setWidth(NAME_WITH);
        column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
        column.setText(Messages.getString("InterfaceMemberDialog.ArgumentTypeColumnTitle")); //$NON-NLS-1$
        column.setWidth(TYPE_WIDTH);
        column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
        column.setWidth(DIRECTION_WIDTH);
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
        
        createMethodButtons(group);
    }
    
    /**
     * Create the buttons method arguments add and remove buttons.
     * 
     * @param pParent the composite where to create the buttons.
     */
    private void createMethodButtons(Composite pParent) {
        // Create the Add-Edit / Remove buttons
        Composite buttonComposite = new Composite(pParent, SWT.None);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gd.horizontalSpan = LabeledRow.LAYOUT_COLUMNS;
        buttonComposite.setLayoutData(gd);
        buttonComposite.setLayout(new GridLayout(LabeledRow.LAYOUT_COLUMNS, false));
        
        mAddButton = new Button(buttonComposite, SWT.NORMAL);
        mAddButton.setText(Messages.getString("InterfaceMemberDialog.New")); //$NON-NLS-1$
        mAddButton.setLayoutData(new GridData());
        mAddButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
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
        mDelButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent pEvent) {
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

    /**
     * {@inheritDoc}
     */
    public void fieldChanged(FieldEvent pEvent) {
        if (pEvent.getProperty().equals(MEMBER_TYPE)) {
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
        } else if (pEvent.getProperty().equals(NAME)) {
            mData.setProperty(IUnoFactoryConstants.NAME, pEvent.getValue().trim());
        } else if (pEvent.getProperty().equals(TYPE)) {
            mData.setProperty(IUnoFactoryConstants.TYPE, pEvent.getValue().trim());
        } else if (pEvent.getProperty().equals(BOUND)) {
            toggleFlag("bound");
        } else if (pEvent.getProperty().equals(READONLY)) {
            toggleFlag("readonly");
        }
    }
    
    /**
     * Toggle the flag property in the options.
     * 
     * @param pFlag the flag to toggle (<code>bound</code> or <code>readonly</code>).
     */
    private void toggleFlag(String pFlag) {
        String flags = (String)mData.getProperty(IUnoFactoryConstants.FLAGS);
        if (flags != null && flags.contains(pFlag)) {
            if (! mBoundRow.getBooleanValue()) {
                // remove the flag
                flags = flags.replace(pFlag, "").trim(); //$NON-NLS-1$
            }
        } else {
            if (mBoundRow.getBooleanValue()) {
                // Set the flag
                if (flags == null) {
                    flags = ""; //$NON-NLS-1$
                }
                flags += " " + pFlag; //$NON-NLS-1$
                flags = flags.trim();
            }
        }
        mData.setProperty(IUnoFactoryConstants.FLAGS, flags);
    }

    /**
     * Class providing an access to the inner data of the uno factory data for
     * the method arguments table.
     * 
     * @author cedricbosdo
     */
    class ParamContentProvider implements IStructuredContentProvider {

        /**
         * {@inheritDoc}
         */
        public Object[] getElements(Object pInputElement) {
            return mData.getInnerData();
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
        }

        /**
         * {@inheritDoc}
         */
        public void inputChanged(Viewer pViewer, Object pOldInput, Object pNewInput) {
        }
        
    }
    
    /**
     * Simply provides the values access for the cell editors of the method
     * arguments table.
     * 
     * @author cedricbosdo
     */
    class ParamCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        public boolean canModify(Object pElement, String pProperty) {
            return pElement instanceof UnoFactoryData && (pProperty.equals(PARAM_TYPE) ||
                    pProperty.equals(PARAM_NAME) || pProperty.equals(PARAM_INOUT));
        }

        /**
         * {@inheritDoc}
         */
        public Object getValue(Object pElement, String pProperty) {
            Object value = null;
            if (pElement instanceof UnoFactoryData) {
                UnoFactoryData data = (UnoFactoryData)pElement;
                
                if (pProperty.equals(PARAM_NAME)) {
                    // get the value of the name
                    value = data.getProperty(IUnoFactoryConstants.NAME);
                } else if (pProperty.equals(PARAM_TYPE)) {
                    // get the value of the type
                    value = data.getProperty(IUnoFactoryConstants.TYPE);
                } else if (pProperty.equals(PARAM_INOUT)) {
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
            if (value == null) {
                value = ""; //$NON-NLS-1$
            }
            
            return value;
        }

        /**
         * {@inheritDoc}
         */
        public void modify(Object pElement, String pProperty, Object pValue) {
            if (((TableItem)pElement).getData() instanceof UnoFactoryData) {
                UnoFactoryData data = (UnoFactoryData)((TableItem)pElement).getData();
                if (pProperty.equals(PARAM_NAME) && pValue instanceof String) {
                    // set the value of the name
                    data.setProperty(IUnoFactoryConstants.NAME, pValue);
                    mArgumentTableViewer.setInput(mData);
                } else if (pProperty.equals(PARAM_TYPE) && pValue instanceof String) {
                    // set the value of the type
                    data.setProperty(IUnoFactoryConstants.TYPE, pValue);
                    mArgumentTableViewer.setInput(mData);
                } else if (pProperty.equals(PARAM_INOUT) && pValue instanceof Integer) {
                    // set the value of the direction
                    String direction = getDirectionFromId(((Integer)pValue).intValue());
                    data.setProperty(IUnoFactoryConstants.ARGUMENT_INOUT, direction);
                    mArgumentTableViewer.setInput(mData);
                }
            }
        }

        /**
         * Utility method translating the direction items position in the list-box
         * into the direction text.
         * 
         * @param pId the item position
         * 
         * @return the direction text
         */
        private String getDirectionFromId(int pId) {
            String direction = null;
            switch (pId) {
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
            return direction;
        }
    }
    
    /**
     * Simply provides the label for the method arguments table.
     * 
     * @author cedricbosdo
     */
    class ParamLabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        public Image getColumnImage(Object pElement, int pColumnIndex) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        public String getColumnText(Object pElement, int pColumnIndex) {
            String label = null;
            UnoFactoryData data = (UnoFactoryData)pElement;
            
            switch (pColumnIndex) {
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

        /**
         * {@inheritDoc}
         */
        public void addListener(ILabelProviderListener pListener) {
        }

        /**
         * {@inheritDoc}
         */
        public void dispose() {
        }

        /**
         * {@inheritDoc}
         */
        public boolean isLabelProperty(Object pElement, String pProperty) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        public void removeListener(ILabelProviderListener pListener) {            
        }
    }
}
