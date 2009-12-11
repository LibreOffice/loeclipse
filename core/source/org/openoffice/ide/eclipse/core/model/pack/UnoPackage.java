/*************************************************************************
 *
 * $RCSfile: UnoPackage.java,v $
 *
 * $Revision: 1.7 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:30 $
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
package org.openoffice.ide.eclipse.core.model.pack;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.Messages;
import org.openoffice.ide.eclipse.core.model.ProjectsManager;
import org.openoffice.ide.eclipse.core.utils.FileHelper;
import org.openoffice.ide.eclipse.core.utils.ZipContent;

/**
 * This class represents a UNO package and should be used to create a UNO package.
 * 
 * <p>In the same way than ant jar target does, the UNO package is defined by an
 * output file and a root directory. All the file that will be added to the
 * package will have to be contained in this directory or one of its children.</p>
 *
 * @author cedricbosdo
 *
 */
public class UnoPackage {

    public static final String MANIFEST_PATH = "manifest.xml"; //$NON-NLS-1$
    
    public static final String ZIP = "zip"; //$NON-NLS-1$
    public static final String UNOPKG = "uno.pkg"; //$NON-NLS-1$
    public static final String OXT = "oxt"; //$NON-NLS-1$

    private static final String BASIC_LIBRARY_INDEX = "script.xlb"; //$NON-NLS-1$
    private static final String DIALOG_LIBRARY_INDEX = "dialog.xlb"; //$NON-NLS-1$
    
    private IProject mPrj;
    private File mDestination;
    private boolean mBuilding = false;
    
    private HashMap<String, ZipContent> mZipEntries = new HashMap<String, ZipContent>();
    private ManifestModel mManifest = new ManifestModel();
    private ArrayList< IPath > mToClean = new ArrayList<IPath>( );

    private IFile mReadManifestFile;
    private IFile mSaveManifestFile;

    private Runnable mDeployJob;
    
    /**
     * Create a new package object. 
     * 
     * <p>The extension has be one of the following. The default extension is 
     * {@link #ZIP}. If the extension is invalid or missing, the file will be
     * renamed in <code>.zip</code>.
     * <ul>
     *      <li>{@link #ZIP}</li>
     *   <li>{@link #UNOPKG}</li>
     *   <li>{@link #OXT}</li>
     * </ul>
     * </p>
     * 
     * @param pOut the file of the package.
     * @param pPrj the project to export 
     */
    public UnoPackage( File pOut, IProject pPrj ) {
        if (! (pOut.getName().endsWith(ZIP) || pOut.getName().endsWith(UNOPKG) || 
                pOut.getName().endsWith(OXT)) ) {
            int pos = pOut.getName().lastIndexOf("."); //$NON-NLS-1$
            if (pos > 0) {
                String name = pOut.getName().substring(0, pos);
                pOut = new File(pOut.getParentFile(), name + "." + ZIP); //$NON-NLS-1$
            } else {
                pOut = new File(pOut.getParentFile(), pOut.getName() + ".zip"); //$NON-NLS-1$
            }
        }
        
        mDestination = pOut;
        mPrj = pPrj;
    }
    
    /**
     * Cleans up the data structure. There is no need to call this method if the
     * package has been closed using {@link #close()}
     */
    public void dispose() {
        mDestination = null;
        mZipEntries.clear();
    }
    
    /**
     * @return the manifest.xml model contained in the package
     */
    public ManifestModel getManifestModel( ) {
        return mManifest;
    }
    
    /**
     * Set the manifest.xml file to use for the package: setting this value will
     * skip the manifest.xml file automatic generation.
     * 
     * <p><strong>Setting this value to a non-existing file is the same as setting it with
     * <code>null</code>: the default value will be used.</strong></p>
     * 
     * @param pFile the file to read.
     * 
     * @see #MANIFEST_PATH The default path value relative to the project
     */
    public void setReadManifestFile( IFile pFile ) {
        if ( pFile != null && pFile.exists( ) ) {
            mReadManifestFile = pFile;
        }
    }
    
    /**
     * @param pFile the file where to save the manifest.xml
     */
    public void setSaveManifestFile( IFile pFile ) {
        if ( pFile != null ) {
            mSaveManifestFile = pFile;
        }
    }

    /**
     * @param pJob the job to run to deploy the package, <code>null</code> 
     *          can be used to remove any previously set deploy job.
     */
    public void setDeployJob( Runnable pJob ) {
        mDeployJob = pJob;
    }
    
