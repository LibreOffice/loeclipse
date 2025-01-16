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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.model.pack.PackagePropertiesModel;
import org.libreoffice.ide.eclipse.core.model.utils.IModelTreeListener;

/**
 * Content section of the Package Contents editor page.
 */
public class ContentsSection extends SectionPart {

    private PackageFormPage mPage;
    private ContainerCheckedTreeViewer mTreeViewer;
    
    private ViewerFilter mTreeFilter;
    private ICheckStateListener mCheckListener;
    private IModelTreeListener mTreeListener;

    /**
     * Constructor.
     *
     * @param page
     *            the form page containing the section
     */
    public ContentsSection(PackageFormPage page) {
        super(page.getManagedForm().getForm().getBody(), page.getManagedForm().getToolkit(),
            ExpandableComposite.TITLE_BAR);

        mPage = page;
        PackagePropertiesModel model = getModel();

        Section section = getSection();

        section.setText(Messages.getString("ContentsSection.Title")); //$NON-NLS-1$
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        mTreeViewer = new ContainerCheckedTreeViewer(section);
        // Configure the tree viewer
        mTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
        WorkbenchContentProvider provider = new WorkbenchContentProvider();
        mTreeViewer.setContentProvider(provider);
        mTreeViewer.setComparator(new ViewerComparator(String.CASE_INSENSITIVE_ORDER));

        addTreeListener(model);
        addCheckStateListener(model);
        addFilter(model);
        setCheckStateProvider(model);
        section.setClient(mTreeViewer.getControl());
    }

    public void setContents() {
        // Initialize TreeView
        if (mTreeViewer != null) {
            mTreeViewer.setInput(mPage.getProject());
        }
    }

    @Override
    public void dispose() {
        mTreeViewer.removeFilter(mTreeFilter);
        mTreeViewer.removeCheckStateListener(mCheckListener);
        getModel().removeTreeListener(mTreeListener);
        super.dispose();
    }

    private PackagePropertiesModel getModel() {
        return ((PackagePropertiesEditor) mPage.getEditor()).getModel();
    }

    private void addTreeListener(PackagePropertiesModel model) {
        IModelTreeListener listener = new IModelTreeListener() {

            @Override
            public void modelRefreshed() {
                if (mTreeViewer != null) {
                    mTreeViewer.refresh();
                }
            }

        };
        model.addTreeListener(listener);
        mTreeListener = listener;
    }

    private void addCheckStateListener(PackagePropertiesModel model) {
        ICheckStateListener listener = new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getElement() instanceof IAdaptable) {
                    IResource res = ((IAdaptable) event.getElement()).getAdapter(IResource.class);
                    if (event.getChecked()) {
                        model.addResource(res);
                    } else {
                        model.removeResource(res);
                    }
                }
            }
        };
        mTreeViewer.addCheckStateListener(listener);
        mCheckListener = listener;
    }

    private void addFilter(PackagePropertiesModel model) {
        ViewerFilter filter = new ViewerFilter() {

            @Override
            public boolean select(Viewer viewer, Object parentElement, Object element) {
                /*
                 * Files to exclude: .* Folders to exclude: build, bin
                 */
                boolean selected = true;
                if (element instanceof IAdaptable) {
                    IResource res = ((IAdaptable) element).getAdapter(IResource.class);
                    if (res != null) {
                        // FIXME: If we want to be able to see soft link pointing outside
                        // FIXME: the Package we need to accept resource not contained in package
                        if (model.isHidden(res) ||
                            res.getName().equals("build") || //$NON-NLS-1$
                            res.getName().equals("bin")) { //$NON-NLS-1$
                            selected = false;
                        } else if (model.getBasicLibraries().contains(res) ||
                                   model.getDialogLibraries().contains(res) ||
                                   model.getDescriptionFiles().containsValue(res)) {
                            selected = false;
                        }
                    }
                }
                return selected;
            }
        };
        mTreeViewer.addFilter(filter);
        mTreeFilter = filter;
    }

    private void setCheckStateProvider(PackagePropertiesModel model) {
        mTreeViewer.setCheckStateProvider(new ICheckStateProvider() {

            @Override
            public boolean isChecked(Object element) {
                boolean checked = false;
                if (element instanceof IAdaptable) {
                    IResource res = ((IAdaptable) element).getAdapter(IResource.class);
                    checked = model.isChecked(res);
                }
                return checked;
            }

            @Override
            public boolean isGrayed(Object element) {
                boolean grayed = false;
                if (element instanceof IAdaptable) {
                    IResource res = ((IAdaptable) element).getAdapter(IResource.class);
                    grayed = model.isGrayed(res);
                }
                return grayed;
            }
        });
    }

}
