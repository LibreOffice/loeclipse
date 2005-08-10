/*************************************************************************
 *
 * $RCSfile: UnoidlDocumentProvider.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/10 12:07:26 $
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
package org.openoffice.ide.eclipse.editors;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.openoffice.ide.eclipse.editors.syntax.UnoidlPartitionScanner;

/**
 * TODOC
 * 
 * @author cbosdonnat
 *
 */
public class UnoidlDocumentProvider extends FileDocumentProvider{

	private static UnoidlPartitionScanner scanner = null;
	
	private final static String[] TYPES = new String[]{
		UnoidlPartitionScanner.IDL_AUTOCOMMENT,
		UnoidlPartitionScanner.IDL_COMMENT,
		UnoidlPartitionScanner.IDL_PREPROCESSOR
	};
	
	public UnoidlDocumentProvider(){
		super();
	}
	
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document!=null){
			IDocumentPartitioner partitioner = createIDLPartitioner();
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
		}
		return document;
	}
	
	private FastPartitioner createIDLPartitioner(){
		return new FastPartitioner(
			getIDLPartitionScanner(), TYPES
		);
	}
	
	private UnoidlPartitionScanner getIDLPartitionScanner(){
		if (scanner == null){
			scanner = new UnoidlPartitionScanner();
		}
		return scanner;
	}
}
