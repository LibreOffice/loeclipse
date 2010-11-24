/*************************************************************************
 *
 * $RCSfile: OOo.java,v $
 *
 * $Revision: 1.10 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2009/04/20 06:16:01 $
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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.model.config.InvalidConfigException;
import org.openoffice.ide.eclipse.core.model.utils.SystemHelper;

/**
 * Representing an OpenOffice.org instance for use in the UNO-IDL projects.
 * 
 * <p>
 * An OpenOffice.org instance is recognized to the following files:
 * <ul>
 * <li><code>program/classes</code> directory</li>
 * <li><code>program/types.rdb</code> registry</li>
 * <li><code>program/bootstraprc</code> file</li>
 * </ul>
 * </p>
 * 
 * <p>
 * A MacOS installation of OpenOffice.org will have some different paths, and of course the windows installation too.
 * This class is used to abstract the platform OOo is installed on.
 * </p>
 * 
 * @author cedricbosdo
 * 
 */
public class OOo extends AbstractOOo {

    /**
     * private constant that holds the ooo name key in the bootstrap properties file.
     */
    private static final String K_PRODUCTKEY = "ProductKey"; //$NON-NLS-1$

    private boolean mDoRemovePackage = false;

    private OOo3PathMapper mMapper;

    /**
     * Creating a new OOo instance specifying its home directory.
     * 
     * @param pOooHome
     *            the OpenOffice.org home directory
     * 
     * @throws InvalidConfigException
     *             is thrown if the home directory doesn't contains the required files and directories
     */
    public OOo(String pOooHome) throws InvalidConfigException {
        super(pOooHome);
    }

    /**
     * Creating a new OOo instance specifying its home directory and name.
     * 
     * @param pOooHome
     *            the OpenOffice.org installation path
     * @param pOooName
     *            the OpenOffice.org instance name
     * 
     * @throws InvalidConfigException
     *             is thrown if the home directory doesn't contains the required files and directories
     */
    public OOo(String pOooHome, String pOooName) throws InvalidConfigException {
        super(pOooHome, pOooName);
    }

    // ----------------------------------------------------- IOOo Implementation

