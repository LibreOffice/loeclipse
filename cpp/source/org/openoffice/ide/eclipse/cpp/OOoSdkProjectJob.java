package org.openoffice.ide.eclipse.cpp;

import java.io.File;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.openoffice.ide.eclipse.core.LogLevels;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;

/**
 * Eclipse Job generating an OOo SDK project to use for the C++ UNO 
 * projects dependencies.
 * 
 * This job also generates the headers from the UNO types defined in 
 * the OpenOffice.org instance. 
 * 
 * @author cbosdonnat
 *
 */
public class OOoSdkProjectJob extends Job {
    
    private static final String INCLUDES_DIR = "includes"; //$NON-NLS-1$
    private static final String PRJ_NAME_PATTERN = "{0} Cpp SDK"; //$NON-NLS-1$
    private static final String LIBS_DIR = "libs"; //$NON-NLS-1$
    
    private IOOo mOOo;
    private ISdk mSdk;
    
    /**
     * Constructor.
     * 
     * @param pOOo the OOo for which to create the SDK project
     * @param pSdk the OOo SDK for which to create the SDK project
     */
    public OOoSdkProjectJob( IOOo pOOo, ISdk pSdk ) {
        super( Messages.getString("OOoSdkProjectJob.Title") + pOOo.getName() ); //$NON-NLS-1$
        mOOo = pOOo;
        mSdk = pSdk;
    }

    /**
     * Get the includes folder in the SDK project corresponding to the 
     * given OOo instance. 
     * 
     * @param pOOo the OOo instance for which to get the includes folder
     * 
     * @return the include folder in the workspace.
     */
    public static IFolder getIncludes( IOOo pOOo ) {
        
        String prjName = MessageFormat.format( PRJ_NAME_PATTERN, pOOo.getName() );
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( prjName );
        
        return prj.getFolder( INCLUDES_DIR );
    }
    
    /**
     * Get the libraries folder in the SDK project corresponding to the 
     * given OOo instance. 
     * 
     * @param pOOo the OOo instance for which to get the libraries folder
     * 
     * @return the libraries folder in the workspace.
     */
    public static IFolder getLibraries( IOOo pOOo ) {
        
        String prjName = MessageFormat.format( PRJ_NAME_PATTERN, pOOo.getName() );
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( prjName );
        
        return prj.getFolder( LIBS_DIR );
    }
    
    @Override
    protected IStatus run( IProgressMonitor pMonitor ) {
        
        IStatus status = new Status( IStatus.OK, Activator.PLUGIN_ID, 
                Messages.getString("OOoSdkProjectJob.OkStatus") ); //$NON-NLS-1$
        
        try {
            // Create the OOo SDK project
            String prjName = MessageFormat.format( PRJ_NAME_PATTERN, mOOo.getName() );
            IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( prjName );
            if ( !prj.exists() ) {
                prj.create( pMonitor );   
            }
            
            // Open the project if not opened
            if (!prj.isOpen()) {
                prj.open( pMonitor );
                PluginLogger.debug("Project opened"); //$NON-NLS-1$
            }
            
            // Link the URE libs here
            createLibLinks( prj, pMonitor );
            
            // Create the includes
            createIncludes( prj, pMonitor );
            
        } catch ( Exception e ) {
            status = new Status( IStatus.ERROR, Activator.PLUGIN_ID, 
                    Messages.getString("OOoSdkProjectJob.FailedStatus"), e ); //$NON-NLS-1$
        }
        
        return status;
    }

