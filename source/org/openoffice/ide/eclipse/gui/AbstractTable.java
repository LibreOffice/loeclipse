/*************************************************************************
 *
 * $RCSfile: AbstractTable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:41 $
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

import java.util.Vector;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICellModifier;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;

public class AbstractTable extends Composite {
	
	protected Table table;
	
	protected TableViewer tableViewer;
	
	private Button add;
	
	private Button del;
	
	private String[] columnTitles;
	
	private int[] columnWidths;
	
	private String[] columnProperties;
	
	private String title;
	
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
	
	protected CellEditor[] createCellEditors(Table table){
		return null;
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
	
	private void createColumns(){
		for (int i=0, length=Math.min(columnWidths.length, 
				columnTitles.length); i<length; i++){
			TableColumn column = new TableColumn(table, SWT.RESIZE | SWT.LEFT);
			column.setWidth(columnWidths[i]);
			column.setText(columnTitles[i]);
		}
	}
	
	protected ITableElement addLine(){
		return null;
	}
	
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
	
	protected void handleDoubleClick(DoubleClickEvent event){
	}
	
	private Vector lines = new Vector();
	
	public Vector getLines(){
		return lines;
	}
	
	private class AbstractContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return lines.toArray();
		}

		public void dispose() {
			// nothing to do here
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Nothing to do here
		}
		
	}
	
	private class AbstractCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			boolean result = false;
			
			if (element instanceof ITableElement){
				result = ((ITableElement)element).canModify(property);
			}
			return result;
		}

		public Object getValue(Object element, String property) {
			Object value = null;
			
			if (element instanceof ITableElement){
				value = ((ITableElement)element).getValue(property);
			}
			return value; 
		}

		public void modify(Object element, String property, Object value) {
			
			TableItem item = (TableItem)element;
			
			if (item.getData() instanceof ITableElement){
				((ITableElement)item.getData()).setValue(property, value);
				tableViewer.refresh();
			}
		}
		
	}
	
	private class AbstractLabelProvider extends LabelProvider 
										implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null; 
			
			if (element instanceof ITableElement){
				image = ((ITableElement)element).getImage(columnProperties[columnIndex]);
			}
			return image;
		}

		public String getColumnText(Object element, int columnIndex) {
			String text = null; 
			
			if (element instanceof ITableElement){
				text = ((ITableElement)element).getLabel(columnProperties[columnIndex]);
			}
			return text;
		}
	}
}