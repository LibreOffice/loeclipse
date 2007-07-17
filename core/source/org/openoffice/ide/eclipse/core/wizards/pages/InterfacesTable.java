/*************************************************************************
 *
 * $RCSfile: InterfacesTable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/07/17 21:01:02 $
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
package org.openoffice.ide.eclipse.core.wizards.pages;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.AbstractTable;
import org.openoffice.ide.eclipse.core.gui.ITableElement;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeBrowser;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;
import org.openoffice.ide.eclipse.core.wizards.Messages;

/**
 * This class corresponds to the table of interface inheritances. The add
 * action launches the UNO Type browser to select one interface. This class 
 * shouldn't be subclassed. 
 * 
 * @author cbosdonnat
 */
public class InterfacesTable extends AbstractTable {

	
	/**
	 * Simplified constructor for this kind of table. It uses a types provider
	 * in order to fetch the UNO types earlier than showing the UNO type browser.
	 * This way it avoids a too long UI freeze time.
	 * 
	 * @param parent the parent composite where to put the table
	 */
	public InterfacesTable(Composite parent) {
		super(
				parent, 
				Messages.getString("InterfacesTable.Title"),  //$NON-NLS-1$
				new String[] {
					Messages.getString("InterfacesTable.OptionalTitle"), //$NON-NLS-1$
					Messages.getString("InterfacesTable.NameTitle") //$NON-NLS-1$
				},
				new int[] {25, 400}, 
				new String[] {
					InheritanceLine.OPTIONAL,
					InheritanceLine.NAME
				}
		);
	}

	/**
	 * Add a new interface in the table
	 * 
	 * @param ifaceName the name of the interface to add
	 * @param optional <code>true</code> if the interface is optional.
	 */
	public void addInterface(String ifaceName, boolean optional) {
		InheritanceLine line = new InheritanceLine();
		line.interfaceName = ifaceName;
		line.optional = optional;
		
		addLine(line);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#createCellEditors(org.eclipse.swt.widgets.Table)
	 */
	protected CellEditor[] createCellEditors(Table table) {
		CellEditor[] editors = new CellEditor[] {
			new CheckboxCellEditor(),
			null
		};
				
		return editors;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#addLine()
	 */
	protected ITableElement addLine() {
		ITableElement line = null;
		
		// Saving the current types filtering
		UnoTypeProvider typesProvider = UnoTypeProvider.getInstance();
		int oldType = typesProvider.getTypes();
		
		// Ask for interfaces only
		typesProvider.setTypes(IUnoFactoryConstants.INTERFACE);
		
		// Launching the UNO Type Browser
		UnoTypeBrowser browser = new UnoTypeBrowser(getShell(), typesProvider);
		if (UnoTypeBrowser.OK == browser.open()) {
			
			String value = null;
			
			InternalUnoType selectedType = browser.getSelectedType();
			if (null != selectedType){
				value = selectedType.getFullName();
			}
			
			// Creates the line only if OK has been pressed
			line = new InheritanceLine();
			((InheritanceLine)line).setInterfaceName(value);
		}
		
		// Restoring the old types filtering
		typesProvider.setTypes(oldType);
		
		return line;
	}
	
	/**
	 * The interface names are stored in path-like strings, ie: using "::"
	 * as separator. This class describes a line in the table and thus has 
	 * to implement {@link ITableElement} interface
	 * 
	 * @author cbosdonnat
	 *
	 */
	public class InheritanceLine implements ITableElement {
		
		public static final String OPTIONAL = "__optional"; //$NON-NLS-1$
		public static final String NAME = "__name"; //$NON-NLS-1$
		
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
		
		
		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getImage(java.lang.String)
		 */
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
		
		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getLabel(java.lang.String)
		 */
		public String getLabel(String property) {
			String label = null;
			
			if (property.equals(NAME)) {
				label = getInterfaceName().toString();
			}
			return label;
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getProperties()
		 */
		public String[] getProperties() {
			return new String[] {
					OPTIONAL,
					NAME
			};
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#canModify(java.lang.String)
		 */
		public boolean canModify(String property) {
			
			return property.equals(OPTIONAL);
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getValue(java.lang.String)
		 */
		public Object getValue(String property) {
			Object result = null;
			
			if (property.equals(OPTIONAL)) {
				result = Boolean.valueOf(isOptional());
			}
			return result;
		}
		
		/*
		 *  (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#setValue(java.lang.String, java.lang.Object)
		 */
		public void setValue(String property, Object value) {
			
			if (property.equals(OPTIONAL) && value instanceof Boolean) {
				
				setOptional(((Boolean)value).booleanValue());
			}
		}
	}
}
