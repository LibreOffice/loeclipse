/*************************************************************************
 *
 * $RCSfile: ChoiceRow.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:07 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Row class that could managed a combo box to select a value among others
 * 
 * @author cbosdonnat
 * @see org.openoffice.ide.eclipse.core.gui.rows.LabeledRow
 *
 */
public class ChoiceRow extends LabeledRow implements ModifyListener{
	
	public ChoiceRow (Composite parent, String property, String label){
		this(parent, property, label, null);
	}
	
	public ChoiceRow (Composite parent, String property, String label, String browse){
		
		super(property);
		Label aLabel = new Label(parent, SWT.NONE);
		aLabel.setText(label);
		
		Combo aField = new Combo(parent, SWT.READ_ONLY);
		aField.addModifyListener(this);
		
		createContent(parent, aLabel, aField, browse);
	}
	
	public void setBrowseSelectionListener(SelectionListener listener){
		if (null != browse){
			browse.addSelectionListener(listener);
		}
	}
	
	//------------- Méthodes de manipulation du combo box
	
	/**
	 * Adds all the strings contained in items at the end of the item list.
	 * 
	 * @param items Array of items to appends to the existing ones.
	 */
	public void addAll(String[] items){
		for (int i=0; i<items.length; i++){
			((Combo)field).add(items[i]);
		}
	}
	
	/**
	 * Adds the provided item at the provided position
	 * 
	 * @param item text of the item to add
	 * @param index position where to add the item in the list
	 * @see Combo#add(java.lang.String, int)
	 */
	public void add(String item, int index){
		((Combo)field).add(item, index);
	}
	
	/**
	 * Append the item at the end of the item list
	 * 
	 * @param item text of the item to append
	 * @see Combo#add(java.lang.String)
	 */
	public void add(String item){
		((Combo)field).add(item);
	}
	
	/**
	 * Removes the items with the provided text
	 * 
	 * @param item text of the items to remove
	 * @see Combo#remove(java.lang.String)
	 */
	public void remove(String item){
		((Combo)field).remove(item);
	}
	
	/**
	 * Remove the item at the position corresponding to index
	 * 
	 * @param index position of the item to remove
	 * @see Combo#remove(int)
	 */
	public void remove(int index){
		((Combo)field).remove(index);
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
		((Combo)field).remove(start, end);
	}
	
	/**
	 * Removes all the items of the combo box
	 * 
	 * @see Combo#removeAll()
	 */
	public void removeAll(){
		((Combo)field).removeAll();
	}
	
	/**
	 * Select the item at the position corresponding to index
	 * 
	 * @param index position of the item to select
	 * @see Combo#select(int)
	 */
	public void select(int index){
		((Combo)field).select(index);
		
		// Fire a modification event to the listener 
		FieldEvent fe = new FieldEvent(this.property, getValue());
		fireFieldChangedEvent(fe);
	}
	
	/**
	 * Set the provided text as the active item if the item is present
	 * in the choice. Otherwise, do nothing.
	 * 
	 * @param text Text of the item to select
	 */
	public void select(String text){
		int result = -1;
		
		Combo cField = ((Combo)field);
		int i = 0;
	    while (i < cField.getItemCount() && -1 == result){
			if (cField.getItem(i).equals(text)){
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
	 */
	public void getItem(int index){
		((Combo)field).getItem(index);
	}

	/**
	 * Returns the number of items of the combo box
	 * 
	 * @return number of items of the combo box
	 * @see Combo#getItemCount()
	 */
	public int getItemCount(){
		return ((Combo)field).getItemCount();
	}
	
	/**
	 * Method that recieve the events from the combo box of the row
	 * 
	 * @param e modification event
	 */
	public void modifyText(ModifyEvent e) {
		FieldEvent fe = new FieldEvent(this.property, getValue());
		fireFieldChangedEvent(fe);
	}

	/**
	 * Returns the selected value
	 */
	public String getValue() {
		String result = null;
		
		int selectedId = ((Combo)field).getSelectionIndex();
		if (-1 != selectedId){
			result = ((Combo)field).getItem(selectedId); 
		}
		return result;
	}
}
