/*************************************************************************
 *
 * $RCSfile: UnoTypeBrowser.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2009/04/20 06:16:01 $
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
package org.openoffice.ide.eclipse.core.unotypebrowser;

import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.rows.ChoiceRow;
import org.openoffice.ide.eclipse.core.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.core.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.core.gui.rows.TextRow;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.helpers.Flags;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;

/**
 * A dialog to browse UNO types. This class doesn't launch the types query:
 * this job is performed by the UNO type provider to avoid very slow window
 * rendering.
 *
 * @author cedricbosdo
 *
 */
public class UnoTypeBrowser extends StatusDialog
implements IFieldChangedListener,
IInitListener {

    private static final String F_INPUT = "__input"; //$NON-NLS-1$
    private static final String F_TYPE_FILTER = "__type_filter"; //$NON-NLS-1$

    private static final int TITLE_HEIGHT = 20;
    private static final int TABLE_HEIGHT = 150;
    private static final int COLUMN_WIDTH = 300;
    private static final int ID_REFRESH = 3;

    private TextRow mInputRow;
    private TableViewer mTypesList;
    private Button mRefreshBtn;

    private ChoiceRow mTypeFilterRow;

    private InternalUnoType mSelectedType;
    private Flags mTypes;

    //    private IUnoidlProject mProject;
    //    private IOOo mOOo;


    /**
     * Creates a new browser dialog. The browser, waits for the type provider
     * to finish its work if it's not already over.
     *
     * @param pParentShell the shell where to create the dialog
     * @param pAllowedTypes the bit-ORed allowed types
     *
     */
    public UnoTypeBrowser(Shell pParentShell, int pAllowedTypes) {
        super(pParentShell);

        mTypes = new Flags(UnoTypeProvider.ALL_TYPES, pAllowedTypes, pAllowedTypes);

        setShellStyle(getShellStyle() | SWT.RESIZE);
        setBlockOnOpen(true);
        setTitle(Messages.getString("UnoTypeBrowser.Title")); //$NON-NLS-1$

        // Initialize the Type Browser
        UnoTypeProvider typesProvider = UnoTypeProvider.getInstance();
        typesProvider.addInitListener(this);

        if (!typesProvider.getState().equals(TypeProviderState.INITIALIZED)) {
            // changes the status to warn the user

            updateStatus(new Status(IStatus.INFO,
                            OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                            IStatus.INFO,
                            Messages.getString("UnoTypeBrowser.WaitTypes"), //$NON-NLS-1$
                            null));
        }
    }

    //---------------------------------------------------- UI creation & control

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createButtonsForButtonBar(Composite pParent) {

        mRefreshBtn = createButton(pParent, ID_REFRESH,
                        Messages.getString("UnoTypeBrowser.RefreshButton"), false); //$NON-NLS-1$
        Image img = OOEclipsePlugin.getImage("REFRESH"); //$NON-NLS-1$
        mRefreshBtn.setImage(img);
        mRefreshBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                // Refresh the cache and the view
                activateFields(false);

                updateStatus(new Status(IStatus.INFO,
                                OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                                IStatus.INFO,
                                Messages.getString("UnoTypeBrowser.WaitTypes"), //$NON-NLS-1$
                                null));

                UnoTypeProvider.getInstance().refreshCache();
            }
        });

        UnoTypeProvider typesProvider = UnoTypeProvider.getInstance();
        if (!typesProvider.getState().equals(TypeProviderState.INITIALIZED)) {
            activateFields(false);
        }

        super.createButtonsForButtonBar(pParent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(Composite pParent) {

        // Create the control that contains all the UI components
        Composite body = (Composite)super.createDialogArea(pParent);
        body.setLayout(new GridLayout(2, false));
        body.setLayoutData(new GridData(GridData.FILL_BOTH));

        // Create the title label
        Label titleLabel = new Label(body, SWT.NONE);
        GridData gdLabel = new GridData(GridData.FILL_BOTH |
                        GridData.VERTICAL_ALIGN_BEGINNING);
        gdLabel.horizontalSpan = 2;
        gdLabel.heightHint = TITLE_HEIGHT;
        titleLabel.setLayoutData(gdLabel);
        titleLabel.setText(Messages.getString("UnoTypeBrowser.TitleTitle")); //$NON-NLS-1$

        // create the input text row
        mInputRow = new TextRow(body, F_INPUT,
                        Messages.getString("UnoTypeBrowser.TypeName")); //$NON-NLS-1$
        mInputRow.setFieldChangedListener(this);

        createList(body);

        // create the types filter row
        mTypeFilterRow = new ChoiceRow(body, F_TYPE_FILTER,
                        Messages.getString("UnoTypeBrowser.FilterLabel"), null, false); //$NON-NLS-1$
        mTypeFilterRow.setTooltip(Messages.getString("UnoTypeBrowser.FilterTooltip")); //$NON-NLS-1$
        mTypeFilterRow.setFieldChangedListener(this);
        setFilterValues();
        mTypeFilterRow.select(0);

        UnoTypeProvider typesProvider = UnoTypeProvider.getInstance();

        mTypesList.setInput(typesProvider);

        mInputRow.setFocus();

        return body;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialized() {

        Runnable run = new Runnable() {

            @Override
            public void run() {
                if (!mTypesList.getTable().isDisposed()) {
                    mTypesList.refresh();
                    activateFields(true);

                    updateStatus(new Status(IStatus.INFO,
                                    OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                                    IStatus.INFO,
                                    "", //$NON-NLS-1$
                                    null));
                }
            }
        };

        Display.getDefault().asyncExec(run);
    }

    /**
     * Create and configure the types list.
     *
     * @param pParent the parent composite where to create the list
     */
    private void createList(Composite pParent) {

        Table table = new Table(pParent, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        gd.heightHint = TABLE_HEIGHT;
        table.setLayoutData(gd);

        TableColumn column = new TableColumn(table, SWT.NONE);
        column.setWidth(COLUMN_WIDTH);

        mTypesList = new TableViewer(table);
        mTypesList.setUseHashlookup(true);
        mTypesList.setLabelProvider(new TypeLabelProvider());
        mTypesList.setContentProvider(new InternalTypesProvider());
        mTypesList.addFilter(new UnoTypesFilter());
        mTypesList.setSorter(new ViewerSorter());
        mTypesList.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent pEvent) {
                if (pEvent.getSelection().isEmpty()) {
                    updateStatus(new Status(IStatus.ERROR,
                                    OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                                    IStatus.ERROR,
                                    Messages.getString("UnoTypeBrowser.EmptySelectionError"), //$NON-NLS-1$
                                    null));
                    mSelectedType = null;
                    getButton(IDialogConstants.OK_ID).setEnabled(false);

                } else {
                    updateStatus(new Status(IStatus.OK,
                                    OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                                    IStatus.OK,
                                    "", //$NON-NLS-1$
                                    null));
                    getButton(IDialogConstants.OK_ID).setEnabled(true);

                    IStructuredSelection selection = (IStructuredSelection)pEvent.getSelection();
                    mSelectedType = (InternalUnoType)selection.getFirstElement();

                    mTypesList.refresh(mSelectedType, true);
                }
            }
        });
    }

    /**
     * Selects the filters row from the types mask.
     */
    private void setFilterValues() {
        // This filter is always present
        addFilter(UnoTypeProvider.ALL_TYPES, "UnoTypeBrowser.FilterAll"); //$NON-NLS-1$

        addFilter(IUnoFactoryConstants.BASICS, "UnoTypeBrowser.FilterSimple"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.SERVICE, "UnoTypeBrowser.FilterServices"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.INTERFACE, "UnoTypeBrowser.FilterInterfaces"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.SINGLETON, "UnoTypeBrowser.FilterSingletons"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.ENUM, "UnoTypeBrowser.FilterEnumerations"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.STRUCT, "UnoTypeBrowser.FilterStructures"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.CONSTANT,"UnoTypeBrowser.FilterConstants"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.CONSTANTS, "UnoTypeBrowser.FilterConstantsGroups"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.EXCEPTION, "UnoTypeBrowser.FilterException"); //$NON-NLS-1$
        addFilter(IUnoFactoryConstants.TYPEDEF, "UnoTypeBrowser.FilterTypedefs"); //$NON-NLS-1$
    }

    /**
     * Add a filter if it is selected by the types mask.
     *
     * The value of the filter option is a string version of the type
     *
     * @param pType the type to add in {@link IUnoFactoryConstants}
     * @param pMessageKey the message key to use to get the message from the messages bundle.
     */
    private void addFilter(int pType, String pMessageKey) {
        if (pType == UnoTypeProvider.ALL_TYPES || mTypes.isFlagSet(pType)) {
            String value = Integer.toString(pType);
            mTypeFilterRow.add(Messages.getString(pMessageKey), value);
        }
    }

    /**
     * Method to activate or unactivate the dialog fields.
     *
     * This method should be used when long operations are performed.
     *
     * @param pActivate <code>true</code> to activate all the fields, <code>false</code> to
     *          set the fields as not active.
     */
    public void activateFields(boolean pActivate) {
        mInputRow.setEnabled(pActivate);
        mTypesList.getTable().setEnabled(pActivate);
        mTypeFilterRow.setEnabled(pActivate);

        // The refresh button can be null during the dialog construction
        if (mRefreshBtn != null) {
            mRefreshBtn.setEnabled(pActivate);
        }

        Button okButton = getButton(IDialogConstants.OK_ID);
        if (null != okButton) {
            okButton.setEnabled(pActivate);
        }

        Button cancelButton = getButton(IDialogConstants.CANCEL_ID);
        if (null != cancelButton) {
            cancelButton.setEnabled(pActivate);
        }
    }

    /**
     * Provides the label and image for the list items.
     *
     * @author cedricbosdo
     */
    class TypeLabelProvider extends LabelProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Image getImage(Object pElement) {
            Image result = null;

            if (pElement instanceof InternalUnoType) {
                int type = ((InternalUnoType)pElement).getType();

                if (IUnoFactoryConstants.SERVICE == type) {
                    result = OOEclipsePlugin.getImage(ImagesConstants.SERVICE);
                } else if (IUnoFactoryConstants.INTERFACE == type) {
                    result = OOEclipsePlugin.getImage(ImagesConstants.INTERFACE);
                } else if (IUnoFactoryConstants.STRUCT == type) {
                    result = OOEclipsePlugin.getImage(ImagesConstants.STRUCT);
                } else if (IUnoFactoryConstants.ENUM == type) {
                    result = OOEclipsePlugin.getImage(ImagesConstants.ENUM);
                } else if (IUnoFactoryConstants.EXCEPTION == type) {
                    result = OOEclipsePlugin.getImage(ImagesConstants.EXCEPTION);
                } else if (IUnoFactoryConstants.CONSTANTS == type) {
                    result = OOEclipsePlugin.getImage(ImagesConstants.CONSTANTS);
                } else if (IUnoFactoryConstants.TYPEDEF == type) {
                    result = OOEclipsePlugin.getImage(ImagesConstants.TYPEDEF);
                }

            }
            return result;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getText(Object pElement) {
            String result = ""; //$NON-NLS-1$

            if (pElement instanceof InternalUnoType) {
                InternalUnoType type = (InternalUnoType)pElement;
                result = type.getName();

                if (!mTypesList.getSelection().isEmpty()) {
                    IStructuredSelection selection = (IStructuredSelection)
                                    mTypesList.getSelection();

                    if (selection.getFirstElement().equals(type)) {
                        result = result + " - " + type.getFullName(); //$NON-NLS-1$
                    }
                }
            }
            return result;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void okPressed() {

        /**
         * The calling method have to close the dialog window after having get the
         * resulting data.
         */

        if (mTypesList.getSelection().isEmpty()) {
            updateStatus(new Status(IStatus.ERROR,
                            OOEclipsePlugin.OOECLIPSE_PLUGIN_ID,
                            IStatus.ERROR,
                            Messages.getString("UnoTypeBrowser.EmptySelectionError"),  //$NON-NLS-1$
                            null));
            getButton(IDialogConstants.OK_ID).setEnabled(false);
            UnoTypeProvider.getInstance().removeInitListener(this);
        } else {
            super.okPressed();
        }
    }

    //------------------------------------------------- React on field changing

    /**
     * Refreshes the dialog.
     */
    private void refresh() {

        activateFields(false);
        mTypesList.refresh();
        activateFields(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void fieldChanged(FieldEvent pEvent) {

        if (pEvent.getProperty().equals(F_TYPE_FILTER)) {
            try {
                mTypes.setTypes(Integer.parseInt(pEvent.getValue()));
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        refresh();

        mInputRow.setFocus();
    }

    //---------------------------------------- Filters the elements in the list

    /**
     * List items filter class.
     *
     * @author cedricbosdo
     */
    private class UnoTypesFilter extends ViewerFilter {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean select(Viewer pViewer, Object pParentElement, Object pElement) {
            boolean select = false;

            if (pElement instanceof InternalUnoType) {
                InternalUnoType type = (InternalUnoType)pElement;
                if (mTypes.isFlagSet(type.getType())) {
                    // The type is correct, check the name
                    if (type.getName().startsWith(mInputRow.getValue())) {
                        select = true;
                    }
                }
            }
            return select;
        }
    }

    //----------------------------------------- Manages the content of the list

    /**
     * @return the selected {@link InternalUnoType}.
     */
    public InternalUnoType getSelectedType() {
        return mSelectedType;
    }

    /**
     * Set the type selected in the list.
     *
     * @param pType the type which should be selected
     */
    public void setSelectedType(InternalUnoType pType) {
        mSelectedType = pType;

        if (null != mTypesList) {
            IStructuredSelection selection = StructuredSelection.EMPTY;
            if (null != mSelectedType) {
                selection = new StructuredSelection(mSelectedType);
            }

            mTypesList.setSelection(selection);
        }
    }

    /**
     * Provides the content to the list viewer.
     *
     * @author cedricbosdo
     *
     */
    private class InternalTypesProvider implements IStructuredContentProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object pInputElement) {
            ArrayList<String> containers = new ArrayList<String>();

            if (mTypes.isFlagSet(IUnoFactoryConstants.BASICS)) {
                containers.add(UnoTypeProvider.BASIC_TYPES_KEY);
            }

            return UnoTypeProvider.getInstance().toArray(containers.toArray(new String[containers.size()]));
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
            // Nothing to do
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inputChanged(Viewer pViewer, Object pOldInput, Object pNewInput) {
            // Should never happen
        }
    }
}
