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
 * Copyright: 2010 by Cédric Bosdonnat <cedric.bosdonnat@free.fr>
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.core.editors.utils;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Base class for an enhanced text editor to be used in the ooeclipse plugins.
 * 
 * @author Cédric Bosdonnat <cedric.bosdonnat@free.fr>
 *
 */
public class OOTextEditor extends TextEditor {

    private Composite mMsgComposite;
    private boolean mMsgVisible = false;
    
    /**
     * Hide or Show the message box of the editor.
     * 
     * @param pVisible <code>true</code> to show it, <code>false</code> to hide.
     */
    public void setMessageVisible(boolean pVisible) {
        mMsgVisible = pVisible;
        if ( mMsgComposite != null ) {
            ((GridData) mMsgComposite.getLayoutData()).exclude = !pVisible;
            mMsgComposite.setVisible(pVisible);
            mMsgComposite.getParent().layout(true, true);
        }
    }
    
    /**
     * Override this method to set or change the editor message controls.
     * 
     * @param pParent the parent composite to use for the creation
     */
    protected void createMessageContent(Composite pParent) {
        // Do nothing here
    }
    
    /**
     * {@inheritDoc}
     */
    protected ISourceViewer createSourceViewer(Composite pParent, IVerticalRuler pRuler, int pStyles) {
        Composite composite = new Composite(pParent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        
        // Create the warning message
        mMsgComposite = new Composite(composite, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
        mMsgComposite.setLayoutData(data);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        mMsgComposite.setLayout(gridLayout);
        
        createMessageContent(mMsgComposite);
        setMessageVisible( mMsgVisible );
        
        Composite editorComposite = new Composite(composite, SWT.NONE);
        editorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.marginHeight = 0;
        fillLayout.marginWidth = 0;
        fillLayout.spacing = 0;
        editorComposite.setLayout(fillLayout);
        
        ISourceViewer srcViewer = super.createSourceViewer(editorComposite, pRuler, pStyles);
        
        return srcViewer;
    }
}
