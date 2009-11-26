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
 * Copyright: 2009 by Cédric Bosdonnat.
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.java.client;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.java.OOoJavaPlugin;
import org.openoffice.ide.eclipse.java.build.Messages;

/**
 * JOD Connector configuration page.
 * 
 * @author cbosdonnat
 *
 */
public class JODContainerPage extends WizardPage implements
        IClasspathContainerPage {

    private static final int LAYOUT_COLS = 2;
    
    private BooleanRow mSlf4jRow;

    private boolean mSlf4j;
    
    /**
     * Needed default constructor.
     */
    public JODContainerPage( ) {
        super( "jodcontainer" ); //$NON-NLS-1$
        
        setTitle( Messages.getString("JODContainerPage.Title") ); //$NON-NLS-1$
        ImageDescriptor image = OOoJavaPlugin.getImageDescriptor(
                Messages.getString("OOoContainerPage.DialogImage")); //$NON-NLS-1$
        setImageDescriptor(image);
    }

    /**
     * {@inheritDoc}
     */
    public boolean finish() {
        // Nothing to do
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public IClasspathEntry getSelection() {
        return JODContainer.createClasspathEntry( mSlf4j );
    }

    /**
     * {@inheritDoc}
     */
    public void setSelection(IClasspathEntry pContainerEntry) {
        if ( pContainerEntry != null ) {
            mSlf4j = JODContainer.checkSlf4jImpl( pContainerEntry.getPath() );
        } else {
            mSlf4j = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createControl( Composite pParent ) {
        Composite body = new Composite( pParent, SWT.NONE );
        body.setLayout( new GridLayout( LAYOUT_COLS, false ) );
        
        // SLF4J boolean row
        mSlf4jRow = new BooleanRow( body, new String(), 
                Messages.getString("JODContainerPage.SLF4JLabel") ); //$NON-NLS-1$
        mSlf4jRow.setValue( mSlf4j );
        mSlf4jRow.setFieldChangedListener( new IFieldChangedListener() {
            
            public void fieldChanged(FieldEvent pEvent) {
                mSlf4j = mSlf4jRow.getBooleanValue();
            }
        });
        
        setControl( body );
    }

}
