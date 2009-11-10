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
package org.openoffice.ide.eclipse.core.editors.description;

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
import org.openoffice.ide.eclipse.core.editors.utils.LocalizedSection;
import org.openoffice.ide.eclipse.core.model.description.DescriptionModel;

/**
 * @author Cédric Bosdonnat
 *
 */
public class GeneralSection extends LocalizedSection< DescriptionModel > {
    
    private Text mNameTxt;
    private Text mIdTxt;
    private Text mVersionTxt;

    
    /**
     * @param pParent the parent composite where to add the section
     * @param pPage the parent page
     */
    public GeneralSection( Composite pParent, DescriptionFormPage pPage ) {
        super( pParent, pPage, Section.TITLE_BAR );
        
        
        getSection( ).setText( Messages.getString("GeneralSection.Title") ); //$NON-NLS-1$
        
        setModel( pPage.getModel() );
    }
    
    /**
     * Loads the values from the model into the controls.
     */
    public void loadData( ) {
        getModel().setSuspendEvent( true );
        mNameTxt.setText( getModel().getDisplayNames().get( mCurrentLocale ) );
        mIdTxt.setText( getModel().getId() );
        mVersionTxt.setText( getModel().getVersion() );
        getModel().setSuspendEvent( false );
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void createControls( FormToolkit pToolkit, Composite pParent ) {
        
        pParent.setLayout( new GridLayout( 2, false ) );
        
        Label descrLbl = pToolkit.createLabel( pParent, 
                Messages.getString("GeneralSection.Description"),  //$NON-NLS-1$
                SWT.WRAP );
        GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 2;
        descrLbl.setLayoutData( gd );
        
        // Name controls
        pToolkit.createLabel( pParent, Messages.getString("GeneralSection.Name") ); //$NON-NLS-1$
        mNameTxt = pToolkit.createText( pParent, new String( ) );
        mNameTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mNameTxt.setEnabled( false );
        mNameTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                getModel().addDisplayName( mCurrentLocale, mNameTxt.getText() );
            }
        });
        
        // Identifier controls
        pToolkit.createLabel( pParent, Messages.getString("GeneralSection.Identifier") ); //$NON-NLS-1$
        mIdTxt = pToolkit.createText( pParent, new String( ) );
        mIdTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mIdTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                getModel().setId( mIdTxt.getText() );
            }
        });
        
        // Version controls
        pToolkit.createLabel( pParent, Messages.getString("GeneralSection.Version") ); //$NON-NLS-1$
        mVersionTxt = pToolkit.createText( pParent, new String( ) );
        mVersionTxt.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ) );
        mVersionTxt.addModifyListener( new ModifyListener () {
            public void modifyText(ModifyEvent pE) {
                getModel().setVersion( mVersionTxt.getText() );
            }
        });
    }
    
    /**
     * {@inheritDoc}
     */
    public void addLocale(Locale pLocale) {
        if ( !getModel().getDisplayNames().containsKey( pLocale ) ) {
            getModel().addDisplayName( pLocale, new String( ) );
        }
        mNameTxt.setEnabled( true );
    }

    /**
     * {@inheritDoc}
     */
    public void deleteLocale(Locale pLocale) {
        getModel().removeDisplayName( pLocale );
        if ( getModel().getDisplayNames().size() == 0 ) {
            mNameTxt.setEnabled( false );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectLocale(Locale pLocale) {
        
        if ( mCurrentLocale != null ) {
            getModel().addDisplayName( mCurrentLocale, mNameTxt.getText( ) );
        }
        super.selectLocale(pLocale);
        String name = getModel().getDisplayNames().get( pLocale );
        mNameTxt.setText( name );
    }
}
