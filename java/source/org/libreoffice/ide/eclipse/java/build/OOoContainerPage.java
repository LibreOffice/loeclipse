/*************************************************************************
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
package org.libreoffice.ide.eclipse.java.build;

import java.util.Vector;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.gui.rows.OOoRow;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.java.OOoJavaPlugin;

/**
 * Edition and creation page for the LibreOffice libraries container.
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
        String msg = Messages.getString("OOoContainerPage.DialogImage"); //$NON-NLS-1$
        ImageDescriptor image = OOoJavaPlugin.getImageDescriptor(msg);
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
    @Override
    public boolean finish() {
        boolean result = true;
        try {
            IOOo ooo = OOoContainer.getOOo(mOOoRow.getValue());

            String prjName = mProject.getProject().getName();
            IUnoidlProject unoPrj = ProjectsManager.getProject(prjName);
            // XXX: We need to check if the project is configured correctly and notify
            if (unoPrj != null && unoPrj.getLanguage() != null) {
                // The project is a UNO project
                unoPrj.setOOo(ooo);
                unoPrj.saveAllProperties();
            } else {
                // remove the previous libraries
                removeOOoDependencies(mProject);

                // Add the new library
                IClasspathEntry entry = getOOoLibraryContainer(mProject, ooo);
                addOOoDependencies(mProject, entry);
                setSelection(entry);
            }
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IClasspathEntry getSelection() {
        return mContainer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelection(IClasspathEntry containerEntry) {
        mContainer = containerEntry;

        if (mContainer == null) {
            mContainer = getDefaultEntry();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createControl(Composite parent) {
        Composite body = new Composite(parent, SWT.NONE);
        body.setLayout(new GridLayout(LAYOUT_COLUMNS, false));

        // Add a list to select the OOo configuration.
        String oooName = mContainer.getPath().segment(OooClasspathContainerInitializer.HINT_SEGMENT);
        IOOo ooo = OOoContainer.getOOo(oooName);
        mOOoRow = new OOoRow(body, OOO, ooo);

        setControl(body);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(IJavaProject project, IClasspathEntry[] currentEntries) {
        mProject = project;

        boolean found = false;
        int i = 0;
        while (i < currentEntries.length && !found) {
            IClasspathEntry entry = currentEntries[i];
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
     * @param ooo the ooo to use for the module or class path
     *
     * @param project the project to change
     */
    public static void addOOoDependencies(IOOo ooo, IJavaProject project) {

        if (null != ooo) {
            addOOoDependencies(project, getOOoLibraryContainer(project, ooo));
        }
    }

    /**
     * Remove all the OOo user libraries from the project build path.
     *
     * @param project the project to change
     */
    public static void removeOOoDependencies(IJavaProject project) {
        try {
            IClasspathEntry[] entries = project.getRawClasspath();
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
            project.setRawClasspath(result, null);

        } catch (JavaModelException e) {
            PluginLogger.error(Messages.getString("OOoContainerPage.ClasspathSetFailed"), e); //$NON-NLS-1$
        }
    }

    /**
     * Add the LibreOffice common JARs to a projects build path .
     *
     * @param project the project to change
     *
     * @param entry the library entry to add
     */
    private static void addOOoDependencies(IJavaProject project, IClasspathEntry entry) {
        try {
            IClasspathEntry[] oldEntries = project.getRawClasspath();
            IClasspathEntry[] entries = new IClasspathEntry[oldEntries.length + 1];

            System.arraycopy(oldEntries, 0, entries, 0, oldEntries.length);

            entries[entries.length - 1] = entry;
            project.setRawClasspath(entries, null);

        } catch (JavaModelException e) {
            PluginLogger.error(Messages.getString("OOoContainerPage.ClasspathSetFailed"), e); //$NON-NLS-1$
        }
    }

    /**
     * Get the LibreOffice library entry to add to projects build path.
     *       If the project is modular then the library will be too.
     *
     * @param project the project to add library
     *
     * @param ooo the library entry to add
     *
     * @return the LibreOffice library in modular format if needed.
     */

    private static IClasspathEntry getOOoLibraryContainer(IJavaProject project, IOOo ooo) {
        IPath path = new Path(OOoClasspathContainer.ID + IPath.SEPARATOR + ooo.getName());
        String value = String.valueOf(JavaRuntime.isModularProject(project));
        IClasspathAttribute attribute = JavaCore.newClasspathAttribute(IClasspathAttribute.MODULE, value);
        return JavaCore.newContainerEntry(path, new IAccessRule[0], new IClasspathAttribute[]{attribute}, false); 
    }

}
