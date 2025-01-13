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
package org.libreoffice.ide.eclipse.core.editors.pack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CellEditor;
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
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.gui.LocaleCellProvider;
import org.libreoffice.ide.eclipse.core.gui.ProjectSelectionDialog;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;

/**
 *
 */
public class PackageDescriptionSection extends SectionPart {

    private static final String P_NAME = "__p_name"; //$NON-NLS-1$
    private static final String P_LOCALE = "__p_locale"; //$NON-NLS-1$
    private static final int NAME_WIDTH = 250;
    private static final int LOCALE_WIDTH = 250;

    private PackageFormPage mPage;
    private TableViewer mTableViewer;

    private Map<IFile, Locale> mDescriptions = new HashMap<>();

    /**
     * Constructor.
     *
     * @param page
     *            the package page where to create the section
     */
    public PackageDescriptionSection(PackageFormPage page) {
        super(page.getManagedForm().getForm().getBody(), page.getManagedForm().getToolkit(),
            ExpandableComposite.TITLE_BAR);

        mPage = page;

        Section section = getSection();

        section.setText(Messages.getString("PackageDescriptionSection.Title")); //$NON-NLS-1$
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        section.setLayoutData(gd);

        Composite clientArea = mPage.getManagedForm().getToolkit().createComposite(section);
        clientArea.setLayout(new GridLayout(2, false));

        // Add the list here
        Table table = new Table(clientArea, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(GridData.FILL_BOTH));

        TableColumn nameCol = new TableColumn(table, SWT.LEFT);
        nameCol.setMoveable(false);
        nameCol.setResizable(false);
        nameCol.setWidth(NAME_WIDTH);

        TableColumn localeCol = new TableColumn(table, SWT.LEFT);
        localeCol.setMoveable(false);
        localeCol.setResizable(false);
        localeCol.setWidth(LOCALE_WIDTH);

        mTableViewer = new TableViewer(table);
        mTableViewer.setColumnProperties(new String[] { P_NAME, P_LOCALE });
        mTableViewer.setCellEditors(new CellEditor[] { null, new LocaleCellProvider(table) });
        mTableViewer.setContentProvider(new DescrContentProvider());
        mTableViewer.setLabelProvider(new DescrLabelProvider());
        mTableViewer.setCellModifier(new DescrCellModifier());

        // Add the buttons here
        createButtons(clientArea);

        mTableViewer.setInput(this);
        section.setClient(clientArea);
    }

    /**
     * @return the package descriptions shown in the section
     */
    public Map<Locale, IFile> getDescriptions() {
        Map<Locale, IFile> descriptions = new HashMap<>();

        Iterator<Entry<IFile, Locale>> iter = mDescriptions.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<IFile, Locale> entry = iter.next();
            descriptions.put(entry.getValue(), entry.getKey());
        }

