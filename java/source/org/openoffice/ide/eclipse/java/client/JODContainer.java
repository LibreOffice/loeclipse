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

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.java.OOoJavaPlugin;

/**
 * Provides JODConnector and its dependencies as a library container.
 * 
 * @author cbosdonnat
 *
 */
public class JODContainer extends ClasspathContainerInitializer {

    public static final String ID = "org.openoffice.ide.eclipse.java.JOD_CONTAINER"; //$NON-NLS-1$
    public static final String WITH_SLF4J_IMPL = "slf4j-impl"; //$NON-NLS-1$
    
    private static final String SLF4J_IMPL_LIBNAME = "slf4j-jdk14-1.5.6.jar"; //$NON-NLS-1$
    private static final String JODCONNECTOR_LIBNAME = "jodconnector.jar"; //$NON-NLS-1$
    
    private static final String[] LIBS = new String[] {
        JODCONNECTOR_LIBNAME,
        "slf4j-api-1.5.6.jar" //$NON-NLS-1$
    };
    
    /**
     * Default constructor.
     */
    public JODContainer( ) {
    }

    @Override
    public void initialize(IPath pContainerPath, IJavaProject pProject)
        throws CoreException {
        
        boolean withSlf4jImpl = checkSlf4jImpl( pContainerPath ); 
        
        JODClasspathContainer container = new JODClasspathContainer( withSlf4jImpl );

        IJavaProject[] projects = new IJavaProject[]{pProject};
        IClasspathContainer[] containers = new IClasspathContainer[]{container};

        JavaCore.setClasspathContainer(pContainerPath, projects, containers, null);
    }

    /**
     * Check if the container path needs to include SLF4J implementation.
     * 
     * @param pContainerPath the path to check
     * 
     * @return <code>true</code> if SLF4J has to be included
     */
    public static boolean checkSlf4jImpl(IPath pContainerPath) {
        return pContainerPath.segmentCount() > 1 && pContainerPath.segment( 1 ).equals( WITH_SLF4J_IMPL );
    }

    /**
     * Utility method to get the container entry.
     * 
     * @param pWithSlf4jImpl <code>true</code> to add the SLF4J jdk14 implementation
     * 
     * @return the container entry
     */
    public static IClasspathEntry createClasspathEntry( boolean pWithSlf4jImpl ) {
        IPath path = new Path( JODContainer.ID );
        if ( pWithSlf4jImpl ) {
            path = path.append( WITH_SLF4J_IMPL );
        }
        
        return JavaCore.newContainerEntry( path );
    }
    
    /**
     * Class implementing the JOD libraries container.
     * 
     * @author cbosdonnat
     *
     */
    public class JODClasspathContainer implements IClasspathContainer {
        
        private boolean mSlf4jImpl;

        /**
         * Constructor.
         * 
         * @param pWithSlf4jImpl <code>true</code> to add the SLF4J jdk14 implementation
         */
        public JODClasspathContainer( boolean pWithSlf4jImpl ) {
            mSlf4jImpl = pWithSlf4jImpl;
        }
        
        /**
         * {@inheritDoc}
         */
        public IClasspathEntry[] getClasspathEntries() {
            
            String[] libsNames = getLibs( );
            
            IClasspathEntry[] libs = new IClasspathEntry[ libsNames.length ];
            for ( int i = 0; i  < libsNames.length; i++ ) {
                String lib = libsNames[i];
                try {
                    URL libUrl = OOoJavaPlugin.getDefault().getBundle().getResource( 
                            OOoJavaPlugin.LIBS_DIR + lib );
                    URL libFileUrl = FileLocator.toFileURL( libUrl );
                    File libFile = new File( libFileUrl.toURI() );
                    IPath libPath = Path.fromOSString( libFile.toString() );
                    
                    IPath srcPath = null;
                    if ( lib.equals( JODCONNECTOR_LIBNAME ) ) {
                        srcPath = libPath;
                    }
                    
                    libs[i] = JavaCore.newLibraryEntry( libPath, srcPath, null );
                } catch ( Exception e ) {
                    PluginLogger.error( Messages.getString("JODContainer.GetLibraryError") + lib,  e ); //$NON-NLS-1$
                }
            }
            
            return libs;
        }

        /**
         * {@inheritDoc}
         */
        public String getDescription() {
            return Messages.getString("JODContainer.Description"); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         */
        public int getKind() {
            return K_APPLICATION;
        }

        /**
         * {@inheritDoc}
         */
        public IPath getPath() {
            return new Path( ID );
        }
        
        /**
         * @return the libraries names to add to the container.
         */
        private String[] getLibs( ) {
            int libsCount = LIBS.length;
            if ( mSlf4jImpl ) {
                libsCount++;
            }
            
            String[] allLibs = new String[ libsCount ];
            System.arraycopy( LIBS, 0, allLibs, 0, LIBS.length );
            
            if ( mSlf4jImpl ) {
                allLibs[ allLibs.length - 1 ] = SLF4J_IMPL_LIBNAME;
            }
            return allLibs;
        }
    }
}
