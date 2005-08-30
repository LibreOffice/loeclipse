/*************************************************************************
 *
 * $RCSfile: NewServiceWizardPage.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/08/30 13:24:26 $
 *
 * The Contents of this file are made available subject to the terms of
 * either of the following licenses
 *
 *     - GNU Lesser General Public License Version 2.1
 *     - Sun Industry Standards Source License Version 1.1
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
 *
 * Sun Industry Standards Source License Version 1.1
 * =================================================
 * The contents of this file are subject to the Sun Industry Standards
 * Source License Version 1.1 (the "License"); You may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.openoffice.org/license.html.
 *
 * Software provided under this License is provided on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING,
 * WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 * MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 * See the License for the specific provisions governing your rights and
 * obligations concerning the Software.
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

package org.openoffice.ide.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.gui.rows.BooleanRow;
import org.openoffice.ide.eclipse.gui.rows.FieldEvent;
import org.openoffice.ide.eclipse.gui.rows.IFieldChangedListener;
import org.openoffice.ide.eclipse.gui.rows.TextRow;
import org.openoffice.ide.eclipse.gui.rows.TypeRow;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.model.Declaration;
import org.openoffice.ide.eclipse.model.Include;
import org.openoffice.ide.eclipse.model.InterfaceService;
import org.openoffice.ide.eclipse.model.ScopedName;
import org.openoffice.ide.eclipse.model.TreeNode;
import org.openoffice.ide.eclipse.model.UnoidlFile;
import org.openoffice.ide.eclipse.model.UnoidlModel;
import org.openoffice.ide.eclipse.model.UnoidlProject;
import org.openoffice.ide.eclipse.unotypebrowser.InternalUnoType;
import org.openoffice.ide.eclipse.unotypebrowser.UnoTypeProvider;

public class NewServiceWizardPage extends WizardPage 
								  implements IFieldChangedListener {

	private UnoidlProject unoProject;
	private String rootName;
	private String serviceName;
	
	private UnoTypeProvider typesProvider;
	
	public NewServiceWizardPage(String pageName, UnoidlProject project) {
		this(pageName, project, "", "");
	}
	
	public NewServiceWizardPage(String pageName, UnoidlProject project, 
								String aRootName, String aServiceName){
		super(pageName);
		
		unoProject = project;
		
		typesProvider = new UnoTypeProvider(project,
				UnoTypeProvider.SERVICE | UnoTypeProvider.INTERFACE);
		
		setTitle(OOEclipsePlugin.getTranslationString(
									I18nConstants.NEW_SERVICE_TITLE));
		setImageDescriptor(OOEclipsePlugin.getImageDescriptor(
									ImagesConstants.NEW_SERVICE_IMAGE));
		
		rootName = null != aRootName ? aRootName: "";
		serviceName = null != aServiceName ? aServiceName: "";
	}
	
	public void dispose() {
		try {
			packageRow.removeFieldChangedlistener();
			nameRow.removeFieldChangedlistener();
			typesProvider.dispose();
		} catch (NullPointerException e) {
			if (null != System.getProperty("DEBUG")) {
				e.printStackTrace();
			}
		}
		
		super.dispose();
	}

	//-------------------------------------------------- Page content managment
	
	private final static String P_PACKAGE           = "__package";
	private final static String P_NAME              = "__name";
	private final static String P_IFACE_INHERITANCE = "__iface_inheritance"; 
	private final static String P_PUBLISHED			= "__published";
	
	private TextRow packageRow;
	private TextRow nameRow;
	private TypeRow ifaceInheritanceRow;
	private BooleanRow publishedRow;
	
	/**
	 * Specific error message label. <code>setErrorMessage()</code> will
	 * use this row instead of the standard one.
	 */
	private Label messageLabel;
	private Label messageIcon;
	private Composite parent;
	
	private boolean constructed = false;
	
	public void createControl(Composite parent) {
		
		this.parent = parent;
		
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout(3, false));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		if (null != unoProject && !constructed) {
			// Creates the package row
			packageRow = new TextRow(body, P_PACKAGE, 
					OOEclipsePlugin.getTranslationString(I18nConstants.PACKAGE_COLON)+
						unoProject.getRootScopedName());
			packageRow.setFieldChangedListener(this);
			packageRow.setText(rootName);
			
			nameRow = new TextRow(body, P_NAME, 
					OOEclipsePlugin.getTranslationString(I18nConstants.SERVICE_NAME));
			nameRow.setFieldChangedListener(this);
			nameRow.setText(serviceName);
			
			
			ifaceInheritanceRow = new TypeRow(body, 
					P_IFACE_INHERITANCE, 
					"Inherited interface",
					typesProvider,
					UnoTypeProvider.INTERFACE);
			ifaceInheritanceRow.setFieldChangedListener(this);
			
			
			publishedRow = new BooleanRow(body, P_PUBLISHED,
					OOEclipsePlugin.getTranslationString(
							I18nConstants.PUBLISHED));
			publishedRow.setFieldChangedListener(this);
			
			// Message controls
			Composite messageComposite = new Composite(body, SWT.NONE);
			messageComposite.setLayout(new GridLayout(2, false));
			GridData gd = new GridData(GridData.FILL_BOTH);
			gd.horizontalSpan = 3;
			messageComposite.setLayoutData(gd);
			
			
			messageIcon = new Label(messageComposite, SWT.LEFT);
			messageIcon.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING |
												   GridData.VERTICAL_ALIGN_END));
			messageIcon.setImage(OOEclipsePlugin.getImage(ImagesConstants.ERROR));
			messageIcon.setVisible(false);
			
			messageLabel = new Label(messageComposite, SWT.LEFT);
			messageLabel.setLayoutData(new GridData(GridData.FILL_BOTH |
					                                GridData.VERTICAL_ALIGN_END));
			
			setPageComplete(isPageComplete());
			constructed = true;
		}
		
		setControl(body);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String newMessage) {
		if (null != messageLabel){
			if (null == newMessage){
				messageLabel.setText("");
				messageIcon.setVisible(false);
				messageLabel.setVisible(false);
			} else {
				messageLabel.setText(newMessage);
				messageIcon.setVisible(true);
				messageLabel.setVisible(true);
			}
		}
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		
		if (visible) {
			typesProvider.setProject(unoProject);
		}
	}
	
	public void setUnoidlProject(UnoidlProject aUnoProject) {
		unoProject = aUnoProject;
		
		createControl(parent);
		parent.layout();
	}
	
	public String getPackage() {
		return packageRow.getValue();
	}
	
	public String getServiceName() {
		return nameRow.getValue();
	}
	
	public String getInheritanceName() {
		return ifaceInheritanceRow.getValue();
	}
	
	public boolean isPublished() {
		return publishedRow.getBooleanValue();
	}
	
	public void setPackage(String value, boolean forced) {
		
		packageRow.setText(value);
		packageRow.setEnabled(!forced);	
	}
	
	public void setName(String value, boolean forced) {
		
		nameRow.setText(value);
		nameRow.setEnabled(!forced);
	}
	
	public void setInheritanceName(String value, boolean forced) {
		
		if (value.matches("[a-zA-Z0-9_]+(.[a-zA-Z0-9_])*")) {
			ifaceInheritanceRow.setText(value);
			ifaceInheritanceRow.setEnabled(!forced);	
		}
	}
	
	public void setPublished(boolean value, boolean forced) {
		
		publishedRow.setValue(value);
		publishedRow.setEnabled(!forced);
	}
	
	public void fieldChanged(FieldEvent e) {
		if (e.getProperty().equals(P_PACKAGE)) {
			// Change the label of the package row
			String text = OOEclipsePlugin.getTranslationString(
							I18nConstants.PACKAGE_COLON)+
						  unoProject.getRootScopedName();
			
			if (null != e.getValue() && !e.getValue().equals("")){
				text = text + ".";
			}
			packageRow.setLabel(text);
			((Composite)getControl()).layout();

		} else if (e.getProperty().equals(P_NAME)) {
			// Test if there is the scoped name already exists
			boolean exists = typesProvider.contains(e.getValue());
			if (exists) {
				setErrorMessage(OOEclipsePlugin.getTranslationString(
						I18nConstants.NAME_EXISTS));
			} else {
				setErrorMessage(null);
			}
			
		}

		setPageComplete(isPageComplete());
	}
	
	public boolean isPageComplete() {
		boolean result = false; 
		
		try {
			result = messageLabel.getText().equals("");
			
			if (ifaceInheritanceRow.getValue().equals("") || 
					nameRow.getValue().equals("")) {
				result = false;
			}
		} catch (NullPointerException e) {
			result = false;
		}
		
		return result;
	}
	
	public InterfaceService createService(String packageName, String name,
			String inheritanceName, boolean published){
		
		InterfaceService service = null;
		
		try {
			String path = unoProject.getPath() + unoProject.getSeparator() +
							unoProject.getRootScopedName();
			
			if (!packageName.equals("")) {
				path = path + Declaration.SEPARATOR + packageName;
			}
			
			unoProject.createModules(
					new ScopedName(unoProject.getRootScopedName() + 
							ScopedName.SEPARATOR + packageName),
					null);
			
			TreeNode parent = UnoidlModel.getUnoidlModel().findNode(path);
			
			// Perform only if the parent exists
			if (null != parent) {
				
				// Creates a file and the necessary folders for this new type
				String[] segments = packageName.split("\\.");
				
				IFolder folder = unoProject.getProject().getFolder(
						unoProject.getUnoidlPrefixPath());
				
				for (int i=0; i<segments.length; i++) {
					folder = folder.getFolder(segments[i]);
					if (!folder.exists()) {
						folder.create(true, true, null);
					}
				}
				
				IFile file = folder.getFile(name + ".idl");
				TreeNode fileNode = unoProject.findNode(
						UnoidlFile.computePath(file));
				UnoidlFile unofile = null;
				
				if (null == fileNode){
					unofile = new UnoidlFile(unoProject, file);
				} else {
					unofile = (UnoidlFile)fileNode;
				}
				
				String ifaceInheritanceName = inheritanceName.replace(".", "::");
				
				service = new InterfaceService(parent, 
						name, 
						unofile, 
						new ScopedName(ifaceInheritanceName));
				((InterfaceService)service).setPublished(
						published);
				
				InternalUnoType type = typesProvider.getType(
						inheritanceName);
				boolean isLibrary = false;
				if (null != type) {
					isLibrary = !type.isLocalType();
				}
				
				unofile.addInclude(
						Include.createInclude(ifaceInheritanceName, isLibrary));
				
				parent.addNode(service);
				unoProject.addNode(unofile);
				
				unofile.addDeclaration(service);
				unofile.save();
			}
		} catch (Exception e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.SERVICE_CREATION_FAILED), e);
		}
		return service;
	}

}