    /**
     * Utility method creating the links to the libraries in the SDK project.
     * 
     * @param pPrj the project in which to create the links
     * @param pMonitor a progress monitor
     * 
     * @throws CoreException if anything wrong happens
     */
    private void createLibLinks(IProject pPrj, IProgressMonitor pMonitor) throws CoreException {
        
        String os = Platform.getOS();
        if ( os.equals( Platform.OS_LINUX ) || os.equals( Platform.OS_SOLARIS ) 
                || os.equals( Platform.OS_MACOSX ) ) {
            // Create the link folder
            IFolder folder = pPrj.getFolder( LIBS_DIR );

            if ( !folder.exists() ) {
                folder.create( true, true, pMonitor );
            }
            
            String ext = "so"; //$NON-NLS-1$
            if ( os.equals( Platform.OS_MACOSX ) ) {
                ext = "dylib"; //$NON-NLS-1$
            }
             
            String[] paths = mOOo.getLibsPath();
            for (String path : paths) {
                Path dirPath = new Path( path );
                // Check for the libs to link
                for ( String lib : CppProjectHandler.LIBS ) {
                    String pattern = "lib{0}.{1}"; //$NON-NLS-1$
                    String libname = MessageFormat.format( pattern, lib, ext );
                    String syslibname = libname + ".3"; //$NON-NLS-1$
                    
                    File libFile = new File( dirPath.toFile(), syslibname );
                    if ( libFile.exists() ) {
                        String dest = folder.getFile( libname ).getLocation().toOSString();
                        String orig = libFile.getAbsolutePath();
                        
                        // Run ln to link the files: present on all *NIX platforms
                        String[] command = { 
                            "ln", "-s", //$NON-NLS-1$ //$NON-NLS-2$
                            orig, dest
                        };
                        try {
                            Process proc = Runtime.getRuntime().exec( command );
                            
                            StringBuffer buf = getErrorString( proc );
                            if ( !buf.toString().trim().equals( new String( ) ) ) {
                                String msg = Messages.getString("OOoSdkProjectJob.LinkError") + //$NON-NLS-1$ 
                                    libname + "\n"; //$NON-NLS-1$
                                msg += buf.toString();
                                PluginLogger. error( msg );
                            }
                            
                            proc.waitFor();
                            
                        } catch ( Exception e ) {
                            PluginLogger.error( Messages.getString("OOoSdkProjectJob.LinkError") + //$NON-NLS-1$ 
                                    libname, e );
                        }
                    }
                }
            }
            folder.refreshLocal( IResource.DEPTH_ONE, pMonitor );
        }
    }

    /**
     * Utility method creating the headers in the SDK project.
     * 
     * @param pProject the project in which to create the headers
     * @param pMonitor a progress monitor
     * 
     * @throws Exception if anything wrong happens
     */
    private void createIncludes( IProject pProject, IProgressMonitor pMonitor) throws Exception {
        // Create the destination folder if needed 
        IFolder folder = pProject.getFolder( INCLUDES_DIR );
        if ( !folder.exists() ) {
            folder.create( true, true, pMonitor );
        }

        // Generate the include into the new project 
        String[] paths = mOOo.getTypesPath();
        String oooTypesArgs = ""; //$NON-NLS-1$
        for (String path : paths) {
            IPath ooTypesPath = new Path (path);
            oooTypesArgs += " \"" + ooTypesPath.toOSString() + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        }

        String cmdPattern = "cppumaker -T\"*\" -Gc -BUCR -O \"{0}\" {1}"; //$NON-NLS-1$
        String command = MessageFormat.format( cmdPattern, 
                folder.getLocation().toFile().getAbsolutePath() , oooTypesArgs ); 
        Process process = mSdk.runToolWithEnv( pProject, mOOo, command, new String[0], pMonitor );

        StringBuffer buf = getErrorString( process );
        if ( !buf.toString().trim().equals( new String( ) ) ) {
            String msg = Messages.getString("OOoSdkProjectJob.IncludesError"); //$NON-NLS-1$
            msg += buf.toString();
            PluginLogger. error( msg );
        }

        process.waitFor();
        
        // Refresh the folder
        folder.refreshLocal( IResource.DEPTH_INFINITE, pMonitor );
    }

    /**
     * Get the error string from the <code>cppumaker</code> process.
     * 
     * @param pProcess the running process
     * 
     * @return the error string buffer
     */
    private StringBuffer getErrorString(Process pProcess) {
        StringBuffer buf = new StringBuffer();
        
        LineNumberReader lineReader = new LineNumberReader(
                new InputStreamReader(pProcess.getErrorStream()));

        try {
            // Only for debugging purpose
            if (PluginLogger.isLevel(LogLevels.DEBUG)) {
                String line = lineReader.readLine();
                while (null != line) {
                    buf.append( line + "\n" ); //$NON-NLS-1$
                    line = lineReader.readLine();
                }
            }
        } catch ( Exception e ) {
            // Not reporting errors in error reader
        }
        return buf;
    }

}
