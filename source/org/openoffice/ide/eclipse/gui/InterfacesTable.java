/*************************************************************************
 *
 * $RCSfile: InterfacesTable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:16 $
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
package org.openoffice.ide.eclipse.gui;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.unotypebrowser.UnoTypeBrowser;
import org.openoffice.ide.eclipse.unotypebrowser.UnoTypeProvider;

/**
 * This class corresponds to the table of interface inheritances. It shouldn't
 * be subclassed.
 * 
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class InterfacesTable extends AbstractTable {


	private UnoTypeProvider typesProvider;
	
	public InterfacesTable(Composite parent, UnoTypeProvider aTypesProvider) {
		super(
				parent, 
				OOEclipsePlugin.getTranslationString(
						I18nConstants.INHERITED_INTERFACE), 
				new String[] {
					OOEclipsePlugin.getTranslationString(
							I18nConstants.OPT_TITLE),
					OOEclipsePlugin.getTranslationString(
							I18nConstants.INTERFACE_NAME_TITLE)
				},
				new int[] {20, 200}, 
				new String[] {
					InheritanceLine.OPTIONAL,
					InheritanceLine.NAME
				}
		); 
		
		typesProvider = aTypesProvider;
	}

	public void addInterface(String ifaceName, boolean optional) {
		InheritanceLine line = new InheritanceLine();
		line.interfaceName = ifaceName;
		line.optional = optional;
		
		addLine(line);
	}
	
	protected CellEditor[] createCellEditors(Table table) {
		CellEditor[] editors = new CellEditor[] {
			new CheckboxCellEditor(),
			null
		};
				
		return editors;
	}
	
	protected ITableElement addLine() {
		ITableElement line = null;
		
		// Saving the current types filtering
		int oldType = typesProvider.getTypes();
		
		// Ask for interfaces only
		typesProvider.setTypes(UnoTypeProvider.INTERFACE);
		
		// Launching the UNO Type Browser
		UnoTypeBrowser browser = new UnoTypeBrowser(getShell(), typesProvider);
		if (UnoTypeBrowser.OK == browser.open()) {
			
			String value = null;
			
			String selectedType = browser.getSelectedType();
			if (null != selectedType){
				String[] splittedType = selectedType.split(" ");
				
				if (2 == splittedType.length) {
					value = splittedType[0];
				}
			}
			
			// Creates the line only if OK has been pressed
			line = new InheritanceLine();
			((InheritanceLine)line).setInterfaceName(value);
		}
		
		// Restoring the old types filtering
		typesProvider.setTypes(oldType);
		
		return line;
	}
	
	public class InheritanceLine implements ITableElement {
		
		public static final String OPTIONAL = "__optional";
		public static final String NAME = "__name";
		
		private String interfaceName;
		private boolean optional = false;
		
		//----------------------------------------------------- Member managment
		
		public String getInterfaceName() {
			return interfaceName;
		}
		
		public boolean isOptional() {
			return optional;
		}
		
		public void setInterfaceName(String interfaceName) {
			this.interfaceName = interfaceName;
		}
		
		public void setOptional(boolean optional) {
			this.optional = optional;
		}
		
		//----------------------------------------- ITableElement implementation
		
		public Image getImage(String property) {
			Image image = null;
			
			if (property.equals(OPTIONAL)) {
				if (isOptional()) {
					image = OOEclipsePlugin.getImage(ImagesConstants.CHECKED);
				} else {
					image = OOEclipsePlugin.getImage(ImagesConstants.UNCHECKED);
				}
			}
			return image;
		}
		
		public String getLabel(String property) {
			String label = null;
			
			if (property.equals(NAME)) {
				label = getInterfaceName().toString();
			}
			return label;
		}
		
		public String[] getProperties() {
			return new String[] {
					OPTIONAL,
					NAME
			};
		}
		
		public boolean canModify(String property) {
			
			return property.equals(OPTIONAL);
		}
		
		public Object getValue(String property) {
			Object result = null;
			
			if (property.equals(OPTIONAL)) {
				result = Boolean.valueOf(isOptional());
			}
			return result;
		}
		
		public void setValue(String property, Object value) {
			
			if (property.equals(OPTIONAL) && value instanceof Boolean) {
				
				setOptional(((Boolean)value).booleanValue());
			}
		}
	}
}
