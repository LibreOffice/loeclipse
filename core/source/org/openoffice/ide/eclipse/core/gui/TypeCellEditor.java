/*************************************************************************
 *
 * $RCSfile: TypeCellEditor.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2009/04/20 06:16:02 $
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
package org.openoffice.ide.eclipse.core.gui;

import java.text.MessageFormat;

import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.unotypebrowser.InternalUnoType;
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeBrowser;

/**
 * Table cell editor for UNO types.
 * 
 * @author cedricbosdo
 *
 */
public class TypeCellEditor extends TextCellEditor {

    private Button mButton;
    private boolean mDialogOpened = false;
    
    private int mType = 0;
    
    private boolean mIncludeSequences = false;
    private boolean mIncludeSimpleTypes = false;
    private boolean mIncludeVoid = true;
    
    /**
     * Constructor.
     * 
     * <p>The types mask is an integer from 0 to 2048-1. The type mask can be
     * obtained by bit-OR of the types constants defined in {@link InternalUnoType}
     * class.</p>
     * 
     * @param pParent the parent composite.
     * @param pTypeMask the types to show.
     */
    public TypeCellEditor(Composite pParent, int pTypeMask) {
        super(pParent, SWT.None);
        
        if (pTypeMask >= 0 && pTypeMask <= InternalUnoType.ALL_TYPES) {
            mType = pTypeMask;
        }
    }
    
    /**
     * Set whether the row should support include auto-completion for sequences.
     * Sequences aren't included in the auto-completion by default. 
     * 
     * @param pInclude <code>true</code> if the row can auto-complete sequences
     */
    public void includeSequences(boolean pInclude) {
        mIncludeSequences = pInclude;
    }
    
    /**
     * Set whether the row should support include auto-completion for simple
     * UNO types. If the simple types are not included in the auto-completion,
     * the void type isn't included too. Simple types aren't included in the 
     * auto-completion by default.
     * 
     * @param pInclude <code>true</code> if the row can auto-complete sequences
     * @see #includeVoid(boolean) to include/exclude the void type
     */
    public void includeSimpleTypes(boolean pInclude) {
        mIncludeSimpleTypes = pInclude;
    }
    
    /**
     * Set whether the row should support include auto-completion for the void 
     * type. The void type is included in the auto-completion by default as long
     * as the simple types are included.
     * 
     * @param pInclude <code>true</code> if the row can auto-complete sequences
     * @see #includeSimpleTypes(boolean) for more precisions on the inclusion
     *         of the void type dependence on the other simple types inclusion.
     */
    public void includeVoid(boolean pInclude) {
        mIncludeVoid = pInclude;
    }
    
    /**
     * Open the type chooser dialog.
     * 
     * @param pParent the parent composite
     * @return the chosen type.
     */
    protected Object openDialogBox(Composite pParent) {
        
        Object result = getValue(); 
        
        // Remove the module type from the current types 
        int allowedTypes = mType & (Integer.MAX_VALUE - IUnoFactoryConstants.MODULE);
        
        UnoTypeBrowser browser = new UnoTypeBrowser(pParent.getShell(), allowedTypes);
        
        if (UnoTypeBrowser.OK == browser.open()) {
            InternalUnoType mSelectedType = browser.getSelectedType();
            if (null != mSelectedType) {
                int pos = text.getCaretPosition();
                text.insert(mSelectedType.getFullName().replaceAll("\\.", "::")); //$NON-NLS-1$ //$NON-NLS-2$
                text.setFocus();
                text.setSelection(pos + mSelectedType.getFullName().length());
                result = text.getText().trim();
            }
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected void focusLost() {
        if (!mDialogOpened) {
            Control focusHolder = Display.getDefault().getFocusControl();
            if (!text.equals(focusHolder) && !mButton.equals(focusHolder)) {
                super.focusLost();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createControl(Composite pParent) {
        
        final Composite editor = new Composite(pParent, SWT.NONE);
        editor.setLayoutData(getLayoutData());
        editor.setLayout(new DialogCellLayout());
        
        super.createControl(editor);
        text.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent pEvent) {
                
                // react on Ctrl+space
                if (pEvent.character == ' ' && (pEvent.stateMask & SWT.CTRL) != 0) {
                    // if the word sequence is started, complete it
                    int pos = text.getCaretPosition();
                    String value = text.getText(0, pos);
                    
                    int i = getStartOfWord(pos, value);
                    
                    if ("sequence".startsWith(value.substring(i,pos)) && mIncludeSequences) { //$NON-NLS-1$
                        String toadd = "sequence".substring(pos - i) + "<>"; //$NON-NLS-1$ //$NON-NLS-2$
                        text.insert(toadd);
                        setValue(text.getText().trim());
                        text.setSelection(pos + toadd.length() - 1);
                        pEvent.doit = false;
                    } else {
                        // check the simple types
                        if (mIncludeSimpleTypes) {
                            String[] simpleTypes = new String[] {
                                "unsigned ", //$NON-NLS-1$
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
        
        
        mButton = new Button(editor, SWT.NONE);
        mButton.setText("..."); //$NON-NLS-1$
        mButton.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent pEvent) {
                TypeCellEditor.this.focusLost();
            }
        });
        
        // open the Uno Types Browser
        mButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                mDialogOpened = true;
                
                Object newValue = openDialogBox(editor);
                
                mDialogOpened = false;
                
                if (newValue != null) {
                    boolean newValidState = isCorrect(newValue);
                    if (newValidState) {
                        markDirty();
                        doSetValue(newValue);
                    } else {
                        // try to insert the current value into the error message.
                        setErrorMessage(MessageFormat.format(getErrorMessage(),
                                new Object[] { newValue.toString() }));
                    }
                    fireApplyEditorValue();
                }
            }
        });
        
        return editor;
    }
    
    /**
     * Get the position of the start of the selected word.
     * 
     * @param pPos the caret position
     * @param pText the text to analyze
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
    
    /**
     * Internal class for laying out the dialog.
     */
    private class DialogCellLayout extends Layout {
        
        /**
         * {@inheritDoc}
         */
        public void layout(Composite pEditor, boolean pForce) {
            Rectangle bounds = pEditor.getClientArea();
            Point size = mButton.computeSize(SWT.DEFAULT, SWT.DEFAULT, pForce);
            if (text != null) {
                text.setBounds(0, 0, bounds.width - size.x, bounds.height);
            }
            mButton.setBounds(bounds.width - size.x, 0, size.x, bounds.height);
        }

        /**
         * {@inheritDoc}
         */
        public Point computeSize(Composite pEditor, int pWidth, int pHeight, boolean pForce) {
            Point size = new Point(pWidth, pHeight);
            
            if (pWidth == SWT.DEFAULT || pHeight == SWT.DEFAULT) {
                Point contentsSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                        pForce);
                Point buttonSize = mButton.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                        pForce);
                // Just return the button width to ensure the button is not clipped
                // if the label is long.
                // The label will just use whatever extra width there is
                size = new Point(buttonSize.x, Math.max(contentsSize.y,
                        buttonSize.y));
            }
            return size;
        }
    }
}
