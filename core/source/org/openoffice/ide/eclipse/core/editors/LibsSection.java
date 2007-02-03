/*************************************************************************
 *
 * $RCSfile: LibsSection.java,v $
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
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.openoffice.ide.eclipse.core.gui.ProjectSelectionDialog;
import org.openoffice.ide.eclipse.core.model.PackagePropertiesModel;
import org.openoffice.ide.eclipse.core.model.UnoPackage;

/**
 * @author cedricbosdo
 *
 */
public class LibsSection extends SectionPart {

	private static final String P_LIBTYPE = "__p_libtype"; //$NON-NLS-1$
	private static final String P_NAME = "__p_name"; //$NON-NLS-1$
	private static final Integer BASIC_LIB = new Integer(0);
	private static final Integer DIALOG_LIB = new Integer(1);
	
	private PackagePropertiesFormPage mPage;
	private TableViewer mTableViewer;
	
	/**
	 * A <code>0</code> as value, means: "Basic Library". A <code>1</code>
	 * means "Dialog Library".
	 */
	private HashMap<Object, Integer> mLibs = new HashMap<Object, Integer>();
	
	public List<IFolder> getBasicLibraries() {
		
		ArrayList<IFolder> libs = new ArrayList<IFolder>();
		
		Iterator<Entry<Object, Integer>> iter = mLibs.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Object, Integer> entry = iter.next();
			IFolder res = (IFolder)entry.getKey();
			
			if (entry.getValue().equals(BASIC_LIB)) {
				libs.add(res);
			}
		}
		
