/*************************************************************************
 *
 * $RCSfile: LibsSection.java,v $
 *
 * $Revision: 1.4 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:51 $
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
package org.libreoffice.ide.eclipse.core.editors.pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.gui.ProjectSelectionDialog;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.model.pack.PackagePropertiesModel;

/**
 * Basic and dialog libraries section of the Contents form page of the package editor.
*/
public class LibsSection extends SectionPart {

    private static final String P_LIBTYPE = "__p_libtype"; //$NON-NLS-1$
    private static final String P_NAME = "__p_name"; //$NON-NLS-1$
    private static final Integer BASIC_LIB = new Integer(0);
    private static final Integer DIALOG_LIB = new Integer(1);
    private static final int FOLDER_COLUMN_WIDTH = 200;
    private static final int TYPE_COLUMN_WIDTH = 100;

    private PackageFormPage mPage;
    private TableViewer mTableViewer;

    /**
     * A <code>0</code> as value, means: "Basic Library". A <code>1</code> means "Dialog Library".
     */
    private HashMap<Object, Integer> mLibs = new HashMap<Object, Integer>();

    /**
     * Constructor.
     *
     * @param pPage
     *            the form page where to create the section
     */
    public LibsSection(PackageFormPage pPage) {
        super(pPage.getManagedForm().getForm().getBody(), pPage.getManagedForm().getToolkit(),
            ExpandableComposite.TITLE_BAR);

        mPage = pPage;

        Section section = getSection();

        section.setText(Messages.getString("LibsSection.Title")); //$NON-NLS-1$
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        Composite clientArea = mPage.getManagedForm().getToolkit().createComposite(section);
        clientArea.setLayout(new GridLayout());

        // Add the list here
        createTable(clientArea);

        // Add the buttons here
        createButtons(clientArea);

        mTableViewer.setInput(this);
        section.setClient(clientArea);
    }

    /**
     * @return all the selected basic libraries
     */
    public List<IFolder> getBasicLibraries() {

        ArrayList<IFolder> libs = new ArrayList<IFolder>();

        Iterator<Entry<Object, Integer>> iter = mLibs.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Object, Integer> entry = iter.next();
            IFolder res = (IFolder) entry.getKey();

            if (entry.getValue().equals(BASIC_LIB)) {
                libs.add(res);
            }
        }

