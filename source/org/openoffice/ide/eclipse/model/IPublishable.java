/*************************************************************************
 *
 * $RCSfile: IPublishable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:15 $
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
package org.openoffice.ide.eclipse.model;

/**
 * Interface to describe a common access to the published modifier of 
 * a declaration
 * 
 * @author cbosdonnat
 *
 */
public interface IPublishable {

	/**
	 * Sets or unset the declaration as published. It will be prefixed
	 * with "published"
	 * 
	 * @param published <code>true</code> sets the declaration as published, 
	 * 			<code>false</code> unset this attribute.
	 */
	public void setPublished(boolean published);
	
	/**
	 * Gets the published value of the declaration. If <code>true</code> is 
	 * returned, the declaration will be prefixed with "published".
	 * 
	 * @return <code>true</code> if the declaration is published
	 */
	public boolean isPublished();
}
