/*************************************************************************
 *
 * $RCSfile: SDK.java,v $
 *
 * $Revision: 1.11 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:48 $
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
package org.openoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.openoffice.ide.eclipse.core.OOEclipsePlugin;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.ITableElement;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.core.model.config.InvalidConfigException;
import org.openoffice.ide.eclipse.core.model.utils.SystemHelper;

/**
 * Class representing a SDK instance used in the UNO-IDL projects.
 * 
 * @author cedricbosdo
 *
 */
public class SDK implements ISdk, ITableElement {
    
    public static final String NAME = "__sdk_name"; //$NON-NLS-1$
    
    public static final String PATH = "__sdk_path"; //$NON-NLS-1$
    
    /**
     * private constant that holds the sdk build id key in the dk.mk file.
     */
    private static final String K_SDK_BUILDID = "BUILDID"; //$NON-NLS-1$
    
    /**
     * private constant that hold the name of the sdk config file (normaly dk.mk)
     * This is set to easily change if there are future sdk organization changes.
     */
    private static final String F_DK_CONFIG = "dk.mk"; //$NON-NLS-1$

    private static final String INCLUDE = "include"; //$NON-NLS-1$
    private static final String LIB = "lib"; //$NON-NLS-1$

    
    
    /* SDK Members */
    
    private String mBuildId;
    private String mSdkHome;
    
    /**
     * Standard and only constructor for the SDK object. The name and buildId will be fetched
     * from the $(SDK_HOME)/settings/dk.mk properties file.
     * 
     * @param pSdkHome absolute path of the SDK root
     * 
     * @throws InvalidConfigException if the path doesn't points to a valid 
     *      OpenOffice.org SDK installation directory.
     */
    public SDK (String pSdkHome) throws InvalidConfigException {
        
        // Sets the path to the SDK
        setHome(pSdkHome);
    }

    //----------------------------------------------------- ISdk Implementation
    
    /**
     * {@inheritDoc}
     */
    public void setHome(String pHome) throws InvalidConfigException {
        try {
        
            // Get the file representing the given sdkHome
            Path homePath = new Path(pHome);
            File homeFile = homePath.toFile();
            
            // First check the existence of this directory
            if (homeFile.exists() && homeFile.isDirectory()) {
                
                /**
                 * <p>If the provided sdk home does not contains <li><code>idl</code></li>
                 * <li><code>settings</code></li> directories, the provided sdk is considered as invalid</p>
                 */
                
                // test for the idl directory
                checkIdlDir(homeFile);
                
                // test for the settings directory
                File settingsFile = checkSettingsDir(homeFile);
                
                // test for the uno-skeletonmaker tool
                String binName = "uno-skeletonmaker"; //$NON-NLS-1$
                if (Platform.getOS().equals(Platform.OS_WIN32)) {
                    binName += ".exe";  //$NON-NLS-1$
                }
                if (!getBinPath(pHome).append(binName).toFile().exists()) {
                    throw new InvalidConfigException(
                            Messages.getString("SDK.MinSdkVersionError"), //$NON-NLS-1$
                            InvalidConfigException.INVALID_SDK_HOME);
                }
                
                // If the settings and idl directory both exists, then try to fetch the name and buildId from
                // the settings/dk.mk properties file
                readSettings(settingsFile);
                this.mSdkHome = pHome;
                
            } else {
                throw new InvalidConfigException(
                        Messages.getString("SDK.NoDirectoryError"), //$NON-NLS-1$
                        InvalidConfigException.INVALID_SDK_HOME);
            }
        } catch (Throwable e) {
            
            if (e instanceof InvalidConfigException) {
                
                // Rethrow the InvalidSDKException
                InvalidConfigException exception = (InvalidConfigException)e;
                throw exception;
            } else {
                PluginLogger.error("Unexpected error during SDK cration", e); //$NON-NLS-1$
                // Unexpected exception thrown
                throw new InvalidConfigException(
                        Messages.getString("SDK.UnexpectedError"),  //$NON-NLS-1$
                        InvalidConfigException.INVALID_SDK_HOME, e);
            }
        }
    }
    
