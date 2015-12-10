/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat.
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
 * Copyright: 2009 by Cédric Bosdonnat.
 *
 * All Rights Reserved.
 *
 ************************************************************************/
package org.openoffice.ide.eclipse.core.actions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.model.pack.PackagePropertiesModel;
import org.openoffice.ide.eclipse.core.model.utils.SystemHelper;
import org.openoffice.plugin.core.model.ManifestModel;
import org.openoffice.plugin.core.model.UnoPackage;

/**
 * Action converting the legacy package.properties into manifest.xml file.
 *
 * @author Cédric Bosdonnat <cedric.bosdonnat@free.fr>
 *
 */
public class ConvertToManifestAction implements IObjectActionDelegate {

    private IFile mPackageFile;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActivePart(IAction pAction, IWorkbenchPart pTargetPart) {
        // No need of the target part
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run( IAction pAction ) {
        PackagePropertiesModel propsModel = new PackagePropertiesModel( mPackageFile );

        String prjName = mPackageFile.getProject().getName();
        IUnoidlProject prj = ProjectsManager.getProject( prjName );
        File prjFile = SystemHelper.getFile( prj );

        // Create a dummy package to get the automatic entries of the manifest
        UnoPackage unoPackage = UnoidlProjectHelper.createMinimalUnoPackage( prj, new File( "foo.oxt" ) ); //$NON-NLS-1$
        ManifestModel manifestModel = unoPackage.getManifestModel();

        for (IFolder lib : propsModel.getBasicLibraries()) {
            manifestModel.addBasicLibrary( lib.getProjectRelativePath().toString() );
        }

        for (IFolder lib : propsModel.getDialogLibraries()) {
            manifestModel.addDialogLibrary( lib.getProjectRelativePath().toString() );
        }

        for (IResource content : propsModel.getContents()) {
            File contentFile = SystemHelper.getFile( content );
            manifestModel.addContent(
                            UnoPackage.getPathRelativeToBase( contentFile, prjFile),
                            contentFile );
        }

        Iterator<Entry<Locale, IFile>> iter = propsModel.getDescriptionFiles().entrySet().iterator();
        while ( iter.hasNext() ) {
            Entry<Locale, IFile> entry = iter.next();
            manifestModel.addDescription( entry.getValue().getProjectRelativePath().toString(), entry.getKey() );
        }

        // Serialize the manifest model into the manifest.xml file
        IFile manifestFile = mPackageFile.getParent().getFile( new Path( UnoPackage.MANIFEST_PATH ) );
        File file = new File( manifestFile.getLocationURI() );

        if ( !file.exists() || file.canWrite() ) {
            FileOutputStream out = null;

            try {
                out = new FileOutputStream( file );
                manifestModel.write( out );
            } catch ( Exception e ) {
                PluginLogger.error( Messages.getString("ConvertToManifestAction.WriteError0"), e ); //$NON-NLS-1$
            } finally {
                try { out.close(); } catch (IOException e) { }
            }

            // Refresh the file
            try {
                manifestFile.refreshLocal( IResource.DEPTH_ZERO, null );
            } catch (CoreException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void selectionChanged(IAction pAction, ISelection pSelection) {
        if ( !pSelection.isEmpty() && pSelection instanceof IStructuredSelection ) {
            IStructuredSelection sel = (IStructuredSelection)pSelection;
            Object o = sel.getFirstElement();
            if ( o instanceof IFile ) {
                mPackageFile = ( IFile )o;
            }
        }
    }
}
