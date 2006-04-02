/*************************************************************************
 *
 * $RCSfile: UnoidlDocumentProvider.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:04 $
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
package org.openoffice.ide.eclipse.core.editors;


import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.openoffice.ide.eclipse.core.editors.syntax.UnoidlPartitionScanner;

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
