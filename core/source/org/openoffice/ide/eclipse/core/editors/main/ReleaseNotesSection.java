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
package org.openoffice.ide.eclipse.core.editors.main;

import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.openoffice.ide.eclipse.core.editors.Messages;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * Section displaying the release notes part of the descriptions.xml file.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class ReleaseNotesSection extends LocalizedSection implements
        ILocaleListener {

    private DescriptionModel mModel;
    
    private Text mUrlTxt;
    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public ReleaseNotesSection(Composite pParent, PackageOverviewFormPage pPage) {
        super( pParent, pPage, Section.TITLE_BAR );
        
        getSection().setText( Messages.getString("ReleaseNotesSection.Title") ); //$NON-NLS-1$
        
        mModel = pPage.getModel();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void loadData() {
        // Nothing to do: everything is localized
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls(FormToolkit pToolkit, Composite pParent) {
        pParent.setLayout( new GridLayout( 2, false ) );
        
        Label descrLbl = pToolkit.createLabel( pParent, 
                Messages.getString("ReleaseNotesSection.Description"),  //$NON-NLS-1$
                SWT.WRAP );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData( gd );
        
        // Url controls
        pToolkit.createLabel( pParent, Messages.getString("ReleaseNotesSection.Url") ); //$NON-NLS-1$
        mUrlTxt = pToolkit.createText( pParent, new String( ) );
        mUrlTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mUrlTxt.setEnabled( false );
        mUrlTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                mModel.mReleaseNotes.put( mCurrentLocale, mUrlTxt.getText() );
                markDirty();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        if ( !mModel.mReleaseNotes.containsKey( pLocale ) ) {
            mModel.mReleaseNotes.put( pLocale, new String( ) );
        }
        mUrlTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        mModel.mReleaseNotes.remove( pLocale );
        if ( mModel.mReleaseNotes.size( ) == 0 ) {
            mUrlTxt.setEnabled( false );
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        
        if ( mCurrentLocale != null ) {
            mModel.mReleaseNotes.put( mCurrentLocale, mUrlTxt.getText() );
        }
        super.selectLocale(pLocale);
        mUrlTxt.setText( mModel.mReleaseNotes.get( pLocale ) );
    }
}
