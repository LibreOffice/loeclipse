/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.editors.pack;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.forms.editor.FormEditor;
import org.libreoffice.ide.eclipse.core.editors.utils.SourcePage;
import org.libreoffice.ide.eclipse.core.model.pack.PackagePropertiesModel;
import org.libreoffice.ide.eclipse.core.model.utils.IModelChangedListener;


/**
 * The project package editor.
 */
public class PackagePropertiesEditor extends FormEditor {

    private SourcePage mSourcePage;
    private PackageFormPage mContentsPage;

    private PackagePropertiesModel mModel;
    private boolean mIgnoreSourceChanges = false;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPages() {

        try {

            // Add the form page with the tree
            mContentsPage = new PackageFormPage(this, "package"); //$NON-NLS-1$
            addPage(mContentsPage);

            // Add the text page for package.properties
            mSourcePage = new SourcePage(this, "source", "package.properties"); //$NON-NLS-1$ //$NON-NLS-2$
            mSourcePage.init(getEditorSite(), getEditorInput());

            // Add listener to be notified when the document content has be changed
            if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
                TextFileDocumentProvider provider = (TextFileDocumentProvider) mSourcePage.getDocumentProvider();
                IDocument doc = provider.getDocument(mSourcePage.getEditorInput());
                if (doc != null) {
                    doc.addDocumentListener(new IDocumentListener() {

                        @Override
                        public void documentAboutToBeChanged(DocumentEvent event) {
                        }
                        @Override
                        public void documentChanged(DocumentEvent event) {
                            mIgnoreSourceChanges = true;
                            mModel.reloadFromString(event.getDocument().get());
                            mIgnoreSourceChanges = false;
                        }
                    });
                }
            }

            addPage(mSourcePage);
        } catch (PartInitException e) {
            // log ?
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException {
        super.init(site, input);

        if (input instanceof IFileEditorInput) {

            IFileEditorInput fileInput = (IFileEditorInput) input;
            IProject prj = fileInput.getFile().getProject();
            String projectName = prj.getName();
            setPartName(projectName);

            // Create the package properties
            mModel = new PackagePropertiesModel(fileInput.getFile());
            mModel.addChangeListener(new IModelChangedListener() {

                @Override
                public void modelChanged() {
                    if (!mIgnoreSourceChanges) {
                        writeToSource(mModel.writeToString());
                    }
                    editorDirtyStateChanged();
                }

                @Override
                public void modelSaved() {
                    editorDirtyStateChanged();
                }

            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {
        return mModel.isDirty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSave(IProgressMonitor monitor) {
        try {
            writeToSource(mModel.write());
        } catch (Exception e) {
            // Log ?
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSaveAs() {
        // Not allowed
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSaveAsAllowed() {
        return false;
    }

    /**
     * @return the project packaging properties file content.
     */
    public PackagePropertiesModel getModel() {
        return mModel;
    }

    /**
     * Write the properties model to the source editor page.
     *
     * @param content
     *            the content to write
     */
    public void writeToSource(String content) {
        if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
            TextFileDocumentProvider docProvider = (TextFileDocumentProvider) mSourcePage.getDocumentProvider();
            IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
            if (doc != null) {
                doc.set(content);
            }
        }
    }

    /**
     * Loads the properties model from the source editor page.
     */
    public void loadFromSource() {

        if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
            TextFileDocumentProvider docProvider = (TextFileDocumentProvider) mSourcePage.getDocumentProvider();
            IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
            if (doc != null) {
                mModel.reloadFromString(doc.get());
            }
        }
    }
}
