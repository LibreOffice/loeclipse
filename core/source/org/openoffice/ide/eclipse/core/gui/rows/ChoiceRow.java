/*************************************************************************
 *
 * $RCSfile: ChoiceRow.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/04 18:17:05 $
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
package org.openoffice.ide.eclipse.core.gui.rows;

import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Row class that could managed a combo box to select a value among others
 * 
 * <p>In order to use this class correctly, please add items and define the
 * default one. As every row type, don't forget to set the Modification
 * listener to be notified of the value changes. This class supports 
 * internationalized items since the version 1.0.3.
 * </p>
 * 
 * @author cbosdonnat
 * @see org.openoffice.ide.eclipse.core.gui.rows.LabeledRow
 *
 */
public class ChoiceRow extends LabeledRow {
	
    private Hashtable<String, String> mTranslations;
        
    /**
     * Create a new choice row. The parent composite should have a grid layout
	 * with 2 or 3 horizontal spans.
     * 
     * @param parent the parent composite where to create the raw 
     * @param property the property name of the raw
     * @param label label the label to print on the left of the raw
     */
	public ChoiceRow (Composite parent, String property, String label){
		this(parent, property, label, null);
	}
	
	/**
     * Create a new choice row with a button on the right. The parent 
     * composite should have a grid layout with 3 horizontal spans.
     * 
     * @param parent the parent composite where to create the row 
     * @param property the property name of the row
     * @param label label the label to print on the left of the row
     * @param browse the label of the button
     */
	public ChoiceRow (Composite parent, String property, String label,
			String browse){
		
		super(property);

        mTranslations = new Hashtable<String, String>();
        
		Label aLabel = new Label(parent, SWT.NONE);
		aLabel.setText(label);
		
		Combo aField = new Combo(parent, SWT.READ_ONLY);
		aField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FieldEvent fe = new FieldEvent(mProperty, getValue());
				fireFieldChangedEvent(fe);
			}
		});
		
		createContent(parent, aLabel, aField, browse);
	}
	
	public ChoiceRow(Composite parent, String property) {
		super(property);
		
		mTranslations = new Hashtable<String, String>();

		Combo aField = new Combo(parent, SWT.READ_ONLY);
		aField.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FieldEvent fe = new FieldEvent(mProperty, getValue());
				fireFieldChangedEvent(fe);
			}
		});
		
		createContent(parent, null, aField, null);
		
		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING); 
		gd.horizontalSpan = ((GridLayout)parent.getLayout()).numColumns;
		aField.setLayoutData(gd);
	}
	
	/**
	 * Set the listener for the browse button action. There is only on
	 * listener, because there is generally no need for more.
	 * 
	 * @param listener the browse action listener
	 */
	public void setBrowseSelectionListener(SelectionListener listener){
		if (null != mBrowse){
			mBrowse.addSelectionListener(listener);
		}
	}
	
	//------------- Combobox handlin methods
	
	/**
	 * Adds all the strings contained in items at the end of the item list.
	 * 
	 * @param items Array of items to appends to the existing ones.
	 */
	public void addAll(String[] items){
		for (int i=0; i<items.length; i++){
			((Combo)mField).add(items[i]);
		}
	}
	
  
    /**
     * Adds a translated item.
     *
     * <p>This method adds the text to the combo box and deals with
     * its translation. If the text is alread contained in the box,
     * nothing will be done.</p>
     */
    public void add(String text, String value, int index){
        if (!mTranslations.containsKey(text)) {
            mTranslations.put(text, value);
            if (index >= 0) {
                ((Combo)mField).add(text, index);
            } else {
                ((Combo)mField).add(text);
            }
        }
    }
    
    /**
     * adds an internationalized item at the end of the list 
     * 
     * @param text the internationalized text
     * @param value the value of the item
     * @see #add(String, String, int)
     */
    public void add(String text, String value) {
    	add(text, value, -1);
    }
    
	/**
	 * Adds the provided item at the provided position
	 * 
	 * @param item text of the item to add
	 * @param index position where to add the item in the list
     * @see #add(java.lang.String, java.lang.String, int)
	 */
	public void add(String item, int index){
    	add(item, item, index);
	}
	
	/**
	 * Append the item at the end of the item list
	 * 
	 * @param item text of the item to append
     * @see #add(java.lang.String, java.lang.String, int)
	 */
	public void add(String item){
		add(item, item, -1);
	}
	
	/**
	 * Removes the items with the provided text
	 * 
	 * @param text text of the items to remove
	 * @see Combo#remove(java.lang.String)
	 */
	public void remove(String text){
        mTranslations.remove(text);
		((Combo)mField).remove(text);
	}
	
	/**
	 * Remove the item at the position corresponding to index
	 * 
	 * @param index position of the item to remove
	 * @see Combo#remove(int)
	 */
	public void remove(int index){
        remove(getItem(index));
	}
	
	/**
	 * Removes all the items between start and end positions
	 * 
	 * @param start position of the first item to remove
	 * @param end position of the last item to remove
	 * 
	 * @see Combo#remove(int, int)
	 */
	public void remove(int start, int end){
        for (int i=start; i<end; i++){
		    remove(i);
        }
	}
	
	/**
	 * Removes all the items of the combo box
	 * 
	 * @see Combo#removeAll()
	 */
	public void removeAll(){
        mTranslations.clear();
		((Combo)mField).removeAll();
	}
	
	/**
	 * Select the item at the position corresponding to index
	 * 
	 * @param index position of the item to select
	 * @see Combo#select(int)
	 */
	public void select(int index){
		((Combo)mField).select(index);
		
		// Fire a modification event to the listener 
		FieldEvent fe = new FieldEvent(this.mProperty, getValue());
		fireFieldChangedEvent(fe);
	}
	
	/**
	 * Set the provided text as the active item if the item is present
	 * in the choice. Otherwise, do nothing.
	 * 
	 * @param value value of the item to select
	 */
	public void select(String value){
		int result = -1;
		
		Combo cField = ((Combo)mField);
		int i = 0;
	    while (i < cField.getItemCount() && -1 == result){
			if (getValue(i).equals(value)){
				result = i;
			}
			i++;
	    }
		cField.select(result);
	}
	
	/**
	 * Returns the index th item of the choice
	 * 
	 * @param index position of the item to fetch
	 * @see Combo#getItem(int)
     * @deprecated This methods only returns the text of the item, use
     *     <code>getValue()</code> to get the selected value.
	 */
	public String getItem(int index){
		return ((Combo)mField).getItem(index);
	}

	/**
	 * Returns the number of items of the combo box
	 * 
	 * @return number of items of the combo box
	 * @see Combo#getItemCount()
	 */
	public int getItemCount(){
		return ((Combo)mField).getItemCount();
	}

	/**
	 * Returns the selected value.
     *
     * @since 1.0.3 
     *      This method returns the language independent value of the item
	 */
	public String getValue() {
		String result = null; 
		
		int selectedId = ((Combo)mField).getSelectionIndex();
		if (-1 != selectedId) {
			result = getValue(selectedId);
		}
		
		return result;
	}
	
	/**
	 * Returns the value of the ith item. 
	 * 
	 * @param i the index of the value to get
	 * @return the language independent value of the item
	 */
	public String getValue(int i) {
		String result = null;
		
		if (i >= 0 && i < getItemCount()){
			String text = ((Combo)mField).getItem(i);
			result = text;
			
			String value = mTranslations.get(text);
			if (value != null) {
				result = value;
			}
		}
		return result;
	}
}