    /**
     * Checks if the <code>settings</code> directory is contained in the SDK 
     * installation path.
     * 
     * @param pHomeFile the SDK installation file handle to check
     * 
     * @return the settings file found
     * 
     * @throws InvalidConfigException the the <code>settings</code> isn't found
     */
    private File checkSettingsDir(File pHomeFile) throws InvalidConfigException {
        File settingsFile = new File(pHomeFile, "settings"); //$NON-NLS-1$
        if (! (settingsFile.exists() && settingsFile.isDirectory()) ) {
            throw new InvalidConfigException(
                    Messages.getString("SDK.NoSettingsDirError"), //$NON-NLS-1$
                    InvalidConfigException.INVALID_SDK_HOME);
        }
        return settingsFile;
    }

    /**
     * Checks if the <code>idl</code> directory is contained in the SDK 
     * installation path.
     * 
     * @param pHomeFile the SDK installation file handle to check
     * 
     * @throws InvalidConfigException the the <code>idl</code> isn't found
     */
    private void checkIdlDir(File pHomeFile) throws InvalidConfigException {
        File idlFile = new File(pHomeFile, "idl"); //$NON-NLS-1$
        if (! (idlFile.exists() && idlFile.isDirectory()) ) {
            throw new InvalidConfigException(
                    Messages.getString("SDK.NoIdlDirError"),  //$NON-NLS-1$
                    InvalidConfigException.INVALID_SDK_HOME);
        }        
    }

    /**
     * {@inheritDoc}
     */
    public String getId() {
        String result = null;
        
        String[] splits = mBuildId.split("\\(.*\\)"); //$NON-NLS-1$
        if (splits.length > 0) {
            result = splits[0];
        }
        
        return result;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getHome() {
        return mSdkHome;
    }
    
    /**
     * {@inheritDoc}
     */
    public IPath getBinPath() {
        return getBinPath(getHome());
    }
    
    /**
     * {@inheritDoc}
     */
    public IPath getIncludePath() {
        return new Path( getHome() ).append( INCLUDE );
    }
    
    /**
     * {@inheritDoc}
     */
    public IPath getLibPath() {
        return new Path( getHome() ).append( LIB );
    }
    
    /**
     * Get the path to the executable files of the SDK.
     * 
     * @param pHome the SDK installation path 
     * @return the path to the binaries folder depending on the platform
     */
    private IPath getBinPath(String pHome) {
        IPath path = null;
        
        // First check for the 3.0 SDK structure
        path = new Path(pHome).append("/bin"); //$NON-NLS-1$
        if (!path.toFile().isDirectory()) {
            path = getSDK2BinPath(pHome);
        }
        
        return path;
    }
    
    /**
     * Get the binaries path for the SDK 2.x structure.
     * 
     * @param pHome the SDK home path
     * 
     * @return the path to the bin directory
     */
    private IPath getSDK2BinPath(String pHome) {
        IPath path = null;
        
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            path = new Path(pHome).append("/windows/bin/"); //$NON-NLS-1$
        } else if (Platform.getOS().equals(Platform.OS_LINUX)) {
            path = new Path(pHome).append("/linux/bin"); //$NON-NLS-1$
        } else if (Platform.getOS().equals(Platform.OS_SOLARIS)) {
            if (Platform.getOSArch().equals(Platform.ARCH_SPARC)) {
                path = new Path(pHome).append("/solsparc/bin"); //$NON-NLS-1$
            } else if (Platform.getOSArch().equals(Platform.ARCH_X86)) {
                path = new Path(pHome).append("/solintel/bin"); //$NON-NLS-1$
            }
        } else if (Platform.getOS().equals(Platform.OS_MACOSX)) {
            path = new Path(pHome).append("/macosx/bin"); //$NON-NLS-1$
        }
        
        return path;
    }

    /**
     * {@inheritDoc}
     */
    public Process runTool(IUnoidlProject pProject, 
            String pShellCommand, IProgressMonitor pMonitor) {
        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject( pProject.getName() );
        return runToolWithEnv(prj, pProject.getOOo(), pShellCommand, new String[0], pMonitor);
    }
    
