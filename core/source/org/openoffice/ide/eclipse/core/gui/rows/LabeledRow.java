/*************************************************************************
 *
 * $RCSfile: LabeledRow.java,v $
 *
 * $Revision: 1.9 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/12/07 08:47:17 $
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

/**
 * Basic class for a property row. Subclasses will override:
 * <ul>
 *   <li>setValue</li>
 *   <li>getValue</li>
 * </ul>
 * Their constructor should respect the following steps:
 * <ol>
 *   <li>Use the property constructor</li>
 *   <li>Create the label and field controls to be used</li>
 *   <li>Call createContents</li>
 * </ol>
 * 
 * @author cedricbosdo
 *
 */
public abstract class LabeledRow {

    public static final int LAYOUT_COLUMNS = 3;
    private static final int FIELD_WIDTH = 10;

    protected Control mLabel;
    protected Control mField;
    protected Button  mBrowse;
    protected String  mProperty;

    protected IFieldChangedListener mListener;
    
    /**
     * Simple constructor only defining the property. This constructor should
     * only be called by the subclasses.
     * 
     * @param pProperty property value given in the field changed event.
     */
    public LabeledRow(String pProperty) {
        this.mProperty = pProperty;
    }
    
    /**
     * Create a field base. This constructor may not be used by subclasses.
     * 
     * @param pParent Composite in which the row will be added
     * @param pProperty Property value given in the field changed event.
     * @param pLabel Control to use for the label. The most common is a text 
     *              control, but it could be something else like an hyperlink.
     * @param pField Control containing the field data.
     * @param pBrowseText Button text. If <code>null</code>, the button isn't 
     *              created.
     */
    public LabeledRow(Composite pParent, String pProperty, Control pLabel,
                      Control pField, String pBrowseText) {
        this.mProperty = pProperty;
        createContent(pParent, pLabel, pField, pBrowseText);
    }
    
    /**
     * Set the tooltip message of the row.
     * 
     * @param pTooltip the tooltip message
     */
    public void setTooltip(String pTooltip) {
        mField.setToolTipText(pTooltip);
    }
    
    /**
     * Replace the current label by a new one.
     * 
     * @param pNewLabel New label to use
     */
    public void setLabel(String pNewLabel) {
        ((Label)mLabel).setText(pNewLabel);
        mLabel.pack(true);
        mLabel.getParent().layout(true);
    }
    
    /**
     * Returns the row label.
     * 
     * @return the row label
     */
    public String getLabel() {
        return ((Label)mLabel).getText();
    }
    
    /**
     * Stores the row controls, creates the button if its text
     * is not <code>null</code> and layout the controls.
     * 
     * @param pParent the parent composite where to create the controls
     * @param pLabel the control for the label
     * @param pField the control for the field
     * @param pBrowseText the text to show on the right button of the row.
     */
    protected void createContent(Composite pParent, Control pLabel,
              Control pField, String pBrowseText) {
        this.mLabel = pLabel;
        this.mField = pField;
        if (null != pBrowseText) {
            mBrowse = new Button(pParent, SWT.PUSH);
            mBrowse.setText(pBrowseText);
        }
        fillRow(pParent);
    }
    
    /**
     * Property getter.
     * 
     * @return the property
     */
    public String getProperty() {
        return this.mProperty;
    }
    
    /**
     * Get or calculate the value of this property.
     * 
     * @return the property value
     */
    public abstract String getValue();
    
    /**
     * Method organizing the different graphic components in the parent 
     * composite.
     * 
     * @param pParent Parent composite.
     */
    protected void fillRow(Composite pParent) {
        Layout layout = pParent.getLayout();
        
        if (layout instanceof GridLayout) {
            // Supposes that the parent layout is a Grid one  
            int span = ((GridLayout)layout).numColumns - 1;
            
            if (mLabel != null) {
                mLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
            }
            
            int fspan = span;
            if (mBrowse != null) { 
                fspan = span - 1;
            }
            
            if (mLabel == null) {
                fspan = fspan - 1;
            }

            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = fspan;
            gd.grabExcessHorizontalSpace = 1 == fspan;
            gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
            gd.widthHint = FIELD_WIDTH;
            mField.setLayoutData(gd);
            
            if (mBrowse != null) {
                mBrowse.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
                        GridData.VERTICAL_ALIGN_CENTER));
            }
        }
    }

    
    /**
     * Defines the listener that will react to the field changes.
     * 
     * @param pListener field changes listener
     */
    public void setFieldChangedListener(IFieldChangedListener pListener) {
        this.mListener = pListener;
    }
    
    /**
     * Removes the field changes listener.
     */
    public void  removeFieldChangedlistener() {
        mListener = null;
    }
    
    /**
     * Fires a change of the row.
     * 
     * @param pEvent the event to throw for the change
     */
    protected void fireFieldChangedEvent(FieldEvent pEvent) {
        if (null != mListener) {
            mListener.fieldChanged(pEvent);
        }
    }
    
    /**
     * Toggle the visibily of the line.
     * 
     * @param pVisible if <code>true</code> the components will visible, otherwise
     *                they will be hidden.
     */
    public void setVisible(boolean pVisible) {
        
        GridData gd = (GridData)mLabel.getLayoutData();
        gd.exclude = !pVisible;
        mLabel.setLayoutData(gd);
        
        gd = (GridData)mField.getLayoutData();
        gd.exclude = !pVisible;
        mField.setLayoutData(gd);
        
        if (null != mBrowse) {
            gd = (GridData)mBrowse.getLayoutData();
            gd.exclude = !pVisible;
            mBrowse.setLayoutData(gd);
        }

        mLabel.setVisible(pVisible);
        mField.setVisible(pVisible);
        if (mBrowse != null) {
            mBrowse.setVisible(pVisible);
        }
    }
    
    /**
     * Set the enabled state of the field and the browse button if
     * the latter exists.
     * 
     * @param pEnabled <code>true</code> activate the row, otherwise the
     *                row is desactivated
     */
    public void setEnabled(boolean pEnabled) {
        mField.setEnabled(pEnabled);
        if (null != mBrowse) {
            mBrowse.setEnabled(pEnabled);
        }
    }
}
