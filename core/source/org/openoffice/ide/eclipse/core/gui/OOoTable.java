/*************************************************************************
 *
 * $RCSfile: OOoTable.java,v $
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
package org.openoffice.ide.eclipse.core.gui;

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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.FileRow;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.model.AbstractOOo;
import org.openoffice.ide.eclipse.core.internal.model.OOo;
import org.openoffice.ide.eclipse.core.internal.model.URE;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.config.IConfigListener;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.InvalidConfigException;

/**
 * This class creates the whole OOo table with it's viewer and content provider. This class encloses an OOo editor
 * dialog.
 *
 * @see AbstractTable for the basic table functions descriptions
 *
 * @author cedricbosdo
 *
 */
public class OOoTable extends AbstractTable {

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 200;
    /**
     * Temporary OOo for storing the values fetched from the dialog.
     */
    private AbstractOOo mTmpOOo;

    /**
     * Main constructor of the OOo Table. It's style can't be configured like other SWT composites. When using a OOo
     * Table, you should add all the necessary Layouts and Layout Data to display it correctly.
     *
     * @param pParent
     *            Composite parent of the table.
     */
    public OOoTable(Composite pParent) {
        super(pParent, Messages.getString("OOoTable.Title"), //$NON-NLS-1$
            new String[] { Messages.getString("OOoTable.NameTitle"), //$NON-NLS-1$
                Messages.getString("OOoTable.PathTitle") //$NON-NLS-1$
        }, new int[] { DEFAULT_WIDTH, DEFAULT_HEIGHT }, new String[] { AbstractOOo.NAME, AbstractOOo.PATH });

        mTableViewer.setInput(OOoContainer.getInstance());
        mTableViewer.setContentProvider(new OOoContentProvider());
    }

    /**
     * Fill the table with the preferences from the OOOS_CONFIG file.
     */
    public void getPreferences() {
        OOoContainer.getInstance();
    }

