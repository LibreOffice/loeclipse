/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.editors.description;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionHandler;
import org.libreoffice.ide.eclipse.core.model.description.DescriptionModel;
import org.xml.sax.InputSource;

/**
 * Editor for the description.xml file.
 */
public class DescriptionEditor extends FormEditor {

    private DescriptionSourcePage mSourcePage;
    private DescriptionFormPage mFormPage;

    private DescriptionModel mDescriptionModel;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPages() {

        try {
            // Add the overview page
            mFormPage = new DescriptionFormPage(this, "form"); //$NON-NLS-1$
            addPage(mFormPage);
            mFormPage.setModel(getDescriptionModel());

            // Add the description.xml source page
            mSourcePage = new DescriptionSourcePage(this, "description", "source"); //$NON-NLS-1$ //$NON-NLS-2$
            mSourcePage.init(getEditorSite(), getEditorInput());
            addPage(mSourcePage);
        } catch (PartInitException e) {
            // log ?
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(IEditorSite pSite, IEditorInput pInput) throws PartInitException {
        super.init(pSite, pInput);

        if (pInput instanceof IFileEditorInput) {

            IFileEditorInput fileInput = (IFileEditorInput) pInput;

            setPartName(fileInput.getName());

            // Load the description.xml file
            try {
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                // Enables the namespaces mapping
                parser.getXMLReader().setFeature("http://xml.org/sax/features/namespaces", true); //$NON-NLS-1$
                parser.getXMLReader().setFeature("http://xml.org/sax/features/namespace-prefixes", true); //$NON-NLS-1$
                DescriptionHandler handler = new DescriptionHandler(getDescriptionModel());
                File file = new File(fileInput.getFile().getLocationURI().getPath());

                getDescriptionModel().setSuspendEvent(true);
                parser.parse(file, handler);

            } catch (Exception e) {
                PluginLogger.error(Messages.getString("PackagePropertiesEditor.DescriptionParseError"), //$NON-NLS-1$
                    e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirty() {
        return mDescriptionModel.isDirty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doSave(IProgressMonitor pMonitor) {
        OutputStream out = null;
        try {
            FileEditorInput input = (FileEditorInput) getEditorInput();
            File file = new File(input.getFile().getLocationURI());
            out = new FileOutputStream(file);
            getDescriptionModel().serialize(out);

            input.getFile().refreshLocal(IResource.DEPTH_ZERO, pMonitor);
        } catch (Exception e) {
            PluginLogger.error(Messages.getString("DescriptionEditor.ErrorSaving"), e); //$NON-NLS-1$
        } finally {
            try {
                out.close();
            } catch (Exception e) {
            }
            mSourcePage.doRevertToSaved();
            mFormPage.reloadData();
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
     * @return the description.xml model.
     */
    public DescriptionModel getDescriptionModel() {
        if (mDescriptionModel == null) {
            mDescriptionModel = new DescriptionModel();
        }
        return mDescriptionModel;
    }

    /**
     * Write the description model to the description source page.
     */
    public void writeDescrToSource() {
        if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
            TextFileDocumentProvider docProvider = (TextFileDocumentProvider) mSourcePage.getDocumentProvider();
            IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
            if (doc != null) {
                // Write the description.xml to a buffer stream
                ByteArrayOutputStream out = null;
                try {
                    out = new ByteArrayOutputStream();
                    mDescriptionModel.serialize(out);
                    doc.set(out.toString());
                } finally {
                    try {
                        out.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
    }

    /**
     * Re-load the model from the XML code shown in the description source page.
     */
    public void loadDescFromSource() {
        if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
            TextFileDocumentProvider docProvider = (TextFileDocumentProvider) mSourcePage.getDocumentProvider();
            IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
            if (doc != null) {

                StringReader reader = null;
                try {
                    SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                    // Enables the namespaces mapping
                    parser.getXMLReader().setFeature("http://xml.org/sax/features/namespaces", true); //$NON-NLS-1$
                    parser.getXMLReader().setFeature("http://xml.org/sax/features/namespace-prefixes", true); //$NON-NLS-1$
                    DescriptionHandler handler = new DescriptionHandler(getDescriptionModel());

                    reader = new StringReader(doc.get());
                    InputSource is = new InputSource(reader);

                    getDescriptionModel().setSuspendEvent(true);
                    parser.parse(is, handler);
                    mFormPage.reloadData();

                    getDescriptionModel().setSuspendEvent(false);

                } catch (Exception e) {
                    PluginLogger.error(Messages.getString("PackagePropertiesEditor.DescriptionParseError"), //$NON-NLS-1$
                        e);
                } finally {
                    reader.close();
                }
            }
        }
    }
}