    /**
     * Add a file or directory to the package.
     * 
     * <p>This method doesn't know about the different languages
     * contributions to the <code>manifest.xml</code> file.</p>
     * 
     * @param pContent the file or folder to add
     */
    public void addContent(IResource pContent) {
        if ( pContent instanceof IFile ) {
            IFile file = (IFile)pContent;
            if (pContent.getName().endsWith(".xcs")) { //$NON-NLS-1$
                addConfigurationSchemaFile( file );
            } else if (pContent.getName().endsWith(".xcu")) { //$NON-NLS-1$
                addConfigurationDataFile( file );
            } else if (pContent.getName().endsWith(".rdb")) { //$NON-NLS-1$
                addTypelibraryFile( file , "RDB"); //$NON-NLS-1$
            } else {
                addOtherFile( file );
            }
        } else if ( isBasicLibrary( pContent ) ) {
            addBasicLibraryFile( ( IFolder ) pContent );
        } else if ( isDialogLibrary( pContent ) ) {
            addDialogLibraryFile( ( IFolder ) pContent );
        } else if ( pContent instanceof IContainer ) {
            // Recurse on the directory
            IContainer container = (IContainer) pContent;
            IResource[] children;
            try {
                children = container.members();
                for (IResource child : children) {
                    addContent( child );
                }
            } catch (CoreException e) {
            }
        }
    }

    /**
     * Add a uno component file, for example a jar, shared library or python file
     * containing the uno implementation. The type of the file defines the 
     * language and should be given as defined in the OOo Developer's Guide, like
     * Java, native, Python.
     * 
     * @param pFile the file to add to the package
     * @param pType the type of the file to add.
     * 
     * @see #addComponentFile(IFile, String, String) for platform support
     */
    public void addComponentFile(IFile pFile, String pType) {
        addComponentFile(pFile, pType, null);
    }
    
    /**
     * Add a uno component file, for example a jar, shared library or python file
     * containing the uno implementation. 
     * 
     * <p>The type of the file defines the language and should be given as defined
     * in the OOo Developer's Guide, like Java, native, Python.</p>
     * 
     * @param pFile the file to add to the package
     * @param pType the type of the file to add.
     * @param pPlatform optional parameter to use only with native type. Please
     *         refer to the OOo Developer's Guide for more information.
     */
    public void addComponentFile(IFile pFile, String pType, String pPlatform) {
        // Do not change the extension from now
        initializeOutput();
    
        // create the manifest entry
        mManifest.addComponentFile( pFile, pType, pPlatform );
        
        // create the ZipContent
        addZipContent( pFile.getProjectRelativePath().toString(), pFile );
    }
    
    /**
     * Add a type library to the package. 
     * 
     * <p>Note that by some strange way, a jar dependency can be added 
     * in the package as a type library like RDB files.</p>
     *  
     * @param pFile the file to add 
     * @param pType the type of the file as specified in the OOo Developer's Guide
     */
    public void addTypelibraryFile(IFile pFile, String pType) {
        // Do not change the extension from now
        initializeOutput();
        
        // create the manifest entry
        mManifest.addTypelibraryFile( pFile, pType );
        
        // create the ZipContent
        addZipContent( pFile.getProjectRelativePath().toString(), pFile );
    }
    
    /**
     * Add a basic library to the package. 
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     * 
     * @param pDir the directory of the basic library.
     */
    public void addBasicLibraryFile(IFolder pDir) {
        // Do not change the extension from now
        initializeOutput();

        mManifest.addBasicLibrary( pDir );
        addZipContent( pDir.getProjectRelativePath().toString(), pDir );
    }
    
    /**
     * Add a dialog library to the package. 
     * 
     * <p>Even if this method may not be used, it is possible.</p>
     * 
     * @param pDir the directory of the dialog library.
     */
    public void addDialogLibraryFile(IFolder pDir) {
        // Do not change the extension from now
        initializeOutput();

        mManifest.addDialogLibrary( pDir );
        addZipContent( pDir.getProjectRelativePath().toString(), pDir );
    }
    
    /**
     * Add an xcu configuration to the package.
     * 
     * @param pFile the xcu file to add
     */
    public void addConfigurationDataFile(IFile pFile) {
        // Do not change the extension from now
        initializeOutput();

        mManifest.addConfigurationDataFile( pFile );
        addZipContent( pFile.getProjectRelativePath().toString(), pFile );
    }
    
