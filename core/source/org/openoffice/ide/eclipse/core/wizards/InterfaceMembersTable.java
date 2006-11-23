/*************************************************************************
 *
 * $RCSfile: InterfaceMembersTable.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2006/11/23 18:27:16 $
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
package org.openoffice.ide.eclipse.core.wizards;

import java.util.Vector;

import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.gui.AbstractTable;
import org.openoffice.ide.eclipse.core.gui.ITableElement;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.IUnoFactoryConstants;
import org.openoffice.ide.eclipse.core.model.UnoFactoryData;

/**
 * @author cbosdonnat
 *
 */
public class InterfaceMembersTable extends AbstractTable {
	
	private static final String TYPE = "__type"; //$NON-NLS-1$
	private static final String NAME = "__name"; //$NON-NLS-1$
	private static final String OPTIONS = "__options"; //$NON-NLS-1$
	
	/**
	 * @param parent
	 * @param aTitle
	 * @param colTitles
	 * @param colWidths
	 * @param colProperties
	 */
	public InterfaceMembersTable(Composite parent) {
		super(	parent, 
				Messages.getString("InterfaceMembersTable.Title"),  //$NON-NLS-1$
				new String[]{
					Messages.getString("InterfaceMembersTable.NameColumnTitle"), //$NON-NLS-1$
					Messages.getString("InterfaceMembersTable.TypeColumnTitle"), //$NON-NLS-1$
					Messages.getString("InterfaceMembersTable.FlagsColumnTitle") //$NON-NLS-1$
				}, 
				new int[]{ 100, 50, 300}, 
				new String[] {
					NAME,
					TYPE,
					OPTIONS
				}
		);
	}
	
	/**
	 * Returns an array of the defined {@link UnoFactoryData}.
	 * 
	 * @return the created factory data
	 */
	public UnoFactoryData[] getUnoFactoryData() {
		Vector lines = getLines();
		int size = lines.size();
		UnoFactoryData[] data = new UnoFactoryData[size];
		
		for (int i=0; i<size; i++) {
			data[i] = ((MemberLine)lines.get(i)).mData;
		}
		return data;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#addLine()
	 */
	protected ITableElement addLine() {
		MemberLine result = null;
		UnoFactoryData data = openDialog(null);
		if (data != null) {
			result = new MemberLine(data);
		}
		return result;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.openoffice.ide.eclipse.core.gui.AbstractTable#handleDoubleClick(org.eclipse.jface.viewers.DoubleClickEvent)
	 */
	protected void handleDoubleClick(DoubleClickEvent event) {
		
		// Open the Member dialog but freeze the member type
		super.handleDoubleClick(event);
		
		if (getSelection() instanceof IStructuredSelection) {
			IStructuredSelection selection = (IStructuredSelection) getSelection();
			Object o = selection.getFirstElement();
			if (o instanceof MemberLine) {
				MemberLine line = (MemberLine) o;
				UnoFactoryData data = openDialog(line.mData);
				line.mData = data;
				mTableViewer.refresh(line);
			}
		}
	}
	
	/**
	 * Open the member dialog for edition or creation.
	 * 
	 * @param content if <code>null</code>, the dialog is opened to create a
	 * 		new member, otherwise it reuses the given data to modify them.
	 * 
	 * @return the created or edited data
	 */
	protected UnoFactoryData openDialog(UnoFactoryData content) {
		InterfaceMemberDialog dlg;
		UnoFactoryData result = content;
		
		if (content == null) {
			dlg = new InterfaceMemberDialog();
		} else {
			dlg = new InterfaceMemberDialog(content);
		}
		
		if (InterfaceMemberDialog.OK == dlg.open()) {
			result = dlg.getData();
		} else {
			if (content == null) {
				dlg.disposeData();
			}
		}
		return result;
	}
	
	/**
	 * This class defines the model of the member lines.
	 * 
	 * @author cedricbosdo
	 * @see AbstractTable
	 */
	class MemberLine implements ITableElement {

		private UnoFactoryData mData;
		
		/**
		 * This constructor instanciates an UnoFactoryData, keep in mind that 
		 * these should be disposed.
		 */
		public MemberLine() {
			mData = new UnoFactoryData();
		}
		
		/**
		 * This constructor only makes a reference copy of the data, don't
		 * dispose them too early.
		 * 
		 * @param data the data for the line
		 */
		public MemberLine(UnoFactoryData data) {
			mData = data;
		}
		
		/*
		 * (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#canModify(java.lang.String)
		 */
		public boolean canModify(String property) {
			return false;
		}

		/*
		 * (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getImage(java.lang.String)
		 */
		public Image getImage(String property) {
			Image image = null;	
			if (property.equals(NAME)) {
				int memberType = ((Integer)mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)).intValue();
				if (memberType == IUnoFactoryConstants.ATTRIBUTE) {
					image = OOEclipsePlugin.getImage(ImagesConstants.ATTRIBUTE);
				} else if (memberType == IUnoFactoryConstants.METHOD) {
					image = OOEclipsePlugin.getImage(ImagesConstants.METHOD);
				}
			}
			
			return image;
		}

		/*
		 * (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getLabel(java.lang.String)
		 */
		public String getLabel(String property) {
			String label = null;
			
			if (property.equals(TYPE)) {
				String type = (String)mData.getProperty(IUnoFactoryConstants.TYPE);
				if (type != null) {
					label = type;
				}
			} else if (property.equals(NAME)) {
				String name = (String)mData.getProperty(IUnoFactoryConstants.NAME);
				if (name != null) {
					label = name;
				}
			} else if (property.equals(OPTIONS)) {
				int memberType = ((Integer)mData.getProperty(IUnoFactoryConstants.MEMBER_TYPE)).intValue();
				if (memberType == IUnoFactoryConstants.ATTRIBUTE) {
					label = (String)mData.getProperty(IUnoFactoryConstants.FLAGS);
					label = label == null ? "": label; //$NON-NLS-1$
				} else if (memberType == IUnoFactoryConstants.METHOD) {
					UnoFactoryData[] args = mData.getInnerData();
					label = ""; //$NON-NLS-1$
					for (int i=0; i<args.length; i++) {
						String name = (String)args[i].getProperty(IUnoFactoryConstants.NAME);
						if (name != null) {
							label += name + " "; //$NON-NLS-1$
						}
					}
				}
			}
			return label;
		}

		/*
		 * (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getProperties()
		 */
		public String[] getProperties() {
			return new String[]{
				TYPE,
				NAME,
				OPTIONS
			};
		}

		/*
		 * (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#getValue(java.lang.String)
		 */
		public Object getValue(String property) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see org.openoffice.ide.eclipse.core.gui.ITableElement#setValue(java.lang.String, java.lang.Object)
		 */
		public void setValue(String property, Object value) {
		}
	}
}
