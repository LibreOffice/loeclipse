/*************************************************************************
 *
 * $RCSfile: ContentsSection.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/02/03 21:42:11 $
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
package org.openoffice.ide.eclipse.core.editors;

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
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.openoffice.ide.eclipse.core.model.IPackageChangeListener;
import org.openoffice.ide.eclipse.core.model.PackagePropertiesModel;
import org.openoffice.ide.eclipse.core.model.UnoPackage;

/**
 * @author cedricbosdo
 *
 */
public class ContentsSection extends SectionPart {

	private PackagePropertiesFormPage mPage;
	private ContainerCheckedTreeViewer mTreeViewer;
	
	public ContentsSection(PackagePropertiesFormPage page) {
		super(page.getManagedForm().getForm().getBody(), 
				page.getManagedForm().getToolkit(), Section.TITLE_BAR);
		
		mPage = page;
		PackagePropertiesModel model = ((PackagePropertiesEditor)mPage.getEditor()).getModel();
		model.addChangeListener(new IPackageChangeListener() {

			public void packagePropertiesChanged() {
				if (mTreeViewer != null) mTreeViewer.refresh();
			}

			public void packagePropertiesSaved() {
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

			public void checkStateChanged(CheckStateChangedEvent event) {
				
				PackagePropertiesEditor editor = (PackagePropertiesEditor)mPage.getEditor();
				
				List<IResource> contents = getContents();
				editor.getModel().clearContents();
				for (IResource resource : contents) {
					editor.getModel().addContent(resource);
				}
			}
		});
		mTreeViewer.addFilter(new ViewerFilter() {

			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				/*
				 * Files to exclude: .*
				 * Folders to exclude: build, bin
				 */
				boolean select = true;
				if (element instanceof IAdaptable) {
					IResource resource = (IResource)((IAdaptable)element).getAdapter(IResource.class);
					if (resource != null) {
						if (resource.getName().startsWith(".") ||  //$NON-NLS-1$
								resource.getName().equals("build") ||  //$NON-NLS-1$
								resource.getName().equals("bin") || //$NON-NLS-1$
								UnoPackage.isContainedInPackage(resource)) {
							select = false;
						}
						
						// Check if the resource is already selected somewhere
						PackagePropertiesEditor editor = (PackagePropertiesEditor)mPage.getEditor();
						PackagePropertiesModel model = editor.getModel();
						
						if (model.getBasicLibraries().contains(resource) ||
								model.getDialogLibraries().contains(resource) ||
								model.getDescriptionFiles().containsValue(resource)) {
							select = false;
						}
					}
				}
				
				return select;
			}
		});
		
		IEditorInput input = mPage.getEditorInput();
		if (input instanceof IFileEditorInput) {
			IFileEditorInput fileInput = (IFileEditorInput)input;
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
		ArrayList<String> checkedFolderPaths = new ArrayList<String>();

		for (Object o : checked) {
			if (o instanceof IAdaptable) {
				IResource res = (IResource)((IAdaptable)o).getAdapter(IResource.class);
				if (res != null) {

					int i = 0;
					boolean isSubResource = false;
					while (i<checkedFolderPaths.size() && !isSubResource) {
						String path = res.getProjectRelativePath().toString();
						if (path.startsWith(checkedFolderPaths.get(i))) {
							isSubResource = true;
						}
						i++;
					}

					if (!isSubResource && !mTreeViewer.getGrayed(res)) {
						if (res.getType() == IResource.FOLDER) {
							String path = res.getProjectRelativePath().toString();
							checkedFolderPaths.add(path);
							contents.add(res);
						} else {
							contents.add(res);
						}
					}
				}
			}
		}
		
		return contents;
	}
	

	/**
	 * Updates the section using the new contents
	 * @param contents
	 */
	public void setContents(List<IResource> contents) {
		// Split the string into several parts and find the files
		if (mPage.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput)mPage.getEditorInput();
			IProject prj = input.getFile().getProject();
			
			mTreeViewer.setCheckedElements(new Object[]{});
			
			for (IResource res : contents) {
				if (res.getProject().equals(prj) && res.exists()) {
					mTreeViewer.setChecked(res, true);
				}
			}
		}
	}
}