        return descriptions;
    }

    /**
     * Set the package descriptions to show in the section.
     *
     * @param descriptions
     *            the descriptions to show.
     */
    public void setDescriptions(Map<Locale, IFile> descriptions) {
        mDescriptions.clear();
        Iterator<Entry<Locale, IFile>> iter = descriptions.entrySet().iterator();

        while (iter.hasNext()) {
            Entry<Locale, IFile> entry = iter.next();
            mDescriptions.put(entry.getValue(), entry.getKey());
        }
        mTableViewer.refresh();
    }

    /**
     * Tells the listeners when the descriptions have changed.
     */
    private void fireSectionModified() {
        PackagePropertiesEditor editor = (PackagePropertiesEditor) mPage.getEditor();

        Map<Locale, IFile> descriptions = getDescriptions();
        editor.getModel().clearDescriptions();
        Iterator<Entry<Locale, IFile>> iter = descriptions.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<Locale, IFile> entry = iter.next();
            editor.getModel().addDescriptionFile(entry.getValue(), entry.getKey());
        }
    }

    /**
     * Create the buttons of the section.
     *
     * @param pParent
     *            the composite where to create the buttons
     */
    private void createButtons(Composite pParent) {
        Composite buttons = mPage.getManagedForm().getToolkit().createComposite(pParent);
        buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
        buttons.setLayout(new GridLayout());

        Button add = mPage.getManagedForm().getToolkit().createButton(buttons,
            Messages.getString("PackageDescriptionSection.AddButton"), SWT.PUSH); //$NON-NLS-1$
        add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        add.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Open the folder chooser dialog and refresh the table
                IProject prj = mPage.getProject();
                PackagePropertiesEditor editor = (PackagePropertiesEditor) mPage.getEditor();

                ProjectSelectionDialog dlg = new ProjectSelectionDialog(prj,
                    Messages.getString("PackageDescriptionSection.AddDescription")); //$NON-NLS-1$

                ArrayList<IResource> hiddenResources = new ArrayList<IResource>();
                hiddenResources.add(prj.getFolder("build")); //$NON-NLS-1$
                hiddenResources.add(prj.getFolder("bin")); //$NON-NLS-1$
                hiddenResources.add(prj.getFile("package.properties")); //$NON-NLS-1$
                hiddenResources.addAll(editor.getModel().getBasicLibraries());
                hiddenResources.addAll(editor.getModel().getDialogLibraries());
                hiddenResources.addAll(editor.getModel().getContents());
                hiddenResources.addAll(editor.getModel().getDescriptionFiles().values());
                hiddenResources.addAll(UnoidlProjectHelper.getContainedFile(prj));
                dlg.setFilteredElements(hiddenResources);

                if (Window.OK == dlg.open()) {
                    IResource res = dlg.getSelected();
                    if (res instanceof IFile) {
                        mDescriptions.put((IFile) res, Locale.getDefault());
                        mTableViewer.add(res);
                        mTableViewer.refresh();
                        fireSectionModified();
                    }
                }
            }
        });

        Button del = mPage.getManagedForm().getToolkit().createButton(buttons,
            Messages.getString("PackageDescriptionSection.DelButton"), SWT.PUSH); //$NON-NLS-1$
        del.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        del.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                // Delete the selected line
                ISelection sel = mTableViewer.getSelection();
                if (sel instanceof IStructuredSelection) {
                    IStructuredSelection structuredSel = (IStructuredSelection) sel;
                    Iterator<?> iter = structuredSel.iterator();
                    while (iter.hasNext()) {
                        Object o = iter.next();
                        mDescriptions.remove(o);
                        mTableViewer.remove(o);
                    }
                    fireSectionModified();
                    mTableViewer.refresh();
                }
            }
        });
    }

    /**
     * Provides the data for the descriptions table.
     */
    private class DescrContentProvider implements IStructuredContentProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Object[] getElements(Object inputElement) {
            return mDescriptions.keySet().toArray();
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
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    /**
     * Modification handler of the description table.
     */
    private class DescrCellModifier implements ICellModifier {

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean canModify(Object element, String property) {
            return property.equals(P_LOCALE);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Object getValue(Object element, String property) {
            Object value = null;
            if (property.equals(P_LOCALE)) {
                value = mDescriptions.get(element);
            }
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void modify(Object element, String property, Object value) {
            if (property.equals(P_LOCALE) && value instanceof Locale) {
                if (element instanceof TableItem) {
                    Object o = ((TableItem) element).getData();
                    if (o instanceof IFile) {
                        mDescriptions.put((IFile) o, (Locale) value);
                        mTableViewer.refresh(o);
                        fireSectionModified();
                    }
                }
            }
        }
    }

    /**
     * Provides the labels and images to show in the descriptions table.
     */
    private class DescrLabelProvider extends LabelProvider implements ITableLabelProvider {

        /**
         * {@inheritDoc}
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            Image image = null;
            if (columnIndex == 0 && element instanceof IAdaptable) {
                IAdaptable adaptable = (IAdaptable) element;
                IWorkbenchAdapter adapter = adaptable.getAdapter(IWorkbenchAdapter.class);
                image = adapter.getImageDescriptor(element).createImage();
            }
            return image;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getColumnText(Object element, int columnIndex) {
            String label = null;
            if (columnIndex == 0 && element instanceof IFile) {
                label = ((IResource) element).getProjectRelativePath().toOSString();
            } else if (columnIndex == 1) {
                label = mDescriptions.get(element).getDisplayName();
            }
            return label;
        }
    }
}
