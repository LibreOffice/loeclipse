/*************************************************************************
 *
 * $RCSfile: ITableElement.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/06/09 06:14:05 $
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

package org.openoffice.ide.eclipse.core.gui;

import org.eclipse.swt.graphics.Image;

/**
 * Interface used by the abstract table to get the labels of it's items
 *  
 * @author cbosdonnat
 *
 */
public interface ITableElement {

	/**
	 * Returns the column image corresponding to the property
	 * 
	 * @param property the property designating the column
	 * @return the image for the column
	 */
	public Image getImage(String property);
	
	/**
	 * Returns the column label corresponding to the property
	 * 
	 * @param property the property designating the column
	 * @return the label for the column
	 */
	public String getLabel(String property);
	
	/**
	 * Returns the line properties in the columns order.
	 */
	public String[] getProperties();
	
	/**
	 * Defines whether the column cell corresponding to the property can be 
	 * modified.
	 * 
	 * @param property the property designating the column
	 * @return <code>true</code> if the cell is editable.
	 */
	public boolean canModify(String property);
	
	/**
	 * Returns the column value corresponding to the property
	 * 
	 * @param property the property designating the column
	 * @return the value for the column
	 */
	public Object getValue(String property);
	
	
	/**
	 * Sets the column value corresponding to the property
	 * 
	 * @param property the property designating the column
	 * @param value the new value for the column
	 */
	public void setValue(String property, Object value);
}
