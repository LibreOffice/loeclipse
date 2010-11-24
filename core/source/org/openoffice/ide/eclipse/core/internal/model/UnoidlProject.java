/*************************************************************************
 *
 * $RCSfile: UnoidlProject.java,v $
 *
 * $Revision: 1.13 $
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Properties;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.openoffice.ide.eclipse.core.PluginLogger;
import org.openoffice.ide.eclipse.core.builders.TypesBuilder;
import org.openoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.openoffice.ide.eclipse.core.model.IUnoidlProject;
import org.openoffice.ide.eclipse.core.model.OOoContainer;
import org.openoffice.ide.eclipse.core.model.SDKContainer;
import org.openoffice.ide.eclipse.core.model.config.IConfigListener;
import org.openoffice.ide.eclipse.core.model.config.IOOo;
import org.openoffice.ide.eclipse.core.model.config.ISdk;
import org.openoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.openoffice.ide.eclipse.core.model.language.IProjectHandler;
import org.openoffice.ide.eclipse.core.model.language.LanguagesHelper;

/**
 * This class implements the UNO-IDL and project nature interface.
 * 
 * @author cedricbosdo
 * 
 */
public class UnoidlProject implements IUnoidlProject, IProjectNature {

    /**
     * Project property that stores the company prefix.
     */
    public static final String COMPANY_PREFIX = "project.prefix"; //$NON-NLS-1$

    /**
     * Project property that stores the output path extension.
     * 
     * <p>
     * If the company prefix is <code>org.openoffice.sample</code> and this property value is <code>impl</code>, the
     * root package of the implementations classes is <code>org.openoffice.sample.impl</code>.
     * </p>
     */
    public static final String OUTPUT_EXT = "project.implementation"; //$NON-NLS-1$

    /**
     * Project property that stores the sdk name to use for the project build.
     */
    public static final String SDK_NAME = "project.sdk"; //$NON-NLS-1$

    /**
     * Project property that stores the name of the OpenOffice.org instance used to run / deploy the project.
     */
    public static final String OOO_NAME = "project.ooo"; //$NON-NLS-1$

    /**
     * Project property that stores the language name.
     */
    public static final String LANGUAGE = "project.language"; //$NON-NLS-1$

    /**
     * Project property that stores the path to the folder containing the sources.
     */
    public static final String SRC_DIRECTORY = "project.srcdir"; //$NON-NLS-1$

    /**
     * Property name for the idl folder.
     */
    public static final String IDL_DIR = "project.idl"; //$NON-NLS-1$

    /**
     * Property name for the build directory.
     */
    public static final String BUILD_DIR = "project.build"; //$NON-NLS-1$

    /**
     * The name of the file containing the UNO project configuration.
     */
    private static final String CONFIG_FILE = ".unoproject"; //$NON-NLS-1$

    private IProject mProject;

    private String mCompanyPrefix;

    private String mOutputExtension;

    private ISdk mSdk;

    private IOOo mOOo;

    private AbstractLanguage mLanguage;

    private String mIdlDir;

    private String mSourcesDir;

    private IConfigListener mConfigListener;

    /**
     * Listener for the configuration to handle the changes on SDK and OOo instances.
     * 
     * @author cedricbosdo
     */
    private class configListener implements IConfigListener {

        /**
         * {@inheritDoc}
         */
        public void ConfigAdded(Object pElement) {
            // the selected SDK or OOo cannot be added again...
        }

