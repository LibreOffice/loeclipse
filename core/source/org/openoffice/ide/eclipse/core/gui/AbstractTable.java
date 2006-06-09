/*************************************************************************
 *
 * $RCSfile: AbstractTable.java,v $
 *
 * $Revision: 1.3 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:14:05 $
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
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;

/**
 * Abstract table structure used in the plugin. This avoid to rewrite to many 
 * times the same code for basic table managment.
 * 
 * <p>In order to create a new table class, the following methods should be
 * overridden:
 * 	<ul>
 * 		<li>{@link #addLine()} to customize the action performed when clicking
 * 			on the <em>Add</em> button.</li>
 * 		<li>{@link #removeLine()} to customize the action performed when 
 * 			clicking on the <em>Del</em> button.</li>
 * 		<li>{@link #handleDoubleClick(DoubleClickEvent)} to customize the
 * 			action performed on a doucle click on the table.</li>
 * 		<li>{@link #createCellEditors(Table)} to customize how to edit the
 * 			cells of the differents columns of the table.</li>
 * 	</ul>
 * </p>
 * 
 * @author cbosdonnat
 *
 */
public class AbstractTable extends Composite implements ISelectionProvider {
	
	/**
	 * Constructor for a generic table. The number of columns is the minimum
	 * of the length of the three arrays in parameter.
	 * 
	 * @param parent the parent composite where to add the table
	 * @param aTitle a title for the table
	 * @param colTitles an array with the colums titles 
	 * @param colWidths an array with the columns width
	 * @param colProperties an array with the columns properties
	 */
	public AbstractTable(Composite parent, String aTitle, String[] colTitles, 
			int[] colWidths, String[] colProperties) {
		super(parent, SWT.NONE);
		
		title = aTitle;
		int nbTitles = colTitles.length;
		int nbWidths = colWidths.length;
		int nbProperties = colProperties.length;
		
		int min = Math.min(nbTitles, Math.min(nbProperties, nbWidths));
		columnProperties = new String[min];
		columnTitles = new String[min];
		columnWidths = new int[min];
		
		for (int i=0; i<min; i++){
			columnProperties[i] = colProperties[i];
			columnWidths[i] = colWidths[i];
			columnTitles[i] = colTitles[i];
		}
		
		columnProperties = colProperties;
		columnTitles = colTitles;
		columnWidths = colWidths;
		
		createContent();
		createColumns();
	}
	
	/**
	 * Convenient method to get the table lines.
	 * 
	 * @return a vector containing the {@link ITableElement} objects 
	 *			representing the lines.
	 */
	public Vector getLines(){
		return lines;
	}
	
	/**
	 * Adding a line to the table model.
	 * 
	 * @param element the line to add.
	 */
	protected void addLine(ITableElement element) {
		lines.add(element);
		tableViewer.add(element);
		tableViewer.refresh();
	}
	
	/**
	 * Creates and layout all the graphic components of the table.
	 */
	protected void createContent(){
		// Creates the layout of the composite with 2 columns and extended at it's maximum size
		setLayout(new GridLayout(2, false));
		setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label sdkLabel = new Label(this, SWT.NONE);
		sdkLabel.setText(title);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		sdkLabel.setLayoutData(gd);
		
		createTable();
		createTableViewer();
		createButtons();
		
		tableViewer.setInput(this);
	}

	/**
	 * Method called to configure the columns cell editors. This method should
	 * be overridden in order to set customized editors. The default action is
	 * to return <code>null</code> to indicate that no editing is alloweded.
	 * 
	 * @param table the table for which to create the cell editors, ie the
	 * 		internal table object of this class.
	 * 
	 * @return the cell editors in the order of the columns
	 */
	protected CellEditor[] createCellEditors(Table table){
		return null;
	}
	
	/**
	 * Method called after an action on the <em>Add</em> button. This method
	 * should be overridden to customize the table.
	 * 
	 * @return the new table line to add.
	 */
	protected ITableElement addLine(){
		return null;
	}
	
	/**
	 * Method called after an action on the <em>Del</em> button. This method
	 * should be overridden to customize the table.
	 * 
	 * @return the table line removed or <code>null</code> if none was removed.
	 */
	protected ITableElement removeLine(){
		
		IStructuredSelection selection = (IStructuredSelection)tableViewer.
												getSelection();
		ITableElement toRemove = null;
		
		if (!selection.isEmpty()){
			if (selection.getFirstElement() instanceof ITableElement){
				toRemove = (ITableElement)selection.getFirstElement();
			}
		}
		return toRemove;
	}
	
	/**
	 * Method called when a double click event has been raised by the table.
	 * This implementation doesn't perform any action and is intended to be
	 * overridden.
	 *  
	 * @param event the double click event raised
	 */
	protected void handleDoubleClick(DoubleClickEvent event){
	}
	
