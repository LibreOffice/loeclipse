/*************************************************************************
 *
 * $RCSfile: TypeCellEditor.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/23 18:27:15 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
import org.openoffice.ide.eclipse.core.unotypebrowser.UnoTypeProvider;

/**
 * Table cell editor for UNO types.
 * 
 * @author cedricbosdo
 *
 */
public class TypeCellEditor extends TextCellEditor {

	private Button button;
	private boolean mDialogOpened = false;
	
	private int mType = 0;
	
	private boolean mIncludeSequences = false;
	private boolean mIncludeSimpleTypes = false;
	private boolean mIncludeVoid = true;
	
	public TypeCellEditor(Composite parent, int aTypeMask) {
		super(parent, SWT.None);
		if (aTypeMask >=0 && aTypeMask <= InternalUnoType.ALL_TYPES) {
			mType = aTypeMask;
		}
	}
	
	/**
	 * Set whether the row should support include auto-completion for sequences.
	 * Sequences aren't included in the auto-completion by default. 
	 * 
	 * @param include <code>true</code> if the row can auto-complete sequences
	 */
	public void includeSequences(boolean include) {
		mIncludeSequences = include;
	}
	
	/**
	 * Set whether the row should support include auto-completion for simple
	 * UNO types. If the simple types are not included in the auto-completion,
	 * the void type isn't included too. Simple types aren't included in the 
	 * auto-completion by default.
	 * 
	 * @param include <code>true</code> if the row can auto-complete sequences
	 * @see #includeVoid(boolean) to include/exclude the void type
	 */
	public void includeSimpleTypes(boolean include) {
		mIncludeSimpleTypes = include;
	}
	
	/**
	 * Set whether the row should support include auto-completion for the void 
	 * type. The void type is included in the auto-completion by default as long
	 * as the simple types are included.
	 * 
	 * @param include <code>true</code> if the row can auto-complete sequences
	 * @see #includeSimpleTypes(boolean) for more precisions on the inclusion
	 * 		of the void type dependence on the other simple types inclusion.
	 */
	public void includeVoid(boolean include) {
		mIncludeVoid = include;
	}
	
	protected Object openDialogBox(Composite parent) {
		
		Object result = getValue(); 
		
		UnoTypeProvider typesProvider = UnoTypeProvider.getInstance();
		
		int oldType = typesProvider.getTypes();
		typesProvider.setTypes(mType & 
				UnoTypeProvider.invertTypeBits(IUnoFactoryConstants.MODULE));
		
		UnoTypeBrowser browser = new UnoTypeBrowser(
				parent.getShell(), typesProvider);
		
		if (UnoTypeBrowser.OK == browser.open()) {
			InternalUnoType mSelectedType = browser.getSelectedType();
			if (null != mSelectedType){
				int pos = text.getCaretPosition();
				text.insert(mSelectedType.getFullName());
				text.setFocus();
				text.setSelection(pos + mSelectedType.getFullName().length());
				result = text.getText().trim();
			}
		}
		
		typesProvider.setTypes(oldType);
		return result;
	}
	
	@Override
	protected void focusLost() {
		if (!mDialogOpened) {
			Control focusHolder = Display.getDefault().getFocusControl();
			if (!text.equals(focusHolder) && !button.equals(focusHolder)) {
				super.focusLost();
			}
		}
	}
	
	@Override
	protected Control createControl(Composite parent) {
		
		final Composite editor = new Composite(parent, SWT.NONE);
		editor.setLayoutData(getLayoutData());
		editor.setLayout(new DialogCellLayout());
		
		super.createControl(editor);
		text.addKeyListener(new KeyAdapter(){
			@Override
			public void keyPressed(KeyEvent e) {
				
				// react on Ctrl+space
				if (e.character == ' ' && (e.stateMask & SWT.CTRL) != 0) {
					// if the word sequence is started, complete it
					int pos = text.getCaretPosition();
					String value = text.getText(0, pos);
					
					int i = pos - 1;
					while (value.charAt(i) >= 'a' && value.charAt(i)<='z' && i>0) { // For unsigned types
						i--;
					}
					
					if (i != 0) i++;
					
					if ("sequence".startsWith(value.substring(i,pos)) && mIncludeSequences) { //$NON-NLS-1$
						String toadd = "sequence".substring(pos-i) + "<>"; //$NON-NLS-1$ //$NON-NLS-2$
						text.insert(toadd);
						setValue(text.getText().trim());
						text.setSelection(pos + toadd.length() - 1);
						e.doit = false;
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
									"type" //$NON-NLS-1$
							};

							for (String type : simpleTypes) {
								if (type.equals("void") && mIncludeVoid) { //$NON-NLS-1$
									if (type.startsWith(value.substring(i, pos))) {
										String toadd = type.substring(pos-i);
										text.insert(toadd);
										setValue(text.getText().trim());
										text.setSelection(pos + toadd.length());
										e.doit = false;
										break;
									}
								}
							}
						}
					}
				}
			}	
		});
		
		
		button = new Button(editor, SWT.NONE);
		button.setText("..."); //$NON-NLS-1$
		button.addFocusListener(new FocusAdapter(){
			public void focusLost(FocusEvent e) {
				TypeCellEditor.this.focusLost();
			}
		});
		
		// open the Uno Types Browser
		button.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
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
     * Internal class for laying out the dialog.
     */
    private class DialogCellLayout extends Layout {
        public void layout(Composite editor, boolean force) {
            Rectangle bounds = editor.getClientArea();
            Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT, force);
            if (text != null) {
				text.setBounds(0, 0, bounds.width - size.x, bounds.height);
			}
            button.setBounds(bounds.width - size.x, 0, size.x, bounds.height);
        }

        public Point computeSize(Composite editor, int wHint, int hHint,
                boolean force) {
            if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
				return new Point(wHint, hHint);
			}
            Point contentsSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    force);
            Point buttonSize = button.computeSize(SWT.DEFAULT, SWT.DEFAULT,
                    force);
            // Just return the button width to ensure the button is not clipped
            // if the label is long.
            // The label will just use whatever extra width there is
            Point result = new Point(buttonSize.x, Math.max(contentsSize.y,
                    buttonSize.y));
            return result;
        }
    }
}
