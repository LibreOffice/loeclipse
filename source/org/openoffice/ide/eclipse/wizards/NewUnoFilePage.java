/*************************************************************************
 *
 * $RCSfile: NewUnoFilePage.java,v $
 *
 * $Revision: 1.5 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2005/11/27 17:48:22 $
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
package org.openoffice.ide.eclipse.wizards;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.part.FileEditorInput;
import org.openoffice.ide.eclipse.OOEclipsePlugin;
import org.openoffice.ide.eclipse.i18n.I18nConstants;
import org.openoffice.ide.eclipse.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.model.UnoidlFile;
import org.openoffice.ide.eclipse.model.UnoidlProject;

public class NewUnoFilePage extends WizardNewFileCreationPage {

	/**
	 * Specific error message label. <code>setErrorMessage()</code> will
	 * use this row instead of the standard one.
	 */
	private Label messageLabel;
	private Label messageIcon;
	
	public NewUnoFilePage(String pageName, IStructuredSelection selection) {
		super(pageName, selection);
		
		setTitle(OOEclipsePlugin.getTranslationString(I18nConstants.NEW_FILE_TITLE));
		setDescription(OOEclipsePlugin.getTranslationString(I18nConstants.NEW_FILE_MESSAGE));
		setImageDescriptor(OOEclipsePlugin.getImageDescriptor(ImagesConstants.NEWFILE_WIZ));
		
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		boolean result = false;
		
		try {
			IPath parentPath = getContainerFullPath();
			IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(parentPath);
			
			result = isCreatable(folder, getFileName());
		} catch (Exception e){
			result = false;
		}
			
		return result;
	}

	//--------------------- Adding a message line at the bottom of the page
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		//Inherits the parents control
		super.createControl(parent);
		Composite control = (Composite)getControl();
		
		// Add an error message label
		Composite messageComposite = new Composite(control, SWT.NONE);
		messageComposite.setLayout(new GridLayout(2, false));
		messageComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		messageIcon = new Label(messageComposite, SWT.LEFT);
		messageIcon.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING |
											   GridData.VERTICAL_ALIGN_END));
		messageIcon.setImage(OOEclipsePlugin.getImage(ImagesConstants.ERROR));
		messageIcon.setVisible(false);
		
		messageLabel = new Label(messageComposite, SWT.LEFT);
		messageLabel.setLayoutData(new GridData(GridData.FILL_BOTH |
				                                GridData.VERTICAL_ALIGN_END));
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
	
	//--------------- Wolrdwide available unoidl file creation methods
	
	/**
	 * Method which writes the basic content of the file, ie: the includes and defines
	 * 
	 * @param file created file where to write this content
	 */
	private static void createFileContent(IFile file) {
		
		UnoidlProject unoProject;
		try {
			unoProject = (UnoidlProject)file.getProject().
				getNature(OOEclipsePlugin.UNO_NATURE_ID);
			UnoidlFile unoidlFile = new UnoidlFile(unoProject, file);
			unoidlFile.save();
		} catch (CoreException e) {
			OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
					I18nConstants.NOT_UNO_PROJECT), e);
		}
	}
	
	/**
	 * This method help creating a new unoidl file with it's basic content. The
	 * unoidl file can be created only if it's parent is unoidl capable and if the
	 * file name ends with <code>.idl</code>. 
	 * 
	 * @param folder parent folder where to put the unoidl file
	 * @param filename name of the file to create
	 * @return <code>true</code> if the creation succeeded, <code>false</code>
	 *         otherwise.
	 */
	public static boolean createUnoidlFile(IFolder folder, String filename){
		return createUnoidlFile(folder, filename, null);
	}
	
	
	/**
	 * This method help creating a new unoidl file with it's basic content. The
	 * unoidl file can be created only if it's parent is unoidl capable and if the
	 * file name ends with <code>.idl</code>. After the file creation, the file is
	 * edited with the unoidl file editor
	 * 
	 * @param folder parent folder where to put the unoidl file
	 * @param filename name of the file to create
	 * @param workbench worbench where to launch the editor
	 * @return <code>true</code> if the creation succeeded, <code>false</code>
	 *         otherwise.
	 */
	public static boolean createUnoidlFile(IFolder folder, String filename, IWorkbench workbench){
		boolean performed = false;
		
		if (null != folder){
			try {
				String idlfolder = folder.getPersistentProperty(new QualifiedName(
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, UnoidlProject.IDL_FOLDER));
				
				if (null != idlfolder && idlfolder.equals("true")){
					
					if (null != filename && filename.endsWith(".idl")){
						IFile file = folder.getFile(filename);
						createFileContent(file);
						
						IFileEditorInput editorInput = new FileEditorInput(file);
						
						// Show the created file in the unoidl editor
						if (null != workbench){
							workbench.getActiveWorkbenchWindow().getActivePage().
										openEditor(editorInput, OOEclipsePlugin.UNO_EDITOR_ID);
						}
						
						performed = true;
					} else {
						OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
								I18nConstants.NOT_IDL_EXTENSION), null); 
					}
				} else {
					OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
							I18nConstants.NOT_IDL_CAPABLE), null);
				}
				
			} catch (CoreException e) {
				OOEclipsePlugin.logError(OOEclipsePlugin.getTranslationString(
						I18nConstants.NOT_IDL_CAPABLE), e);
			}
		}
		return performed;
	}

	/**
	 * This method checks if the parent folder is unoidl capable and if the filename
	 * ends with <code>.idl</code>. A Unoidl capable folder is a folder that possesses
	 * a persistent property IDL_FOLDER set to <code>true</code>
	 * 
	 * @param folder parent folder of the file
	 * @param filename file of the file to create
	 * @return <code>true</code> if the file can be created, <code>false</code> otherwise
	 */
	private boolean isCreatable(IFolder folder, String filename){
		boolean creatable = false;
		
		if (null != folder){
			try {
				String idlfolder = folder.getPersistentProperty(new QualifiedName(
						OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, UnoidlProject.IDL_FOLDER));
				
				if (null != idlfolder && idlfolder.equals("true")){
					
					if (null != filename && filename.endsWith(".idl")){
						
						creatable = true;
					} else {
						setErrorMessage(OOEclipsePlugin.getTranslationString(
								I18nConstants.NOT_IDL_EXTENSION));
					}
				} else {
					setErrorMessage(OOEclipsePlugin.getTranslationString(
							I18nConstants.NOT_IDL_CAPABLE));
				}
				
			} catch (CoreException e) {
				setErrorMessage(OOEclipsePlugin.getTranslationString(
						I18nConstants.NOT_IDL_CAPABLE));
			}
		}
		
		return creatable;
	}
}