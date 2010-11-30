/*************************************************************************
 *
 * $RCSfile: AbstractOOo.java,v $
 *
 * $Revision: 1.10 $
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
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.swt.graphics.Image;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.gui.ITableElement;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.config.IExtraOptionsProvider;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.InvalidConfigException;
import org.openoffice.ide.eclipse.core.model.utils.SystemHelper;

/**
 * Helper class to add the table element features to the OOo classes. All the {@link IOOo} interface still has to be
 * implemented by the subclasses
 * 
 * @author cbosdonnat
 * 
 */
public abstract class AbstractOOo implements IOOo, ITableElement {

    public static final String NAME = "__ooo_name"; //$NON-NLS-1$

    public static final String PATH = "__ooo_path"; //$NON-NLS-1$

    protected static final String FILE_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

    private static String sPlatform;

    private String mHome;
    private String mName;

    /**
     * Creating a new OOo or URE instance specifying its home directory.
     * 
     * @param pOooHome
     *            the OpenOffice.org or URE home directory
     * @throws InvalidConfigException
     *             is thrown if the home directory doesn't contains the required files and directories
     */
    public AbstractOOo(String pOooHome) throws InvalidConfigException {
        setHome(pOooHome);
    }

    /**
     * Creating a new OOo or URE instance specifying its home directory and name.
     * 
     * @param pOooHome
     *            the OpenOffice.org or URE installation directory
     * @param pName
     *            the OpenOffice.org or URE instance name
     * 
     * @throws InvalidConfigException
     *             if the home directory doesn't contains the required files and directories
     */
    public AbstractOOo(String pOooHome, String pName) throws InvalidConfigException {
        setHome(pOooHome);
        setName(pName);
    }