		return libs;
	}
	
	public List<IFolder> getDialogLibraries() {
		
		ArrayList<IFolder> libs = new ArrayList<IFolder>();
		
		Iterator<Entry<Object, Integer>> iter = mLibs.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Object, Integer> entry = iter.next();
			IFolder res = (IFolder)entry.getKey();
			
			if (entry.getValue().equals(DIALOG_LIB)) {
				libs.add(res);
			}
		}
		
		return libs;
	}
	
	public void setLibraries(PackagePropertiesModel model) {
		
		List<IFolder> basicLibs = model.getBasicLibraries();
		List<IFolder> dialogLibs = model.getDialogLibraries();
		
		// transform the value string into table elements
		mLibs.clear();
		
		if (mPage.getEditorInput() instanceof IFileEditorInput) {
			IFileEditorInput input = (IFileEditorInput)mPage.getEditorInput();
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
	
	public LibsSection(PackagePropertiesFormPage page) {
		super(page.getManagedForm().getForm().getBody(), 
				page.getManagedForm().getToolkit(), Section.TITLE_BAR);
		
		mPage = page;
		
		Section section = getSection();
		
		section.setText(Messages.getString("LibsSection.Title")); //$NON-NLS-1$
		section.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		
		Composite clientArea = mPage.getManagedForm().getToolkit().createComposite(section);
		clientArea.setLayout(new GridLayout());
		
		// Add the list here
		Table table = new Table(clientArea, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		mTableViewer = new TableViewer(table);
		mTableViewer.setContentProvider(new LibsContentProvider());
		mTableViewer.setLabelProvider(new LibsLabelProvider());
		mTableViewer.setCellEditors(new CellEditor[]{
				null,
				new ComboBoxCellEditor(mTableViewer.getTable(),
						new String[]{Messages.getString("LibsSection.BasicLibrary"), Messages.getString("LibsSection.DialogLibrary")}) //$NON-NLS-1$ //$NON-NLS-2$
		});
		mTableViewer.setCellModifier(new LibsCellModifier());
		mTableViewer.setColumnProperties(new String[]{P_NAME, P_LIBTYPE});
		
		TableColumn folderColumn = new TableColumn(table, SWT.LEFT);
		folderColumn.setMoveable(false);
		folderColumn.setResizable(true);
		folderColumn.setWidth(200);
		
		TableColumn typeColumn = new TableColumn(table, SWT.LEFT);
		typeColumn.setMoveable(false);
		typeColumn.setResizable(true);
		typeColumn.setWidth(100);
		
		// Add the buttons here
		Composite buttons = mPage.getManagedForm().getToolkit().createComposite(clientArea);
		buttons.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buttons.setLayout(new GridLayout(2, true));
		
		Button addBtn = mPage.getManagedForm().getToolkit().createButton(
				buttons, Messages.getString("LibsSection.AddButton"), SWT.PUSH); //$NON-NLS-1$
		addBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END | 
				GridData.GRAB_HORIZONTAL));
		addBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Open the folder chooser dialog and refresh the table
				IProject prj = mPage.getProject();
				PackagePropertiesEditor editor = (PackagePropertiesEditor)mPage.getEditor();
				
				ProjectSelectionDialog dlg = new ProjectSelectionDialog(prj, 
						Messages.getString("LibsSection.AddDescription")); //$NON-NLS-1$
				dlg.setShowOnlyFolders(true);
				
				ArrayList<IResource> hiddenResources = new ArrayList<IResource>();
				hiddenResources.add(prj.getFolder("build")); //$NON-NLS-1$
				hiddenResources.add(prj.getFolder("bin")); //$NON-NLS-1$
				hiddenResources.addAll(editor.getModel().getBasicLibraries());
				hiddenResources.addAll(editor.getModel().getDialogLibraries());
				hiddenResources.addAll(editor.getModel().getContents());
				hiddenResources.addAll(UnoPackage.getContainedFile(prj));
				dlg.setFilteredElements(hiddenResources);
				
				if (ProjectSelectionDialog.OK == dlg.open()) {
					IResource res = dlg.getSelected();
					mLibs.put(res, BASIC_LIB);
					mTableViewer.add(res);
					mTableViewer.refresh();
					fireSectionModified();
				}
			}
		});
		
		Button delBtn = mPage.getManagedForm().getToolkit().createButton(
				buttons, Messages.getString("LibsSection.DelButton"), SWT.PUSH); //$NON-NLS-1$
		delBtn.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING |
				GridData.GRAB_HORIZONTAL));
		delBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Delete the selected line
				ISelection sel = mTableViewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection structuredSel = (IStructuredSelection)sel;
					Iterator iter = structuredSel.iterator();
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
		
		mTableViewer.setInput(this);
		section.setClient(clientArea);
	}
	
	private void fireSectionModified() {
		PackagePropertiesEditor editor = (PackagePropertiesEditor)mPage.getEditor();
		
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
	
	private class LibsContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return mLibs.keySet().toArray();
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}
	
	private class LibsCellModifier implements ICellModifier {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			return property.equals(P_LIBTYPE);
		}

		public Object getValue(Object element, String property) {
			Object value = null;
			if (property.equals(P_LIBTYPE)) {
				value = mLibs.get(element);
			}
			return value;
		}

		public void modify(Object element, String property, Object value) {
			if (property.equals(P_LIBTYPE) && value instanceof Integer) {
				if (element instanceof TableItem) {
					Object o = ((TableItem)element).getData();
					mLibs.put(o, (Integer)value);
					mTableViewer.refresh(o);
					fireSectionModified();
				}
			}
		}
		
	}
	
	private class LibsLabelProvider extends LabelProvider implements ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null;
			if (columnIndex == 0 && element instanceof IFolder) {
				IFolder folder = ((IFolder)element);
				IWorkbenchAdapter adapter = (IWorkbenchAdapter)folder.getAdapter(IWorkbenchAdapter.class);
				image = adapter.getImageDescriptor(element).createImage();
			}
			return image;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			String label = null;
			if (columnIndex == 0 && element instanceof IFolder) {
				label = ((IFolder)element).getProjectRelativePath().toOSString();
			} else if (columnIndex == 1){
				if (mLibs.get(element).equals(BASIC_LIB)) {
					label = Messages.getString("LibsSection.BasicLibrary"); //$NON-NLS-1$
				} else {
					label = Messages.getString("LibsSection.DialogLibrary"); //$NON-NLS-1$
				}
			}
			return label;
		}
	}
}