    /**
     * Saves the OOos in the OOOS_CONFIG file.
     *
     */
    public void savePreferences() {

        OOoContainer.saveOOos();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ITableElement addLine() {

        AbstractOOo ooo = openDialog(null);
        OOoContainer.addOOo(ooo);
        return ooo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ITableElement removeLine() {

        ITableElement o = super.removeLine();
        if (null != o && o instanceof IOOo) {
            OOoContainer.delOOo((IOOo) o);
        }
        return o;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handleDoubleClick(DoubleClickEvent pEvent) {
        if (!pEvent.getSelection().isEmpty()) {

            // Get the double clicked OOo line
            AbstractOOo ooo = (AbstractOOo) ((IStructuredSelection) pEvent.getSelection()).getFirstElement();

            // Launch the dialog
            ooo = openDialog(ooo);
            OOoContainer.updateOOo(ooo.getName(), ooo);
        }
    }

    /**
     * This method create and calls the dialog box to be launched on LibreOffice edition or LibreOffice creation. The
     * parameter <code>pOoo</code> could be null: in this case, a new one will be created. Otherwise the fields of the
     * old one will be changed. This is useful for OOo editing: the object reference is the same.
     *
     * @param pOoo
     *            the LibreOffice instance to show in the dialog
     * @return the modified or created LibreOffice instance
     */
    protected AbstractOOo openDialog(AbstractOOo pOoo) {

        // Gets the shell of the active eclipse window
        Shell shell = Display.getDefault().getActiveShell();

        OOoDialog dialog = new OOoDialog(shell, pOoo);
        if (Window.OK == dialog.open()) {
            // The user validates his choice, perform the changes
            AbstractOOo newOOo = mTmpOOo;
            mTmpOOo = null;

            if (null != pOoo) {
                // Only an existing OOo modification
                try {
                    pOoo.setHome(newOOo.getHome());
                } catch (InvalidConfigException e) {
                    PluginLogger.error(e.getLocalizedMessage(), e);
                    // localized in OOo class
                }
            } else {
                // Creation of a new OOo
                pOoo = newOOo;
            }

        }
        return pOoo;
    }

    /**
     * The OOo content provider is a class which provides the OOos objects to the viewer.
     *
     * @author cedricbosdo
     *
     */
    class OOoContentProvider implements IStructuredContentProvider, IConfigListener {

        /**
         * Crates a content provider using the OOo container.
         */
        public OOoContentProvider() {
            if (null == OOoContainer.getInstance()) {
                OOoContainer.getInstance();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object pInputElement) {
            return OOoContainer.toArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            OOoContainer.removeListener(this);
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
            if (pElement instanceof OOo) {
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
            if (null != pElement && pElement instanceof IOOo) {
                // Only one OOo to remove
                mTableViewer.remove(pElement);
            } else {
                // All the OOo have been removed
                if (null != mTableViewer) {
                    int i = 0;
                    IOOo oooi = (IOOo) mTableViewer.getElementAt(i);

                    while (null != oooi) {
                        mTableViewer.remove(oooi);
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
            if (pElement instanceof OOo) {
                // Note that we can do this only because the OOo Container guarantees
                // that the reference of the ooo will not change during an update
                mTableViewer.update(pElement, null);
            }
        }
    }

    /**
     * Class for the LibreOffice add/edit dialog.
     *
     * @author cedricbosdo
     */
    class OOoDialog extends StatusDialog implements IFieldChangedListener {

        private static final String P_OOO_PATH = "__ooo_path"; //$NON-NLS-1$
        private static final String P_OOO_NAME = "__ooo_name"; //$NON-NLS-1$
        private static final int LAYOUT_COLUMNS = 3;

        private final Color mWHITE = new Color(getDisplay(), 255, 255, 255);

        private FileRow mOOopathRow;

        private TextRow mNameRow;

        private AbstractOOo mOOo;

        /**
         * Create the LibreOffice dialog without any Ooo instance.
         *
         * @param pParentShell
         *            the shell where to put the dialog
         */
        protected OOoDialog(Shell pParentShell) {
            this(pParentShell, null);
        }

        /**
         * Create the LibreOffice dialog with an OOo instance to edit.
         *
         * @param pParentShell
         *            the shell where to put the dialog
         * @param pOoo
         *            the OOo instance to edit
         */
        protected OOoDialog(Shell pParentShell, AbstractOOo pOoo) {
            super(pParentShell);
            setShellStyle(getShellStyle() | SWT.RESIZE);
            this.mOOo = pOoo;

            // This dialog is a modal one
            setBlockOnOpen(true);
            setTitle(Messages.getString("OOoTable.DialogTitle")); //$NON-NLS-1$
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
            image.setImage(OOEclipsePlugin.getImage(ImagesConstants.OOO_DIALOG_IMAGE));
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = LAYOUT_COLUMNS;
            image.setLayoutData(gd);

            // Creates each line of the dialog
            mOOopathRow = new FileRow(body, P_OOO_PATH, Messages.getString("OOoTable.PathTitle"), true); //$NON-NLS-1$
            mOOopathRow.setFieldChangedListener(this);

            // put the value of the edited OOo in the fields
            if (null != mOOo) {
                mOOopathRow.setValue(mOOo.getHome());
            }

            mNameRow = new TextRow(body, P_OOO_NAME, Messages.getString("OOoTable.NameTitle")); //$NON-NLS-1$
            mNameRow.setFieldChangedListener(this);

            if (null != mOOo && null != mOOo.getName()) {
                mNameRow.setValue(mOOo.getName());
                mNameRow.setEnabled(false);
            }

            // activate the OK button only if the OOo is correct
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

            if (!mOOopathRow.getValue().equals("")) { //$NON-NLS-1$
                isValid(null);
                super.okPressed();
            } else {
                updateStatus(new Status(IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.ERROR,
                    Messages.getString("OOoTable.MissingFieldError"), //$NON-NLS-1$
                    null));
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fieldChanged(FieldEvent pEvent) {

            // The result doesn't matter: we only want to update the status of the windows
            Button okButton = getButton(IDialogConstants.OK_ID);
            if (null != okButton) {

                if (pEvent.getProperty().equals(P_OOO_PATH)) {
                    okButton.setEnabled(isValid(pEvent.getProperty()));
                }

                // checks if the name is unique and toggle a warning
                if (pEvent.getProperty().equals(P_OOO_NAME)) {
                    boolean unique = !OOoContainer.containsName(pEvent.getValue());

                    if (unique) {
                        updateStatus(new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.OK, "", null)); //$NON-NLS-1$
                    } else {
                        updateStatus(new Status(IStatus.WARNING, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.WARNING,
                            Messages.getString("OOoTable.NameExistsError"), //$NON-NLS-1$
                            null));
                    }
                }
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

            // Try to create an OOo
            try {
                mTmpOOo = new OOo(mOOopathRow.getValue(), mNameRow.getValue());

                if (null != mTmpOOo.getName()) {
                    mNameRow.setValue(mTmpOOo.getName());
                }

                updateStatus(new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.OK, "", null)); //$NON-NLS-1$

                result = true;

            } catch (InvalidConfigException e) {

                try {

                    mTmpOOo = new URE(mOOopathRow.getValue(), mNameRow.getValue());
                    if (null != mTmpOOo.getName()) {
                        mNameRow.setValue(mTmpOOo.getName());
                    }

                    updateStatus(new Status(IStatus.OK, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.OK, "", null)); //$NON-NLS-1$

                    result = true;

                } catch (InvalidConfigException ex) {
                    updateStatus(new Status(IStatus.ERROR, OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, IStatus.ERROR,
                        Messages.getString("OOoTable.InvalidPathError"), //$NON-NLS-1$
                        ex));
                }
            }

            return result;
        }
    }
}
