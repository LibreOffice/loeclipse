/*************************************************************************
 *
 * $RCSfile: TypeRow.java,v $
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

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.openoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeBrowser;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;

public class TypeRow extends TextRow {

	private InternalUnoType mSelectedType;
	private int mType = 0;
	
	public TypeRow(Composite parent, String property, String label, int aType) {
		super(parent, property, label);
		
		if (aType >=0 && aType < 512) {
			mType = aType;
		}
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.rows.LabeledRow#createContent(org.eclipse.swt.widgets.Composite, org.eclipse.swt.widgets.Control, org.eclipse.swt.widgets.Control, java.lang.String)
	 */
	protected void createContent(Composite parent, Control label, 
			Control field, String browseText) {

		super.createContent(parent, label, field, Messages.getString("TypeRow.Browse")); //$NON-NLS-1$
		
		final Shell shell = parent.getShell();
		
		((Button)mBrowse).addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				UnoTypeProvider typesProvider = UnoTypeProvider.getInstance();
				
				int oldType = typesProvider.getTypes();
				typesProvider.setTypes(mType);
				
				UnoTypeBrowser browser = new UnoTypeBrowser(
						shell, typesProvider);
				browser.setSelectedType(mSelectedType);
				
				if (UnoTypeBrowser.OK == browser.open()) {
					mSelectedType = browser.getSelectedType();
					if (null != mSelectedType){
						setValue(mSelectedType.getFullName());
					}
				}
				
				typesProvider.setTypes(oldType);
			}
		});
		
		((Button)mBrowse).addDisposeListener(new DisposeListener(){

			public void widgetDisposed(DisposeEvent e) {
				UnoTypeProvider.getInstance().stopProvider();
			}
			
		});
	}
}
