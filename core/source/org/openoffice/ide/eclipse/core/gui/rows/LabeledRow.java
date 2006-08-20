/*************************************************************************
 *
 * $RCSfile: LabeledRow.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/08/20 11:56:00 $
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
 * @author cbosdonnat
 *
 */
public abstract class LabeledRow {
		
	protected Control mLabel;
	protected Control mField;
	protected Button  mBrowse;
	protected String  mProperty;

	protected IFieldChangedListener mListener;
	
	/**
	 * Simple constructor only defining the property. This constructor should
	 * only be called by the subclasses.
	 * 
	 * @param property property value given in the field changed event.
	 */
	public LabeledRow(String property){
		this.mProperty = property;
	}
	
	/**
	 * Create a field base. This constructor may not be used by subclasses.
	 * 
	 * @param parent Composite in which the row will be added
	 * @param property Property value given in the field changed event.
	 * @param label Control to use for the label. The most common is a text 
	 *              control, but it could be something else like an hyperlink.
	 * @param field Control containing the field data.
	 * @param browseText Button text. If <code>null</code>, the button isn't 
	 *              created.
	 */
	public LabeledRow(Composite parent, String property, Control label,
			          Control field, String browseText){
		this.mProperty = property;
		createContent(parent, label, field, browseText);
	}
	
	/**
	 * Replace the current label by a new one.
	 * 
	 * @param newLabel New label to use
	 */
	public void setLabel(String newLabel){
		((Label)mLabel).setText(newLabel);
		mLabel.pack(true);
		mLabel.getParent().layout(true);
	}
	
	/**
	 * @see LabeledRow#LabeledRow(Composite, String, Control, Control, String)
	 */
	protected void createContent(Composite parent, Control label,
	          Control field, String browseText){
		this.mLabel = label;
		this.mField = field;
		if (null != browseText){
			mBrowse = new Button(parent, SWT.PUSH);
			mBrowse.setText(browseText);
		}
		fillRow(parent);
	}
	
	/**
	 * Property getter
	 */
	public String getProperty(){
		return this.mProperty;
	}
	
	/**
	 * Get or calculate the value of this property.
	 */
	public abstract String getValue();
	
	/**
	 * Method organizing the different graphic components in the parent 
	 * composite.
	 * 
	 * @param parent Parent composite.
	 */
	protected void fillRow(Composite parent){
		Layout layout = parent.getLayout();
		
		if (layout instanceof GridLayout){
			// Supposes that the parent layout is a Grid one  
			int span = ((GridLayout)layout).numColumns - 1;
			
			mLabel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_CENTER));
			
			int fspan = mBrowse != null ? span -1 : span;

			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = fspan;
			gd.grabExcessHorizontalSpace = (1 == fspan);
			gd.verticalAlignment = GridData.VERTICAL_ALIGN_CENTER;
			gd.widthHint = 10;
			mField.setLayoutData(gd);
			
			if (mBrowse != null){
				mBrowse.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL |
						GridData.VERTICAL_ALIGN_CENTER));
			}
		}
	}

	
	/**
	 * Defines the listener that will react to the field changes.
	 * 
	 * @param listener field changes listener
	 */
	public void setFieldChangedListener(IFieldChangedListener listener){
		this.mListener = listener;
	}
	
	/**
	 * Removes the field changes listener.
	 */
	public void  removeFieldChangedlistener(){
		mListener = null;
	}
	
	/**
	 * Fires a change of the row
	 * 
	 * @param e the event to throw for the change
	 */
	protected void fireFieldChangedEvent(FieldEvent e){
		if (null != mListener){
			mListener.fieldChanged(e);
		}
	}
	
	/**
	 * Toggle the visibily of the line.
	 * 
	 * @param visible if <code>true</code> the components will visible, otherwise
	 *                they will be hidden.
	 */
	public void setVisible(boolean visible){
		
		GridData gd = (GridData)mLabel.getLayoutData();
		gd.exclude = !visible;
		mLabel.setLayoutData(gd);
		
		gd = (GridData)mField.getLayoutData();
		gd.exclude = !visible;
		mField.setLayoutData(gd);
		
		if (null != mBrowse){
			gd = (GridData)mBrowse.getLayoutData();
			gd.exclude = !visible;
			mBrowse.setLayoutData(gd);
		}

		mLabel.setVisible(visible);
		mField.setVisible(visible);
		if (mBrowse != null){
			mBrowse.setVisible(visible);
		}
	}
	
	/**
	 * Set the enabled state of the field and the browse button if
	 * the latter exists.
	 * 
	 * @param enabled <code>true</code> activate the row, otherwise the
	 *                row is desactivated
	 */
	public void setEnabled(boolean enabled){
		mField.setEnabled(enabled);
		if (null != mBrowse){
			mBrowse.setEnabled(enabled);
		}
	}
}
