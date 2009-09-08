/*************************************************************************
 *
 * The Contents of this file are made available subject to the terms of
 * the GNU Lesser General Public License Version 2.1
 *
 * GNU Lesser General Public License Version 2.1
 * =============================================
 * Copyright 2009 by Novell, Inc.
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
 * Copyright: 2009 by Novell, Inc.
 *
 * All Rights Reserved.
 * 
 ************************************************************************/
package org.openoffice.ide.eclipse.cpp.client;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.wizards.CCProjectWizard;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.cpp.Activator;
import org.openoffice.ide.eclipse.cpp.CppProjectHandler;


public class ClientWizard extends CCProjectWizard {

    private static final String HELPER_DIR_NAME = "helper";
    private UnoConnectionPage mCnxPage;

    public ClientWizard() {
        super( );
        setWindowTitle( "UNO Client C++ project" );
    }
    
    @Override
    public void addPages() {
        mCnxPage = new UnoConnectionPage();
        UnoClientWizardPage mainPage = new UnoClientWizardPage( "cdtmain", mCnxPage );
        fMainPage = mainPage;
        fMainPage.setTitle( getWindowTitle() );
        fMainPage.setDescription( "Create the UNO C++ client application project" );
        fMainPage.setImageDescriptor( Activator.imageDescriptorFromPlugin( Activator.PLUGIN_ID, 
                "icons/uno_client_wiz.png" ) );
        
        addPage(fMainPage);
        
        mCnxPage.setMainPage( mainPage );
        addPage( mCnxPage );
        
    }
    
    /**
     * Do the final configuration for UNO client and generate some code here.
     */
    @Override
    protected boolean setCreated() throws CoreException {
        
        boolean created = super.setCreated();
        
        IOOo ooo = mCnxPage.getOoo();
        ISdk sdk = mCnxPage.getSdk();
        
        // Add the necessary includes/libs/macros
        CppProjectHandler.addOOoDependencies( ooo, sdk, newProject );
        
        // Copy the helper files in the helper source dir
        IFolder srcDir = newProject.getFolder( HELPER_DIR_NAME );
        srcDir.create( true, true, null );
        
        copyResource( "connection.hxx", srcDir ); //$NON-NLS-1$
        copyResource( "connection.cxx", srcDir ); //$NON-NLS-1$
        
        srcDir.refreshLocal( IResource.DEPTH_ONE, null );
        
        // Add the helper dir to the source path entries
        ICProject cprj = CoreModel.getDefault().getCModel().getCProject( newProject.getName() );
        IPathEntry[] entries = CoreModel.getRawPathEntries( cprj );
        IPathEntry[] newEntries = new IPathEntry[ entries.length + 1 ];
        System.arraycopy( entries, 0, newEntries, 0, entries.length );
        newEntries[ newEntries.length - 1 ] = CoreModel.newSourceEntry( srcDir.getFullPath() );
        CoreModel.setRawPathEntries( cprj, newEntries, null );
        
        // TODO Run the cppumaker on the ooo types ( asynchronous )
        
        // TODO Setup the launch config
        
        return created;
    }

    private void copyResource(String pResName, IFolder pSrcDir) {
        InputStream in = this.getClass().getResourceAsStream( pResName );
        File destFile = pSrcDir.getFile( pResName ).getLocation().toFile();
        
        FileWriter out = null;
        try {
            
            LineNumberReader reader = new LineNumberReader( new InputStreamReader( in ) );
            out = new FileWriter( destFile );
            
            String line = reader.readLine();
            while ( line != null ) {
                out.append( line + "\n" ); //$NON-NLS-1$
                line = reader.readLine();
            }
            
        } catch ( Exception e ) {
            
        } finally {
            try { in.close(); } catch ( Exception e ) { }
            try { out.close(); } catch ( Exception e ) { }
        }
    }
}