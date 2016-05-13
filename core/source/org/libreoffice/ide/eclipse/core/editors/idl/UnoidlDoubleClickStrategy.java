/*************************************************************************
 *
 * $RCSfile: UnoidlDoubleClickStrategy.java,v $
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
package org.libreoffice.ide.eclipse.core.editors.idl;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextViewer;

/**
 * This class is responsible for the selection of words using double-clics in the UNO-IDL editor.
 *
 *
 */
public class UnoidlDoubleClickStrategy implements ITextDoubleClickStrategy {

    protected ITextViewer mText;

    /**
     * {@inheritDoc}
     */
    @Override
    public void doubleClicked(ITextViewer pPart) {
        int pos = pPart.getSelectedRange().x;

        if (pos < 0) {
            return;
        }

        mText = pPart;

        if (!selectComment(pos)) {
            selectWord(pos);
        }
    }

    /**
     * Test if the caret is positioned in a comment partition.
     *
     * @param pCaretPos
     *            the caret position
     * @return <code>true</code> if the cursor is in a comment, <code>false</code> otherwise
     */
    protected boolean selectComment(int pCaretPos) {

        boolean selected = false;

        IDocument doc = mText.getDocument();
        int startPos, endPos;

        try {

            int pos = getCommentStartPosition(pCaretPos, doc);
            int c = doc.getChar(pos);

            if (c == '\"') {
                startPos = pos;

                pos = pCaretPos;
                int length = doc.getLength();
                c = ' ';

                while (pos < length) {
                    c = doc.getChar(pos);
                    if (c == Character.LINE_SEPARATOR || c == '\"') {
                        break;
                    }
                    ++pos;
                }

                if (c == '\"') {
                    endPos = pos;

                    int offset = startPos + 1;
                    int len = endPos - offset;
                    mText.setSelectedRange(offset, len);
                    selected = true;
                }
            }
        } catch (BadLocationException x) {
        }

        return selected;
    }

    /**
     * Get the position of the start of the current comment or the same position than the caret.
     *
     * @param pCaretPos
     *            the position of the caret
     * @param pDoc
     *            the edited document
     * @return the start of the comment or the caret position
     *
     * @throws BadLocationException
     *             if something wrong happens during the document reading
     */
    private int getCommentStartPosition(int pCaretPos, IDocument pDoc) throws BadLocationException {
        int pos = pCaretPos;
        char c = ' ';

        while (pos >= 0) {
            c = pDoc.getChar(pos);
            if (c == '\\') {
                pos -= 2;
                continue;
            }
            if (c == Character.LINE_SEPARATOR || c == '\"') {
                break;
            }
            --pos;
        }

        return pos;
    }

    /**
     * Test if the caret is positioned in a word partition.
     *
     * @param pCaretPos
     *            the caret position
     * @return <code>true</code> if the cursor is in a word, <code>false</code> ortherwise
     */
    protected boolean selectWord(int pCaretPos) {

        boolean selected = false;

        IDocument doc = mText.getDocument();
        int startPos, endPos;

        try {

            int pos = pCaretPos;
            char c;

            while (pos >= 0) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                --pos;
            }

            startPos = pos;

            pos = pCaretPos;
            int length = doc.getLength();

            while (pos < length) {
                c = doc.getChar(pos);
                if (!Character.isJavaIdentifierPart(c)) {
                    break;
                }
                ++pos;
            }

            endPos = pos;
            selectRange(startPos, endPos);
            selected = true;

        } catch (BadLocationException x) {
        }

        return selected;
    }

    /**
     * Define the text selection using a range.
     *
     * @param pStartPos
     *            the position of the selection start
     * @param pStopPos
     *            the position of the selection end
     */
    private void selectRange(int pStartPos, int pStopPos) {
        int offset = pStartPos + 1;
        int length = pStopPos - offset;
        mText.setSelectedRange(offset, length);
    }
}