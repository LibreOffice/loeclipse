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
package org.libreoffice.ide.eclipse.core.editors.syntax;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.swt.custom.StyleRange;

/**
 * The UNO-IDL document repairer. This is used by the UNO-IDL editor. In order to fully understand the editor
 * mechanisms, please report to Eclipse plugin developer's guide.
 */
public class NonRuleBasedDamagerRepairer implements IPresentationDamager, IPresentationRepairer {

    /**
     * The document this object works on .
     */
    protected IDocument mDocument;

    /**
     * The default text attribute if non is returned as data by the current token.
     */
    protected TextAttribute mDefaultTextAttribute;

    /**
     * Default constructor.
     *
     * @param pDefaultTextAttribute
     *            the attribute to assign to default text
     */
    public NonRuleBasedDamagerRepairer(TextAttribute pDefaultTextAttribute) {
        Assert.isNotNull(pDefaultTextAttribute);

        mDefaultTextAttribute = pDefaultTextAttribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDocument(IDocument pDocument) {
        mDocument = pDocument;
    }

    /**
     * Returns the end offset of the line that contains the specified offset. If the offset is inside a line delimiter,
     * the end offset of the next line.
     *
     * @param pOffset
     *            the offset whose line end offset must be computed
     * @return the line end offset for the given offset
     * @exception BadLocationException
     *                if offset is invalid in the current document
     */
    protected int endOfLineOf(int pOffset) throws BadLocationException {
        int endOffset = mDocument.getLength();

        IRegion info = mDocument.getLineInformationOfOffset(pOffset);
        if (pOffset <= info.getOffset() + info.getLength()) {
            endOffset = info.getOffset() + info.getLength();
        }

        int line = mDocument.getLineOfOffset(pOffset);
        try {
            info = mDocument.getLineInformation(line + 1);
            endOffset = info.getOffset() + info.getLength();
        } catch (BadLocationException x) {
            endOffset = mDocument.getLength();
        }

        return endOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRegion getDamageRegion(ITypedRegion pPartition, DocumentEvent pEvent,
        boolean pDocumentPartitioningChanged) {

        IRegion damaged = pPartition;

        if (!pDocumentPartitioningChanged) {
            try {

                IRegion info = mDocument.getLineInformationOfOffset(pEvent.getOffset());
                int start = Math.max(pPartition.getOffset(), info.getOffset());

                int length = pEvent.getLength();
                if (pEvent.getText() == null) {
                    length = pEvent.getText().length();
                }
                int end = pEvent.getOffset() + length;

                if (info.getOffset() <= end && end <= info.getOffset() + info.getLength()) {
                    // optimize the case of the same line
                    end = info.getOffset() + info.getLength();
                } else {
                    end = endOfLineOf(end);
                }

                end = Math.min(pPartition.getOffset() + pPartition.getLength(), end);
                damaged = new Region(start, end - start);

            } catch (BadLocationException x) {
            }
        }

        return damaged;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPresentation(TextPresentation pPresentation, ITypedRegion pRegion) {
        addRange(pPresentation, pRegion.getOffset(), pRegion.getLength(), mDefaultTextAttribute);
    }

    /**
     * Adds style information to the given text presentation.
     *
     * @param pPresentation
     *            the text presentation to be extended
     * @param pOffset
     *            the offset of the range to be styled
     * @param pLength
     *            the length of the range to be styled
     * @param pAttr
     *            the attribute describing the style of the range to be styled
     */
    protected void addRange(TextPresentation pPresentation, int pOffset, int pLength, TextAttribute pAttr) {

        if (pAttr != null) {
            pPresentation.addStyleRange(new StyleRange(pOffset, pLength, pAttr.getForeground(), pAttr.getBackground(),
                pAttr.getStyle()));
        }
    }
}