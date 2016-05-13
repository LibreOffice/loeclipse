/*************************************************************************
 *
 * $RCSfile: AbstractTable.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:28 $
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

package org.libreoffice.ide.eclipse.core.gui;

import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Abstract table structure used in the plugin. This avoid to rewrite to many times the same code for basic table
 * managment.
 *
 * <p>
 * In order to create a new table class, the following methods should be overridden:
 * <ul>
 * <li>{@link #addLine()} to customize the action performed when clicking on the <em>Add</em> button.</li>
 * <li>{@link #removeLine()} to customize the action performed when clicking on the <em>Del</em> button.</li>
 * <li>{@link #handleDoubleClick(DoubleClickEvent)} to customize the action performed on a doucle click on the table.
 * </li>
 * <li>{@link #createCellEditors(Table)} to customize how to edit the cells of the differents columns of the table.</li>
 * </ul>
 * </p>
 *
 *
 */
public class AbstractTable extends Composite implements ISelectionProvider {

    protected Table mTable;

    protected TableViewer mTableViewer;

    private Vector<ITableElement> mLines = new Vector<ITableElement>();

    private Button mAdd;

    private Button mDel;

    // Columns configuration

    private String[] mColumnTitles;

    private int[] mColumnWidths;

    private String[] mColumnProperties;

    private String mTitle;

    /**
     * Constructor for a generic table. The number of columns is the minimum of the length of the three arrays in
     * parameter.
     *
     * @param pParent
     *            the parent composite where to add the table
     * @param pTitle
     *            a title for the table
     * @param pColTitles
     *            an array with the colums titles
     * @param pColWidths
     *            an array with the columns width
     * @param pColProperties
     *            an array with the columns properties
     */
    public AbstractTable(Composite pParent, String pTitle, String[] pColTitles, int[] pColWidths,
        String[] pColProperties) {
        super(pParent, SWT.NONE);

        mTitle = pTitle;
        int nbTitles = pColTitles.length;
        int nbWidths = pColWidths.length;
        int nbProperties = pColProperties.length;

        int min = Math.min(nbTitles, Math.min(nbProperties, nbWidths));
        mColumnProperties = new String[min];
        mColumnTitles = new String[min];
        mColumnWidths = new int[min];

        for (int i = 0; i < min; i++) {
            mColumnProperties[i] = pColProperties[i];
            mColumnWidths[i] = pColWidths[i];
            mColumnTitles[i] = pColTitles[i];
        }

        mColumnProperties = pColProperties;
        mColumnTitles = pColTitles;
        mColumnWidths = pColWidths;

        createContent();
        createColumns();
    }

    /**
     * Cleans up the table after having used it.
     */
    @Override
    public void dispose() {
        if (mLines != null) {
            mLines.clear();
        }
    }

    /**
     * Convenient method to get the table lines.
     *
     * @return a vector containing the {@link ITableElement} objects representing the lines.
     */
    public Vector<ITableElement> getLines() {
        return mLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setToolTipText(String pString) {
        mTableViewer.getTable().setToolTipText(pString);
    }

    /**
     * Adding a line to the table model.
     *
     * @param pElement
     *            the line to add.
     */
    protected void addLine(ITableElement pElement) {
        mLines.add(pElement);
        mTableViewer.add(pElement);
        mTableViewer.refresh();
    }

    /**
     * Creates and layout all the graphic components of the table.
     */
    protected void createContent() {
        // Creates the layout of the composite with 2 columns and extended at it's maximum size
        setLayout(new GridLayout(2, false));
        setLayoutData(new GridData(GridData.FILL_BOTH));

        Label sdkLabel = new Label(this, SWT.NONE);
        sdkLabel.setText(mTitle);

        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        sdkLabel.setLayoutData(gd);

        createTable();
        createTableViewer();
        createButtons();

        mTableViewer.setInput(this);
    }

    /**
     * Method called to configure the columns cell editors. This method should be overridden in order to set customized
     * editors. The default action is to return <code>null</code> to indicate that no editing is allowed.
     *
     * @param pTable
     *            the table for which to create the cell editors, i.e. the internal table object of this class.
     *
     * @return the cell editors in the order of the columns
     */
    protected CellEditor[] createCellEditors(Table pTable) {
        return null;
    }

    /**
     * Method called after an action on the <em>Add</em> button. This method should be overridden to customize the
     * table.
     *
     * @return the new table line to add.
     */
    protected ITableElement addLine() {
        return null;
    }

    /**
     * Method called after an action on the <em>Del</em> button. This method should be overridden to customize the
     * table.
     *
     * @return the table line removed or <code>null</code> if none was removed.
     */
    protected ITableElement removeLine() {

        IStructuredSelection selection = (IStructuredSelection) mTableViewer.getSelection();
        ITableElement toRemove = null;

        if (!selection.isEmpty()) {
            if (selection.getFirstElement() instanceof ITableElement) {
                toRemove = (ITableElement) selection.getFirstElement();
            }
        }
        return toRemove;
    }

    /**
     * Method called when a double click event has been raised by the table. This implementation doesn't perform any
     * action and is intended to be overridden.
     *
     * @param pEvent
     *            the double click event raised
     */
    protected void handleDoubleClick(DoubleClickEvent pEvent) {
    }

    /**
     * Creates the table component.
     */
    private void createTable() {
        mTable = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);

        // The table uses two lines of the layout because of the two buttons Add and Del
        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        gd.verticalSpan = 2;
        mTable.setLayoutData(gd);

        // Sets the graphical properties of the line
        mTable.setLinesVisible(false);
        mTable.setHeaderVisible(true);
    }