    /**
     * Add an xcs configuration to the package.
     * 
     * @param pFile the xcs file to add
     */
    public void addConfigurationSchemaFile(IFile pFile) {
        // Do not change the extension from now
        initializeOutput();

        mManifest.addConfigurationSchemaFile( pFile );
        addZipContent( pFile.getProjectRelativePath().toString(), pFile );
    }
    
    /**
     * Add a localized description of the package.
     * 
     * @param pFile the file containing the description for that locale
     * @param pLocale the locale of the description. Can be <code>null</code>.
     */
    public void addPackageDescription(IFile pFile, Locale pLocale) {
        mManifest.addDescription( pFile, pLocale );
        addZipContent( pFile.getProjectRelativePath().toString(), pFile );
    }
    
    /**
     * Adds a file or directory to the package but do not include it in the 
     * manifest. 
     * 
     * <p>This could be used for example for images.</p>
     * 
     * @param pFile the file or directory to add.
     */
    public void addOtherFile(IFile pFile) {
        // Do not change the extension from now 
        initializeOutput();
        
        addZipContent( pFile.getProjectRelativePath().toString(), pFile );
    }
    
    /**
     * Writes the package on the disk and cleans up the data. The UnoPackage
     * instance cannot be used after this operation: it should unreferenced.
     * 
     * @param pMonitor the progress monitor
     *
     * @return the file of the package or <code>null</code> if nothing happened.
     */
    public File close( IProgressMonitor pMonitor ) {
        File result = null;
        
        if (mBuilding) {
            try {
                IFile manifestFile = mReadManifestFile;
                if ( manifestFile == null ) {
                    // Write the manifest if it doesn't exist
                    manifestFile = getSaveManifestFile();
                    FileOutputStream writer = new FileOutputStream( manifestFile.getLocation().toFile() );
                    mManifest.write( writer );
                    writer.close();
                    manifestFile.refreshLocal( IResource.DEPTH_ZERO, null );
                }
                
                // Write the ZipContent
                FileOutputStream out = new FileOutputStream(mDestination);
                ZipOutputStream zipOut = new ZipOutputStream(out);
                
                Iterator<ZipContent> entries = mZipEntries.values().iterator();
                while (entries.hasNext()) {
                    ZipContent content = entries.next();
                    content.writeContentToZip(zipOut);
                }
                
                // Add the manifest to the zip
                ZipContent manifest = new ZipContent("META-INF/manifest.xml", manifestFile); //$NON-NLS-1$
                manifest.writeContentToZip(zipOut);
                
                // close the streams
                zipOut.close();
                out.close();
                
            } catch (Exception e) {
                PluginLogger.error(Messages.getString("UnoPackage.PackageCreationError"), e); //$NON-NLS-1$
            }
            
            result = mDestination;
    
            cleanResources();
            
            // Deploy the package if a job is set
            if ( mDeployJob != null ) {
                Display.getDefault().asyncExec( mDeployJob );
            }
            
            // Refresh the project and return the status
            try {
                mPrj.refreshLocal( IResource.DEPTH_INFINITE, pMonitor );
            } catch ( Exception e ) {
            }
            
            dispose();
        }
        return result;
    }

    /**
     * @return a list of the files that are already queued for addition 
     *         to the package.
     */
    public List<IResource> getContainedFiles() {
        ArrayList<IResource> files = new ArrayList<IResource>(mZipEntries.size());
        for (ZipContent content : mZipEntries.values()) {
            files.add( content.getFile() );
        }
        return files;
    }
    
    /**
     * Checks if the resource is contained in the UNO package.
     * 
     * @param pRes the resource to check
     * @return <code>true</code> if the resource is contained in the package
     */
    public static boolean isContainedInPackage(IResource pRes) {
        boolean contained = false;
        
        String prjName = pRes.getProject().getName();
        IUnoidlProject prj = ProjectsManager.getProject(prjName); 
        
        if (prj != null) {
            
            File outputDir = new File(System.getProperty("user.home")); //$NON-NLS-1$
            
            File dest = new File(outputDir, prj.getName() + ".zip"); //$NON-NLS-1$
            UnoPackage unoPackage = UnoidlProjectHelper.createMinimalUnoPackage(prj, dest);
            
            List<IResource> files = unoPackage.getContainedFiles();
            int i = 0;
            while (i < files.size() && !contained) {
                IResource res = files.get(i);
                if ( res.getLocation().equals( pRes.getLocation() ) ) {
                    contained = true;
                }
                i++;
            }
            unoPackage.dispose();
        }
        
        return contained;
    }
    