    /**
     * {@inheritDoc}
     */
    public void setHome(String pHome) throws InvalidConfigException {

        Path homePath = new Path(pHome);
        File homeFile = homePath.toFile();

        /* Checks if the directory exists */
        if (!homeFile.isDirectory() || !homeFile.canRead()) {
            mHome = null;
            throw new InvalidConfigException(Messages.getString("AbstractOOo.NoDirectoryError") + //$NON-NLS-1$
                            homeFile.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
        }

        mHome = pHome;

        /* Checks if the classes paths are directories */
        checkClassesDir();

        /* Checks if types registries are readable files */
        checkTypesRdb();

        /* Checks if services.rdb is a readable file */
        checkServicesRdb();

        /* Checks if unorc is a readable file */
        checkUnoIni();
    }

    /**
     * {@inheritDoc}
     */
    public String getHome() {
        return mHome;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return mName;
    }

    /**
     * Set the new name only if it's neither null nor the empty string. The name will be rendered unique and therefore
     * may be changed.
     * 
     * @param pName
     *            the name to set
     */
    protected void setName(String pName) {
        if (pName != null && !pName.equals("")) { //$NON-NLS-1$
            mName = OOoContainer.getUniqueName(pName);
        }
    }

    /**
     * Check if the UNO configuration file is present in the OOo installation directory.
     * 
     * @throws InvalidConfigException
     *             if the UNO configuration file isn't present.
     */
    private void checkUnoIni() throws InvalidConfigException {
        Path unorcPath = new Path(getUnorcPath());
        File unorcFile = unorcPath.toFile();

        if (!unorcFile.isFile() || !unorcFile.canRead()) {
            mHome = null;
            throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + //$NON-NLS-1$
                            unorcFile.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
        }
    }

    /**
     * Check if the <code>services.rdb</code> file is present in the OOo installation directory.
     * 
     * @throws InvalidConfigException
     *             if the <code>services.rdb</code> file isn't present
     */
    private void checkServicesRdb() throws InvalidConfigException {
        String[] paths = getServicesPath();

        for (String path : paths) {
            Path servicesPath = new Path(path);
            File servicesFile = servicesPath.toFile();

            if (!servicesFile.isFile() || !servicesFile.canRead()) {
                mHome = null;
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + //$NON-NLS-1$
                                servicesFile.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
            }
        }
    }

    /**
     * Check if the <code>types.rdb</code> file is present in the OOo installation directory.
     * 
     * @throws InvalidConfigException
     *             if the <code>types.rdb</code> file isn't present
     */
    private void checkTypesRdb() throws InvalidConfigException {
        String[] paths = getTypesPath();
        for (String path : paths) {
            Path typesPath = new Path(path);
            File typesFile = typesPath.toFile();

            if (!typesFile.isFile() || !typesFile.canRead()) {
                mHome = null;
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoFileError") + //$NON-NLS-1$
                                typesFile.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
            }
        }
    }

    /**
     * Check if the classes directory exits in the OOo installation folder.
     * 
     * @throws InvalidConfigException
     *             if the classes directory can't be found
     */
    private void checkClassesDir() throws InvalidConfigException {
        String[] paths = getClassesPath();
        for (String path : paths) {
            Path javaPath = new Path(path);
            File javaDir = javaPath.toFile();

            if (!javaDir.isDirectory() || !javaDir.canRead()) {
                mHome = null;
                throw new InvalidConfigException(Messages.getString("AbstractOOo.NoDirectoryError") + //$NON-NLS-1$
                                javaDir.getAbsolutePath(), InvalidConfigException.INVALID_OOO_HOME);
            }
        }
    }

    // -------------------------------------------- ITableElement Implementation

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
            result = getName();
        } else if (pProperty.equals(PATH)) {
            result = getHome();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getProperties() {
        return new String[] { NAME, PATH };
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
     * Run a UNO application using an implementation of the <code>XMain</code> interface.
     * 
     * @param pPrj
     *            the UNO project to run
     * @param pMain
     *            the fully qualified name of the main service to run
     * @param pArgs
     *            the UNO program arguments
     * @param pLaunch
     *            the Eclipse launch instance
     * @param pMonitor
     *            the monitor reporting the run progress
     */
    public void runUno(IUnoidlProject pPrj, String pMain, String pArgs, ILaunch pLaunch, IProgressMonitor pMonitor) {

        String libpath = pPrj.getLanguage().getProjectHandler().getLibraryPath(pPrj);
        libpath = libpath.replace("\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        libpath = libpath.replace(" ", "%20"); //$NON-NLS-1$ //$NON-NLS-2$
        libpath = "file:///" + libpath; //$NON-NLS-1$

        String unoPath = getUnoPath();
        if (getPlatform().equals(Platform.OS_WIN32)) {
            /* uno is already in the PATH variable, so don't worry */
            unoPath = "uno"; //$NON-NLS-1$
        }

        String command = unoPath + " -c " + pMain + //$NON-NLS-1$
                        " -l " + libpath + //$NON-NLS-1$
                        " -- " + pArgs; //$NON-NLS-1$

        String[] env = pPrj.getLanguage().getLanguageBuidler().getBuildEnv(pPrj);

        IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pPrj.getName());

        if (getJavaldxPath() != null) {
            Process p = pPrj.getSdk().runToolWithEnv(prj, pPrj.getOOo(), getJavaldxPath(), env, pMonitor);
            InputStream out = p.getInputStream();
            StringWriter writer = new StringWriter();

            try {
                int c = out.read();
                while (c != -1) {
                    writer.write(c);
                    c = out.read();
                }
            } catch (IOException e) {
            }

            String libPath = writer.getBuffer().toString();
            env = SystemHelper.addEnv(env, "LD_LIBRARY_PATH", libPath.trim(), //$NON-NLS-1$
                            System.getProperty("path.separator")); //$NON-NLS-1$
        }

        Process p = pPrj.getSdk().runToolWithEnv(prj, pPrj.getOOo(), command, env, pMonitor);
        DebugPlugin.newProcess(pLaunch, p, Messages.getString("AbstractOOo.UreProcessName") + pMain); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public void runOpenOffice(IUnoidlProject pPrj, ILaunch pLaunch, IPath pUserInstallation,
                    IExtraOptionsProvider pExtraOptionsProvider, IProgressMonitor pMonitor) {
        try {
            IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(pPrj.getName());
            String[] env = pPrj.getLanguage().getLanguageBuidler().getBuildEnv(pPrj);

            String pathSeparator = System.getProperty("path.separator");
            String[] sPaths = pPrj.getOOo().getBinPath();
            StringBuilder sPathValue = new StringBuilder();
            for (String sPath : sPaths) {
                sPathValue.append(sPath);
                sPathValue.append(pathSeparator);
            }

            String command = "soffice.bin";

            env = SystemHelper.addEnv(env, "PATH", sPathValue.toString(), pathSeparator);
            env = SystemHelper.addEnv(env, "SAL_ALLOW_LINKOO_SYMLINKS", "1", null );
            env = addUserProfile(pUserInstallation, env);
            env = pExtraOptionsProvider.addEnv(env);

            PluginLogger.debug("Launching OpenOffice from commandline: " + command);
            Process p = pPrj.getSdk().runToolWithEnv(prj, pPrj.getOOo(), command, env, pMonitor);
            DebugPlugin.newProcess(pLaunch, p, Messages.getString("AbstractOOo.OpenOfficeProcessName")); //$NON-NLS-1$
        } catch (Exception e) {
            e.printStackTrace();
            PluginLogger.error("Error running OpenOffice", e);
        }
    }

    /**
     * Adds the proper env variables for the user profile.
     * 
     * @param pUserInstallation
     *            the path to the user profile foldr.
     * @param pEnv
     *            the original env.
     * @return the new env.
     * @throws URISyntaxException
     *             if something goes wrong.
     */
    protected String[] addUserProfile(IPath pUserInstallation, String[] pEnv) throws URISyntaxException {
        if (null != pUserInstallation) {
            // We have to turn the path to a URI something like file:///foo/bar/.ooo-debug
            // TODO find a better way to get the proper URI.
            URI userInstallationURI = new URI("file", "", pUserInstallation.toFile().toURI().getPath(), null);
            pEnv = SystemHelper.addEnv(pEnv, "UserInstallation", userInstallationURI.toString(), null);
        }
        return pEnv;
    }

    /**
     * Sets the target platform for tests.
     * 
     * @param pPlatform
     *            the target platform
     */
    public static void setPlatform(String pPlatform) {
        sPlatform = pPlatform;
    }

    /**
     * @return the system platform, or the test one if set.
     */
    protected String getPlatform() {
        String result = sPlatform;
        if (sPlatform == null) {
            result = Platform.getOS();
        }
        return result;
    }
}
