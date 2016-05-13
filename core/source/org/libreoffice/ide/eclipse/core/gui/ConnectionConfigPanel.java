/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat, Inc.
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
 * Copyright: 2009 by Cédric Bosdonnat, Inc.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.gui;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * Generic component for the selection of the parameters of a UNO connection.
 *
 * <p>
 * Use the {@link #setPatterns(String, String)} method to define the patterns depending on the implementation language.
 * </p>
*/
public class ConnectionConfigPanel {

    private static final String[] TYPE_VALUES = { Messages.getString("ConnectionConfigPanel.Pipe"), //$NON-NLS-1$
        Messages.getString("ConnectionConfigPanel.Socket") }; //$NON-NLS-1$

    private boolean mIsPipe;
    private String[] mPatterns;

    private String mName;
    private String mHost;
    private String mPort;

    private Combo mTypeList;
    private ArrayList<Composite> mDetails;

    private Composite mDetailsComposite;
    private Text mNameTxt;
    private Text mHostTxt;
    private Text mPortTxt;

    private StackLayout mStackLayout;

    /**
     * Constructor.
     *
     * <p>
     * In order to properly work, the parent composite has to have a {@link GridLayout} set.
     * </p>
     *
     * @param pParent
     *            the parent composite to create the controls in.
     */
    public ConnectionConfigPanel(Composite pParent) {
        mIsPipe = false;
        mPatterns = new String[TYPE_VALUES.length];

        mName = "somename"; //$NON-NLS-1$
        mPort = "8100"; //$NON-NLS-1$
        mHost = "localhost"; //$NON-NLS-1$

        createControls(pParent);
    }

    /**
     * Set the patterns to use in {@link #getConnectionCode()}.
     *
     * @param pPipe
     *            the pattern for the pipe connection type. The parameter <code>{0}</code> is the pipe's name.
     *
     * @param pSocket
     *            the pattern for the socket connection type. The parameters mapping is the following:
     *            <ul>
     *            <li><code>{0}</code> maps to the host</li>
     *            <li><code>{1}</code> maps to the port</li>
     *            </ul>
     */
    public void setPatterns(String pPipe, String pSocket) {
        mPatterns[0] = pPipe;
        mPatterns[1] = pSocket;
    }

    /**
     * @return the formatted connection code
     *
     * @see #setPatterns(String, String) for the used patterns
     */
    public String getConnectionCode() {

        String cnxString = null;

        if (mIsPipe) {
            cnxString = MessageFormat.format(mPatterns[0], mName);
        } else {
            cnxString = MessageFormat.format(mPatterns[1], mHost, mPort);
        }

        return cnxString;
    }

    /**
     * Creates all the component's controls.
     *
     * @param pParent
     *            the parent composite where to create the controls
     */
    protected void createControls(Composite pParent) {
        Layout layout = pParent.getLayout();
        if (layout instanceof GridLayout) {
            int layoutColumns = ((GridLayout) layout).numColumns;

            Group body = new Group(pParent, SWT.NONE);
            body.setText(Messages.getString("ConnectionConfigPanel.GroupTitle")); //$NON-NLS-1$

            GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
            gd.horizontalSpan = layoutColumns;
            body.setLayoutData(gd);
            body.setLayout(new GridLayout(2, false));

            Label typeLbl = new Label(body, SWT.NONE);
            typeLbl.setText(Messages.getString("ConnectionConfigPanel.CnxTypeLabel")); //$NON-NLS-1$
            typeLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            mTypeList = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
            mTypeList.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
            mTypeList.setItems(TYPE_VALUES);
            int pos = 1;
            if (mIsPipe) {
                pos = 0;
            }
            mTypeList.select(pos);
            mTypeList.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetSelected(SelectionEvent pEvent) {
                    mIsPipe = mTypeList.getSelectionIndex() == 0;
                    updateDetails();
                }
            });

            mDetails = new ArrayList<Composite>();
            mDetailsComposite = new Composite(body, SWT.NONE);
            gd = new GridData(SWT.FILL, SWT.FILL, true, true);
            gd.horizontalSpan = 2;
            mDetailsComposite.setLayoutData(gd);
            mStackLayout = new StackLayout();
            mDetailsComposite.setLayout(mStackLayout);

            mDetails.add(createPipeControls());
            mDetails.add(createSocketControls());

            updateDetails();
        }
    }

    /**
     * Changes the shown details composite.
     */
    protected void updateDetails() {
        int pos = 1;
        if (mIsPipe) {
            pos = 0;
        }
        mStackLayout.topControl = mDetails.get(pos);
        mDetailsComposite.layout();
    }

    /**
     * @return the newly created composite containing the pipe's configuration controls.
     */
    private Composite createPipeControls() {
        Composite body = new Composite(mDetailsComposite, SWT.NONE);
        body.setLayout(new GridLayout(2, false));

        Label nameLbl = new Label(body, SWT.NONE);
        nameLbl.setText(Messages.getString("ConnectionConfigPanel.Name")); //$NON-NLS-1$
        nameLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        mNameTxt = new Text(body, SWT.SINGLE | SWT.BORDER);
        mNameTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mNameTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent pE) {
                mName = mNameTxt.getText();
            }
        });
        mNameTxt.setText(mName);

        return body;
    }

    /**
     * @return the newly created composite containing the socket's configuration controls.
     */
    private Composite createSocketControls() {
        Composite body = new Composite(mDetailsComposite, SWT.NONE);
        body.setLayout(new GridLayout(2, false));

        Label hostLbl = new Label(body, SWT.NONE);
        hostLbl.setText(Messages.getString("ConnectionConfigPanel.Host")); //$NON-NLS-1$
        hostLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        mHostTxt = new Text(body, SWT.SINGLE | SWT.BORDER);
        mHostTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mHostTxt.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent pE) {
                mHost = mHostTxt.getText();
            }
        });
        mHostTxt.setText(mHost);

        Label portLbl = new Label(body, SWT.NONE);
        portLbl.setText(Messages.getString("ConnectionConfigPanel.Port")); //$NON-NLS-1$
        portLbl.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

        mPortTxt = new Text(body, SWT.SINGLE | SWT.BORDER);
        mPortTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        mPortTxt.addVerifyListener(new VerifyListener() {

            @Override
            public void verifyText(VerifyEvent pEvent) {
                try {
                    if (pEvent.text.length() > 0) {
                        Integer.parseInt(pEvent.text);
                    }
                } catch (NumberFormatException e) {
                    pEvent.doit = false;
                }
            }
        });
        mPortTxt.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent pEvent) {
                mPort = mPortTxt.getText();
            }
        });
        mPortTxt.setText(mPort);

        return body;
    }
}
