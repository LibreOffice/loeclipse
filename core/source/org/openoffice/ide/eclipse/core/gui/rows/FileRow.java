/*************************************************************************
 *
 * $RCSfile: FileRow.java,v $
 *
 * $Revision: 1.3 $
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
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;

/**
 * GUI row for a file selection. It supports only the Grid Layout
 * and can be configured to select either a file or a directory.
 * 
 * @author cbosdonnat
 *
 */
public class FileRow extends LabeledRow{
	
	private String mValue = new String();
	private boolean mDirectory = false;
	
	/**
	 * File row contructor.
	 * 
	 * @param parent composite parent of the row.
	 * @param property property name used in field changing event.
	 * @param label label to print on the left of the row.
	 * @param directory if <code>true</code>, the field is a directory path, 
	 *                  otherwise the field is a file path.
	 */
	public FileRow (Composite parent, String property, String label, 
			boolean directory){
		super(property);
		
		Label aLabel = new Label(parent, SWT.SHADOW_NONE | SWT.LEFT);
		aLabel.setText(label);
		
		Text aField = new Text(parent, SWT.BORDER);
			
		createContent(parent, aLabel, aField,
				Messages.getString("FileRow.Browse")); //$NON-NLS-1$
		
		mField.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e){
				setValue(getValue());
			}
		});
		
		mBrowse.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
		});
		
		mDirectory = directory;
	}
	
	/**
	 * Method called when the button browse is clicked
	 */
	protected void browse() {
		BusyIndicator.showWhile(mBrowse.getDisplay(), new Runnable(){
			public void run() {
				doOpenFileSelectionDialog();
			}
		});
	}

	/**
	 * Open the File selection dialog
	 */
	protected void doOpenFileSelectionDialog() {
		Shell shell = OOEclipsePlugin.getDefault().getWorkbench().
								getActiveWorkbenchWindow().getShell();
		
		String newFile = null;
		
		if (mDirectory){
			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			
			String file = getValue();
			if (file != null){
				dialog.setText(Messages.getString("FileRow.DirectoryTitle")); //$NON-NLS-1$
				
				newFile = dialog.open();
			}
			
		} else {
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			
			String file = getValue();
			if (file != null){
				dialog.setFileName(file);
			}
			
			dialog.setText(Messages.getString("FileRow.FileTitle")); //$NON-NLS-1$

			newFile = dialog.open();
		}
		
		if (newFile != null) {
			setValue(newFile);
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.rows.LabeledRow#getValue()
	 */
	public String getValue() {
		return mValue;
	}
	
	/**
	 * Set a new value to the row
	 * 
	 * @param aValue the new value
	 */
	public void setValue(String aValue){

		((Text)mField).setText(aValue);
		mValue = aValue;
		FieldEvent fe = new FieldEvent (getProperty(), getValue());
		fireFieldChangedEvent(fe);
	}
}
