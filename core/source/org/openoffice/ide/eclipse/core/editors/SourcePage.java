/*************************************************************************
 *
 * $RCSfile: SourcePage.java,v $
 *
 * $Revision: 1.2 $
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

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.ide.IDE;

/**
 * A Text editor to use as a tab in multiple page editors.
 * 
 * @author cedricbosdo
 *
 */
public class SourcePage extends TextEditor implements IFormPage {

    private FormEditor mEditor;
    private Control mControl;
    private int mIndex;
    private String mId;
    
    /**
     * Source editor page constructor.
     * 
     * @param pFormEditor the editor hosting the page.
     * @param pId the page identifier
     * @param pTitle the page title
     */
    public SourcePage(FormEditor pFormEditor, String pId, String pTitle) {
        mId = pId;
        initialize(pFormEditor);
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canLeaveThePage() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public FormEditor getEditor() {
        return mEditor;
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        return mId;
    }

    /**
     * {@inheritDoc}
     */
    public int getIndex() {
        return mIndex;
    }

    /**
     * {@inheritDoc}
     */
    public IManagedForm getManagedForm() {
        // Not a form page
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void createPartControl(Composite pParent) {
        super.createPartControl(pParent);
        Control[] children = pParent.getChildren();
        mControl = children[children.length - 1];
    }
    
    /**
     * {@inheritDoc}
     */
    public Control getPartControl() {
        return mControl;
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(FormEditor pEditor) {
        mEditor = pEditor;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActive() {
        return this.equals(mEditor.getActivePageInstance());
    }

    /**
     * {@inheritDoc}
     */
    public boolean isEditor() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean selectReveal(Object pObject) {
        boolean reveal = false;
        if (pObject instanceof IMarker) {
            IDE.gotoMarker(this, (IMarker)pObject);
            reveal = true;
        }
        return reveal;
    }

    /**
     * {@inheritDoc}
     */
    public void setActive(boolean pActive) {
    }

    /**
     * {@inheritDoc}
     */
    public void setIndex(int pIndex) {
        mIndex = pIndex;
    }
}
