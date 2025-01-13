/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2010 by Cédric Bosdonnat
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
 * The Initial Developer of the Original Code is: Cédric Bosdonnat.
 *
 * Copyright: 2010 by Cédric Bosdonnat
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.libreoffice.ide.eclipse.core.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.ProjectsManager;
import org.libreoffice.ide.eclipse.core.model.language.ILanguageBuilder;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.libreoffice.ide.eclipse.core.utils.FilesFinder;
import org.libreoffice.plugin.core.model.UnoPackage;

/**
 * Common helper GUI part to select elements to add in the UNO package to be exported.
 */
@SuppressWarnings("restriction")
public class PackageContentSelector extends Composite {

    private ResourceTreeAndListGroup mResourceGroup;
    private IUnoidlProject mProject;

    /**
     * Constructor based on SWT composite's one.
     *
     * @param pParent
     *            the parent composite.
     * @param pStyle
     *            the SWT style to give to the composite
     */
    public PackageContentSelector(Composite pParent, int pStyle) {
        super(pParent, pStyle);

        setLayout(new GridLayout(2, false));
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        mResourceGroup = new ResourceTreeAndListGroup(this, new ArrayList<Object>(),
            getResourceProvider(IResource.FOLDER | IResource.FILE),
            WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
            getResourceProvider(IResource.FILE),
            WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(), SWT.NONE,
            DialogUtil.inRegularFontMode(this));
    }

    /**
     * Set the project to work on.
     *
     * @param pPrj
     *            the project to show.
     */
    public void setProject(IUnoidlProject pPrj) {
        mProject = pPrj;
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(mProject.getName());
        mResourceGroup.setRoot(prj);
    }

    /**
     * Populate the resource view with some default data (mainly the XCU / XCS files).
     */
    public void loadDefaults() {
        List<IFile> files = getDefaultContent(mProject);
        for (IFile file : files) {
            mResourceGroup.initialCheckListItem(file);
            mResourceGroup.initialCheckTreeItem(file);
        }
    }

    /**
     * @return all the selected items
     */
    public List<?> getSelected() {
        return mResourceGroup.getAllWhiteCheckedItems();
    }

    /**
     * Set the given resources to selected.
     *
     * @param pSelected
     *            the items to select.
     */
    public void setSelected(List<IResource> pSelected) {
        for (IResource res : pSelected) {
            mResourceGroup.initialCheckTreeItem(res);
        }
    }

    /**
     * Get the default files to include in a package (mainly the XCU / XCS files).
     *
     * @param pUnoPrj
     *            the uno project to get the defaults from
     *
     * @return the list of the files to include by default
     */
    public static List<IFile> getDefaultContent(IUnoidlProject pUnoPrj) {
        // Select the XCU / XCS files by default
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pUnoPrj.getName());
        FilesFinder finder = new FilesFinder(
            new String[] { IUnoidlProject.XCU_EXTENSION, IUnoidlProject.XCS_EXTENSION });
        try {
            finder.addExclude(pUnoPrj.getDistFolder().getFullPath());
            prj.accept(finder);
        } catch (CoreException e) {
            PluginLogger.error("Could not visit the project's content.", e);
        }

        return finder.getResults();
    }

    /**
     * Convenience method to create and populate the UnoPackage.
     *
     * @param project
     *            the project to export
     * @param destFile
     *            the file to export to
     * @param resources
     *            the files and folder to add to the OXT
     *
     * @return the populated package model
     *
     * @throws Exception
     *             if anything goes wrong.
     */
    public static UnoPackage createPackage(IUnoidlProject project, File destFile, List<?> resources)
        throws Exception {
        UnoPackage pack = null;

        File prjFile = SystemHelper.getFile(project);

        // Export the library
        IFile library = null;
        ILanguageBuilder langBuilder = project.getLanguage().getLanguageBuilder();
        library = langBuilder.createLibrary(project);

        // Create the package model
        pack = UnoidlProjectHelper.createMinimalUnoPackage(project, destFile);

        if (library != null && library.exists()) {
            pack.addToClean(SystemHelper.getFile(library));
            File libraryFile = SystemHelper.getFile(library);
            pack.addFile(UnoPackage.getPathRelativeToBase(libraryFile, prjFile), libraryFile);
        }

        IFile descrFile = project.getFile(IUnoidlProject.DESCRIPTION_FILENAME);
        if (descrFile.exists()) {
            File resFile = SystemHelper.getFile(descrFile);
            pack.addContent(UnoPackage.getPathRelativeToBase(resFile, prjFile), resFile);
        }

        // Add the additional content to the package
        for (Object item : resources) {
            if (item instanceof IResource) {
                File resFile = SystemHelper.getFile((IResource) item);
                pack.addContent(UnoPackage.getPathRelativeToBase(resFile, prjFile), resFile);
            }
        }

        return pack;
    }

    /**
     * @param pResourceType
     *            the type of the resources to return by the provider.
     *
     * @return a content provider for <code>IResource</code>s that returns only children of the given resource type.
     */
    private ITreeContentProvider getResourceProvider(final int pResourceType) {
        return new WorkbenchContentProvider() {
            @Override
            public Object[] getChildren(Object object) {
                ArrayList<IResource> results = new ArrayList<IResource>();

                if (object instanceof ArrayList<?>) {
                    ArrayList<?> objs = (ArrayList<?>) object;
                    for (Object o : objs) {
                        if (o instanceof IResource) {
                            results.add((IResource) o);
                        }
                    }
                } else if (object instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) object).members();

                        // filter out the desired resource types
                        for (int i = 0; i < members.length; i++) {
                            // And the test bits with the resource types to see if they are what we want
                            if ((members[i].getType() & pResourceType) > 0 && !isHiddenResource(members[i])) {
                                results.add(members[i]);
                            }
                        }
                    } catch (CoreException e) {
                    }
                }
                return results.toArray();
            }
        };
    }

    /**
     * @param pRes
     *            the resource to be checked
     *
     * @return <code>true</code> if the resource is hidden in the lists, <code>false</code> otherwise.
     */
    private boolean isHiddenResource(IResource pRes) {
        boolean hidden = false;

        // Hide the binaries: they are always included from somewhere else
        IUnoidlProject unoprj = ProjectsManager.getProject(pRes.getProject().getName());
        hidden |= unoprj.getFolder(unoprj.getBuildPath()).equals(pRes);

        IFolder[] bins = unoprj.getBinFolders();
        for (IFolder bin : bins) {
            hidden |= bin.equals(pRes);
        }

        // Hide the hidden files
        hidden |= pRes.getName().startsWith("."); //$NON-NLS-1$

        // Hide files which are always included in the package
        hidden |= pRes.getName().equals(IUnoidlProject.DESCRIPTION_FILENAME);
        hidden |= pRes.getName().equals("MANIFEST.MF"); //$NON-NLS-1$
        hidden |= pRes.getName().equals("manifest.xml"); //$NON-NLS-1$
        hidden |= pRes.getName().equals("types.rdb"); //$NON-NLS-1$

        return hidden;
    }
}
