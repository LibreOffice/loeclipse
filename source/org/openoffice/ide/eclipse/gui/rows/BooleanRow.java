/*************************************************************************
 *
 * $RCSfile: BooleanRow.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:23 $
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
package org.openoffice.ide.eclipse.gui.rows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class BooleanRow extends LabeledRow {
	
	private boolean value;
	
	public BooleanRow(Composite parent, String property, String label) {
		super(property);
		
		Button checkbox = new Button(parent, SWT.CHECK);
		checkbox.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
				toggleValue();
			};
		});
		checkbox.setText(label);
		
		Label text = new Label(parent, SWT.NONE);
		
		createContent(parent, checkbox,text, null);
	}

	public void setLabel(String newLabel){
		((Label)field).setText(newLabel);
	}
	
	public void setValue(boolean aValue){
		if (value != aValue){
			((Button)label).setSelection(aValue);
			toggleValue();
		}
	}
	
	public void toggleValue(){
		value = !value;
		fireFieldChangedEvent(new FieldEvent(property, getValue()));
	}
	
	public boolean getBooleanValue(){
		return value;
	}
	
	public String getValue() {
		return Boolean.toString(value);
	}

	public void setEnabled(boolean enabled) {
		((Button)label).setEnabled(enabled);
	}
}
