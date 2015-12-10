/*************************************************************************
 *
 * $RCSfile: ProjectSelectionDialog.java,v $
 *
 * $Revision: 1.3 $
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Dialog used to select a file or folder in a UNO project.
 *
 * @author cedricbosdo
 *
 */
public class ProjectSelectionDialog extends Dialog {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 350;
    private TreeViewer mTreeViewer;
    private IProject mProject;
    private String mDescription;
    private List<IResource> mNotShownResources = new ArrayList<IResource>();

    private boolean mFoldersOnly = false;
    private IResource mSelected;

    /**
     * Constructor.
     *
     * @param pPrj
     *            the UNO project where to select the resource
     * @param pDescription
     *            a message explaining the selection to the user
     */
    public ProjectSelectionDialog(IProject pPrj, String pDescription) {
        super(Display.getDefault().getActiveShell());
        mProject = pPrj;
        mDescription = pDescription;

        setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void configureShell(Shell pNewShell) {

        int width = DEFAULT_WIDTH;
        int height = DEFAULT_HEIGHT;

        Rectangle screenBounds = Display.getDefault().getClientArea();
        int x = (screenBounds.width - width) / 2;
        int y = (screenBounds.height - height) / 2;

        pNewShell.setBounds(x, y, width, height);
        super.configureShell(pNewShell);
        pNewShell.setText(Messages.getString("ProjectSelectionDialog.Title")); //$NON-NLS-1$
    }

    /**
     * Set whether to show or hide the files.
     *
     * @param pOnlyFolders
     *            <code>true</code> to show only the folder, <code>false</code> to see everything.
     */
    public void setShowOnlyFolders(boolean pOnlyFolders) {
        mFoldersOnly = pOnlyFolders;
    }

    /**
     * @return the selected resource.
     */
    public IResource getSelected() {
        return mSelected;
    }

    /**
     * Set the list of elements which should be shown in the dialog.
     *
     * @param pNotToShow
     *            the list of resources to hide.
     */
    public void setFilteredElements(List<IResource> pNotToShow) {
        if (mNotShownResources != null) {
            mNotShownResources.clear();
        }
        mNotShownResources = pNotToShow;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Control createDialogArea(Composite pParent) {
        Composite body = (Composite) super.createDialogArea(pParent);
        body.setLayout(new GridLayout());

        Label label = new Label(body, SWT.WRAP);
        label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        label.setText(mDescription);

        mTreeViewer = new TreeViewer(body);
        mTreeViewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
        mTreeViewer.setContentProvider(new WorkbenchContentProvider());
        mTreeViewer.setLabelProvider(new WorkbenchLabelProvider());
        mTreeViewer.addFilter(new ViewerFilter() {

            @Override
            public boolean select(Viewer pViewer, Object pParentElement, Object pElement) {
                boolean select = true;
                if (pElement instanceof IAdaptable) {
                    IAdaptable adaptable = (IAdaptable) pElement;
                    select = adaptable.getAdapter(IFolder.class) != null;

                    if (!mFoldersOnly) {
                        IFile file = adaptable.getAdapter(IFile.class);
                        if (file != null) {
                            select = !file.getName().startsWith("."); //$NON-NLS-1$
                        }
                    }

                    // Test if the resource has to be hidden
                    if (select && mNotShownResources.contains(pElement)) {
                        select = false;
                    }
                }
                return select;
            }

        });
        mTreeViewer.setInput(mProject);
        mTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            @Override
            public void selectionChanged(SelectionChangedEvent pEvent) {
                if (pEvent.getSelection() instanceof IStructuredSelection) {
                    IStructuredSelection sel = (IStructuredSelection) pEvent.getSelection();
                    Object o = sel.getFirstElement();
                    if (o instanceof IResource) {
                        mSelected = (IResource) o;
                    } else {
                        mSelected = null;
                    }
                }
            }

        });

        return body;
    }
}
