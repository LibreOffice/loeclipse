/*************************************************************************
 *
 * $RCSfile: TextRow.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:14:06 $
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
 * 		for a file row based on this class
 * @see org.openoffice.ide.eclipse.core.gui.rows.TypeRow
 * 		for a Uno type selection row based on this class 
 * 
 * @author cbosdonnat
 *
 */
public class TextRow extends LabeledRow
					 implements FocusListener, KeyListener {
	
	private String value = new String();
	
	public TextRow(Composite parent, String property, String label){
		super(property);
		
		Label aLabel = new Label(parent, SWT.LEFT | SWT.SHADOW_NONE);
		aLabel.setText(label);
		Text aField = new Text(parent, SWT.BORDER);
		
		createContent(parent, aLabel, aField, null);
		field.addFocusListener(this);
		field.addKeyListener(this);
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
	 */
	public void focusGained(FocusEvent e) {
		// Ne fait rien...
	}

	/**
	 * Cette méthode vérifie si la valeur a changée et envoie un evenement
	 * au listener si tel est le cas.
	 * 
	 * @param e FocusEvent recu
	 */
	public void focusLost(FocusEvent e) {
		if (!((Text)field).getText().equals(value)){
			setValue(((Text)field).getText());
		}
	}
	
	private String oldValue;
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyPressed(KeyEvent e) {
		oldValue = ((Text)field).getText();
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events.KeyEvent)
	 */
	public void keyReleased(KeyEvent e) {
		if (e.getSource().equals(field)) {
			if (!((Text)field).getText().equals(oldValue)){
				if (!((Text)field).getText().equals(value)){
					setValue(((Text)field).getText());
				}
			}
		}
	}

	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.rows.LabeledRow#getValue()
	 */
	public String getValue() {
		return value;
	}
	
	public void setValue(String aValue){
		String newText = aValue;
		if (null == aValue){
			newText = "";
		}
		
		if (!((Text)field).getText().equals(newText)){
			((Text)field).setText(newText);
		}
		
		value = newText;
		FieldEvent fe = new FieldEvent(getProperty(), getValue());
		fireFieldChangedEvent(fe);
	}
	
	public void setFocus(){
		
		Text textField = ((Text)field);
		textField.setFocus();
		
		// Makes the cursor to go at the end of the text
		textField.setSelection(textField.getText().length());
	}
}
