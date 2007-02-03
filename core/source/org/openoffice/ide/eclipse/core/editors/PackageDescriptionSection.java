/*************************************************************************
 *
 * $RCSfile: PackageDescriptionSection.java,v $
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
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.openoffice.ide.eclipse.core.gui.LocaleCellProvider;
import org.openoffice.ide.eclipse.core.gui.ProjectSelectionDialog;
import org.openoffice.ide.eclipse.core.model.UnoPackage;

/**
 * @author cedricbosdo
 *
 */
public class PackageDescriptionSection extends SectionPart {
	
	private static final String P_NAME = "__p_name"; //$NON-NLS-1$
	private static final String P_LOCALE = "__p_locale"; //$NON-NLS-1$
	
	private PackagePropertiesFormPage mPage;
	private TableViewer mTableViewer;
	
	private HashMap<IFile, Locale> mDescriptions = new HashMap<IFile, Locale>();
	
	public PackageDescriptionSection(PackagePropertiesFormPage page) {
		super(page.getManagedForm().getForm().getBody(), 
				page.getManagedForm().getToolkit(), Section.TITLE_BAR);
		
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
		nameCol.setWidth(200);
		
		TableColumn localeCol = new TableColumn(table, SWT.LEFT);
		localeCol.setMoveable(false);
		localeCol.setResizable(false);
		localeCol.setWidth(200);
		
		mTableViewer = new TableViewer(table);
		mTableViewer.setColumnProperties(new String[]{P_NAME, P_LOCALE});
		mTableViewer.setCellEditors(new CellEditor[]{
				null,
				new LocaleCellProvider(table)
		});
		mTableViewer.setContentProvider(new DescrContentProvider());
		mTableViewer.setLabelProvider(new DescrLabelProvider());
		mTableViewer.setCellModifier(new DescrCellModifier());
		
		
		// Add the buttons here
		
		Composite buttons = mPage.getManagedForm().getToolkit().createComposite(clientArea);
		buttons.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
		buttons.setLayout(new GridLayout());
		
		Button add = mPage.getManagedForm().getToolkit().createButton(buttons, Messages.getString("PackageDescriptionSection.AddButton"), SWT.PUSH); //$NON-NLS-1$
		add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		add.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Open the folder chooser dialog and refresh the table
				IProject prj = mPage.getProject();
				PackagePropertiesEditor editor = (PackagePropertiesEditor)mPage.getEditor();
				
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
				hiddenResources.addAll(UnoPackage.getContainedFile(prj));
				dlg.setFilteredElements(hiddenResources);
				
				if (ProjectSelectionDialog.OK == dlg.open()) {
					IResource res = dlg.getSelected();
					if (res instanceof IFile) {
						mDescriptions.put((IFile)res, Locale.getDefault());
						mTableViewer.add(res);
						mTableViewer.refresh();
						fireSectionModified();
					}
				}
			}
		});
		
		Button del = mPage.getManagedForm().getToolkit().createButton(buttons, Messages.getString("PackageDescriptionSection.DelButton"), SWT.PUSH); //$NON-NLS-1$
		del.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		del.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Delete the selected line
				ISelection sel = mTableViewer.getSelection();
				if (sel instanceof IStructuredSelection) {
					IStructuredSelection structuredSel = (IStructuredSelection)sel;
					Iterator iter = structuredSel.iterator();
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
		
		mTableViewer.setInput(this);
		section.setClient(clientArea);
	}
	
	public Map<Locale, IFile> getDescriptions() {
		HashMap<Locale, IFile> descriptions = new HashMap<Locale, IFile>();
		
		Iterator<Entry<IFile, Locale>> iter = mDescriptions.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<IFile, Locale> entry = iter.next();
			descriptions.put(entry.getValue(), entry.getKey());
		}
		
		return descriptions;
	}
	
	public void setDescriptions(Map<Locale, IFile> descriptions) {
		mDescriptions.clear();
		Iterator<Entry<Locale, IFile>> iter = descriptions.entrySet().iterator();
		
		while (iter.hasNext()) {
			Entry<Locale, IFile> entry = iter.next();
			mDescriptions.put(entry.getValue(), entry.getKey());
		}
		mTableViewer.refresh();
	}
	
	private void fireSectionModified() {
		PackagePropertiesEditor editor = (PackagePropertiesEditor)mPage.getEditor();
		
		Map<Locale, IFile> descriptions = getDescriptions();
		editor.getModel().clearDescriptions();
		Iterator<Entry<Locale, IFile>> iter = descriptions.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Locale, IFile> entry = iter.next();
			editor.getModel().addDescriptionFile(entry.getValue(), entry.getKey());
		}
	}
	
	private class DescrContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object inputElement) {
			return mDescriptions.keySet().toArray();
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
	
	private class DescrCellModifier implements ICellModifier {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			return property.equals(P_LOCALE);
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			Object value = null;
			if (property.equals(P_LOCALE)) {
				value = mDescriptions.get(element);
			}
			return value;
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			if (property.equals(P_LOCALE) && value instanceof Locale) {
				if (element instanceof TableItem) {
					Object o = ((TableItem)element).getData();
					if (o instanceof IFile) {
						mDescriptions.put((IFile)o, (Locale)value);
						mTableViewer.refresh(o);
						fireSectionModified();
					}
				}
			}
		}
	}
	
	private class DescrLabelProvider extends LabelProvider implements ITableLabelProvider {

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			Image image = null;
			if (columnIndex == 0 && element instanceof IAdaptable) {
				IAdaptable adaptable = ((IAdaptable)element);
				IWorkbenchAdapter adapter = (IWorkbenchAdapter)adaptable.getAdapter(IWorkbenchAdapter.class);
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
			if (columnIndex == 0 && element instanceof IFile) {
				label = ((IResource)element).getProjectRelativePath().toOSString();
			} else if (columnIndex == 1){
				label = mDescriptions.get(element).getDisplayName();
			}
			return label;
		}
	}
}