    /**
     * Get the list of the files contained in the minimal UNO package. 
     * 
     * @param pPrj the project for which to get the minimal resources
     * @return the list of files
     */
    public static List<IResource> getContainedFile(IProject pPrj) {
        ArrayList<IResource> resources = new ArrayList<IResource>();
        
        String prjName = pPrj.getName();
        IUnoidlProject unoprj = ProjectsManager.getProject(prjName); 
        
        if (unoprj != null) {
            
            File outputDir = new File(System.getProperty("user.home")); //$NON-NLS-1$
            
            File dest = new File(outputDir, pPrj.getName() + ".zip"); //$NON-NLS-1$
            UnoPackage unoPackage = UnoidlProjectHelper.createMinimalUnoPackage(unoprj, dest);
            
            resources.addAll( unoPackage.getContainedFiles() );
            
            unoPackage.dispose();
        }
        
        return resources;
    }
    
    /**
     * Add the path to a resource to clean after having exported the package.
     * The resource won't be cleaned if the package isn't exported.
     * 
     * @param pPath the path to the resource to clean.
     */
    public void addToClean( IPath pPath ) {
        mToClean.add( pPath );
    }
    
    /**
     * Creates the main elements for the package creation. 
     * 
     * <p>After this step, the extension cannot be changed. Calling this 
     * method when the package has already been initialized does nothing.</p>
     * 
     */
    private void initializeOutput() {
        mBuilding = true;
    }
    
    /**
     * Recursively add the file or directory to the Zip entries.
     * 
     * @param pRelativePath the relative path of the file to add
     * @param pFile the file or directory to add
     */
    private void addZipContent(String pRelativePath, IResource pFile) {
        if (pRelativePath != null) {
            if ( pFile instanceof IContainer ) {
                // Add all the children
                try {
                    IResource[] children = ((IContainer)pFile).members();
                    for (IResource child : children) {
                        addZipContent( pRelativePath + "/" + child.getName(), child ); //$NON-NLS-1$
                    }
                } catch ( Exception e ) {
                }
            } else {
                ZipContent content = new ZipContent(pRelativePath, pFile);
                mZipEntries.put(pRelativePath, content);
            }
        }
    }
    
    /**
     * Clean the resources added using {@link #addToClean(IPath)}.
     */
    private void cleanResources() {
        for ( IPath path : mToClean ) {
            FileHelper.remove( new File( path.toOSString() ) );
        }
        
        // Remove the default manifest file if needed
        IFile manifestFile = getSaveManifestFile();
        if ( mSaveManifestFile == null && 
                !manifestFile.equals( mReadManifestFile ) && 
                manifestFile.exists() ) {
            try {
                manifestFile.delete( true, null );
            } catch ( Exception e ) {
            }
        }
    }
    
    /**
     * Checks if the resource is a dialog library.
     * 
     * @param pRes the resource to check
     * 
     * @return <code>true</code> if the resource is a dialog library, 
     *          <code>false</code> in any other case
     */
    private boolean isDialogLibrary( IResource pRes ) {
        boolean result = false;
        if ( pRes instanceof IFolder ) {
            IFolder folder = ( IFolder ) pRes;
            result = folder.getFile( DIALOG_LIBRARY_INDEX ).exists();
        }
        return result;
    }

    /**
     * Checks if the resource is a basic library.
     * 
     * @param pRes the resource to check
     * 
     * @return <code>true</code> if the resource is a basic library, 
     *          <code>false</code> in any other case
     */
    private boolean isBasicLibrary( IResource pRes ) {
        boolean result = false;
        if ( pRes instanceof IFolder ) {
            IFolder folder = ( IFolder ) pRes;
            result = folder.getFile( BASIC_LIBRARY_INDEX ).exists();
        }
        return result;
    }
    
    /**
     * @return the manifest file to write either defined by the setter or 
     *          the default value.
     */
    private IFile getSaveManifestFile( ) {
        IFile file = mSaveManifestFile;
        if ( file == null ) {
            file = mPrj.getFile( MANIFEST_PATH );
        }
        return file;
    }
}