    /**
     * Overridden to initialize the path mapper for 00o3 installations.
     * 
     * @param pHome
     *            the OOo installation path to set.
     * 
     * @throws InvalidConfigException
     *             if the path doesn't point to a valid OOo installation.
     */
    @Override
    public void setHome(String pHome) throws InvalidConfigException {

        if (getPlatform().equals(Platform.OS_MACOSX)) {
            pHome = pHome + FILE_SEP + "Contents"; //$NON-NLS-1$
        }

        mMapper = new OOo3PathMapper(pHome);
        super.setHome(pHome);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getClassesPath() {

        String[] paths = new String[] { getLibsPath()[0] + FILE_SEP + "classes" //$NON-NLS-1$
        };

        if (mMapper.isVersion3()) {
            paths = mMapper.getClasses();
        }

        return paths;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getLibsPath() {
        // Nothing if not OOo3
        String[] otherPaths = mMapper.getAdditionnalLibs();

        String libs = getHome() + FILE_SEP + "program"; //$NON-NLS-1$
        if (getPlatform().equals(Platform.OS_MACOSX)) {
            libs = getHome() + FILE_SEP + "MacOS"; //$NON-NLS-1$
        }

        return mMapper.mergeArrays(new String[] { libs }, otherPaths);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getBinPath() {
        // Nothing if not OOo3
        String[] otherPaths = mMapper.getAdditionnalBins();

        String bins = getHome() + FILE_SEP + "program"; //$NON-NLS-1$
        if (Platform.getOS().equals(Platform.OS_MACOSX)) {
            bins = getHome() + FILE_SEP + "MacOS"; //$NON-NLS-1$
        }

        return mMapper.mergeArrays(new String[] { bins }, otherPaths);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getTypesPath() {
        String[] paths = { getLibsPath()[0] + FILE_SEP + "types.rdb" //$NON-NLS-1$ 
        };

        if (mMapper.isVersion3()) {
            paths = mMapper.getTypes();
        }

        return paths;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getServicesPath() {
        String[] paths = new String[] { getLibsPath()[0] + FILE_SEP + "services.rdb" //$NON-NLS-1$
        };

        // Change the paths for OOo3 installs
        if (mMapper.isVersion3()) {
            paths = mMapper.getServices();
        }
        return paths;
    }

    /**
     * {@inheritDoc}
     */
    public String getUnorcPath() {
        String path = getLibsPath()[0] + FILE_SEP + "bootstrap"; //$NON-NLS-1$
        if (getPlatform().equals(Platform.OS_WIN32)) {
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
        if (getPlatform().equals(Platform.OS_WIN32)) {
            uno = "uno.exe"; //$NON-NLS-1$
        }
        String unoPath = getLibsPath()[0] + FILE_SEP + uno;

        if (mMapper.isVersion3()) {
            unoPath = mMapper.getUnoPath();
        }

        return unoPath;
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
                bootstraprcProperties.load(new FileInputStream(unorcFile));

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
    public String createUnoCommand(String pImplementationName, String pLibLocation, String[] pRegistriesPaths,
                    String[] pArgs) {

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
            if (getPlatform().equals(Platform.OS_WIN32)) {
                classpath += "\""; //$NON-NLS-1$
            }

            String[] oooClassesPaths = getClassesPath();
            for (String oooClassesPath : oooClassesPaths) {
                File oooClasses = new File(oooClassesPath);
                String[] content = oooClasses.list();

                for (int i = 0, length = content.length; i < length; i++) {
                    String contenti = content[i];
                    if (contenti.endsWith(".jar")) { //$NON-NLS-1$
                        classpath += oooClassesPath + fileSeparator + contenti + pathSeparator;
                    }
                }
            }

            classpath += pLibLocation;
            if (getPlatform().equals(Platform.OS_WIN32)) {
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
        String javaldx = getLibsPath() + FILE_SEP + "javaldx"; //$NON-NLS-1$
        return javaldx;
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
    public void updatePackage(File pPackageFile, IPath pUserInstallation) {

        // Check if there is already a package with the same name
        try {
            if (containsPackage(pPackageFile.getName(), pUserInstallation)) {
                mDoRemovePackage = false;
                Display.getDefault().syncExec(new Runnable() {
                    public void run() {
                        mDoRemovePackage = MessageDialog.openConfirm(Display.getDefault().getActiveShell(), Messages
                                        .getString("OOo.PackageExportTitle"), //$NON-NLS-1$
                                        Messages.getString("OOo.PackageAlreadyInstalled")); //$NON-NLS-1$
                    }
                });
                if (mDoRemovePackage) {
                    // remove it
                    removePackage(pPackageFile.getName(), pUserInstallation);
                }
            }

            // Add the package
            addPackage(pPackageFile, pUserInstallation);

        } catch (Exception e) {
            Display.getDefault().asyncExec(new Runnable() {
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(), Messages
                                    .getString("OOo.PackageExportTitle"), //$NON-NLS-1$
                                    Messages.getString("OOo.DeploymentError")); //$NON-NLS-1$
                }
            });
            PluginLogger.error(Messages.getString("OOo.DeploymentError"), e); //$NON-NLS-1$
        }
    }

    /**
     * Add a Uno package to the OOo user packages.
     * 
     * FIXME This method has to handle license ap proval
     * 
     * @param pPackageFile
     *            the package file to add
     * @param pUserInstallation
     *            path to the user profile folder.
     * @throws Exception
     *             if anything wrong happens
     */
    private void addPackage(File pPackageFile, IPath pUserInstallation) throws Exception {
        String path = pPackageFile.getAbsolutePath();
        if (getPlatform().equals(Platform.OS_WIN32)) {
            path = "\"" + path + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        }
        String shellCommand = "unopkg gui -f " + path; //$NON-NLS-1$

        String[] env = SystemHelper.getSystemEnvironement();
        String pathsep = System.getProperty("path.separator"); //$NON-NLS-1$
        env = SystemHelper.addEnv(env, "PATH", getHome() + FILE_SEP + "program", pathsep); //$NON-NLS-1$ //$NON-NLS-2$
        env = addUserProfile(pUserInstallation, env);

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
            throw new Exception(Messages.getString("OOo.PackageAddError") + //$NON-NLS-1$
                            pPackageFile.getAbsolutePath());
        }
    }

    /**
     * Remove the named package from the OOo packages.
     * 
     * @param pName
     *            the name of the package to remove
     * @param pUuserInstallation
     *            TODO
     * @throws Exception
     *             if anything wrong happens
     */
    private void removePackage(String pName, IPath pUuserInstallation) throws Exception {
        String shellCommand = "unopkg remove " + pName; //$NON-NLS-1$

        String[] env = SystemHelper.getSystemEnvironement();
        String filesep = System.getProperty("file.separator"); //$NON-NLS-1$
        String pathsep = System.getProperty("path.separator"); //$NON-NLS-1$
        env = SystemHelper.addEnv(env, "PATH", getHome() + filesep + "program", pathsep); //$NON-NLS-1$ //$NON-NLS-2$
        env = addUserProfile(pUuserInstallation, env);

        SystemHelper.runTool(shellCommand, env, null);
    }

    /**
     * Check if the named package is already installed on OOo.
     * 
     * @param pName
     *            the package name to look for
     * @param pUserInstallation
     *            path to the user profile.
     * @return <code>true</code> if the package is installed, <code>false</code> otherwise
     * @throws Exception
     *             if anything wrong happens
     */
    private boolean containsPackage(String pName, IPath pUserInstallation) throws Exception {
        boolean contained = false;

        String shellCommand = "unopkg list"; //$NON-NLS-1$

        String[] env = SystemHelper.getSystemEnvironement();
        String filesep = System.getProperty("file.separator"); //$NON-NLS-1$
        String pathsep = System.getProperty("path.separator"); //$NON-NLS-1$
        env = SystemHelper.addEnv(env, "PATH", getHome() + filesep + "program", pathsep); //$NON-NLS-1$ //$NON-NLS-2$
        env = addUserProfile(pUserInstallation, env);

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

    /**
     * A class providing the paths for the OOo3 installation.
     * 
     * @author cbosdonnat
     * 
     */
    private class OOo3PathMapper {

        private String mHome;
        private String mBasis;

        private boolean mVersionChecked = false;

        /**
         * This field holds the URE instance to use for OOo3.
         */
        private URE mUre;

        /**
         * Create a new mapper object to get the OOo3 layers paths.
         * 
         * @param pHome
         *            the OOo install home
         */
        public OOo3PathMapper(String pHome) {
            mHome = pHome;
        }

        /**
         * @return <code>true</code> if the openoffice install corresponds to a 3.0 installation layout,
         *         <code>false</code> otherwise.
         */
        public boolean isVersion3() {
            boolean version3 = false;

            if (!mVersionChecked) {
                try {
                    Path homePath = new Path(mHome);
                    File homeFile = homePath.toFile();

                    File basis = getPortableLink("basis-link", homeFile); //$NON-NLS-1$
                    File ure = getPortableLink("ure-link", basis); //$NON-NLS-1$

                    version3 = basis.isDirectory() && ure.isDirectory();

                    if (version3) {
                        mBasis = basis.getCanonicalPath();
                        mUre = new URE(ure.getCanonicalPath());
                    }
                } catch (Exception e) {
                    version3 = false;
                }

                mVersionChecked = true;
            } else {
                version3 = mUre != null;
            }

            return version3;
        }

        /**
         * @return the libraries path to add for OOo3 or an empty array if not an OOo3 install.
         */
        public String[] getAdditionnalLibs() {
            String[] additionnal = new String[0];

            if (isVersion3()) {
                String[] ureLibs = mUre.getLibsPath();
                String basisLibs = mBasis + FILE_SEP + "program"; //$NON-NLS-1$

                additionnal = mergeArrays(ureLibs, new String[] { basisLibs });
            }

            return additionnal;
        }

        /**
         * @return the binaries path to add for OOo3 or an empty array if not an OOo3 install.
         */
        public String[] getAdditionnalBins() {
            String[] additionnal = new String[0];

            if (isVersion3()) {
                String[] ureBins = mUre.getBinPath();
                String basisBins = mBasis + FILE_SEP + "program"; //$NON-NLS-1$

                additionnal = mergeArrays(ureBins, new String[] { basisBins });
            }

            return additionnal;
        }

        /**
         * @return the OOo 3.0 classes path or an empty array if not an OOo3 install.
         */
        public String[] getClasses() {
            String[] classes = new String[0];

            if (isVersion3()) {
                String[] ureClasses = mUre.getClassesPath();
                String basisClasses = mBasis + FILE_SEP + "program" + //$NON-NLS-1$ 
                                FILE_SEP + "classes"; //$NON-NLS-1$

                classes = mergeArrays(ureClasses, new String[] { basisClasses });
            }

            return classes;
        }

        /**
         * @return the OOo3 types path or an empty array if not an OOo3 install.
         */
        public String[] getTypes() {
            String[] types = new String[0];

            if (isVersion3()) {
                String[] ureTypes = mUre.getTypesPath();
                String basisTypes = mBasis + FILE_SEP + "program" + //$NON-NLS-1$ 
                                FILE_SEP + "offapi.rdb"; //$NON-NLS-1$

                types = mergeArrays(ureTypes, new String[] { basisTypes });
            }

            return types;
        }

        /**
         * @return the OOo3 services.rdb files or <code>null</code> if not an OOo3 install.
         */
        public String[] getServices() {
            String[] types = new String[0];

            if (isVersion3()) {
                String[] ureTypes = mUre.getServicesPath();
                String basisTypes = mBasis + FILE_SEP + "program" + //$NON-NLS-1$ 
                                FILE_SEP + "services.rdb"; //$NON-NLS-1$

                types = mergeArrays(ureTypes, new String[] { basisTypes });
            }

            return types;
        }

        /**
         * @return the path to the uno executable for OOo3 of <code>null</code> if not an OOo3 install.
         */
        public String getUnoPath() {
            String path = null;
            if (isVersion3()) {
                path = mUre.getUnoPath();
            }

            return path;
        }

        /**
         * Merge two string arrays into one.
         * 
         * The duplicated elements are not removed.
         * 
         * @param pArray1
         *            the first array to merge
         * @param pArray2
         *            the second array to merge
         * 
         * @return the array with the elements of both arrays
         */
        public String[] mergeArrays(String[] pArray1, String[] pArray2) {
            String[] result = new String[pArray1.length + pArray2.length];

            System.arraycopy(pArray1, 0, result, 0, pArray1.length);
            System.arraycopy(pArray2, 0, result, pArray1.length, pArray2.length);

            return result;
        }

        /**
         * Get the file object for the link defined as a child of a folder.
         * 
         * On Windows platform, the link relative location is specified as the content of a file named after the link
         * name. On Unix-based systems symbolic links are supported.
         * 
         * @param pName
         *            the name of the symbolic link
         * @param pParent
         *            the parent directory file
         * 
         * @return the file representing the link target or <code>null</code>
         */
        private File getPortableLink(String pName, File pParent) {
            File link = null;

            File linkFile = new File(pParent, pName);
            if (getPlatform().equals(Platform.OS_WIN32)) {
                // Read the content of the file to get the true folder
                try {
                    FileInputStream is = new FileInputStream(linkFile);
                    byte[] buf = new byte[is.available()];
                    is.read(buf);

                    String relativePath = new String(buf);
                    linkFile = new File(pParent, relativePath);
                    link = linkFile;
                } catch (Exception e) {
                    // the returned link is null to show the error
                }
            } else {
                link = linkFile;
            }

            return link;
        }
    }
}
