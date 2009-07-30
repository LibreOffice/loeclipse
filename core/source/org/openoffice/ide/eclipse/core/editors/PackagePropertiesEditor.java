/*************************************************************************
 *
 * $RCSfile: PackagePropertiesEditor.java,v $
 *
 * $Revision: 1.3 $
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
package org.openoffice.ide.eclipse.core.editors;

import java.io.File;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IElementStateListener;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.editors.main.PackageContentsFormPage;
import org.openoffice.ide.eclipse.core.editors.main.PackageOverviewFormPage;
import org.openoffice.ide.eclipse.core.model.IPackageChangeListener;
import org.openoffice.ide.eclipse.core.model.PackagePropertiesModel;
import org.openoffice.ide.eclipse.core.model.description.DescriptionHandler;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * The project package editor.
 * 
 * @author cedricbosdo
 *
 */
public class PackagePropertiesEditor extends FormEditor {

    private SourcePage mSourcePage;
    private SourcePage mDescriptionPage;
    private PackageOverviewFormPage mOverviewPage;
    private PackageContentsFormPage mContentsPage;
    
    private IEditorInput mPropsEditorInput;
    private IEditorInput mDescrEditorInput;
    
    private DescriptionModel mDescriptionModel;
    private PackagePropertiesModel mModel;
    private boolean mIgnoreSourceChanges = false;
    
    /**
     * Default constructor.
     */
    public PackagePropertiesEditor() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void addPages() {
        
        try {
            
            // Add the overview page
            mOverviewPage = new PackageOverviewFormPage( this, "overview" ); //$NON-NLS-1$
            addPage( mOverviewPage );
            mOverviewPage.setModel( getDescriptionModel() );
            
            // Add the form page with the tree
            mContentsPage = new PackageContentsFormPage(this, "package"); //$NON-NLS-1$
            addPage(mContentsPage);
            
            // Add the text page for package.properties
            mSourcePage = new SourcePage(this, "source", "package.properties"); //$NON-NLS-1$ //$NON-NLS-2$
            mSourcePage.init(getEditorSite(), mPropsEditorInput);
            mSourcePage.getDocumentProvider().addElementStateListener(new IElementStateListener() {

                public void elementContentAboutToBeReplaced(Object pElement) {
                }

                public void elementContentReplaced(Object pElement) {
                }

                public void elementDeleted(Object pElement) {
                }

                public void elementDirtyStateChanged(Object pElement, boolean pIsDirty) {
                    if (!mIgnoreSourceChanges) {
                        mModel.setQuiet(true);
                    }
                    loadFromSource();
                    if (!mIgnoreSourceChanges) {
                        mModel.setQuiet(false);
                    }
                }

                public void elementMoved(Object pOriginalElement, Object pMovedElement) {
                }                
            });
            addPage(mSourcePage);
            
            // Add the description.xml source page
            mDescriptionPage = new SourcePage( this, "description", "description.xml"); //$NON-NLS-1$ //$NON-NLS-2$
            mDescriptionPage.init( getEditorSite(), mDescrEditorInput );
            addPage( mDescriptionPage );
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
            
            IFileEditorInput fileInput = (IFileEditorInput)pInput;
            IProject prj = fileInput.getFile().getProject();
            String projectName = prj.getName();
            
            IFile descrFile = prj.getFile( "description.xml" ); //$NON-NLS-1$
            mDescrEditorInput = new FileEditorInput( descrFile );
            
            IFile propsFile = prj.getFile( "package.properties" ); //$NON-NLS-1$
            mPropsEditorInput = new FileEditorInput( propsFile );
            
            setPartName(projectName);
            
            // Load the description
            try {
                SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
                // Enables the namespaces mapping
                parser.getXMLReader().setFeature( "http://xml.org/sax/features/namespaces" , true ); //$NON-NLS-1$
                parser.getXMLReader().setFeature( 
                        "http://xml.org/sax/features/namespace-prefixes", true ); //$NON-NLS-1$
                DescriptionHandler handler = new DescriptionHandler( getDescriptionModel() );
                File file = new File( descrFile.getLocationURI().getPath() );
                parser.parse(file, handler);
                
            } catch ( Exception e ) {
                PluginLogger.error( Messages.getString("PackagePropertiesEditor.DescriptionParseError"), e ); //$NON-NLS-1$
            }
            
            
            // Create the package properties
            mModel = new PackagePropertiesModel(fileInput.getFile());
            mModel.addChangeListener(new IPackageChangeListener() {

                public void packagePropertiesChanged() {
                    editorDirtyStateChanged();
                    writeToSource();
                }

                public void packagePropertiesSaved() {
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
    public void doSave(IProgressMonitor pMonitor) {
        try {
            mModel.write();
            mSourcePage.doRevertToSaved();
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
     * @return the description.xml model.
     */
    public DescriptionModel getDescriptionModel( ) {
        if ( mDescriptionModel == null ) {
            mDescriptionModel = new DescriptionModel( );
        }
        return mDescriptionModel;
    }
    
    /**
     * @return the project packaging properties file content.
     */
    public PackagePropertiesModel getModel() {
        return mModel;
    }
    
    /**
     * Write the properties model to the source editor page.
     */
    public void writeToSource() {
        if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
            TextFileDocumentProvider docProvider = (TextFileDocumentProvider)mSourcePage.getDocumentProvider();
            IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
            if (doc != null) {
                mIgnoreSourceChanges = true;
                doc.set(mModel.writeToString());
                mIgnoreSourceChanges = false;
            }
        }
    }
    
    /**
     * Loads the properties model from the source editor page.
     */
    public void loadFromSource() {
        
        if (mSourcePage.getDocumentProvider() instanceof TextFileDocumentProvider) {
            TextFileDocumentProvider docProvider = (TextFileDocumentProvider)mSourcePage.getDocumentProvider();
            IDocument doc = docProvider.getDocument(mSourcePage.getEditorInput());
            if (doc != null) {
                mModel.reloadFromString(doc.get());
            }
        }
    }
}
