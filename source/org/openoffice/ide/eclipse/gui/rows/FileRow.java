/*************************************************************************
 *
 * $RCSfile: FileRow.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/07/18 19:36:05 $
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
package org.openoffice.ide.eclipse.gui.rows;

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
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class FileRow extends LabeledRow{
	
	private String value = new String();
	private boolean directory = false;
	
	/**
	 * 
	 * @param parent
	 * @param property
	 * @param label
	 * @param directory if <code>true</code>, the field is a directory path, otherwise the
	 *                  field is a file path
	 */
	public FileRow (Composite parent, String property, String label, boolean directory){
		super(property);
		
		Label aLabel = new Label(parent, SWT.SHADOW_NONE | SWT.LEFT);
		aLabel.setText(label);
		
		Text aField = new Text(parent, SWT.BORDER);
			
		createContent(parent, aLabel, aField,OOEclipsePlugin.getTranslationString(I18nConstants.BROWSE));
		
		field.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e){
				setFile(getFile());
			}
		});
		
		
		browse.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
		});
		
		this.directory = directory;
	}
	
	protected void browse() {
		BusyIndicator.showWhile(browse.getDisplay(), new Runnable(){
			public void run() {
				doOpenFileSelectionDialog();
			}
		});
	}

	protected void doOpenFileSelectionDialog() {
		Shell shell = OOEclipsePlugin.getDefault().getWorkbench().
								getActiveWorkbenchWindow().getShell();
		
		String newFile = null;
		
		if (directory){
			DirectoryDialog dialog = new DirectoryDialog(shell, SWT.OPEN);
			
			String file = getFile();
			if (file != null){
				dialog.setText(OOEclipsePlugin.getTranslationString(I18nConstants.DIR_SELECT_TITLE));
				
				newFile = dialog.open();
			}
			
		} else {
			FileDialog dialog = new FileDialog(shell, SWT.OPEN);
			
			String file = getFile();
			if (file != null){
				dialog.setFileName(file);
			}
			
			dialog.setText(OOEclipsePlugin.getTranslationString(I18nConstants.FILE_SELECT_TITLE));

			newFile = dialog.open();
		}
		
		if (newFile != null) {
			setFile(newFile);
		}
	}

	public void setFile(String filePath){
		((Text)field).setText(filePath);
		setValue(filePath);
	}
	
	public String getFile(){
		return getValue();
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String aValue){
		value = aValue;
		FieldEvent fe = new FieldEvent (getProperty(), getValue());
		fireFieldChangedEvent(fe);
	}
}
