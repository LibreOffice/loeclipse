/*************************************************************************
 *
 * $RCSfile: ChoiceRow.java,v $
 *
 * $Revision: 1.8 $
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
 * @author cedricbosdo
 * @see org.openoffice.ide.eclipse.core.gui.rows.LabeledRow
 *
 */
public class ChoiceRow extends LabeledRow {
    
    private Hashtable<String, String> mTranslations;
        
    /**
     * Create a new choice row. The parent composite should have a grid layout
     * with 2 or 3 horizontal spans.
     * 
     * @param pParent the parent composite where to create the raw 
     * @param pProperty the property name of the raw
     * @param pLabel label the label to print on the left of the raw
     */
    public ChoiceRow (Composite pParent, String pProperty, String pLabel) {
        this(pParent, pProperty, pLabel, null);
    }
    
    /**
     * Create a new choice row with a button on the right. The parent 
     * composite should have a grid layout with 3 horizontal spans.
     * 
     * @param pParent the parent composite where to create the row 
     * @param pProperty the property name of the row
     * @param pLabel label the label to print on the left of the row
     * @param pBrowse the label of the button
     */
    public ChoiceRow (Composite pParent, String pProperty, String pLabel,
            String pBrowse) {
        
        super(pProperty);

        mTranslations = new Hashtable<String, String>();
        
        Label aLabel = new Label(pParent, SWT.NONE);
        aLabel.setText(pLabel);
        
        Combo aField = new Combo(pParent, SWT.READ_ONLY);
        aField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                FieldEvent fe = new FieldEvent(mProperty, getValue());
                fireFieldChangedEvent(fe);
            }
        });
        
        createContent(pParent, aLabel, aField, pBrowse);
    }
    
    /**
     * Create a new choice row.
     * 
     * @param pParent the parent composite
     * @param pProperty the property name to use in events
     */
    public ChoiceRow(Composite pParent, String pProperty) {
        super(pProperty);
        
        mTranslations = new Hashtable<String, String>();

        Combo aField = new Combo(pParent, SWT.READ_ONLY);
        aField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                FieldEvent fe = new FieldEvent(mProperty, getValue());
                fireFieldChangedEvent(fe);
            }
        });
        
        createContent(pParent, null, aField, null);
        
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING); 
        gd.horizontalSpan = ((GridLayout)pParent.getLayout()).numColumns;
        aField.setLayoutData(gd);
    }
    
    /**
     * Set the listener for the browse button action. There is only on
     * listener, because there is generally no need for more.
     * 
     * @param pListener the browse action listener
     */
    public void setBrowseSelectionListener(SelectionListener pListener) {
        if (null != mBrowse) {
            mBrowse.addSelectionListener(pListener);
        }
    }
    
    //------------- Combobox handling methods
    
    /**
     * Adds all the strings contained in items at the end of the item list.
     * 
     * @param pItems Array of items to appends to the existing ones.
     */
    public void addAll(String[] pItems) {
        for (int i = 0; i < pItems.length; i++) {
            ((Combo)mField).add(pItems[i]);
        }
    }
    
  
    /**
     * Adds a translated item.
     *
     * <p>This method adds the text to the combo box and deals with
     * its translation. If the text is already contained in the box,
     * nothing will be done.</p>
     * 
     * @param pText the translated item text
     * @param pValue the item value
     * @param pIndex te item index
     */
    public void add(String pText, String pValue, int pIndex) {
        if (!mTranslations.containsKey(pText)) {
            mTranslations.put(pText, pValue);
            if (pIndex >= 0) {
                ((Combo)mField).add(pText, pIndex);
            } else {
                ((Combo)mField).add(pText);
            }
        }
    }
    
    /**
     * adds an internationalized item at the end of the list.
     * 
     * @param pText the internationalized text
     * @param pValue the value of the item
     * 
     * @see #add(String, String, int)
     */
    public void add(String pText, String pValue) {
        add(pText, pValue, -1);
    }
    
    /**
     * Adds the provided item at the provided position.
     * 
     * @param pItem text of the item to add
     * @param pIndex position where to add the item in the list
     * @see #add(java.lang.String, java.lang.String, int)
     */
    public void add(String pItem, int pIndex) {
        add(pItem, pItem, pIndex);
    }
    
    /**
     * Append the item at the end of the item list.
     * 
     * @param pItem text of the item to append
     * @see #add(java.lang.String, java.lang.String, int)
     */
    public void add(String pItem) {
        add(pItem, pItem, -1);
    }
    
    /**
     * Removes the items with the provided text.
     * 
     * @param pText text of the items to remove
     * @see Combo#remove(java.lang.String)
     */
    public void remove(String pText) {
        mTranslations.remove(pText);
        ((Combo)mField).remove(pText);
    }
    
    /**
     * Remove the item at the position corresponding to index.
     * 
     * @param pIndex position of the item to remove
     * @see Combo#remove(int)
     */
    public void remove(int pIndex) {
        remove(getItem(pIndex));
    }
    
    /**
     * Removes all the items between start and end positions.
     * 
     * @param pStart position of the first item to remove
     * @param pEnd position of the last item to remove
     * 
     * @see Combo#remove(int, int)
     */
    public void remove(int pStart, int pEnd) {
        for (int i = pStart; i < pEnd; i++) {
            remove(i);
        }
    }
    
    /**
     * Removes all the items of the combo box.
     * 
     * @see Combo#removeAll()
     */
    public void removeAll() {
        mTranslations.clear();
        ((Combo)mField).removeAll();
    }
    
    /**
     * Select the item at the position corresponding to index.
     * 
     * @param pIndex position of the item to select
     * @see Combo#select(int)
     */
    public void select(int pIndex) {
        ((Combo)mField).select(pIndex);
        
        // Fire a modification event to the listener 
        FieldEvent fe = new FieldEvent(this.mProperty, getValue());
        fireFieldChangedEvent(fe);
    }
    
    /**
     * Set the provided text as the active item if the item is present
     * in the choice. Otherwise, do nothing.
     * 
     * @param pValue value of the item to select
     */
    public void select(String pValue) {
        int result = -1;
        
        Combo cField = (Combo)mField;
        int i = 0;
        while (i < cField.getItemCount() && -1 == result) {
            if (getValue(i).equals(pValue)) {
                result = i;
            }
            i++;
        }
        cField.select(result);
    }
    
    /**
     * Returns the index the item of the choice.
     * 
     * @param pIndex position of the item to fetch
     * @return the index the item of the choice
     * 
     * @see Combo#getItem(int)
     * 
     * @deprecated This methods only returns the text of the item, use
     *     <code>getValue()</code> to get the selected value.
     */
    public String getItem(int pIndex) {
        return ((Combo)mField).getItem(pIndex);
    }

    /**
     * Returns the number of items of the combo box.
     * 
     * @return number of items of the combo box
     * @see Combo#getItemCount()
     */
    public int getItemCount() {
        return ((Combo)mField).getItemCount();
    }

    /**
     * @return the selected value.
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
     * Returns the value of the i-th item. 
     * 
     * @param pIndex the index of the value to get
     * @return the language independent value of the item
     */
    public String getValue(int pIndex) {
        String result = null;
        
        if (pIndex >= 0 && pIndex < getItemCount()) {
            String text = ((Combo)mField).getItem(pIndex);
            result = text;
            
            String value = mTranslations.get(text);
            if (value != null) {
                result = value;
            }
        }
        return result;
    }
}
