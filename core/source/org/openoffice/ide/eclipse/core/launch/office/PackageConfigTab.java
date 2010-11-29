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
package org.openoffice.ide.eclipse.core.launch.office;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.PackageContentSelector;
import org.openoffice.ide.eclipse.core.i18n.ImagesConstants;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;

/**
 * Tab for selecting the content of the package to test.
 * 
 * @author Cédric Bosdonnat
 *
 */
public class PackageConfigTab extends AbstractLaunchConfigurationTab {

    PackageContentSelector mContentSelector;
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite pParent) {
        
        Composite body = new Composite( pParent, SWT.NONE );
        body.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
        body.setLayout( new GridLayout() );
        
        mContentSelector = new PackageContentSelector( body, SWT.NONE );
        
        setControl( body );
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return "Package content";
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Image getImage() {
        return OOEclipsePlugin.getImage(ImagesConstants.PACKAGE_CONTENT);
    }

    /**
     * {@inheritDoc}
     */
    public void initializeFrom(ILaunchConfiguration pConfiguration) {
        try {
            String prjName = pConfiguration.getAttribute( IOfficeLaunchConstants.PROJECT_NAME, new String() );
            IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( prjName );
            mContentSelector.setProject( prj );
            
            String paths = pConfiguration.getAttribute( IOfficeLaunchConstants.CONTENT_PATHS, new String() );
            if ( paths.isEmpty() ) {
                mContentSelector.loadData( ProjectsManager.getProject( prjName ) );
            } else {
                String[] pathsItems = paths.split( IOfficeLaunchConstants.PATHS_SEPARATOR );
                ArrayList<IResource> selected = new ArrayList<IResource>();
                for (String path : pathsItems) {
                    IResource res = prj.findMember( path );
                    if ( res != null ) {
                        selected.add( res );
                    }
                }
                mContentSelector.setSelected( selected );
            }
        } catch (CoreException e) {
            PluginLogger.error(Messages.OfficeTab_Configurationerror, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void performApply(ILaunchConfigurationWorkingCopy pConfiguration) {
        List<?> selected = mContentSelector.getSelected();
        String paths = new String();
        for (Object obj : selected) {
            if ( obj instanceof IResource ) {
                IResource res = (IResource)obj;
                
                if ( !paths.isEmpty() ) {
                    paths += IOfficeLaunchConstants.PATHS_SEPARATOR;
                }
                paths += res.getProjectRelativePath().toString();
            }
        }
        pConfiguration.setAttribute( IOfficeLaunchConstants.CONTENT_PATHS, paths );
    }

    /**
     * {@inheritDoc}
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy pConfiguration) {
        try {
            String prjName = pConfiguration.getAttribute( IOfficeLaunchConstants.PROJECT_NAME, new String() );
            if ( !prjName.isEmpty() ) {
                IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( prjName );
                mContentSelector.setProject( prj );
                
                mContentSelector.loadData( ProjectsManager.getProject( prjName ) );
            }
        } catch (CoreException e) {
            // Don't do anything in that case
        }
    }
}