    /**
     * Creates and configure the table viewer which will render its content.
     */
    private void createTableViewer() {
        // Creates the table viewer
        mTableViewer = new TableViewer(mTable);

        // Sets the column properties to know which column is edited afterwards
        mTableViewer.setColumnProperties(mColumnProperties);

        // Manages the label to print in the cells from the model
        mTableViewer.setLabelProvider(new AbstractLabelProvider());

        mTableViewer.setContentProvider(new AbstractContentProvider());

        mTableViewer.setCellEditors(createCellEditors(mTable));
        mTableViewer.setCellModifier(new AbstractCellModifier());

        // Listen to a double clic to popup an edition dialog
        mTableViewer.addDoubleClickListener(new IDoubleClickListener() {

            @Override
            public void doubleClick(DoubleClickEvent pEvent) {
                handleDoubleClick(pEvent);
            }

        });
    }

    /**
     * Creates and configure the Add and Del button components.
     */
    private void createButtons() {
        // Creates the two buttons ADD and DEL
        mAdd = new Button(this, SWT.NONE);
        mAdd.setText(Messages.getString("AbstractTable.Add")); //$NON-NLS-1$
        GridData gdAdd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
        mAdd.setLayoutData(gdAdd);
        mAdd.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                ITableElement element = addLine();

                if (null != element) {
                    mLines.add(element);
                    mTableViewer.add(element);
                    mTableViewer.refresh();
                }
            }
        });

        mDel = new Button(this, SWT.NONE);
        mDel.setText(Messages.getString("AbstractTable.Del")); //$NON-NLS-1$
        GridData gdDel = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.HORIZONTAL_ALIGN_FILL);
        mDel.setLayoutData(gdDel);
        mDel.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                ITableElement element = removeLine();

                mLines.remove(element);
                mTableViewer.remove(element);
                mTableViewer.refresh();
            }
        });
    }

    /**
     * Creates and configures all the table columns.
     *
     */
    private void createColumns() {
        for (int i = 0, length = Math.min(mColumnWidths.length, mColumnTitles.length); i < length; i++) {
            TableColumn column = new TableColumn(mTable, SWT.RESIZE | SWT.LEFT);
            column.setWidth(mColumnWidths[i]);
            column.setText(mColumnTitles[i]);
        }
    }

    /**
     * Provides the content of the table. The main method used here is the {@link #getElements(Object)} one which
     * returns all the {@link ITableElement} lines.
     *
     *
     */
    private class AbstractContentProvider implements IStructuredContentProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object pInputElement) {
            return mLines.toArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            // nothing to do here
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inputChanged(Viewer pViewer, Object pOldInput, Object pNewInput) {
            // Nothing to do here
        }

    }

    /**
     * This class is responsible to handle the different editon actions performed on the table cells. This uses the
     * {@link ITableElement#canModify(String)}, {@link ITableElement#getValue(String)} and
     * {@link ITableElement#setValue(String, Object)}.
     *
     */
    private class AbstractCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canModify(Object pElement, String pProperty) {
            boolean result = false;

            if (pElement instanceof ITableElement) {
                result = ((ITableElement) pElement).canModify(pProperty);
            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(Object pElement, String pProperty) {
            Object value = null;

            if (pElement instanceof ITableElement) {
                value = ((ITableElement) pElement).getValue(pProperty);
            }
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void modify(Object pElement, String pProperty, Object pValue) {

            TableItem item = (TableItem) pElement;

            if (item.getData() instanceof ITableElement) {
                ((ITableElement) item.getData()).setValue(pProperty, pValue);
                mTableViewer.refresh();
            }
        }

    }

    /**
     * The class responsible to provide the labels and images for each table cell. This class will use the
     * {@link ITableElement#getLabel(String)} and {@link ITableElement#getImage(String)} methods.
     *
     */
    private class AbstractLabelProvider extends LabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Image getColumnImage(Object pElement, int pColumnIndex) {
            Image image = null;

            if (pElement instanceof ITableElement) {
                image = ((ITableElement) pElement).getImage(mColumnProperties[pColumnIndex]);
            }
            return image;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnText(Object pElement, int pColumnIndex) {
            String text = null;

            if (pElement instanceof ITableElement) {
                text = ((ITableElement) pElement).getLabel(mColumnProperties[pColumnIndex]);
            }
            return text;
        }
    }

    // ------------------------------------- Implementation of ISelectionProvider

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSelectionChangedListener(ISelectionChangedListener pListener) {
        mTableViewer.addSelectionChangedListener(pListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISelection getSelection() {
        return mTableViewer.getSelection();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeSelectionChangedListener(ISelectionChangedListener pListener) {
        mTableViewer.removeSelectionChangedListener(pListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(ISelection pSelection) {
        mTableViewer.setSelection(pSelection);
    }
}