    /**
     * {@inheritDoc}
     */
    public Process runToolWithEnv(IProject pProject, IOOo pOOo, 
            String pShellCommand, String[] pEnv, IProgressMonitor pMonitor) {
        
        Process process = null;
        
        try {
            if ( null != pOOo ) {
                
                // Get the environment variables and copy them. Needs Java 1.5
                String[] sysEnv = SystemHelper.getSystemEnvironement();
                
                String[] vars = mergeVariables(sysEnv, pEnv);
               
                vars = updateEnvironment(vars, pOOo);
                
                // Run only if the OS and ARCH are valid for the SDK
                if (null != vars) {
                    File projectFile = pProject.getLocation().toFile();
                    process = SystemHelper.runTool(pShellCommand, vars, projectFile);
                }
            }
            
        } catch (IOException e) {
            // Error while launching the process 
            
            MessageDialog dialog = new MessageDialog(
                    OOEclipsePlugin.getDefault().getWorkbench().
                        getActiveWorkbenchWindow().getShell(),
                    Messages.getString("SDK.PluginError"), //$NON-NLS-1$
                    null,
                    Messages.getString("SDK.ProcessError"), //$NON-NLS-1$
                    MessageDialog.ERROR,
                    new String[]{Messages.getString("SDK.Ok")}, 0); //$NON-NLS-1$
            dialog.setBlockOnOpen(true);
            dialog.create();
            dialog.open();
            
        } catch (SecurityException e) {
            // SubProcess creation unauthorized
            
            MessageDialog dialog = new MessageDialog(
                    OOEclipsePlugin.getDefault().getWorkbench().
                            getActiveWorkbenchWindow().getShell(),
                    Messages.getString("SDK.PluginError"), //$NON-NLS-1$
                    null,
                    Messages.getString("SDK.ProcessError"), //$NON-NLS-1$
                    MessageDialog.ERROR,
                    new String[]{Messages.getString("SDK.Ok")},    0); //$NON-NLS-1$
            dialog.setBlockOnOpen(true);
            dialog.create();
            dialog.open();
        } catch (Exception e) {
            PluginLogger.error(e.getMessage(), null);
        }
        
        return process;
    }
    
    /**
     * Merge two environment variables arrays.
     * 
     * <p>The modified array is the first one: the variables defined in the
     * second parameter will be merged into the first array.</p>
     * 
     * @param pBaseEnv the array to modify
     * @param pToMergeEnv the array containing the environment variables to merge
     * 
     * @return the merged environment variables.
     */
    private String[] mergeVariables(String[] pBaseEnv, String[] pToMergeEnv) {
        //TODO cdan should add a test for this method (all entries in pToMergeEnv should be found in the result) 
        // PATH merging
        String[] vars = pBaseEnv;
        for (int i = 0; i < pToMergeEnv.length; i++) {
            String envi = pToMergeEnv[i];
            Matcher m = Pattern.compile("([^=]+)=(.*)").matcher(envi); //$NON-NLS-1$
            if (m.matches()) {
                String name = m.group(1);
                String value = m.group(2);
                vars = SystemHelper.addEnv(vars, name, value, SystemHelper.PATH_SEPARATOR);
            }
        }
        return vars;
    }

    //-------------------------------------------- ITableElement Implementation
    
    /**
     * Update the environment variables needed for the execution of an SDK tool.
     * 
     * <p>This method set the <code>PATH</code>, <code>LD_LIBRARY_PATH</code> or
     * <code>DYLD_LIBRARY_PATH</code> depending on the platform.</p>
     * 
     * @param pVars the environment variables to update
     * @param pOoo the OpenOffice.org instance to use along with the SDK
     * 
     * @return the update environment variables. 
     * 
     * @throws Exception if the platform isn't among the platforms for which the
     *      OpenOffice.org SDK is available.
     */
    private String[] updateEnvironment(String[] pVars, IOOo pOoo) throws Exception {
        String[] oooBinPaths = pOoo.getBinPath();
        String[] binPaths = new String[ oooBinPaths.length + 1 ];
        binPaths[ 0 ] = getBinPath().toOSString();
        System.arraycopy( oooBinPaths, 0, binPaths, 1, oooBinPaths.length );
        
        String[] oooLibs = pOoo.getLibsPath();
        
        // Create the exec parameters depending on the OS
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            
            // Definining path variables
            pVars = SystemHelper.addPathEnv(pVars, "PATH", binPaths); //$NON-NLS-1$
            
        } else if (Platform.getOS().equals(Platform.OS_LINUX) ||
                Platform.getOS().equals(Platform.OS_SOLARIS)) {
            
            // An UN*X platform   
            String[] tmpVars = SystemHelper.addPathEnv(pVars, "PATH",  //$NON-NLS-1$
                    binPaths );
            pVars = SystemHelper.addPathEnv(tmpVars, "LD_LIBRARY_PATH", //$NON-NLS-1$
                    oooLibs );
            
        } else if (Platform.getOS().equals(Platform.OS_MACOSX)) { 
            
            String[] tmpVars = SystemHelper.addPathEnv(pVars, "PATH",  //$NON-NLS-1$
                    binPaths );
            pVars = SystemHelper.addPathEnv(tmpVars, "DYLD_LIBRARY_PATH", //$NON-NLS-1$
                    oooLibs);
            
        } else {
            // Unmanaged OS
            throw new Exception(Messages.getString("SDK.InvalidSdkError")); //$NON-NLS-1$
        }
        