        /**
         * {@inheritDoc}
         */
        public void ConfigRemoved(Object pElement) {
            if (pElement instanceof ISdk) {
                if (pElement == getSdk()) {

                    // Sets the selected SDK to null, it will tag the project as invalid
                    setSdk(null);
                }
            } else if (pElement instanceof IOOo) {
                if (pElement == getOOo()) {

                    // Removes OOo dependencies
                    getLanguage().getProjectHandler().removeOOoDependencies(getOOo(), getProject());

                    // Sets the selected OOo to null, it will tag the project as invalid
                    setOOo(null);
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        public void ConfigUpdated(Object pElement) {
            if (pElement instanceof IOOo) {
                if (pElement == getOOo()) {
                    // the ooo is updated thanks to it's reference. Remove the old jar files
                    // from the classpath and the new ones

                    // Removes OOo dependencies
                    getLanguage().getProjectHandler().removeOOoDependencies(getOOo(), getProject());
                    getLanguage().getProjectHandler().addOOoDependencies(getOOo(), getProject());
                }
            }
        }
    }

    // ------------------------------------------------------------ Constructors

    /**
     * Default constructor initializing the configuration listener.
     */
    public UnoidlProject() {

        mConfigListener = new configListener();

        SDKContainer.addListener(mConfigListener);
        OOoContainer.addListener(mConfigListener);
    }

    /**
     * Removes the listeners needed by the UNO project.
     */
    public void dispose() {
        SDKContainer.removeListener(mConfigListener);
        OOoContainer.removeListener(mConfigListener);
    }

    /**
     * Return the path of the file in the idl folder. If the given file doesn't belong to the idl folder,
     * <code>null</code> is returned.
     * 
     * @param pResource
     *            resource of which the idl path is asked
     * @return idl relative path or <code>null</code>
     */
    public IPath getIdlRelativePath(IResource pResource) {
        IPath result = null;

        IPath projectRelative = pResource.getProjectRelativePath();

        if (projectRelative.toString().startsWith(getIdlPath().toString())) {
            result = projectRelative.removeFirstSegments(getIdlPath().segmentCount());
        }
        return result;
    }

    // *************************************************************************
    // IUnoidlModel Implementation
    // *************************************************************************

    /**
     * {@inheritDoc}
     */
    public AbstractLanguage getLanguage() {
        return mLanguage;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return getProject().getName();
    }

    /**
     * {@inheritDoc}
     */
    public IOOo getOOo() {
        return mOOo;
    }

    /**
     * {@inheritDoc}
     */
    public ISdk getSdk() {
        return mSdk;
    }

    /**
     * {@inheritDoc}
     */
    public void setLanguage(AbstractLanguage pNewLanguage) {

        if (mLanguage == null && pNewLanguage != null) {
            mLanguage = pNewLanguage;
            mLanguage.getProjectHandler().addProjectNature(getProject());
            PluginLogger.debug("Language specific nature added"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setOOo(IOOo pOoo) {

        setErrorMarker(null == pOoo || null == getSdk());

        try {
            IProjectHandler langHandler = getLanguage().getProjectHandler();

            // Remove the old OOo libraries
            langHandler.removeOOoDependencies(mOOo, getProject());

            // Add the new ones
            langHandler.addOOoDependencies(pOoo, getProject());
        } catch (Exception e) {
            // This might happen at some stage of the project creation
        }

        this.mOOo = pOoo;
    }

    /**
     * {@inheritDoc}
     */
    public void setSdk(ISdk pSdk) {

        setErrorMarker(pSdk == null || null == getOOo());

        this.mSdk = pSdk;
    }

    /**
     * {@inheritDoc}
     */
    public void setIdlDir(String pIdlDir) {
        mIdlDir = pIdlDir;
    }

    /**
     * {@inheritDoc}
     */
    public void setSourcesDir(String pSourcesDir) {
        if (pSourcesDir == null || pSourcesDir.equals("")) { //$NON-NLS-1$
            pSourcesDir = UnoidlProjectHelper.SOURCE_BASIS;
        }

        // Add a / at the beginning of the path
        if (!pSourcesDir.startsWith("/")) { //$NON-NLS-1$
            pSourcesDir = "/" + pSourcesDir; //$NON-NLS-1$
        }

        mSourcesDir = pSourcesDir;
    }

    /**
     * {@inheritDoc}
     */
    public String getRootModule() {
        String result = ""; //$NON-NLS-1$

        if (null != mCompanyPrefix) {
            result = mCompanyPrefix.replaceAll("\\.", "::"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public IPath getRootModulePath() {
        IPath result = null;

        if (null != mCompanyPrefix) {
            result = getIdlPath().append(mCompanyPrefix.replaceAll("\\.", "/")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    public void setCompanyPrefix(String pPrefix) {
        mCompanyPrefix = pPrefix;
    }

    /**
     * {@inheritDoc}
     */
    public String getCompanyPrefix() {
        return mCompanyPrefix;
    }

    /**
     * {@inheritDoc}
     */
    public void setOutputExtension(String pOutputExt) {
        mOutputExtension = pOutputExt;
    }

    /**
     * {@inheritDoc}
     */
    public String getOutputExtension() {
        return mOutputExtension;
    }

    /**
     * {@inheritDoc}
     */
    public IPath getBuildPath() {
        String buildDir = getProperty(BUILD_DIR);
        if (!buildDir.startsWith("/")) { //$NON-NLS-1$
            buildDir = "/" + buildDir; //$NON-NLS-1$
        }

        return getFolder(buildDir).getProjectRelativePath();
    }

    /**
     * {@inheritDoc}
     */
    public IPath getIdlPath() {
        String idlDir = getProperty(IDL_DIR);
        if (!idlDir.startsWith("/")) { //$NON-NLS-1$
            idlDir = "/" + idlDir; //$NON-NLS-1$
        }

        return getFolder(idlDir).getProjectRelativePath();
    }

    /**
     * {@inheritDoc}
     */
    public IPath getImplementationPath() {
        String path = new String(mCompanyPrefix + "." + mOutputExtension).replace('.', '/'); //$NON-NLS-1$
        return getSourcePath().append(path);
    }

    /**
     * {@inheritDoc}
     */
    public IPath getProjectPath() {
        return getProject().getLocation();
    }

    /**
     * {@inheritDoc}
     */
    public IPath getTypesPath() {
        return new Path("types.rdb"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IPath getServicesPath() {
        return new Path("services.rdb"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    public IPath getSourcePath() {
        if (mSourcesDir == null) {
            mSourcesDir = getProperty(SRC_DIRECTORY);
        }
        return getFolder(mSourcesDir).getProjectRelativePath();
    }

    /**
     * {@inheritDoc}
     */
    public IPath getUrdPath() {
        return getFolder(getBuildPath().append(UnoidlProjectHelper.URD_BASIS)).getProjectRelativePath();
    }

    public IPath getDistPath() {
        return getFolder(UnoidlProjectHelper.DIST_BASIS).getProjectRelativePath();
    }

    public IFolder getDistFolder() throws CoreException {
        IFolder folder = getFolder(getDistPath());
        // TODO find better way to make sure the folder exists.
        if (!folder.exists()) {
            folder.getLocation().toFile().mkdirs();
        }
        return folder;
    }

    public IPath getOpenOfficeUserProfilePath() {
        return getFolder(getDistPath().append(UnoidlProjectHelper.OO_PROFILE_BASIS)).getProjectRelativePath();
    }

    public IFolder getOpenOfficeUserProfileFolder() throws CoreException {
        IFolder folder = getFolder(getOpenOfficeUserProfilePath());
        // TODO find better way to make sure the folder exists.
        if (!folder.exists()) {
            folder.getLocation().toFile().mkdirs();
        }
        return folder;
    }

    /**
     * {@inheritDoc}
     */
    public IFile getFile(IPath pPath) {
        return getProject().getFile(pPath);
    }

    /**
     * {@inheritDoc}
     */
    public IFile getFile(String pPath) {
        return getProject().getFile(pPath);
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getFolder(IPath pPath) {
        return getProject().getFolder(pPath);
    }

    /**
     * {@inheritDoc}
     */
    public IFolder getFolder(String pPath) {
        return getProject().getFolder(pPath);
    }

    /**
     * @return the UNO project configuration file
     * 
     * @see #CONFIG_FILE for the configuration file name
     */
    public File getConfigFile() {
        return new File(getProjectPath().append(CONFIG_FILE).toOSString());
    }

    /**
     * Reads a property from the UNO project configuration file.
     * 
     * <p>
     * Returns the property corresponding to the given name. If the configuration file doesn't exists, a default one
     * will be created.
     * </p>
     * 
     * @param pPropertyName
     *            the name of the property to get
     * @return the property value or <code>null</code> if not found.
     * 
     * @see #CONFIG_FILE for the configuration file name
     */
    public String getProperty(String pPropertyName) {

        Properties properties = new Properties();
        File configFile = getConfigFile();
        String property = null;

        FileInputStream in = null;
        try {
            // Create a default configuration file if needed
            if (!configFile.exists()) {
                UnoidlProjectHelper.createDefaultConfig(configFile);
            }

            in = new FileInputStream(configFile);
            properties.load(in);
            property = properties.getProperty(pPropertyName);
        } catch (Exception e) {
            String pattern = Messages.getString("UnoidlProject.UnreadableConfigFileWarning"); //$NON-NLS-1$
            String msg = MessageFormat.format(pattern, CONFIG_FILE);
            PluginLogger.warning(msg, e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
        }

        return property;
    }

    /**
     * Define a property in the UNO project configuration file.
     * 
     * @param pName
     *            the property name
     * @param pValue
     *            the property value
     */
    public void setProperty(String pName, String pValue) {
        Properties properties = new Properties();
        File configFile = getConfigFile();

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            // Create a default configuration file if needed
            if (!configFile.exists()) {
                UnoidlProjectHelper.createDefaultConfig(configFile);
            }

            in = new FileInputStream(configFile);
            properties.load(in);

            properties.setProperty(pName, pValue);

            out = new FileOutputStream(configFile);
            properties.store(out, Messages.getString("UnoidlProject.ConfigFileComment")); //$NON-NLS-1$

            // Refresh the configuration file
            getFile(CONFIG_FILE).refreshLocal(IResource.DEPTH_ZERO, null);

        } catch (Exception e) {
            String pattern = Messages.getString("UnoidlProject.PropertyChangeError"); //$NON-NLS-1$
            String message = MessageFormat.format(pattern, pName, pValue);
            PluginLogger.warning(message, e);
        } finally {
            try {
                in.close();
            } catch (IOException e) {
            }
            try {
                out.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void saveAllProperties() {

        Properties properties = new Properties();
        File configFile = getConfigFile();

        // Create a default configuration file if needed
        if (!configFile.exists()) {
            UnoidlProjectHelper.createDefaultConfig(configFile);
        }

        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            in = new FileInputStream(configFile);
            properties.load(in);

            properties.setProperty(LANGUAGE, mLanguage.getName());
            properties.setProperty(OOO_NAME, mOOo.getName());
            properties.setProperty(SDK_NAME, mSdk.getId());
            properties.setProperty(IDL_DIR, mIdlDir);
            properties.setProperty(SRC_DIRECTORY, mSourcesDir);
            properties.setProperty(COMPANY_PREFIX, mCompanyPrefix);
            properties.setProperty(OUTPUT_EXT, mOutputExtension);

            out = new FileOutputStream(configFile);
            properties.store(out, Messages.getString("UnoidlProject.ConfigFileComment")); //$NON-NLS-1$

            // Refresh the configuration file
            getFile(CONFIG_FILE).refreshLocal(IResource.DEPTH_ZERO, null);

        } catch (Exception e) {
            PluginLogger.warning(Messages.getString("UnoidlProject.ConfigFileSaveError"), e); //$NON-NLS-1$
        } finally {
            try {
                in.close();
            } catch (Exception e) {
            }
            try {
                out.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public IFolder[] getBinFolders() {
        return getLanguage().getProjectHandler().getBinFolders(this);
    }

    // *************************************************************************
    // IProjectNature Implementation
    // *************************************************************************

    /**
     * {@inheritDoc}
     */
    public void configure() throws CoreException {

        // Load all the persistent properties into the members

        String sdkKey = getProperty(SDK_NAME);
        if (sdkKey != null) {
            setSdk(SDKContainer.getSDK(sdkKey));
        }

        String prefix = getProperty(COMPANY_PREFIX);
        if (prefix != null) {
            mCompanyPrefix = prefix;
        }

        String outputExt = getProperty(OUTPUT_EXT);
        if (outputExt != null) {
            mOutputExtension = outputExt;
        }

        String languageName = getProperty(LANGUAGE);
        if (languageName != null) {
            setLanguage(LanguagesHelper.getLanguageFromName(languageName));
        }

        String idlDir = getProperty(IDL_DIR);
        if (idlDir != null) {
            setIdlDir(idlDir);
        }

        String srcDir = getProperty(SRC_DIRECTORY);
        if (srcDir != null) {
            setSourcesDir(srcDir);
        }

        String oooKey = getProperty(OOO_NAME);
        if (oooKey != null) {
            IOOo someOOo = OOoContainer.getSomeOOo(oooKey);
            setOOo(someOOo);
        }

        // Save any change from the read project file
        saveAllProperties();
    }

    /**
     * {@inheritDoc}
     */
    public void deconfigure() throws CoreException {
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    public IProject getProject() {
        return mProject;
    }

    /**
     * {@inheritDoc}
     */
    public void setProject(IProject pProject) {
        mProject = pProject;
    }

    // *************************************************************************
    // Useful methods for the nature implementation
    // *************************************************************************

    /**
     * Set the builders for the project.
     * 
     * <p>
     * This method configures the builders using the implementation language informations
     * </p>
     * 
     * @throws CoreException
     *             if the builders can't be set.
     */
    public void setBuilders() throws CoreException {
        if (!(null == mSdk || null == mOOo || null == mCompanyPrefix || null == mOutputExtension)) {

            // Set the types builder
            IProjectDescription descr = getProject().getDescription();
            ICommand[] builders = descr.getBuildSpec();
            ICommand[] newCommands = new ICommand[builders.length + 1];

            ICommand typesbuilderCommand = descr.newCommand();
            typesbuilderCommand.setBuilderName(TypesBuilder.BUILDER_ID);
            newCommands[0] = typesbuilderCommand;

            System.arraycopy(builders, 0, newCommands, 1, builders.length);

            descr.setBuildSpec(newCommands);
            getProject().setDescription(descr, null);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "UNO Project " + getName(); //$NON-NLS-1$
    }

    /**
     * Toggle an error marker on the project indicating that the there is either no OpenOffice.org nor SDK set.
     * 
     * @param pSet
     *            <code>true</code> if the error marker should be set, <code>false</code> otherwise.
     */
    private void setErrorMarker(boolean pSet) {

        IProject prjRes = getProject();

        try {
            if (pSet) {
                IMarker marker = prjRes.createMarker(IMarker.PROBLEM);
                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                marker.setAttribute(IMarker.MESSAGE, Messages.getString("UnoidlProject.NoOOoSdkError")); //$NON-NLS-1$
            } else {
                prjRes.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
            }
        } catch (CoreException e) {
            if (pSet) {
                PluginLogger.error(Messages.getString("UnoidlProject.CreateMarkerError") + //$NON-NLS-1$
                                getProjectPath().toString(), e);
            } else {
                PluginLogger.error(Messages.getString("UnoidlProject.RemoveMarkerError"), e); //$NON-NLS-1$
            }
        }

    }
}
