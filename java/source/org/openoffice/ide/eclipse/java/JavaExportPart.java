/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat
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
 * Copyright: 2009 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.java;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.openoffice.ide.eclipse.core.model.language.LanguageExportPart;
import org.openoffice.ide.eclipse.core.wizards.pages.ManifestExportPage;

/**
 * Dialog part for the Ant scripts export configuration.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class JavaExportPart extends LanguageExportPart {

    private static final String DEFAULT_ANT_FILENAME = "build.xml"; //$NON-NLS-1$
    
    private Button mSaveScripts;
    private Composite mNameRow;
    private Label mNameRowLbl;
    private Text mNameRowTxt;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void createControls(Composite pParent) {
        mSaveScripts = new Button( pParent, SWT.CHECK );
        mSaveScripts.setText( Messages.getString("JavaExportPart.SaveAntScript") ); //$NON-NLS-1$
        mSaveScripts.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
        mSaveScripts.addSelectionListener( new SelectionListener() {
            
            public void widgetSelected( SelectionEvent pE ) {
                boolean enabled = mSaveScripts.getSelection();
                mNameRowLbl.setEnabled( enabled );
                mNameRowTxt.setEnabled( enabled );
            }
            
            public void widgetDefaultSelected( SelectionEvent pE ) {
                widgetSelected( pE );
            }
        });

        mNameRow = new Composite( pParent, SWT.NONE );
        mNameRow.setLayout( new GridLayout( 2, false ) );
        GridData gd = new GridData( SWT.FILL, SWT.BEGINNING, true, false );
        gd.horizontalIndent = ManifestExportPage.HORIZONTAL_INDENT;
        mNameRow.setLayoutData( gd );
        
        mNameRowLbl = new Label( mNameRow, SWT.NONE );
        mNameRowLbl.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
        mNameRowLbl.setText( Messages.getString("JavaExportPart.AntFile") ); //$NON-NLS-1$
        mNameRowLbl.setEnabled( false );
        
        mNameRowTxt = new Text( mNameRow, SWT.BORDER | SWT.SINGLE );
        mNameRowTxt.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
        mNameRowTxt.setText( DEFAULT_ANT_FILENAME );
        mNameRowTxt.setEnabled( false );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        if ( mSaveScripts != null ) {
            mSaveScripts.dispose();
            mNameRow.dispose();
        }
    }

}
