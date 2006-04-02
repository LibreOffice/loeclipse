/*************************************************************************
 *
 * $RCSfile: RegDocumentProvider.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/04/02 20:13:03 $
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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.i18n.I18nConstants;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * TODOC 
 * 
 * @author cbosdonnat
 *
 */
public class RegDocumentProvider extends FileDocumentProvider {
	
	public RegDocumentProvider() {
		super();
	}

	protected IDocument createDocument(Object element) throws CoreException {
		// create a document from the output of the regview execution
		
		IDocument document = new Document(
				OOEclipsePlugin.getTranslationString(I18nConstants.DOCUMENT_ERROR)); 
		
		if (element instanceof IFileEditorInput){
		
			IFile file = ((IFileEditorInput)element).getFile();
			IUnoidlProject unoproject = ProjectsManager.getInstance().getProject(
					file.getProject().getName());
			
			// Try to run regview on the file
		
			String command = "regview " + file.getProjectRelativePath().toOSString(); 
					
			Process process = OOEclipsePlugin.runTool(unoproject, command, null);
						
			// Get the process ouput to fill the document with
			LineNumberReader reader = new LineNumberReader(
						new InputStreamReader(process.getInputStream()));
			
			try {
				String output = "";
				String tmpLine = reader.readLine();
				
				while (null != tmpLine){
					// The two first lines of the output are not interesting 
					
					if (reader.getLineNumber() > 2){
						output = output + tmpLine + "\r\n";
					}
					tmpLine = reader.readLine();
				}
				
				document = new Document(output);
				
				// Do not forget to destroy the process
				process.destroy();
				
			} catch (IOException e){ 
				document = new Document(
						OOEclipsePlugin.getTranslationString(I18nConstants.REGVIEW_READ_ERROR));
				
				// Do not forget to destroy the process, even after an error 
				process.destroy();
			}
		}
			
		return document;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
	 */
	protected IAnnotationModel createAnnotationModel(Object element)
			throws CoreException {
		
		// there is no need of an annotation model here
		
		return null;
	}

	protected void doSaveDocument(IProgressMonitor monitor, Object element,
			IDocument document, boolean overwrite) throws CoreException {
		
		// This kind of document cannot be edited, nor saved
	}

}
