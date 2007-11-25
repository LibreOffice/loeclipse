/*************************************************************************
 *
 * $RCSfile: OOo.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:26 $
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
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Properties;

import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.internal.helpers.SystemHelper;
import org.openoffice.ide.eclipse.core.preferences.InvalidConfigException;

/**
 * Representing an OpenOffice.org instance for use in the UNO-IDL projects.
 * 
 * <p>An OpenOffice.org instance is recognized to the following files:
 *     <ul>
 *         <li><code>program/classes</code> directory</li>
 *         <li><code>program/types.rdb</code> registry</li>
 *         <li><code>program/bootstraprc</code> file</li>
 *     </ul>
 * </p>
 * 
 * <p>A MacOS installation of OpenOffice.org will have some different paths, and
 * of course the windows installation too. This class is used to abstract the 
 * platform OOo is installed on.</p>
 * 
 * @author cedricbosdo
 *
 */
public class OOo extends AbstractOOo {
    
    /**
     * private constant that holds the ooo name key in the bootstrap
     * properties file.
     */
    private static final String  K_PRODUCTKEY = "ProductKey"; //$NON-NLS-1$
    
    private boolean mDoRemovePackage = false;
    
    /**
     * Creating a new OOo instance specifying its home directory.
     * 
     * @param pOooHome the OpenOffice.org home directory
     * 
     * @throws InvalidConfigException is thrown if the home directory doesn't
     *         contains the required files and directories
     */
    public OOo(String pOooHome) throws InvalidConfigException {
        super(pOooHome);
    }
    
    /**
     * Creating a new OOo instance specifying its home directory and name.
     * 
     * @param pOooHome the OpenOffice.org installation path
     * @param pOooName the OpenOffice.org instance name
     * 
     * @throws InvalidConfigException is thrown if the home directory doesn't
     *         contains the required files and directories
     */
    public OOo(String pOooHome, String pOooName) throws InvalidConfigException {
        super(pOooHome, pOooName);
    }
        
    //----------------------------------------------------- IOOo Implementation
    
    /**
     * {@inheritDoc}
     */
    public String getClassesPath() {
        return getLibsPath() + FILE_SEP + "classes";  //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String getLibsPath() {
        String libs = getHome() + FILE_SEP + "program"; //$NON-NLS-1$
        if (Platform.getOS().equals(Platform.OS_MACOSX)) {
            libs = getHome() + FILE_SEP + "Contents" + FILE_SEP + "MacOS";
        }
        return libs;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getTypesPath() {
        return getLibsPath() + FILE_SEP + "types.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public String getServicesPath() {
        return getLibsPath() + FILE_SEP + "services.rdb"; //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * {@inheritDoc}
     */
    public String getUnorcPath() {
        String path = getLibsPath() + FILE_SEP + "bootstrap"; //$NON-NLS-1$ //$NON-NLS-2$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            path += ".ini"; //$NON-NLS-1$
        } else {
            path += "rc"; //$NON-NLS-1$
        }
        return path;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getUnoPath() {
        String uno = "uno.bin"; //$NON-NLS-1$
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            uno = "uno.exe"; //$NON-NLS-1$
        }
        return getLibsPath() + FILE_SEP + uno; //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * {@inheritDoc}
     */
    protected void setName(String pName) {
        
        String name = pName;
        if (name == null || name.equals("")) { //$NON-NLS-1$
            name = getOOoName();
        }
        
        super.setName(name);
    }
    
    /**
     * @return The OOo name as defined in Bootstraprc or <code>null</code>.
     */
    private String getOOoName() {
        
        String oooname = null;
        
        Path unorcPath = new Path(getUnorcPath());
        File unorcFile = unorcPath.toFile();
        
        if (unorcFile.exists() && unorcFile.isFile()) {
        
            Properties bootstraprcProperties = new Properties();
            try {
                bootstraprcProperties.load(
                        new FileInputStream(unorcFile));
                
                // Checks if the name and buildid properties are set
                if (bootstraprcProperties.containsKey(K_PRODUCTKEY)) {
                    
                    // Sets the both value
                    oooname = bootstraprcProperties.getProperty(K_PRODUCTKEY);
                }
                
            } catch (Exception e) {
                // Nothing to report
            }
        }
        
        return oooname;
    }
    
    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "OOo " + getName(); //$NON-NLS-1$
    }
    
    /**
     * {@inheritDoc}
     */
    public String createUnoCommand(String pImplementationName, String pLibLocation,
            String[] pRegistriesPaths, String[] pArgs) {
        
        String command = ""; //$NON-NLS-1$
        
        if (pLibLocation != null && !pLibLocation.equals("")) { //$NON-NLS-1$
            // Put the args into one string
            String sArgs = ""; //$NON-NLS-1$
            for (int i = 0; i < pArgs.length; i++) {
                sArgs += pArgs[i];

                if (i < pArgs.length - 1) {
                    sArgs += " "; //$NON-NLS-1$
                }
            }

            // Defines OS specific constants
            String pathSeparator = System.getProperty("path.separator"); //$NON-NLS-1$
            String fileSeparator = System.getProperty("file.separator"); //$NON-NLS-1$

            // Constitute the classpath for OOo Boostrapping
            String classpath = "-cp "; //$NON-NLS-1$
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                classpath += "\""; //$NON-NLS-1$
            }

            String oooClassesPath = getClassesPath();
            File oooClasses = new File(oooClassesPath);
            String[] content = oooClasses.list();

            for (int i = 0, length = content.length; i < length; i++) {
                String contenti = content[i];
                if (contenti.endsWith(".jar")) { //$NON-NLS-1$
                    classpath += oooClassesPath + 
                        fileSeparator + contenti + pathSeparator;
                }
            }

            classpath += pLibLocation;
            if (Platform.getOS().equals(Platform.OS_WIN32)) {
                classpath += "\""; //$NON-NLS-1$
            }

            command = "java " + classpath + " " + //$NON-NLS-1$ //$NON-NLS-2$
                pImplementationName + " " + sArgs; //$NON-NLS-1$
        }
            
        return command;
    }
    
