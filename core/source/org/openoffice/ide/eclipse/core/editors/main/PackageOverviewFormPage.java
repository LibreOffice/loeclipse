/*************************************************************************
 *
 * $RCSfile: PackageOverviewFormPage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:51 $
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
package org.openoffice.ide.eclipse.core.editors.main;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openoffice.ide.eclipse.core.editors.Messages;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * The form page of the package editor helping to configure the project's
 * description and main properties.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class PackageOverviewFormPage extends FormPage {

    private LocaleSelector mLocaleSel;
    private DescriptionModel mModel;
    
    /**
     * Constructor.
     * 
     * @param pEditor the editor where to add the page
     * @param pId the page identifier
     */
    public PackageOverviewFormPage(FormEditor pEditor, String pId ) {
        super(pEditor, pId, Messages.getString("PackageOverviewFormPage.Title")); //$NON-NLS-1$
    }

    /**
     * @param pModel the description.xml model to set
     */
    public void setModel( DescriptionModel pModel ) {
        mModel = pModel;
    }
    
    /**
     * @return the description model for the page.
     */
    public DescriptionModel getModel( ) {
        return mModel;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createFormContent(IManagedForm pManagedForm) {
        super.createFormContent(pManagedForm);
        
        ScrolledForm form = pManagedForm.getForm();
        form.setText( Messages.getString("PackageOverviewFormPage.Title") ); //$NON-NLS-1$
        
        form.getBody().setLayout( new GridLayout( ) );
        
        FormToolkit toolkit = getManagedForm().getToolkit();
        toolkit.decorateFormHeading( form.getForm() );
        
        Label descrLbl = toolkit.createLabel( form.getBody(), 
                Messages.getString("PackageOverviewFormPage.Description"),  //$NON-NLS-1$
                SWT.WRAP );
        descrLbl.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        
        Composite body = toolkit.createComposite( form.getBody() );
        body.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        body.setLayout( new GridLayout( ) );
        
        // Create the locale selector line
        Composite bottomLine = toolkit.createComposite( form.getBody() );
        bottomLine.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        bottomLine.setLayout( new GridLayout( ) );
        
        mLocaleSel = new LocaleSelector( toolkit, bottomLine );   
        
        createMainPage( toolkit, body );
        
        mLocaleSel.loadLocales( mModel.getAllLocales() );
    }

    /**
     * Creates the main tab page.
     * 
     * @param pToolkit the toolkit used to create the page
     * @param pParent the parent composite where to create the page.
     * 
     * @return the page control
     */
    private Control createMainPage( FormToolkit pToolkit, Composite pParent ) {
        
        Composite body = pToolkit.createComposite( pParent );
        body.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        body.setLayout( new GridLayout( 2, true ) );
        
        Composite leftColumn = pToolkit.createComposite( body );
        leftColumn.setLayoutData( new GridData( GridData.FILL_BOTH ));
        leftColumn.setLayout( new GridLayout( ) ); 
        
        
        Composite rightColumn = pToolkit.createComposite( body );
        rightColumn.setLayoutData( new GridData( GridData.FILL_BOTH ));
        rightColumn.setLayout( new GridLayout( ) );
        
        /*
         * Left column:                         Right column:
         *    + Section "General"                  + Section "Update mirrors"
         *    + Section "Integration"              + Section "License"
         *    + Section "Publisher"
         *    + Section "Release notes"
         */
        GeneralSection generalSection = new GeneralSection( leftColumn, this );
        mLocaleSel.addListener( generalSection );
        
        new IntegrationSection( leftColumn, this );
        
        PublisherSection publisherSection = new PublisherSection( leftColumn, this );
        mLocaleSel.addListener( publisherSection );
        
        ReleaseNotesSection releaseNotesSection = new ReleaseNotesSection( leftColumn, this );
        mLocaleSel.addListener( releaseNotesSection );
        
        new MirrorsSection( rightColumn, this );
        
        IFileEditorInput input = (IFileEditorInput)getEditorInput();
        IProject project = input.getFile().getProject();
        LicenseSection licenseSection = new LicenseSection( rightColumn, this, project );
        mLocaleSel.addListener( licenseSection );
        
        return body;
    }
}
