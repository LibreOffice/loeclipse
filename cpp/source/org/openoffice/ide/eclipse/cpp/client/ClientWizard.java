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
import java.text.MessageFormat;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.ui.wizards.CCProjectWizard;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.ui.IWorkbenchPage;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.core.utils.WorkbenchHelper;
import org.openoffice.ide.eclipse.cpp.Activator;
import org.openoffice.ide.eclipse.cpp.CppProjectHandler;


public class ClientWizard extends CCProjectWizard {

    private static final String SRC_DIR_NAME = "src"; //$NON-NLS-1$
    private static final String CLIENT_FILE = "client.cxx"; //$NON-NLS-1$
    
    private UnoConnectionPage mCnxPage;
    private IWorkbenchPage mActivePage;

    public ClientWizard() {
        super( );
        setWindowTitle( "UNO Client C++ project" );
        mActivePage = WorkbenchHelper.getActivePage();
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
    
    @Override
    public boolean performFinish() {
        boolean finished = super.performFinish();
        
        try {
            IOOo ooo = mCnxPage.getOoo();
            ISdk sdk = mCnxPage.getSdk();

            // Copy the helper files in the helper source dir
            IFolder srcDir = newProject.getFolder( SRC_DIR_NAME );
            srcDir.create( true, true, null );

            copyResource( "connection.hxx", srcDir, new String() ); //$NON-NLS-1$
            copyResource( "connection.cxx", srcDir, new String() ); //$NON-NLS-1$
            
            // TODO Make that configurable in the wizard
            String cnxInitCode = "SocketConnection cnx( 8080, \"localhost\" );"; //$NON-NLS-1$
            copyResource( CLIENT_FILE, srcDir, cnxInitCode );

            srcDir.refreshLocal( IResource.DEPTH_ONE, null );

            // Add the helper dir to the source path entries
            ICProject cprj = CoreModel.getDefault().getCModel().getCProject( newProject.getName() );
            IPathEntry[] entries = CoreModel.getRawPathEntries( cprj );
            IPathEntry[] newEntries = new IPathEntry[ entries.length + 1 ];
            System.arraycopy( entries, 0, newEntries, 0, entries.length );
            newEntries[ newEntries.length - 1 ] = CoreModel.newSourceEntry( srcDir.getFullPath() );
            CoreModel.setRawPathEntries( cprj, newEntries, null );

            CppProjectHandler.addOOoDependencies( ooo, sdk, newProject );
            
            // TODO Setup the launch config
            
            selectAndReveal( srcDir.getFile( CLIENT_FILE ) );
            WorkbenchHelper.showFile( srcDir.getFile( CLIENT_FILE ), mActivePage );
        
        } catch ( Exception e ) {
            PluginLogger.error( "Couldn't set OOo Client config", e );
        }
        
        return finished;
    }

    private void copyResource(String pResName, IContainer pSrcDir, String pReplacement ) {
        InputStream in = this.getClass().getResourceAsStream( pResName );
        File destFile = new File( pSrcDir.getLocation().toFile(), pResName );
        
        FileWriter out = null;
        try {
            
            LineNumberReader reader = new LineNumberReader( new InputStreamReader( in ) );
            out = new FileWriter( destFile );
            
            String line = reader.readLine();
            while ( line != null ) {
                out.append( MessageFormat.format( line, pReplacement ) + "\n" ); //$NON-NLS-1$
                line = reader.readLine();
            }
            
        } catch ( Exception e ) {
            
        } finally {
            try { in.close(); } catch ( Exception e ) { }
            try { out.close(); } catch ( Exception e ) { }
        }
    }
}