        return pVars;
    }

    /**
     * {@inheritDoc}
     */
    public Image getImage(String pProperty) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getLabel(String pProperty) {
        String result = ""; //$NON-NLS-1$
        if (pProperty.equals(NAME)) {
            result = getId();
        } else if (pProperty.equals(PATH)) {
            result = getHome();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getProperties() {
        return new String[] {NAME, PATH};
    }

    /**
     * {@inheritDoc}
     */
    public boolean canModify(String pProperty) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(String pProperty) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void setValue(String pProperty, Object pValue) {
        // Nothing to do
    }
    
    /**
     * Reads the <code>dk.mk</code> file to get the SDK name and build id. They are set in the 
     * SDK object if they are both fetched. Otherwise an invalid SDK exception is thrown.
     * 
     * @param pSettingsFile the setting directory file handle.
     * 
     * @throws InvalidConfigException Exception thrown when one of the following problems happened
     *          <ul>
     *             <li>the given settings file isn't a valid directory</li>
     *             <li>the <code>settings/dk.mk</code> file doesn't exists or is unreadable</li>
     *             <li>one or both of the sdk name or build id key is not set</li>
     *          </ul>
     */
    private void readSettings(File pSettingsFile) throws InvalidConfigException {
        
        if (pSettingsFile.exists() && pSettingsFile.isDirectory()) {
        
            // Get the dk.mk file
            File dkFile = new File(pSettingsFile, F_DK_CONFIG);
            
            Properties dkProperties = new Properties();
            FileInputStream in = null;
            try {
                in = new FileInputStream(dkFile);
                dkProperties.load(in);
                
                // Checks if the name and buildid properties are set
                if (dkProperties.containsKey(K_SDK_BUILDID)) {
                    
                    mBuildId = dkProperties.getProperty(K_SDK_BUILDID);
                } else if (dkProperties.containsKey(K_SDK_BUILDID)) {
                    
                    mBuildId = dkProperties.getProperty(K_SDK_BUILDID);
                } else {
                    throw new InvalidConfigException(
                            Messages.getString("SDK.MissingKeyError") + K_SDK_BUILDID, //$NON-NLS-1$
                            InvalidConfigException.INVALID_SDK_HOME);
                }
                
            } catch (FileNotFoundException e) {
                throw new InvalidConfigException(
                        Messages.getString("SDK.NoFileError") + "settings/" + F_DK_CONFIG ,  //$NON-NLS-1$ //$NON-NLS-2$
                        InvalidConfigException.INVALID_SDK_HOME);
            } catch (IOException e) {
                throw new InvalidConfigException(
                        Messages.getString("SDK.NoReadableFileError") +  //$NON-NLS-1$
                        "settings/" + F_DK_CONFIG,  //$NON-NLS-1$
                        InvalidConfigException.INVALID_SDK_HOME);
            } finally {
                try { in.close(); } catch (Exception e) { }
            }
            
        } else {
            throw new InvalidConfigException(
                    Messages.getString("SDK.NoDirectoryError") + pSettingsFile.getAbsolutePath(), //$NON-NLS-1$
                    InvalidConfigException.INVALID_SDK_HOME);
        }
    }
}
