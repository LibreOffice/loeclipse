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
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.libreoffice.ide.eclipse.core.editors.Messages;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.model.pack.PackagePropertiesModel;
import org.libreoffice.ide.eclipse.core.model.utils.IModelChangedListener;

/**
 * Content section of the Package Contents editor page.
 */
public class ContentsSection extends SectionPart {

    private PackageFormPage mPage;
    private ContainerCheckedTreeViewer mTreeViewer;

    /**
     * Constructor.
     *
     * @param pPage
     *            the form page containing the section
     */
    public ContentsSection(PackageFormPage pPage) {
        super(pPage.getManagedForm().getForm().getBody(), pPage.getManagedForm().getToolkit(),
            ExpandableComposite.TITLE_BAR);

        mPage = pPage;
        PackagePropertiesModel model = ((PackagePropertiesEditor) mPage.getEditor()).getModel();
        model.addChangeListener(new IModelChangedListener() {

            @Override
            public void modelChanged() {
                if (mTreeViewer != null) {
                    mTreeViewer.refresh();
                }
            }

            @Override
            public void modelSaved() {
            }
        });

        Section section = getSection();

        section.setText(Messages.getString("ContentsSection.Title")); //$NON-NLS-1$
        section.setLayoutData(new GridData(GridData.FILL_BOTH));

        mTreeViewer = new ContainerCheckedTreeViewer(section);
        // Configure the tree viewer
        mTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
        WorkbenchContentProvider provider = new WorkbenchContentProvider();
        mTreeViewer.setContentProvider(provider);
        mTreeViewer.addCheckStateListener(new ICheckStateListener() {

            @Override
            public void checkStateChanged(CheckStateChangedEvent pEvent) {

                PackagePropertiesEditor editor = (PackagePropertiesEditor) mPage.getEditor();

                List<IResource> contents = getContents();
                editor.getModel().clearContents();
                for (IResource resource : contents) {
                    editor.getModel().addContent(resource);
                }
            }
        });
        mTreeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer pViewer, Object pParentElement, Object pElement) {
                /*
                 * Files to exclude: .* Folders to exclude: build, bin
                 */
                boolean select = true;
                if (pElement instanceof IAdaptable) {
                    IResource resource = ((IAdaptable) pElement).getAdapter(IResource.class);
                    if (resource != null) {
                        if (resource.getName().startsWith(".") || //$NON-NLS-1$
                            resource.getName().equals("build") || //$NON-NLS-1$
                            resource.getName().equals("bin") || //$NON-NLS-1$
                            UnoidlProjectHelper.isContainedInPackage(resource)) {
                            select = false;
                        }

                        // Check if the resource is already selected somewhere
                        PackagePropertiesEditor editor = (PackagePropertiesEditor) mPage.getEditor();
                        PackagePropertiesModel model = editor.getModel();

                        if (model.getBasicLibraries().contains(resource)
                            || model.getDialogLibraries().contains(resource)
                            || model.getDescriptionFiles().containsValue(resource)) {
                            select = false;
                        }
                    }
                }

                return select;
            }
        });

        IEditorInput input = mPage.getEditorInput();
        if (input instanceof IFileEditorInput) {
            IFileEditorInput fileInput = (IFileEditorInput) input;
            mTreeViewer.setInput(fileInput.getFile().getProject());
        }

        section.setClient(mTreeViewer.getControl());

    }

    /**
     * @return the list of files and folders to add to the package
     */
    public List<IResource> getContents() {
        ArrayList<IResource> contents = new ArrayList<IResource>();

        // Write the selections to the document
        Object[] checked = mTreeViewer.getCheckedElements();

        for (Object o : checked) {
            if (o instanceof IAdaptable) {
                IResource res = ((IAdaptable) o).getAdapter(IResource.class);
                if (res != null) {
                    addResourceToContent(contents, res);
                }
            }
        }

        return contents;
    }

    /**
     * Add a resource to the contents list, but removes all duplicates entries (all files of a selected directory).
     *
     * @param pContents
     *            the resource list to update
     * @param pResource
     *            the resource to add
     */
    private void addResourceToContent(ArrayList<IResource> pContents, IResource pResource) {
        int i = 0;
        boolean isSubResource = false;

        ArrayList<String> checkedFolderPaths = new ArrayList<String>();

        while (i < checkedFolderPaths.size() && !isSubResource) {
            String path = pResource.getProjectRelativePath().toString();
            if (path.startsWith(checkedFolderPaths.get(i))) {
                isSubResource = true;
            }
            i++;
        }

        if (!isSubResource && !mTreeViewer.getGrayed(pResource) && pResource.getType() == IResource.FOLDER) {
            String path = pResource.getProjectRelativePath().toString();
            checkedFolderPaths.add(path);
            pContents.add(pResource);
        } else if (!isSubResource && !mTreeViewer.getGrayed(pResource)) {
            pContents.add(pResource);
        }
    }

    /**
     * Updates the section using the new contents.
     *
     * @param pContents
     *            the package contents to put in the section
     */
    public void setContents(List<IResource> pContents) {
        // Split the string into several parts and find the files
        if (mPage.getEditorInput() instanceof IFileEditorInput) {
            IFileEditorInput input = (IFileEditorInput) mPage.getEditorInput();
            IProject prj = input.getFile().getProject();

            mTreeViewer.setCheckedElements(new Object[] {});

            for (IResource res : pContents) {
                if (res.getProject().equals(prj) && res.exists()) {
                    mTreeViewer.setChecked(res, true);
                }
            }
        }
    }
}
