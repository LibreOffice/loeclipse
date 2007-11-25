/*************************************************************************
 *
 * $RCSfile: NewUnoFilePage.java,v $
 *
 * $Revision: 1.2 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:29 $
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
package org.openoffice.ide.eclipse.core.wizards.pages;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.part.FileEditorInput;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.internal.model.UnoidlProject;
import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.CompositeFactory;
import org.openoffice.ide.eclipse.core.wizards.Messages;

/**
 * Simple Uno file creation page, this wizard will be removed when all
 * the UNO types will have their own wizard. 
 * 
 * @author cedricbosdo
 *
 */
public class NewUnoFilePage extends WizardNewFileCreationPage {
    
    /**
     * Constructor.
     * 
     * @param pPageName the page name
     * @param pSelection the selection where to create the IDL file
     */
    public NewUnoFilePage(String pPageName, IStructuredSelection pSelection) {
        super(pPageName, pSelection);
        
        setTitle(Messages.getString("NewUnoFilePage.Title")); //$NON-NLS-1$
        setDescription(Messages.getString("NewUnoFilePage.Message")); //$NON-NLS-1$
        setImageDescriptor(OOEclipsePlugin.getImageDescriptor(ImagesConstants.NEWFILE_WIZ));
        
    }
    
    /**
         * {@inheritDoc}
         */
    public boolean canFlipToNextPage() {
        boolean result = false;
        
        try {
            IPath parentPath = getContainerFullPath();
            IFolder folder = ResourcesPlugin.getWorkspace().getRoot().getFolder(parentPath);
            
            result = isCreatable(folder, getFileName());
        } catch (Exception e) {
            result = false;
        }
            
        return result;
    }
    
    //--------------- Wolrdwide available unoidl file creation methods
    
    /**
     * Method which writes the basic content of the file, ie: the includes and defines.
     * 
     * @param pFile created file where to write this content
     */
    private static void createFileContent(IFile pFile) {
        
        try {
            UnoidlProject unoProject = (UnoidlProject)pFile.getProject().
                    getNature(OOEclipsePlugin.UNO_NATURE_ID);
            
            IUnoComposite composite = CompositeFactory.createFile(pFile);
            composite.create(true);
            composite.dispose();
            
            unoProject.getProject().refreshLocal(IProject.DEPTH_INFINITE, null);
            
        } catch (Exception e) {
            PluginLogger.error(
                Messages.getString("NewUnoFilePage.UnoProjectError"), e); //$NON-NLS-1$
        }
    }
    
    /**
     * This method help creating a new unoidl file with it's basic content. The
     * unoidl file can be created only if it's parent is unoidl capable and if the
     * file name ends with <code>.idl</code>. 
     * 
     * @param pFolder parent folder where to put the unoidl file
     * @param pFilename name of the file to create
     * @return <code>true</code> if the creation succeeded, <code>false</code>
     *         otherwise.
     */
    public static boolean createUnoidlFile(IFolder pFolder, String pFilename) {
        return createUnoidlFile(pFolder, pFilename, null);
    }
    
    
    /**
     * This method help creating a new unoidl file with it's basic content. The
     * unoidl file can be created only if it's parent is unoidl capable and if the
     * file name ends with <code>.idl</code>. After the file creation, the file is
     * edited with the unoidl file editor
     * 
     * @param pFolder parent folder where to put the unoidl file
     * @param pFilename name of the file to create
     * @param pWorkbench worbench where to launch the editor
     * @return <code>true</code> if the creation succeeded, <code>false</code>
     *         otherwise.
     */
    public static boolean createUnoidlFile(IFolder pFolder, String pFilename, IWorkbench pWorkbench) {
        boolean performed = false;
        
        if (null != pFolder) {
            try {
                String idlfolder = pFolder.getPersistentProperty(new QualifiedName(
                        OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, UnoidlProject.IDL_FOLDER));
                
                if (null != idlfolder && idlfolder.equals("true")) { //$NON-NLS-1$
                    
                    if (null != pFilename && pFilename.endsWith(".idl")) { //$NON-NLS-1$
                        IFile file = pFolder.getFile(pFilename);
                        createFileContent(file);
                        
                        IFileEditorInput editorInput = new FileEditorInput(file);
                        
                        // Show the created file in the unoidl editor
                        pWorkbench.getActiveWorkbenchWindow().getActivePage().
                                openEditor(editorInput, OOEclipsePlugin.UNO_EDITOR_ID);
                        
                        performed = true;
                    } else {
                        PluginLogger.error(
                            Messages.getString("NewUnoFilePage.WrongExtensionError"), null);  //$NON-NLS-1$
                    }
                } else {
                    PluginLogger.error(
                            Messages.getString("NewUnoFilePage.NoIdlFolderError"), null); //$NON-NLS-1$
                }
                
            } catch (CoreException e) {
                PluginLogger.error(
                        Messages.getString("NewUnoFilePage.NoIdlFolderError"), e); //$NON-NLS-1$
            } catch (NullPointerException e) {
                PluginLogger.debug("Can't open the IDL file: " + pFilename, e);
            }
        }
        return performed;
    }

    /**
     * This method checks if the parent folder is unoidl capable and if the filename
     * ends with <code>.idl</code>. A Unoidl capable folder is a folder that possesses
     * a persistent property IDL_FOLDER set to <code>true</code>
     * 
     * @param pFolder parent folder of the file
     * @param pFilename file of the file to create
     * @return <code>true</code> if the file can be created, <code>false</code> otherwise
     */
    private boolean isCreatable(IFolder pFolder, String pFilename) {
        boolean creatable = false;
        
        if (null != pFolder) {
            try {
                String idlfolder = pFolder.getPersistentProperty(new QualifiedName(
                        OOEclipsePlugin.OOECLIPSE_PLUGIN_ID, UnoidlProject.IDL_FOLDER));
                
                if (null != idlfolder && idlfolder.equals("true")) { //$NON-NLS-1$
                    
                    if (null != pFilename && pFilename.endsWith(".idl")) { //$NON-NLS-1$
                        
                        creatable = true;
                    } else {
                        setErrorMessage(Messages.getString("NewUnoFilePage.WrongExtensionError")); //$NON-NLS-1$
                    }
                } else {
                    setErrorMessage(Messages.getString("NewUnoFilePage.NoIdlFolderError")); //$NON-NLS-1$
                }
                
            } catch (CoreException e) {
                setErrorMessage(Messages.getString("NewUnoFilePage.NoIdlFolderError")); //$NON-NLS-1$
            }
        }
        
        return creatable;
    }
}