    /**
     * {@inheritDoc}
     */
    public String getJavaldxPath() {
        String javaldx = getLibsPath() + FILE_SEP + "javaldx";
        return  javaldx; 
    }
    
    /**
     * {@inheritDoc}
     */
    public boolean canManagePackages() {
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    public void updatePackage(File pPackageFile) {
        
        // Check if there is already a package with the same name
        try {
            if (containsPackage(pPackageFile.getName())) {
                mDoRemovePackage = false;
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        mDoRemovePackage = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), 
                                Messages.getString("OOo.PackageExportTitle"),  //$NON-NLS-1$
                                Messages.getString("OOo.PackageAlreadyInstalled")); //$NON-NLS-1$
                    }
                });
                if (mDoRemovePackage) {
                    // remove it
                    removePackage(pPackageFile.getName());
                }
            }

            // Add the package
            addPackage(pPackageFile);

        } catch (Exception e) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), 
                            Messages.getString("OOo.PackageExportTitle"),  //$NON-NLS-1$
                            Messages.getString("OOo.DeploymentError"));     //$NON-NLS-1$
                }
            });
            PluginLogger.error(Messages.getString("OOo.DeploymentError"), e); //$NON-NLS-1$
        }
    }
    
    /**
     * Add a Uno package to the OOo user packages.
     * 
     * @param pPackageFile the package file to add
     * @throws Exception if anything wrong happens
     */
    private void addPackage(File pPackageFile) throws Exception {
        String path = pPackageFile.getAbsolutePath();
        if (Platform.getOS().equals(Platform.OS_WIN32)) {
            path = "\"" + path + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        }
        String shellCommand = "unopkg add " + path; //$NON-NLS-1$
        
        String[] env = SystemHelper.getSystemEnvironement();
        String pathsep = System.getProperty("path.separator"); //$NON-NLS-1$
        env = SystemHelper.addEnv(env, "PATH", getHome() + FILE_SEP + "program", pathsep); //$NON-NLS-1$ //$NON-NLS-2$
        
        Process process = SystemHelper.runTool(shellCommand, env, null);
        
        InputStreamReader in = new InputStreamReader(process.getInputStream());
        LineNumberReader reader = new LineNumberReader(in);

        String line = reader.readLine();
        boolean failed = false;
        while (null != line && !failed) {
            if (line.contains("failed")) { //$NON-NLS-1$
                failed = true;
            }
            line = reader.readLine();
        }
        
        try {
            reader.close();
            in.close();
        } catch (Exception e) {
        }
        
        if (failed) {
            throw new Exception(Messages.getString("OOo.PackageAddError") + 
                    pPackageFile.getAbsolutePath()); //$NON-NLS-1$
        }
    }
    
    /**
     * Remove the named package from the OOo packages.
     * 
     * @param pName the name of the package to remove
     * @throws Exception if anything wrong happens
     */
    private void removePackage(String pName) throws Exception {
        String shellCommand = "unopkg remove " + pName; //$NON-NLS-1$
        
        String[] env = SystemHelper.getSystemEnvironement();
        String filesep = System.getProperty("file.separator"); //$NON-NLS-1$
        String pathsep = System.getProperty("path.separator"); //$NON-NLS-1$
        env = SystemHelper.addEnv(env, "PATH", getHome() + filesep + "program", pathsep); //$NON-NLS-1$ //$NON-NLS-2$
        
        SystemHelper.runTool(shellCommand, env, null);
    }
    
    /**
     * Check if the named package is already installed on OOo.
     * 
     * @param pName the package name to look for
     * @return <code>true</code> if the package is installed, 
     *             <code>false</code> otherwise
     * @throws Exception if anything wrong happens
     */
    private boolean containsPackage(String pName) throws Exception {
        boolean contained = false;
        
        String shellCommand = "unopkg list"; //$NON-NLS-1$
        
        String[] env = SystemHelper.getSystemEnvironement();
        String filesep = System.getProperty("file.separator"); //$NON-NLS-1$
        String pathsep = System.getProperty("path.separator"); //$NON-NLS-1$
        env = SystemHelper.addEnv(env, "PATH", getHome() + filesep + "program", pathsep); //$NON-NLS-1$ //$NON-NLS-2$
        
        Process process = SystemHelper.runTool(shellCommand, env, null);
        InputStreamReader in = new InputStreamReader(process.getInputStream());
        LineNumberReader reader = new LineNumberReader(in);

        String line = reader.readLine();
        while (null != line && !contained) {
            if (line.endsWith(pName)) {
                contained = true;
            }
            line = reader.readLine();
        }
        
        try {
            reader.close();
            in.close();
        } catch (Exception e) {
        }
        
        return contained;
    }
}
