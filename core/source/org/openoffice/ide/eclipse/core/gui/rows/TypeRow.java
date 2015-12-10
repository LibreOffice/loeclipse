/*************************************************************************
 *
 * $RCSfile: TypeRow.java,v $
 *
 * $Revision: 1.10 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:50 $
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
package org.openoffice.ide.eclipse.core.gui.rows;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeBrowser;

/**
 * Row for the selection of a UNO type.
 *
 * <p>
 * The row allows to type the text in a text field or selecting the type using the UNO type browser. The text field
 * support a simple auto-completion.
 * </p>
 *
 * @author cedricbosdo
 *
 */
public class TypeRow extends TextRow {

    private InternalUnoType mSelectedType;
    private int mType = 0;

    private boolean mIncludeSequences = false;
    private boolean mIncludeSimpleTypes = false;
    private boolean mIncludeVoid = true;

    /**
     * Creates a row for the selection of a UNO type.
     *
     * <p>
     * The types mask is an integer from 0 to 2048-1. The type mask can be obtained by bit-OR of the types constants
     * defined in {@link InternalUnoType} class.
     * </p>
     *
     * @param pParent
     *            the parent composite where to create the row
     * @param pProperty
     *            the property name of the row
     * @param pLabel
     *            the label of the row
     * @param pType
     *            the types mask of the row.
     */
    public TypeRow(Composite pParent, String pProperty, String pLabel, int pType) {
        super(pParent, pProperty, pLabel);

        if (pType >= 0 && pType <= InternalUnoType.ALL_TYPES) {
            mType = pType;
        }
    }

    /**
     * Set whether the row should support include auto-completion for sequences. Sequences aren't included in the
     * auto-completion by default.
     *
     * @param pInclude
     *            <code>true</code> if the row can auto-complete sequences
     */
    public void includeSequences(boolean pInclude) {
        mIncludeSequences = pInclude;
    }

    /**
     * Set whether the row should support include auto-completion for simple UNO types. If the simple types are not
     * included in the auto-completion, the void type isn't included too. Simple types aren't included in the
     * auto-completion by default.
     *
     * @param pInclude
     *            <code>true</code> if the row can auto-complete sequences
     * @see #includeVoid(boolean) to include/exclude the void type
     */
    public void includeSimpleTypes(boolean pInclude) {
        mIncludeSimpleTypes = pInclude;
    }

    /**
     * Set whether the row should support include auto-completion for the void type. The void type is included in the
     * auto-completion by default as long as the simple types are included.
     *
     * @param pInclude
     *            <code>true</code> if the row can auto-complete sequences
     * @see #includeSimpleTypes(boolean) for more precisions on the inclusion of the void type dependence on the other
     *      simple types inclusion.
     */
    public void includeVoid(boolean pInclude) {
        mIncludeVoid = pInclude;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createContent(Composite pParent, Control pLabel, Control pField, String pBrowseText, boolean pLink) {

        super.createContent(pParent, pLabel, pField, Messages.getString("TypeRow.Browse"), true); //$NON-NLS-1$

        // Add a completion listener on the Text field
        ((Text) mField).addKeyListener(new KeyAdapter() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void keyPressed(KeyEvent pEvent) {

                // react on Ctrl+space
                if (pEvent.character == ' ' && (pEvent.stateMask & SWT.CTRL) != 0) {
                    // if the word sequence is started, complete it
                    Text text = (Text) mField;

                    int pos = text.getCaretPosition();
                    String value = text.getText(0, pos);

                    int i = getStartOfWord(pos, text.getText());

                    if ("sequence".startsWith(value.substring(i, pos)) && mIncludeSequences) { //$NON-NLS-1$
                        String toadd = "sequence".substring(pos - i) + "<>"; //$NON-NLS-1$ //$NON-NLS-2$
                        text.insert(toadd);
                        setValue(text.getText().trim());
                        text.setSelection(pos + toadd.length() - 1);
                        pEvent.doit = false;
                    } else {
                        // check the simple types
                        if (mIncludeSimpleTypes) {
                            String[] simpleTypes = new String[] { "unsigned ", //$NON-NLS-1$
                                "string", //$NON-NLS-1$
                                "short", //$NON-NLS-1$
                                "long", //$NON-NLS-1$
                                "hyper", //$NON-NLS-1$
                                "double", //$NON-NLS-1$
                                "float", //$NON-NLS-1$
                                "any", //$NON-NLS-1$
                                "void", //$NON-NLS-1$
                                "char", //$NON-NLS-1$
                                "type", //$NON-NLS-1$
                                "boolean" //$NON-NLS-1$
                            };

                            for (String type : simpleTypes) {
                                if ((!type.equals("void") || mIncludeVoid) && //$NON-NLS-1$
                                    type.startsWith(value.substring(i, pos))) {
                                    String toadd = type.substring(pos - i);
                                    text.insert(toadd);
                                    setValue(text.getText().trim());
                                    text.setSelection(pos + toadd.length());
                                    pEvent.doit = false;
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        });

        final Shell shell = pParent.getShell();

        addBrowseSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                super.widgetSelected(pEvent);

                // Remove the module type from the current types
                int allowedTypes = mType & Integer.MAX_VALUE - IUnoFactoryConstants.MODULE;

                UnoTypeBrowser browser = new UnoTypeBrowser(shell, allowedTypes);
                browser.setSelectedType(mSelectedType);

                if (Window.OK == browser.open()) {
                    mSelectedType = browser.getSelectedType();
                    if (null != mSelectedType) {
                        Text text = (Text) mField;
                        int pos = text.getCaretPosition();
                        text.insert(mSelectedType.getFullName().replaceAll("\\.", "::")); //$NON-NLS-1$ //$NON-NLS-2$
                        text.setFocus();
                        text.setSelection(pos + mSelectedType.getFullName().length());
                        setValue(text.getText().trim());
                    }
                }
            }
        });
    }

    /**
     * Get the position of the start of the selected word.
     *
     * @param pPos
     *            the caret position
     * @param pText
     *            the text to analyze
     *
     * @return the position of the first character of the word in the text.
     */
    private int getStartOfWord(int pPos, String pText) {
        int i = pPos - 1;
        // For unsigned types
        while (pText.charAt(i) >= 'a' && pText.charAt(i) <= 'z' && i > 0) {
            i--;
        }

        if (i != 0) {
            i++;
        }
        return i;
    }
}
