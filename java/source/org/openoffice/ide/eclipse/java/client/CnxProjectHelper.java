/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Cédric Bosdonnat
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
package org.openoffice.ide.eclipse.java.client;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.launching.JavaRuntime;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.java.JavaProjectHandler;
import org.openoffice.ide.eclipse.java.OOoJavaPlugin;
import org.openoffice.ide.eclipse.java.utils.TemplatesHelper;

/**
 * Helper class for the creation of the UNO connection library project.
 * 
 * @author cbosdonnat
 *
 */
public class CnxProjectHelper {

    public static final String DEST_PACKAGE = "org.openoffice.connection"; //$NON-NLS-1$
    
    private static final String LICENSE_FILE = "LICENSE.txt"; //$NON-NLS-1$
    private static final String AUTHORS_FILE = "AUTHORS.txt"; //$NON-NLS-1$
    private static final String[] HELPER_CLASSES = {
        "AbstractConnection", //$NON-NLS-1$
        "Connection", //$NON-NLS-1$
        "OpenOfficeException", //$NON-NLS-1$
        "PipeConnection", //$NON-NLS-1$
        "SocketConnection" //$NON-NLS-1$
    };
    
    // TODO Should be configured ?
    private static final String CNX_PROJECT_NAME = "Java Uno Connector"; //$NON-NLS-1$
    private static final String SRC_DIR = "src"; //$NON-NLS-1$
    private static final String BIN_DIR = "bin"; //$NON-NLS-1$
    
    
    /**
     * Get the java project named {@value #CNX_PROJECT_NAME} if it exists or create it.
     *  
     * <p>This project contains the LGPL library for the UNO Connection and should
     * be used as a dependency of the Java UNO Client projects.</p>
     * 
     * @param pOOo the OOo instance to use as project's dependencies
     * @param pMonitor a progress monitor to use for reporting the progress
     *  
     * @return the connector project
     * 
     * @throws Exception if the project can't be created or opened
     */
    public static IJavaProject getConnectionProject( IOOo pOOo, IProgressMonitor pMonitor ) throws Exception {
        
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( CNX_PROJECT_NAME );
        
        boolean creating = false;
        if ( !prj.exists() ) {
            IProjectDescription desc = ResourcesPlugin.getWorkspace().newProjectDescription( CNX_PROJECT_NAME );
            desc.setNatureIds( new String[] { JavaCore.NATURE_ID } );
            prj.create( desc, pMonitor );
            creating = true;
        }
        
        if ( !prj.isOpen() ) {
            // Opens the project if it is closed
            prj.open( pMonitor );
        }
        
        // Create the src folder if not existing
        IFolder src = prj.getFolder( SRC_DIR );
        if ( !src.exists() ) {
            src.create( true, true, pMonitor );
        }
        
        IFolder bin = prj.getFolder( BIN_DIR );
        if ( !bin.exists() ) {
            bin.create( true, true, pMonitor );
        }
        
        IJavaProject cnxPrj = null;
        // Create & configure the java project
        if ( creating ) {
            cnxPrj = JavaCore.create( prj );
            IClasspathEntry srcEntry = JavaCore.newSourceEntry( src.getFullPath() );
            IClasspathEntry jreEntry = JavaRuntime.getDefaultJREContainerEntry();
            cnxPrj.setRawClasspath( new IClasspathEntry[] { srcEntry, jreEntry }, pMonitor );
            cnxPrj.setOutputLocation( bin.getFullPath(), pMonitor );
            cnxPrj.save( pMonitor, true );
            
            JavaProjectHandler prjHandler = new JavaProjectHandler();
            prjHandler.addOOoDependencies( pOOo, prj );
            
            // Set the Java builder
            setJavaBuilder( prj, pMonitor );
            
            // Add the sources 
            String path = src.getProjectRelativePath().append( DEST_PACKAGE.replace( '.', '/' ) ).toString();
            for ( String helperClass : HELPER_CLASSES ) {
                TemplatesHelper.copyTemplate( prj, helperClass + TemplatesHelper.JAVA_EXT, 
                        ClientWizard.class, path, DEST_PACKAGE );
            }
            
            TemplatesHelper.copyTemplate( prj, LICENSE_FILE, 
                    CnxProjectHelper.class, null );
            TemplatesHelper.copyTemplate( prj, AUTHORS_FILE, 
                    CnxProjectHelper.class, null );
            
            prj.refreshLocal( IResource.DEPTH_INFINITE, pMonitor );
        } else if ( prj.hasNature( JavaCore.NATURE_ID ) ) {
            cnxPrj = JavaCore.create( prj );
        } else {
            throw new CoreException( new Status( IStatus.ERROR, OOoJavaPlugin.PLUGIN_ID, 
                    "Already existing non-Java project: " + CNX_PROJECT_NAME ) );
        }
        
        return cnxPrj;
    }


    /**
     * Add the Java builder to the the project builders. 
     * 
     * @param pPrj the project on which to add the Java builder
     * @param pMonitor the monitor to report the progress
     * 
     * @throws CoreException if the project's description can't be get or set
     */
    private static void setJavaBuilder(IProject pPrj, IProgressMonitor pMonitor) throws CoreException {
        IProjectDescription descr = pPrj.getDescription();
        ICommand[] builders = descr.getBuildSpec();
        ICommand[] newCommands = new ICommand[builders.length + 1];
    
        ICommand typesbuilderCommand = descr.newCommand();
        typesbuilderCommand.setBuilderName( JavaCore.BUILDER_ID );
        newCommands[0] = typesbuilderCommand;
        
        System.arraycopy( builders, 0, newCommands, 1, builders.length );
        
        descr.setBuildSpec( newCommands );
        pPrj.setDescription( descr, pMonitor );
    }
}
