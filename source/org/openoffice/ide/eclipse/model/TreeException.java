/*************************************************************************
 *
 * $RCSfile: TreeException.java,v $
 *
 * $Revision: 1.2 $
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
package org.openoffice.ide.eclipse.model;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class TreeException extends Exception {

	public final static int UNKOWN_ERROR = 0;
	public final static int NO_ROOT = 1;
	public final static int NODE_NOT_FOUND = 2; 
	public final static int BAD_TYPE_NODE = 3;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4928660652495454713L;
	
	private int code = 0;

	public TreeException(int aCode, String message) {
		super(message);
		code = aCode;
	}

	public TreeException(int aCode, String message, Throwable cause) {
		super(message, cause);
		code = aCode;
	}
	
	public int getCode(){
		return code;
	}
}
