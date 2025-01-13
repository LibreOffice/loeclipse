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
package org.libreoffice.ide.eclipse.core.gui.rows;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Row class with a text and a configurable button to open a dialog.
 */
public class DialogRow extends LabeledRow implements ModifyListener {

    private String mValue = new String();

    /**
     * Simple constructor for the Dialog row creation. The button text will be set to <em>Browse</em>.
     *
     * @param parent
     *            the composite in which to create the row
     * @param property
     *            the property to recognize an event from this row
     * @param label
     *            the label on the left of the row
     * @param link
     *            tells whether to create a browse link or button
     */
    public DialogRow(Composite parent, String property, String label, boolean link) {
        this(parent, property, label, Messages.getString("DialogRow.BrowseLabel"), link); //$NON-NLS-1$
    }

    /**
     * Constructor for the Dialog row creation allowing to change the button text.
     *
     * @param parent
     *            the composite in which to create the row
     * @param property
     *            the property to recognize an event from this row
     * @param label
     *            the label on the left of the row
     * @param btnLabel
     *            the label of the button opening the dialog
     * @param link
     *            tells whether to create a browse link or button
     *
     */
    public DialogRow(Composite parent, String property, String label, String btnLabel, boolean link) {
        super(property);

        Label aLabel = new Label(parent, SWT.LEFT | SWT.SHADOW_NONE);
        aLabel.setText(label);
        Text aField = new Text(parent, SWT.BORDER);

        createContent(parent, aLabel, aField, btnLabel, link);
        ((Text) mField).addModifyListener(this);

        addBrowseSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                String newValue = doOpenDialog();
                if (!mValue.equals(newValue)) {
                    setValue(newValue);
                }
            }
        });
    }

    /**
     * Open the dialog when clicking on the right button. Subclasses, may implement this method. Default returns an
     * empty string.
     *
     * @return the new value for the row
     */
    public String doOpenDialog() {
        return ""; //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValue() {
        return mValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void modifyText(ModifyEvent event) {
        setValue(((Text) mField).getText().trim());
    }

    /**
     * Set a new value to the row.
     *
     * @param pValue
     *            the new value
     */
    public void setValue(String pValue) {
        String newText = pValue;
        if (null == pValue) {
            newText = ""; //$NON-NLS-1$
        }

        if (!((Text) mField).getText().equals(newText)) {
            ((Text) mField).setText(newText);
        }

        mValue = newText;
        FieldEvent fe = new FieldEvent(getProperty(), getValue());
        fireFieldChangedEvent(fe);
    }
}
