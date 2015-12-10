/*************************************************************************
 *
 * $RCSfile: RegDocumentProvider.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:28 $
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
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
package org.openoffice.ide.eclipse.core.editors.registry;

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
import org.openoffice.ide.eclipse.core.editors.Messages;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * Provides the document content for the registry editor. This class uses the <code>regview</code> tool.
 *
 * @see org.openoffice.ide.eclipse.core.editors.registry.RegEditor for the editor
 *
 * @author cbosdonnat
 *
 */
public class RegDocumentProvider extends FileDocumentProvider {

    /**
     * Default constructor.
     */
    public RegDocumentProvider() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IDocument createDocument(Object pElement) throws CoreException {
        // create a document from the output of the regview execution

        IDocument document = new Document(Messages.getString("RegDocumentProvider.DocumentCreationError")); //$NON-NLS-1$

        if (pElement instanceof IFileEditorInput) {

            IFile file = ((IFileEditorInput) pElement).getFile();
            IUnoidlProject unoproject = ProjectsManager.getProject(file.getProject().getName());

            // Try to run regview on the file

            String command = "regview " + file.getLocation().toOSString(); //$NON-NLS-1$

            Process process = unoproject.getSdk().runTool(unoproject, command, null);

            // Get the process ouput to fill the document with
            InputStreamReader in = new InputStreamReader(process.getInputStream());
            LineNumberReader reader = new LineNumberReader(in);

            try {
                String output = ""; //$NON-NLS-1$
                String tmpLine = reader.readLine();

                while (null != tmpLine) {
                    // The two first lines of the output are not interesting

                    if (reader.getLineNumber() > 2) {
                        output = output + tmpLine + "\r\n"; //$NON-NLS-1$
                    }
                    tmpLine = reader.readLine();
                }

                document = new Document(output);

            } catch (IOException e) {
                document = new Document(Messages.getString("RegDocumentProvider.RegviewError")); //$NON-NLS-1$

                // Do not forget to destroy the process, even after an error
                process.destroy();
            } finally {
                try {
                    reader.close();
                    in.close();
                } catch (IOException e) {
                }
            }
        }

        return document;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected IAnnotationModel createAnnotationModel(Object pElement) throws CoreException {

        // there is no need of an annotation model here
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSaveDocument(IProgressMonitor pMonitor, Object pElement, IDocument pDocument, boolean pOverwrite)
        throws CoreException {

        // This kind of document cannot be edited, nor saved
    }
}