	/**
	 * Creates the table component.
	 */
	private void createTable(){
		table = new Table(this, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		
		// The table uses two lines of the layout because of the two buttons Add and Del
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.verticalSpan = 2;
		table.setLayoutData(gd);
		
		// Sets the graphical properties of the line
		table.setLinesVisible(false);
		table.setHeaderVisible(true);
	}

	/**
	 * Creates and configure the table viewer which will render its content
	 */
	private void createTableViewer() {
		// Creates the table viewer
		tableViewer = new TableViewer(table);
		
		// Sets the column properties to know which column is edited afterwards
		tableViewer.setColumnProperties(columnProperties);
		
		// Manages the label to print in the cells from the model
		tableViewer.setLabelProvider(new AbstractLabelProvider());
		
		tableViewer.setContentProvider(new AbstractContentProvider());
		
		tableViewer.setCellEditors(createCellEditors(table));
		tableViewer.setCellModifier(new AbstractCellModifier());
		
		// Listen to a double clic to popup an edition dialog
		tableViewer.addDoubleClickListener(new IDoubleClickListener(){

			public void doubleClick(DoubleClickEvent event) {
				handleDoubleClick(event);
			}
			
		});
	}	
	
	/**
	 * Creates and configure the Add and Del button components.
	 */
	private void createButtons() {
		// Creates the two buttons ADD and DEL
		add = new Button(this, SWT.NONE);
		add.setText(OOEclipsePlugin.getTranslationString(I18nConstants.ADD));
		GridData gdAdd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING |
				                      GridData.HORIZONTAL_ALIGN_FILL);
		add.setLayoutData(gdAdd);
		add.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				ITableElement element = addLine();
				
				if (null != element){
					lines.add(element);
					tableViewer.add(element);
					tableViewer.refresh();
				}
			}
		});
		
		del = new Button(this, SWT.NONE);
		del.setText(OOEclipsePlugin.getTranslationString(I18nConstants.DEL));
		GridData gdDel = new GridData(GridData.VERTICAL_ALIGN_BEGINNING |
                					  GridData.HORIZONTAL_ALIGN_FILL);
		del.setLayoutData(gdDel);
		del.addSelectionListener(new SelectionAdapter(){

			public void widgetSelected(SelectionEvent e) {
				ITableElement element = removeLine();
				
				lines.remove(element);
				tableViewer.remove(element);
				tableViewer.refresh();
			}
		});
	}
	
	/**
	 * Creates and configures all the table columns
	 *
	 */
	private void createColumns(){
		for (int i=0, length=Math.min(columnWidths.length, 
				columnTitles.length); i<length; i++){
			TableColumn column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
			column.setWidth(columnWidths[i]);
			column.setText(columnTitles[i]);
		}
	}
	
	private Vector lines = new Vector();
	
	// Components private instances
	
	protected Table table;
	
	protected TableViewer tableViewer;
	
	private Button add;
	
	private Button del;
	
	// Columns configuration
	
	private String[] columnTitles;
	
	private int[] columnWidths;
	
	private String[] columnProperties;
	
	private String title;
	
	/**
	 * Provides the content of the table. The main method used here is the
	 * {@link #getElements(Object)} one which returns all the 
	 * {@link ITableElement} lines.
	 * 
	 * @author cbosdonnat
	 *
	 */
	private class AbstractContentProvider implements IStructuredContentProvider {

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return lines.toArray();
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// nothing to do here
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do here
		}
		
	}
	
	/**
	 * This class is responsible to handle the different editon actions 
	 * performed on the table cells. This uses the 
	 * {@link ITableElement#canModify(String)}, {@link ITableElement#getValue(String)}
	 * and {@link ITableElement#setValue(String, Object)}.
	 * 
	 * @author cbosdonnat
	 */
	private class AbstractCellModifier implements ICellModifier {

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			boolean result = false;
			
			if (element instanceof ITableElement){
				result = ((ITableElement)element).canModify(property);
			}
			return result;
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			Object value = null;
			
			if (element instanceof ITableElement){
				value = ((ITableElement)element).getValue(property);
			}
			return value; 
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			
			TableItem item = (TableItem)element;
			
			if (item.getData() instanceof ITableElement){
				((ITableElement)item.getData()).setValue(property, value);
				tableViewer.refresh();
			}
		}
		
	}
	
	/**
	 * The class responsible to provide the labels and images for each 
	 * table cell. This class will use the {@link ITableElement#getLabel(String)}
	 * and {@link ITableElement#getImage(String)} methods.
	 * 
	 * @author cbosdonnat
	 */
	private class AbstractLabelProvider extends LabelProvider 
										implements ITableLabelProvider {

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null; 
			
			if (element instanceof ITableElement){
				image = ((ITableElement)element).getImage(columnProperties[columnIndex]);
			}
			return image;
		}

		/*
		 *  (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			String text = null; 
			
			if (element instanceof ITableElement){
				text = ((ITableElement)element).getLabel(columnProperties[columnIndex]);
			}
			return text;
		}
	}

	//------------------------------------- Implementation of ISelectionListener
	
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.addSelectionChangedListener(listener);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
	 */
	public ISelection getSelection() {
		return tableViewer.getSelection();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
	 */
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		tableViewer.removeSelectionChangedListener(listener);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
	 */
	public void setSelection(ISelection selection) {
		tableViewer.setSelection(selection);
	}
}
