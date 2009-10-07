/*************************************************************************
 *
 * $RCSfile: TextRow.java,v $
 *
 * $Revision: 1.6 $
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
package org.openoffice.ide.eclipse.core.gui.rows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * GUI row for a text input. It supports only the Grid Layout
 * and can be extended to manage more complex texts.
 * 
 * @see org.openoffice.ide.eclipse.core.gui.rows.FileRow
 *         for a file row based on this class
 * @see org.openoffice.ide.eclipse.core.gui.rows.TypeRow
 *         for a UNO type selection row based on this class 
 * 
 * @author cedricbosdo
 *
 */
public class TextRow extends LabeledRow
                     implements FocusListener, KeyListener {
    
    private String mValue = new String();
    private String mOldValue;
    
    /**
     * Create a new text row.
     * 
     * @param pParent the parent composite where to create the row
     * @param pProperty the property name of the row's value
     * @param pLabel the label of the row
     */
    public TextRow(Composite pParent, String pProperty, String pLabel) {
        super(pProperty);
        
        Label aLabel = new Label(pParent, SWT.LEFT | SWT.SHADOW_NONE);
        aLabel.setText(pLabel);
        Text aField = new Text(pParent, SWT.BORDER);
        
        createContent(pParent, aLabel, aField, null, false);
        mField.addFocusListener(this);
        mField.addKeyListener(this);
    }
    
    /**
     * {@inheritDoc}
     */
    public void focusGained(FocusEvent pEvent) {
        // Ne fait rien...
    }

    /**
     * {@inheritDoc}
     */
    public void focusLost(FocusEvent pEvent) {
        if (!((Text)mField).getText().equals(mValue)) {
            setValue(((Text)mField).getText());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    public void keyPressed(KeyEvent pEvent) {
        mOldValue = ((Text)mField).getText();
    }

    /**
     * {@inheritDoc}
     */
    public void keyReleased(KeyEvent pEvent) {
        if (pEvent.getSource().equals(mField)) {
            if (!((Text)mField).getText().equals(mOldValue)) {
                if (!((Text)mField).getText().equals(mValue)) {
                    setValue(((Text)mField).getText());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getValue() {
        return mValue;
    }
    
    /**
     * Set the value of the row.
     * 
     * @param pValue the value to set
     */
    public void setValue(String pValue) {
        String newText = pValue;
        if (null == pValue) {
            newText = ""; //$NON-NLS-1$
        }
        
        if (!((Text)mField).getText().equals(newText)) {
            ((Text)mField).setText(newText);
        }
        
        mValue = newText;
        FieldEvent fe = new FieldEvent(getProperty(), getValue());
        fireFieldChangedEvent(fe);
    }
    
    /**
     * Sets the focus on the row.
     */
    public void setFocus() {
        
        Text textField = (Text)mField;
        textField.setFocus();
        
        // Makes the cursor to go at the end of the text
        textField.setSelection(textField.getText().length());
    }
}
