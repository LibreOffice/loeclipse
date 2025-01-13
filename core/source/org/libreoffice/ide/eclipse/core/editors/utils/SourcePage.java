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
package org.libreoffice.ide.eclipse.core.editors.utils;

import org.eclipse.core.resources.IMarker;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.ide.IDE;

/**
 * A Text editor to use as a tab in multiple page editors.
 */
public class SourcePage extends OOTextEditor implements IFormPage {

    private FormEditor mEditor;
    private Control mControl;
    private int mIndex;
    private String mId;

    /**
     * Source editor page constructor.
     *
     * @param formEditor
     *            the editor hosting the page.
     * @param pageId
     *            the page identifier
     * @param title
     *            the page title
     */
    public SourcePage(FormEditor formEditor, String pageId, String title) {
        mId = pageId;
        initialize(formEditor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canLeaveThePage() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormEditor getEditor() {
        return mEditor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getId() {
        return mId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIndex() {
        return mIndex;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IManagedForm getManagedForm() {
        // Not a form page
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPartControl(Composite pParent) {
        super.createPartControl(pParent);
        Control[] children = pParent.getChildren();
        mControl = children[children.length - 1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Control getPartControl() {
        return mControl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(FormEditor editor) {
        mEditor = editor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        return this.equals(mEditor.getActivePageInstance());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEditor() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean selectReveal(Object object) {
        boolean reveal = false;
        if (object instanceof IMarker) {
            IDE.gotoMarker(this, (IMarker) object);
            reveal = true;
        }
        return reveal;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActive(boolean active) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIndex(int index) {
        mIndex = index;
    }
}
