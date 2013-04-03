/*************************************************************************
 *
 * $RCSfile: OOoContainerPage.java,v $
 *
 * $Revision: 1.1 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/12/26 14:40:18 $
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
package org.openoffice.ide.eclipse.java.build;

import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.rows.OOoRow;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.java.OOoJavaPlugin;

/**
 * Edition and creation page for the LibreOffice libraries container.
 * 
 * @author cedricbosdo
 *
 */
public class OOoContainerPage extends WizardPage implements
        IClasspathContainerPage, IClasspathContainerPageExtension {

    private static final int LAYOUT_COLUMNS = 3;

    private static final String OOO = "ooo"; //$NON-NLS-1$
    
    private IClasspathEntry mContainer;
    private IJavaProject mProject;

    private OOoRow mOOoRow;

    /**
     * Needed default constructor.
     */
    public OOoContainerPage() {
        super("oocontainer"); //$NON-NLS-1$
        
        setTitle(Messages.getString("OOoContainerPage.DialogTitle")); //$NON-NLS-1$
        setDescription(Messages.getString("OOoContainerPage.DialogDescription")); //$NON-NLS-1$
        ImageDescriptor image = OOoJavaPlugin.getImageDescriptor(
                Messages.getString("OOoContainerPage.DialogImage")); //$NON-NLS-1$
        setImageDescriptor(image);
        
        mContainer = getDefaultEntry();
    }
    
    /**
     * @return the default OOo container path
     */
    private IClasspathEntry getDefaultEntry() {
        IClasspathEntry result = null;
        
        IOOo someOOo = OOoContainer.getSomeOOo(null);
        if (someOOo != null) {
            String name = someOOo.getName();
            IPath path = new Path(OOoClasspathContainer.ID + IPath.SEPARATOR + name);
            result = JavaCore.newContainerEntry(path);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public boolean finish() {
        boolean result = true;
        try {
            IOOo ooo = OOoContainer.getOOo(mOOoRow.getValue());
            
            String prjName = mProject.getProject().getName();
            IUnoidlProject unoPrj = ProjectsManager.getProject(prjName);
            if (unoPrj != null) {
                // The project is a UNO project
                unoPrj.setOOo(ooo);
                unoPrj.saveAllProperties();
            } else {
                // remove the previous libraries
                removeOOoDependencies(mProject);
                
                // Add the new library
                IPath path = new Path(OOoClasspathContainer.ID + IPath.SEPARATOR + ooo.getName());
                IClasspathEntry containerEntry = JavaCore.newContainerEntry(path);
                setSelection(containerEntry);
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public IClasspathEntry getSelection() {
        return mContainer;
    }

    /**
     * {@inheritDoc}
     */
    public void setSelection(IClasspathEntry pContainerEntry) {
        mContainer = pContainerEntry;
        
        if (mContainer == null) {
            mContainer = getDefaultEntry();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void createControl(Composite pParent) {
        Composite body = new Composite(pParent, SWT.NONE);
        body.setLayout(new GridLayout(LAYOUT_COLUMNS, false));
        
        // Add a list to select the OOo configuration.
        String oooName = mContainer.getPath().segment(
                OooClasspathContainerInitializer.HINT_SEGMENT);
        IOOo ooo = OOoContainer.getOOo(oooName);
        mOOoRow = new OOoRow(body, OOO, ooo);
        
        setControl(body);
    }

    /**
     * {@inheritDoc}
     */
    public void initialize(IJavaProject pProject, IClasspathEntry[] pCurrentEntries) {
        mProject = pProject;
        
        boolean found = false;
        int i = 0;
        while (i < pCurrentEntries.length && !found) {
            IClasspathEntry entry = pCurrentEntries[i];
            if (entry.getPath().segment(0).startsWith(OOoClasspathContainer.ID)) {
                found = true;
                mContainer = entry;
            }
            i++;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        mOOoRow.dispose();
        
        super.dispose();
    }
    
    /**
     * Add the LibreOffice common JARs to a projects build path.
     * 
     * @param pOoo the ooo to use for the classpath
     * @param pProject the project to change
     */
    public static void addOOoDependencies(IOOo pOoo, IJavaProject pProject) {
        
        if (null != pOoo) {
            try {
                IClasspathEntry[] oldEntries = pProject.getRawClasspath();
                IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + 1];
                
                System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);
                
                IPath path = new Path(OOoClasspathContainer.ID + IPath.SEPARATOR + pOoo.getName());
                IClasspathEntry containerEntry = JavaCore.newContainerEntry(path);
                entries[entries.length - 1] = containerEntry;
                
                pProject.setRawClasspath(entries, null);
            } catch (JavaModelException e) {
                PluginLogger.error(
                        Messages.getString("OOoContainerPage.ClasspathSetFailed"), e); //$NON-NLS-1$
            }
        }
    }
    
    /**
     * Remove all the OOo user libraries from the project build path.
     * 
     * @param pProject the project to change
     */
    public static void removeOOoDependencies(IJavaProject pProject) {
        try {
            IClasspathEntry[] entries = pProject.getRawClasspath();
            Vector<IClasspathEntry> newEntries = new Vector<IClasspathEntry>();

            // Copy all the sources in a new entry container
            for (int i = 0, length = entries.length; i < length; i++) {
                IClasspathEntry entry = entries[i];
                
                if (!entry.getPath().segment(0).equals(OOoClasspathContainer.ID)) {
                    newEntries.add(entry);
                }
            }
            
            IClasspathEntry[] result = new IClasspathEntry[newEntries.size()];
            result = newEntries.toArray(result);
            
            pProject.setRawClasspath(result, null);
            
        } catch (JavaModelException e) {
            PluginLogger.error(
                    Messages.getString("OOoContainerPage.ClasspathSetFailed"), e); //$NON-NLS-1$
        }
    }
}
