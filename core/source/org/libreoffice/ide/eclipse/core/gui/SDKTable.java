/*************************************************************************
 *
 * $RCSfile: SDKTable.java,v $
 *
 * $Revision: 1.7 $
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
package org.libreoffice.ide.eclipse.core.gui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.libreoffice.ide.eclipse.core.OOEclipsePlugin;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.libreoffice.ide.eclipse.core.gui.rows.FileRow;
import org.libreoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.libreoffice.ide.eclipse.core.gui.rows.TextRow;
import org.libreoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.libreoffice.ide.eclipse.core.internal.model.SDK;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.core.model.config.IConfigListener;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * This class creates the whole SDK table with it's viewer and content provider. This class encloses a SDK editor
 * dialog.
 *
 * @see AbstractTable for the basic table functions descriptions
 *
 * @author cedricbosdo
 *
 */
public class SDKTable extends AbstractTable {

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 200;
    /**
     * Temporary SDK for storing the values fetched from the dialog.
     */
    private SDK mTmpSdk;

    /**
     * Main constructor of the SDK Table. It's style can't be configured like other SWT composites. When using a SDK
     * Table, you should add all the necessary Layouts and Layout Data to display it correctly.
     *
     * @param pParent
     *            Composite parent of the table.
     */
    public SDKTable(Composite pParent) {
        super(pParent, Messages.getString("SDKTable.Title"), //$NON-NLS-1$
            new String[] { Messages.getString("SDKTable.NameTitle"), //$NON-NLS-1$
                Messages.getString("SDKTable.PathTitle") //$NON-NLS-1$
        }, new int[] { DEFAULT_WIDTH, DEFAULT_HEIGHT }, new String[] { SDK.NAME, SDK.PATH });

        mTableViewer.setInput(SDKContainer.getInstance());
        mTableViewer.setContentProvider(new SDKContentProvider());
    }

    /**
     * Fill the table with the preferences from the SDKS_CONFIG file.
     */
    public void getPreferences() {
        SDKContainer.getInstance();
    }

