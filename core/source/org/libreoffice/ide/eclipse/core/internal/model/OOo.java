/*************************************************************************
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
package org.libreoffice.ide.eclipse.core.internal.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.model.config.InvalidConfigException;
import org.libreoffice.ide.eclipse.core.model.utils.SystemHelper;

/**
 * Representing an LibreOffice instance for use in the UNO-IDL projects.
 *
 * <p>
 * A LibreOffice instance is recognized to the following files:
 * <ul>
 * <li><code>program/classes</code> directory</li>
 * <li><code>program/types.rdb</code> registry</li>
 * <li><code>program/bootstraprc</code> file</li>
 * </ul>
 * </p>
 *
 * <p>
 * A MacOS installation of LibreOffice will have some different paths, and of course the windows installation too. This
 * class is used to abstract the platform LibreOffice is installed on.
 * </p>
 */
public class OOo extends AbstractOOo {

    /**
     * private constant that holds the LibreOffice name key in the bootstrap properties file.
     */
    private static final String K_PRODUCTKEY = "ProductKey"; //$NON-NLS-1$

    private OOo3PathMapper mMapper;

    /**
     * Creating a new LibreOffice instance specifying its home directory.
     *
     * @param pOooHome
     *            the LibreOffice home directory
     *
     * @throws InvalidConfigException
     *             is thrown if the home directory doesn't contains the required files and directories
     */
    public OOo(String pOooHome) throws InvalidConfigException {
        super(pOooHome);
    }

    /**
     * Creating a new LibreOffice instance specifying its home directory and name.
     *
     * @param pOooHome
     *            the LibreOffice installation path
     * @param pOooName
     *            the LibreOffice instance name
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
     *            the LibreOffice installation path to set.
     *
     * @throws InvalidConfigException
     *             if the path doesn't point to a valid LibreOffice installation.
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
    @Override
    public String[] getClassesPath() {
        return mMapper.getClasses();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
    public String[] getTypesPath() {
        return mMapper.getTypes();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getServicesPath() {
        return mMapper.getServices();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnorcPath() {
        String path = getLibsPath()[0] + FILE_SEP + "bootstrap"; //$NON-NLS-1$
        if (getPlatform().equals(Platform.OS_WIN32)) {
            path += ".ini"; //$NON-NLS-1$
        } else if (getPlatform().equals(Platform.OS_MACOSX)){
            path = getHome() + FILE_SEP + "Resources" + FILE_SEP + "bootstraprc";
        }
        else {
            path += "rc"; //$NON-NLS-1$
        }
        return path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUnoPath() {
        return mMapper.getUnoPath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setName(String pName) {

        String name = pName;
        if (name == null || name.equals("")) { //$NON-NLS-1$
            name = getOOoName();
        }

        super.setName(name);
    }

    /**
     * @return The LibreOffice name as defined in Bootstraprc or <code>null</code>.
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
    @Override
    public String toString() {
        return "OOo " + getName(); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public String getJavaldxPath() {
        String javaldx = getLibsPath() + FILE_SEP + "javaldx"; //$NON-NLS-1$
        return javaldx;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canManagePackages() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePackage(File pPackageFile, IPath pUserInstallation) {

        // Check if there is already a package with the same name
        try {
            // Add the package
            addPackage(pPackageFile, pUserInstallation);

        } catch (Exception e) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    MessageDialog.openError(Display.getDefault().getActiveShell(),
                        Messages.getString("OOo.PackageExportTitle"), //$NON-NLS-1$
                        Messages.getString("OOo.DeploymentError")); //$NON-NLS-1$
                }
            });
            PluginLogger.error(Messages.getString("OOo.DeploymentError"), e); //$NON-NLS-1$
        }
    }

    /**
     * Add a Uno package to the LibreOffice user packages.
     *
     * FIXME This method has to handle license approval
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
        String shellCommand = MessageFormat.format("unopkg add -f \"{0}\"", path); //$NON-NLS-1$

        // We need system env variables - at least on Linux the unopkg is in the global path, but not in instdir/program
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
     * A class providing the paths for the OOo3 installation.
     */
    private class OOo3PathMapper {

        private String mHome;

        /**
         * This field holds the URE instance to use for OOo3.
         */
        private URE mUre;

