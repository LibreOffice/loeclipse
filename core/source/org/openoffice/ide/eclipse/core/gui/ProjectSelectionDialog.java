/*************************************************************************
 *
 * $RCSfile: ProjectSelectionDialog.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/03 21:29:52 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the GNU Lesser General Public License Version 2.1
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
 * @author cedricbosdo
 *
 */
public class ProjectSelectionDialog extends Dialog {

	private TreeViewer mTreeViewer;
	private IProject mProject;
	private String mDescription;
	private List<IResource> mNotShownResources = new ArrayList<IResource>();
	
	private boolean mFoldersOnly = false;
	private IResource mSelected;
	
	public ProjectSelectionDialog(IProject prj, String description) {
		super(Display.getDefault().getActiveShell());
		mProject = prj;
		mDescription = description;
		
		setShellStyle(SWT.RESIZE | SWT.APPLICATION_MODAL);
	}
	
	@Override
	protected void configureShell(Shell newShell) {
		
		int width = 300;
		int height = 350;
		
		Rectangle screenBounds  = Display.getDefault().getClientArea();
		int x = (screenBounds.width - width) / 2;
		int y = (screenBounds.height - height) / 2;
		
		newShell.setBounds(x, y, width, height);
		super.configureShell(newShell);
		newShell.setText("Project content chooser");
	}
	
	public void setShowOnlyFolders(boolean onlyFolders) {
		mFoldersOnly = onlyFolders;
	}
	
	public IResource getSelected() {
		return mSelected;
	}
	
	public void setFilteredElements(List<IResource> notToShow) {
		if (mNotShownResources != null) mNotShownResources.clear();
		mNotShownResources = notToShow;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite body = (Composite)super.createDialogArea(parent);
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
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				boolean select = true;
				if (element instanceof IAdaptable) {
					IAdaptable adaptable = (IAdaptable)element;	
					select = adaptable.getAdapter(IFolder.class) != null;
					
					if (!mFoldersOnly) {
						IFile file = (IFile)adaptable.getAdapter(IFile.class);
						if (file != null) {
							select = !file.getName().startsWith(".");
						}
					}
					
					// Test if the resource has to be hidden
					if (select && mNotShownResources.contains(element)) {
						select = false;
					}
				}
				return select;
			}
			
		});
		mTreeViewer.setInput(mProject);
		mTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(SelectionChangedEvent event) {
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)event.getSelection();
					Object o = sel.getFirstElement();
					if (o instanceof IResource) {
						mSelected = (IResource)o;
					} else {
						mSelected = null;
					}
				}
			}
			
		});
		
		return body;
	}
}