    /**
     * Saves the SDK preferences.
     *
     */
    public void savePreferences() {

        SDKContainer.saveSDKs();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDoubleClick(DoubleClickEvent pEvent) {
        if (!pEvent.getSelection().isEmpty()) {

            // Get the double clicked SDK line
            SDK sdk = (SDK) ((IStructuredSelection) pEvent.getSelection()).getFirstElement();

            // Launch the dialog
            sdk = openDialog(sdk);
            SDKContainer.updateSDK(sdk.getId(), sdk);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ITableElement addLine() {
        // Launch add SDK dialog
        SDK sdk = openDialog(null);
        SDKContainer.addSDK(sdk);
        return sdk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ITableElement removeLine() {
        ITableElement o = super.removeLine();
        if (null != o && o instanceof SDK) {
            SDKContainer.delSDK((SDK) o);
        }

        return o;
    }

    /**
     * This method create and calls the dialog box to be launched on SDK edition or SDK creation. The parameter
     * <code>pSdk</code> could be null: in this case, a new one will be created. Otherwise the fields of the old one
     * will be changed. This is useful for SDK editing: the object reference is the same.
     *
     * @param pSdk
     *            the SDK instance to edit if any
     *
     * @return the modified or created SDK instance
     */
    protected SDK openDialog(SDK pSdk) {

        // Gets the shell of the active eclipse window
        Shell shell = OOEclipsePlugin.getDefault().getWorkbench().getActiveWorkbenchWindow().getShell();

        SDKDialog dialog = new SDKDialog(shell, pSdk);
        if (Window.OK == dialog.open()) {
            // The user validates his choice, perform the changes
            SDK newSDK = mTmpSdk;
            mTmpSdk = null;

            if (null != pSdk) {
                // Only an existing SDK modification
                try {
                    pSdk.setHome(newSDK.getHome());
                } catch (InvalidConfigException e) {
                    PluginLogger.error(e.getLocalizedMessage(), e);
                    // localized in SDK class
                }
            } else {
                // Creation of a new SDK

                pSdk = newSDK;
            }

        }

        return pSdk;
    }

    /**
     * The SDK content provider is a class which provides the SDKs objects to the viewer.
     *
     * @author cedricbosdo
     *
     */
    class SDKContentProvider implements IStructuredContentProvider, IConfigListener {

        /**
         * Constructor.
         */
        public SDKContentProvider() {
            if (null == SDKContainer.getInstance()) {
                SDKContainer.getInstance();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object pInputElement) {
            return SDKContainer.toArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            SDKContainer.removeListener(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inputChanged(Viewer pViewer, Object pOldInput, Object pNewInput) {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void ConfigAdded(Object pElement) {
            if (pElement instanceof SDK) {
                mTableViewer.add(pElement);

                // This redrawing order is necessary to avoid having strange columns
                mTable.redraw();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void ConfigRemoved(Object pElement) {
            if (null != pElement && pElement instanceof SDK) {
                // Only one SDK to remove
                mTableViewer.remove(pElement);
            } else {
                // All the SDK have been removed
                if (null != mTableViewer) {
                    int i = 0;
                    SDK sdki = (SDK) mTableViewer.getElementAt(i);

                    while (null != sdki) {
                        mTableViewer.remove(sdki);
                    }
                }
            }

            // This redrawing order is necessary to avoid having strange columns
            mTable.redraw();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void ConfigUpdated(Object pElement) {
            if (pElement instanceof SDK) {
                // Note that we can do this only because the SDK Container guarantees
                // that the reference of the sdk will not change during an update
                mTableViewer.update(pElement, null);
            }
        }
    }

    /**
     * Class for the SDK add/edit dialog.
     *
     * @author cedricbosdo
     */
    class SDKDialog extends StatusDialog implements IFieldChangedListener {

        private static final String P_SDK_PATH = "__sdk_path"; //$NON-NLS-1$

        private static final int LAYOUT_COLUMNS = 3;

        private FileRow mSdkpathRow;

        private TextRow mBuidlidRow;

        private SDK mSdk;

        private final Color mWHITE = new Color(getDisplay(), 255, 255, 255);

        /**
         * Create the SDK dialog without any SDK instance.
         *
         * @param pParentShell
         *            the shell where to put the dialog
         */
        protected SDKDialog(Shell pParentShell) {
            this(pParentShell, null);
        }

        /**
         * Create the SDK dialog with an SDK instance to edit.
         *
         * @param pParentShell
         *            the shell where to put the dialog
         * @param pSdk
         *            the SDK instance to edit
         */
        protected SDKDialog(Shell pParentShell, SDK pSdk) {
            super(pParentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE);
            this.mSdk = pSdk;

            // This dialog is a modal one
            setBlockOnOpen(true);
            setTitle(Messages.getString("SDKTable.DialogTitle")); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected Control createDialogArea(Composite pParent) {

            Composite body = new Composite(pParent, SWT.None);
            body.setLayout(new GridLayout(LAYOUT_COLUMNS, false));
            body.setLayoutData(new GridData(GridData.FILL_BOTH));

            Label image = new Label(body, SWT.RIGHT);
            // White background
            image.setBackground(mWHITE);
            image.setImage(OOEclipsePlugin.getImage(ImagesConstants.SDK_DIALOG_IMAGE));
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = LAYOUT_COLUMNS;
            image.setLayoutData(gd);

            // Creates each line of the dialog
            mSdkpathRow = new FileRow(body, P_SDK_PATH, Messages.getString("SDKTable.PathTitle"), true); //$NON-NLS-1$
            mSdkpathRow.setFieldChangedListener(this);

            // put the value of the edited SDK in the fields
            if (null != mSdk) {
                mSdkpathRow.setValue(mSdk.getHome());
            }

            mBuidlidRow = new TextRow(body, "", //$NON-NLS-1$
                Messages.getString("SDKTable.NameTitle")); //$NON-NLS-1$
            // This line is only to show the value
            mBuidlidRow.setEnabled(false);

            if (null != mSdk && null != mSdk.getId()) {
                mBuidlidRow.setValue(mSdk.getId());
            }

            // activate the OK button only if the SDK is correct
            Button okButton = getButton(IDialogConstants.OK_ID);
            if (null != okButton) {
                okButton.setEnabled(isValid(null));
            }

            return body;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void okPressed() {
            // Perform data controls on the fields: they are all mandatory
            // If there is one field missing, print an error line at the bottom
            // of the dialog.

            if (!mSdkpathRow.getValue().equals("")) { //$NON-NLS-1$
                isValid(null);
                super.okPressed();
            } else {
                updateStatus(new Status(IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.ERROR,
                    Messages.getString("SDKTable.MissingFieldError"), //$NON-NLS-1$
                    null));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void cancelPressed() {

            super.cancelPressed();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fieldChanged(FieldEvent pEvent) {
            // The result doesn't matter: we only want to update the status of the windows

            Button okButton = getButton(IDialogConstants.OK_ID);
            if (null != okButton) {
                okButton.setEnabled(isValid(pEvent.getProperty()));
            }
        }

        /**
         * Checks if the property is valid.
         *
         * @param pProperty
         *            the property to check
         * @return <code>true</code> if the property is valid, <code>false</code> otherwise.
         */
        private boolean isValid(String pProperty) {
            boolean result = false;

            // Try to create an SDK
            try {
                mTmpSdk = new SDK(mSdkpathRow.getValue());

                if (null != mTmpSdk.getId()) {
                    mBuidlidRow.setValue(mTmpSdk.getId());
                }

                updateStatus(new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.OK, "", null)); //$NON-NLS-1$
                result = true;
            } catch (InvalidConfigException e) {
                if (pProperty.equals(P_SDK_PATH) && InvalidConfigException.INVALID_SDK_HOME == e.getErrorCode()) {
                    updateStatus(new Status(IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.ERROR,
                        e.getMessage(), e));
                } else {
                    updateStatus(new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.OK, "", //$NON-NLS-1$
                        e));
                }
            }

            return result;
        }
    }
}