        // private fields
        private File mapperBasisBins;
        private File mapperBasisClasses;
        private List<File> mapperBasisTypes;
        private List<File> mapperBasisServices;

        /**
         * Create a new mapper object to get the OOo3 layers paths.
         *
         * @param pHome
         *            the LibreOffice install home
         * @throws InvalidConfigException
         */
        public OOo3PathMapper(String pHome) throws InvalidConfigException {
            mHome = pHome;
            initPaths();
        }

        private void initPaths() throws InvalidConfigException {
            // locate ure directory (directory which contains bin/uno.bin or bin/uno.exe
            String unoRelativePath;
            if (getPlatform().equals(Platform.OS_MACOSX))
                unoRelativePath = "MacOS/" + URE.getUnoExecutable();
            else
                unoRelativePath = "program/" + URE.getUnoExecutable(); //todo
            File ureDir = locateUniqueContainer(mHome, unoRelativePath);
            if (ureDir == null) {
                mHome = null;
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + unoRelativePath,
                    InvalidConfigException.INVALID_OOO_HOME);
            }
            mUre = new URE(ureDir.getAbsolutePath());
        }

        private File locateUniqueContainer(String baseDir, String pUnoRelativePath) throws InvalidConfigException {
            try {
                File base = new File(baseDir);
                if (!base.exists() || !base.isDirectory() || !base.canRead()) {
                    return null;
                }
                List<File> dirs = new RelativeFileLocator(base, pUnoRelativePath).getFiles();
                if (dirs == null) {
                    throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + pUnoRelativePath,
                        InvalidConfigException.INVALID_OOO_HOME);
                }
                // remove link if there is duplicate
                if (dirs.size() > 1) {
                    List<File> linksList = new ArrayList<File>();
                    for (File tmpFile : dirs) {
                        if (AbstractOOo.isSymbolicLink(tmpFile)) {
                            linksList.add(tmpFile);
                        }
                    }
                    if (!linksList.isEmpty()) {
                        for (File link : linksList) {
                            File linkTarget = AbstractOOo.getTargetLink(link);
                            if (dirs.contains(linkTarget)) {
                                dirs.remove(linkTarget);
                            }
                        }
                    }
                }
                if (dirs.size() != 1) {
                    throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + pUnoRelativePath,
                        InvalidConfigException.INVALID_OOO_HOME);
                } else {
                    return dirs.get(0);
                }
            } catch (IOException e) {
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + pUnoRelativePath,
                    InvalidConfigException.INVALID_OOO_HOME);
            }
        }

        private List<File> locateFiles(String baseDir, String pUnoRelativePath) throws InvalidConfigException {
            try {
                File base = new File(baseDir);
                if (!base.exists() || !base.isDirectory() || !base.canRead()) {
                    return null;
                }
                List<File> dirs = new RelativeFileLocator(base, pUnoRelativePath).getFiles();
                if (dirs == null) {
                    return Collections.emptyList();
                }
                // remove link if there is duplicate
                if (dirs.size() > 1) {
                    List<File> linksList = new ArrayList<File>();
                    for (File tmpFile : dirs) {
                        if (AbstractOOo.isSymbolicLink(tmpFile)) {
                            linksList.add(tmpFile);
                        }
                    }
                    if (!linksList.isEmpty()) {
                        for (File link : linksList) {
                            File linkTarget = AbstractOOo.getTargetLink(link);
                            if (dirs.contains(linkTarget)) {
                                dirs.remove(linkTarget);
                            }
                        }
                    }
                }
                if (dirs.size() == 0) {
                    return Collections.emptyList();
                }
                List<File> returnList = new ArrayList<File>();
                for (File tmpFile : dirs) {
                    returnList.add(new File(tmpFile, pUnoRelativePath));
                }
                return returnList;
            } catch (IOException e) {
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + pUnoRelativePath,
                    InvalidConfigException.INVALID_OOO_HOME);
            }
        }

        /**
         * @return the libraries path to add for OOo3 or an empty array if not an OOo3 install.
         * @throws InvalidConfigException
         */
        public String[] getAdditionnalLibs() {
            return mUre.getLibsPath();
        }

        /**
         * @return the binaries path to add for OOo3 or an empty array if not an OOo3 install.
         */
        public String[] getAdditionnalBins() {
            String[] additionnal = new String[0];

            String[] ureLibs = new String[0];
            if (mUre != null) {
                ureLibs = mUre.getBinPath();
            }

            File basisLibs = this.mapperBasisBins;
            if (basisLibs == null) {
                String sofficeName = "soffice.bin";
                if (getPlatform().equals(Platform.OS_MACOSX))
                    sofficeName = "soffice";
                try {
                    basisLibs = locateUniqueContainer(mHome, sofficeName);
                    this.mapperBasisBins = basisLibs;
                } catch (InvalidConfigException e) {
                    e.printStackTrace();
                }
            }

            if (basisLibs != null) {
                additionnal = mergeArrays(ureLibs, new String[] { basisLibs.getAbsolutePath() });
            } else {
                additionnal = ureLibs;
            }

            return additionnal;
        }

        /**
         * @return the OOo 3.0 classes path or an empty array if not an OOo3 install.
         */
        public String[] getClasses() {
            String[] classes = new String[0];

            String[] ureClasses = new String[0];
            if (mUre != null) {
                ureClasses = mUre.getClassesPath();
            }

            File basisClasses = this.mapperBasisClasses;
            if (mapperBasisClasses == null) {
                try {
                    basisClasses = locateUniqueContainer(mHome, "unoil.jar");
                    mapperBasisClasses = basisClasses;
                } catch (InvalidConfigException e) {
                    e.printStackTrace();
                }
            }

            if (basisClasses != null) {
                classes = mergeArrays(ureClasses, new String[] { basisClasses.getAbsolutePath() });
            } else {
                classes = ureClasses;
            }

            return classes;
        }

        /**
         * @return the OOo3 types path or an empty array if not an OOo3 install.
         */
        public String[] getTypes() {
            String[] types = new String[0];

            String[] ureTypes = new String[0];
            if (mUre != null) {
                ureTypes = mUre.getTypesPath();
            }

            List<File> basisTypes = this.mapperBasisTypes;
            if (mapperBasisTypes == null) {
                try {
                    basisTypes = locateFiles(mHome, "offapi.rdb");
                    mapperBasisTypes = basisTypes;
                } catch (InvalidConfigException e) {
                    e.printStackTrace();
                }
            }

            if (basisTypes != null && basisTypes.size() > 0) {
                List<String> servicesPathList = new ArrayList<String>();
                for (File typeFile : basisTypes) {
                    if (typeFile != null) {
                        servicesPathList.add(typeFile.getAbsolutePath());
                    }
                }
                types = mergeArrays(ureTypes, servicesPathList.toArray(new String[servicesPathList.size()]));
            } else {
                types = ureTypes;
            }

            return types;
        }

        /**
         * @return the OOo3 services.rdb files or <code>null</code> if not an OOo3 install.
         */
        public String[] getServices() {
            String[] types = new String[0];

            String[] ureTypes = new String[0];
            if (mUre != null) {
                ureTypes = mUre.getServicesPath();
            }

            List<File> basisTypes = this.mapperBasisServices;
            if (mapperBasisServices == null) {
                try {
                    basisTypes = locateFiles(mHome, "services.rdb");
                    mapperBasisServices = basisTypes;
                } catch (InvalidConfigException e) {
                    e.printStackTrace();
                }
            }

            if (basisTypes != null && basisTypes.size() > 0) {
                List<String> servicesPathList = new ArrayList<String>();
                for (File typeFile : basisTypes) {
                    if (typeFile != null) {
                        servicesPathList.add(typeFile.getAbsolutePath());
                    }
                }
                types = mergeArrays(ureTypes, servicesPathList.toArray(new String[servicesPathList.size()]));
            } else {
                types = ureTypes;
            }

            return types;
        }

        /**
         * @return the path to the uno executable for OOo3 of <code>null</code> if not an OOo3 install.
         */
        public String getUnoPath() {
            String path = null;
            if (mUre != null) {
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
            if (pArray1 == null) {
                return pArray2;
            }
            if (pArray2 == null) {
                return pArray1;
            }
            String[] result = new String[pArray1.length + pArray2.length];

            System.arraycopy(pArray1, 0, result, 0, pArray1.length);
            System.arraycopy(pArray2, 0, result, pArray1.length, pArray2.length);

            return result;
        }
    }
}
