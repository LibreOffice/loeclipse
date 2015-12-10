/*************************************************************************
 *
 * $RCSfile: RegEditor.java,v $
 *
 * $Revision: 1.4 $
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
package org.openoffice.ide.eclipse.core.editors.registry;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.editors.utils.OOTextEditor;

/**
 * Editor class to view the UNO registries, ie <code>.urd</code> and
 * <code>.rdb</code> files. This editor is read-only and simply shows
 * the registry content without syntax highlighting.
 *
 * @author cedricbosdo
 */
public class RegEditor extends OOTextEditor {

    /**
     * Default constructor, initializing the document provider.
     *
     * @see RegDocumentProvider for the document provider
     */
    public RegEditor() {
        super();
        setDocumentProvider(new RegDocumentProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ISourceViewer createSourceViewer(Composite pParent, IVerticalRuler pRuler, int pStyles) {
        ISourceViewer sourceViewer = super.createSourceViewer(pParent, pRuler, pStyles);

        // The database viewer only shows the result of regview, thus the file isn't editable
        sourceViewer.setEditable(false);

        return sourceViewer;
    }
}