        return libs;
    }

    /**
     * @return all the selected dialog libraries
     */
    public List<IFolder> getDialogLibraries() {

        ArrayList<IFolder> libs = new ArrayList<IFolder>();

        Iterator<Entry<Object, Integer>> iter = mLibs.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Object, Integer> entry = iter.next();
            IFolder res = (IFolder) entry.getKey();

            if (entry.getValue().equals(DIALOG_LIB)) {
                libs.add(res);
            }
        }

        return libs;
    }

    /**
     * Fill the libraries section from the properties file.
     *
     * @param pModel
     *            the properties file content.
     */
    public void setLibraries(PackagePropertiesModel pModel) {

        List<IFolder> basicLibs = pModel.getBasicLibraries();
        List<IFolder> dialogLibs = pModel.getDialogLibraries();

        // transform the value string into table elements
        mLibs.clear();

        if (mPage.getEditorInput() instanceof IFileEditorInput) {
            IFileEditorInput input = (IFileEditorInput) mPage.getEditorInput();
            IProject prj = input.getFile().getProject();

            for (IFolder lib : basicLibs) {
                if (lib.getProject().equals(prj) && lib.exists()) {
                    mLibs.put(lib, BASIC_LIB);
                }
            }

            for (IFolder lib : dialogLibs) {
                if (lib.getProject().equals(prj) && lib.exists()) {
                    mLibs.put(lib, DIALOG_LIB);
                }
            }

            if (mTableViewer != null) {
                mTableViewer.refresh();
            }
        }
    }

    /**
     * Creates the table GUI.
     *
     * @param pClientArea
     *            the composite where to create the table
     */
    private void createTable(Composite pClientArea) {
        Table table = new Table(pClientArea, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        mTableViewer = new TableViewer(table);
        mTableViewer.setContentProvider(new LibsContentProvider());
        mTableViewer.setLabelProvider(new LibsLabelProvider());
        mTableViewer.setCellEditors(
            new CellEditor[] { null,
                new ComboBoxCellEditor(mTableViewer.getTable(),
                    new String[] { Messages.getString(
                        "LibsSection.BasicLibrary"), //$NON-NLS-1$
                        Messages.getString("LibsSection.DialogLibrary") }) //$NON-NLS-1$
            });
        mTableViewer.setCellModifier(new LibsCellModifier());
        mTableViewer.setColumnProperties(new String[] { P_NAME, P_LIBTYPE });

        TableColumn folderColumn = new TableColumn(table, SWT.LEFT);
        folderColumn.setMoveable(false);
        folderColumn.setResizable(true);
        folderColumn.setWidth(FOLDER_COLUMN_WIDTH);

        TableColumn typeColumn = new TableColumn(table, SWT.LEFT);
        typeColumn.setMoveable(false);
        typeColumn.setResizable(true);
        typeColumn.setWidth(TYPE_COLUMN_WIDTH);
    }

    /**
     * Create the add / del buttons.
     *
     * @param pClientArea
     *            the composite where to create the buttons
     */
    private void createButtons(Composite pClientArea) {
        Composite buttons = mPage.getManagedForm().getToolkit().createComposite(pClientArea);
        buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        buttons.setLayout(new GridLayout(2, true));

        Button addBtn = mPage.getManagedForm().getToolkit().createButton(buttons,
            Messages.getString("LibsSection.AddButton"), SWT.PUSH); //$NON-NLS-1$
        addBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | GridData.GRAB_HORIZONTAL));
        addBtn.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                // Open the folder chooser dialog and refresh the table
                IProject prj = mPage.getProject();
                PackagePropertiesEditor editor = (PackagePropertiesEditor) mPage.getEditor();

                ProjectSelectionDialog dlg = new ProjectSelectionDialog(prj,
                    Messages.getString("LibsSection.AddDescription")); //$NON-NLS-1$
                dlg.setShowOnlyFolders(true);

                ArrayList<IResource> hiddenResources = new ArrayList<IResource>();
                hiddenResources.add(prj.getFolder("build")); //$NON-NLS-1$
                hiddenResources.add(prj.getFolder("bin")); //$NON-NLS-1$
                hiddenResources.addAll(editor.getModel().getBasicLibraries());
                hiddenResources.addAll(editor.getModel().getDialogLibraries());
                hiddenResources.addAll(editor.getModel().getContents());
                hiddenResources.addAll(UnoidlProjectHelper.getContainedFile(prj));
                dlg.setFilteredElements(hiddenResources);

                if (Window.OK == dlg.open()) {
                    IResource res = dlg.getSelected();
                    mLibs.put(res, BASIC_LIB);
                    mTableViewer.add(res);
                    mTableViewer.refresh();
                    fireSectionModified();
                }
            }
        });

        Button delBtn = mPage.getManagedForm().getToolkit().createButton(buttons,
            Messages.getString("LibsSection.DelButton"), SWT.PUSH); //$NON-NLS-1$
        delBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.GRAB_HORIZONTAL));
        delBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent pEvent) {
                // Delete the selected line
                ISelection sel = mTableViewer.getSelection();
                if (sel instanceof IStructuredSelection) {
                    IStructuredSelection structuredSel = (IStructuredSelection) sel;
                    Iterator<?> iter = structuredSel.iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();
                        mLibs.remove(o);
                        mTableViewer.remove(o);
                    }
                    fireSectionModified();
                    mTableViewer.refresh();
                }
            }
        });
    }

    /**
     * Tells to all the listeners that the section has been changed.
     */
    private void fireSectionModified() {
        PackagePropertiesEditor editor = (PackagePropertiesEditor) mPage.getEditor();

        List<IFolder> dialogLibs = getDialogLibraries();
        editor.getModel().clearDialogLibraries();
        for (IFolder lib : dialogLibs) {
            editor.getModel().addDialogLibrary(lib);
        }

        List<IFolder> basicLibs = getBasicLibraries();
        editor.getModel().clearBasicLibraries();
        for (IFolder lib : basicLibs) {
            editor.getModel().addBasicLibrary(lib);
        }
    }

    /**
     * Content provider for the libraries.
     *
     */
    private class LibsContentProvider implements IStructuredContentProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object pInputElement) {
            return mLibs.keySet().toArray();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void dispose() {
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void inputChanged(Viewer pViewer, Object pOldInput, Object pNewInput) {
        }
    }

    /**
     * Cell modifier for the types in the table.
     *
     */
    private class LibsCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canModify(Object pElement, String pProperty) {
            return pProperty.equals(P_LIBTYPE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(Object pElement, String pProperty) {
            Object value = null;
            if (pProperty.equals(P_LIBTYPE)) {
                value = mLibs.get(pElement);
            }
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void modify(Object pElement, String pProperty, Object pValue) {
            if (pProperty.equals(P_LIBTYPE) && pValue instanceof Integer) {
                if (pElement instanceof TableItem) {
                    Object o = ((TableItem) pElement).getData();
                    mLibs.put(o, (Integer) pValue);
                    mTableViewer.refresh(o);
                    fireSectionModified();
                }
            }
        }
    }

    /**
     * Label provider for the table content.
     *
     */
    private class LibsLabelProvider extends LabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Image getColumnImage(Object pElement, int pColumnIndex) {
            Image image = null;
            if (pColumnIndex == 0 && pElement instanceof IFolder) {
                IFolder folder = (IFolder) pElement;
                IWorkbenchAdapter adapter = folder.getAdapter(IWorkbenchAdapter.class);
                image = adapter.getImageDescriptor(pElement).createImage();
            }
            return image;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnText(Object pElement, int pColumnIndex) {
            String label = null;
            if (pColumnIndex == 0 && pElement instanceof IFolder) {
                label = ((IFolder) pElement).getProjectRelativePath().toOSString();
            } else if (pColumnIndex == 1) {
                if (mLibs.get(pElement).equals(BASIC_LIB)) {
                    label = Messages.getString("LibsSection.BasicLibrary"); //$NON-NLS-1$
                } else {
                    label = Messages.getString("LibsSection.DialogLibrary"); //$NON-NLS-1$
                }
            }
            return label;
        }
    }
}
