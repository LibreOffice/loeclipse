/*************************************************************************
 *
 * $RCSfile: TypeRow.java,v $
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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeBrowser;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;

public class TypeRow extends TextRow {

	private UnoTypeProvider typesProvider;
	private int type = 0;
	
	public TypeRow(Composite parent, String property, String label, 
			   UnoTypeProvider aTypeProvider, int aType) {
		super(parent, property, label);
		
		if (aType >=0 && aType < 512) {
			type = aType;
		}
		typesProvider = aTypeProvider;
	}
	
	protected void createContent(Composite parent, Control label, 
			Control field, String browseText) {

		super.createContent(parent, label, field, 
				OOEclipsePlugin.getTranslationString(I18nConstants.BROWSE));
		
		final Shell shell = parent.getShell();
		
		((Button)browse).addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				
				int oldType = typesProvider.getTypes();
				typesProvider.setTypes(type);
				
				UnoTypeBrowser browser = new UnoTypeBrowser(
						shell, typesProvider);
				
				if (UnoTypeBrowser.OK == browser.open()) {
					String selectedType = browser.getSelectedType();
					if (null != selectedType){
						String[] splittedType = selectedType.split(" ");
						
						if (2 == splittedType.length) {
							setValue(splittedType[0]);
						}
					}
				}
				
				typesProvider.setTypes(oldType);
			}
		});
	}
}
