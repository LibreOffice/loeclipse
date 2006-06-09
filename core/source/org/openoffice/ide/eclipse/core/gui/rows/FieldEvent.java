/*************************************************************************
 *
 * $RCSfile: FieldEvent.java,v $
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

/**
 * Event describing a raw change. Each row is associated to one property in 
 * order to recognize it when it changes.
 * 
 * @author cbosdonnat
 *
 */
public class FieldEvent {

	private String property;
	private String value;
	
	/**
	 * Creates a new row change event
	 * 
	 * @param property the property associated to the changed row
	 * @param value the new value of the row
	 */
	public FieldEvent(String property, String value){
		this.property = property;
		this.value = value;
	}

	/**
	 * Returns the property associated to the changed row
	 */
	public String getProperty() {
		return property;
	}

	/**
	 * Returns the new value of the row
	 */
	public String getValue() {
		return value;
	}
}
