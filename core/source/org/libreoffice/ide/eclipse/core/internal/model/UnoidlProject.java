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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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
import org.libreoffice.ide.eclipse.core.PluginLogger;
import org.libreoffice.ide.eclipse.core.builders.TypesBuilder;
import org.libreoffice.ide.eclipse.core.internal.helpers.UnoidlProjectHelper;
import org.libreoffice.ide.eclipse.core.model.IUnoidlProject;
import org.libreoffice.ide.eclipse.core.model.OOoContainer;
import org.libreoffice.ide.eclipse.core.model.SDKContainer;
import org.libreoffice.ide.eclipse.core.model.config.IConfigListener;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.libreoffice.ide.eclipse.core.model.language.IProjectHandler;
import org.libreoffice.ide.eclipse.core.model.language.LanguagesHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class implements the UNO-IDL and project nature interface.
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
     * If the company prefix is <code>org.libreoffice.sample</code> and this property value is <code>impl</code>, the
     * root package of the implementations classes is <code>org.libreoffice.sample.impl</code>.
     * </p>
     */
    public static final String OUTPUT_EXT = "project.implementation"; //$NON-NLS-1$

    /**
     * Project property that stores the sdk name to use for the project build.
     */
    public static final String SDK_NAME = "project.sdk"; //$NON-NLS-1$

    /**
     * Project property that stores the name of the LibreOffice instance used to run / deploy the project.
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
     * Property name for the build file.
     */
    public static final String BUILD_FILE = "build.properties"; //$NON-NLS-1$

    /**
     * The name of the file containing the UNO project configuration.
     */
    private static final String CONFIG_FILE = ".unoproject"; //$NON-NLS-1$

    /**
     * The name of the META-INF/manifest.xml file.
     */
    private static final String MANIFEST_FILE = "META-INF/manifest.xml"; //$NON-NLS-1$

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
         */
    private class configListener implements IConfigListener {

        /**
         * {@inheritDoc}
         */
        @Override
        public void ConfigAdded(Object element) {
            // the selected SDK or OOo cannot be added again...
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void ConfigRemoved(Object element) {
            if (element instanceof ISdk) {
                if (element == getSdk()) {

                    // Sets the selected SDK to null, it will tag the project as invalid
                    setSdk(null);
                }
            } else if (element instanceof IOOo) {
                if (element == getOOo()) {

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
        @Override
        public void ConfigUpdated(Object element) {
            if (element instanceof IOOo) {
                if (element == getOOo()) {
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
    @Override
    public void dispose() {
        SDKContainer.removeListener(mConfigListener);
        OOoContainer.removeListener(mConfigListener);
    }

    /**
     * Return the path of the file in the idl folder. If the given file doesn't belong to the idl folder,
     * <code>null</code> is returned.
     *
     * @param res
     *            resource of which the idl path is asked
     * @return idl relative path or <code>null</code>
     */
    public IPath getIdlRelativePath(IResource res) {
        IPath result = null;

        IPath projectRelative = res.getProjectRelativePath();

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
    @Override
    public AbstractLanguage getLanguage() {
        return mLanguage;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return getProject().getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IOOo getOOo() {
        return mOOo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISdk getSdk() {
        return mSdk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLanguage(AbstractLanguage newLanguage) {

        if (mLanguage == null && newLanguage != null) {
            mLanguage = newLanguage;
            mLanguage.getProjectHandler().addProjectNature(getProject());
            PluginLogger.debug("Language specific nature added"); //$NON-NLS-1$
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOOo(IOOo ooo) {

        setErrorMarker(null == ooo || null == getSdk());

        try {
            IProjectHandler langHandler = getLanguage().getProjectHandler();

            // Remove the old OOo libraries
            langHandler.removeOOoDependencies(mOOo, getProject());

            // Add the new ones
            langHandler.addOOoDependencies(ooo, getProject());
        } catch (Exception e) {
            // This might happen at some stage of the project creation
        }

        this.mOOo = ooo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSdk(ISdk pSdk) {

        setErrorMarker(pSdk == null || null == getOOo());

        this.mSdk = pSdk;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIdlDir(String idlDir) {
        mIdlDir = idlDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSourcesDir(String sourcesDir) {
        if (sourcesDir == null || sourcesDir.equals("")) { //$NON-NLS-1$
            sourcesDir = UnoidlProjectHelper.SOURCE_BASIS;
        }

        // Add a / at the beginning of the path
        if (!sourcesDir.startsWith("/")) { //$NON-NLS-1$
            sourcesDir = "/" + sourcesDir; //$NON-NLS-1$
        }

        mSourcesDir = sourcesDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
    @Override
    public void setCompanyPrefix(String pPrefix) {
        mCompanyPrefix = pPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCompanyPrefix() {
        return mCompanyPrefix;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOutputExtension(String outputExt) {
        mOutputExtension = outputExt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getOutputExtension() {
        return mOutputExtension;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public String getIdlDir() {
        String idlDir = getProperty(IDL_DIR);
        if (idlDir.startsWith("/")) { //$NON-NLS-1$
            idlDir = idlDir.substring(1);
        }
        return idlDir;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public IPath getImplementationPath() {
        String path = new String(mCompanyPrefix + "." + mOutputExtension).replace('.', '/'); //$NON-NLS-1$
        return getSourcePath().append(path);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getProjectPath() {
        return getProject().getLocation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getTypesPath() {
        return new Path(IDLTYPES_FILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getServicesPath() {
        return new Path("services.rdb"); //$NON-NLS-1$
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getSourcePath() {
        if (mSourcesDir == null) {
            mSourcesDir = getProperty(SRC_DIRECTORY);
        }
        return getFolder(mSourcesDir).getProjectRelativePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getUrdPath() {
        return getFolder(getBuildPath().append(UnoidlProjectHelper.URD_BASIS)).getProjectRelativePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getDistPath() {
        return getFolder(UnoidlProjectHelper.DIST_BASIS).getProjectRelativePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFolder getDistFolder() throws CoreException {
        IFolder folder = getFolder(getDistPath());
        if (!folder.exists()) {
            folder.getLocation().toFile().mkdirs();
        }
        return folder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IPath getOfficeUserProfilePath() {
        return getFolder(getDistPath().append(UnoidlProjectHelper.OO_PROFILE_BASIS)).getProjectRelativePath();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFolder getOfficeUserProfileFolder() throws CoreException {
        IFolder folder = getFolder(getOfficeUserProfilePath());
        if (!folder.exists()) {
            folder.getLocation().toFile().mkdirs();
        }
        return folder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFile getFile(IPath pPath) {
        return getProject().getFile(pPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFile getFile(String pPath) {
        return getProject().getFile(pPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFolder getFolder(IPath pPath) {
        return getProject().getFolder(pPath);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
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
            String msg = Messages.getString("UnoidlProject.UnreadableConfigFileWarning"); //$NON-NLS-1$
            PluginLogger.warning(MessageFormat.format(msg, CONFIG_FILE), e);
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
     * @param name
     *            the property name
     * @param value
     *            the property value
     */
    @Override
    public void setProperty(String name, String value) {
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

            properties.setProperty(name, value);

            out = new FileOutputStream(configFile);
            properties.store(out, Messages.getString("UnoidlProject.ConfigFileComment")); //$NON-NLS-1$

            // Refresh the configuration file
            getFile(CONFIG_FILE).refreshLocal(IResource.DEPTH_ZERO, null);

        } catch (Exception e) {
            String msg = Messages.getString("UnoidlProject.PropertyChangeError"); //$NON-NLS-1$
            PluginLogger.warning(MessageFormat.format(msg, name, value), e);
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
    @Override
    public void saveAllProperties() {

        if (mLanguage == null || mOOo == null || mSdk == null) {
            PluginLogger.warning(Messages.getString("UnoidlProject.InconsistentConfigurationError")); //$NON-NLS-1$
            return;
        }

        saveUnoProject();

        // Save the build.properties file if exist
        File buildFile = getBuildFile();
        if (buildFile.exists()) {
            Properties properties = getBuildProperties(buildFile);
            setProjectBuildProperties(properties);
            saveBuildProperties(properties, buildFile);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveJavaBuildProperties(List<IResource> files) {
        File buildFile = getBuildFile();
        Properties properties = getBuildProperties(buildFile);
        String libs = String.join(", ", files.stream().map(IResource::getFullPath)
                                                      .map(IPath::makeRelative)
                                                      .map(IPath::toString)
                                                      .collect(Collectors.toList())); //$NON-NLS-1$
        String msg = Messages.getString("UnoidlProject.SaveJavaBuildProperties"); //$NON-NLS-1$
        PluginLogger.debug(MessageFormat.format(msg, buildFile.toString(), libs));
        properties.put("uno.java.classpath", libs); //$NON-NLS-1$
        saveBuildProperties(properties, buildFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasBuildFile() {
        return getBuildFile().exists();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createBuildProperties() {
        Properties properties = new Properties();
        setProjectBuildProperties(properties);
        saveBuildProperties(properties, getBuildFile());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFolder[] getBinFolders() {
        return getLanguage().getProjectHandler().getBinFolders(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFile getComponentsFile() {
        return getFile(COMPONENTS_FILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IFile getTypesFile() {
        return getFile(IDLTYPES_FILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getComponentsDocument() {
        return getComponentsDocument(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Document getComponentsDocument(boolean create) {
        Document document = null;
        File file = getComponentsFile().getLocation().toFile();
        try {
            DocumentBuilder builder = getNewDocumentBuilder();
            if (file.exists()) {
                document = builder.parse(file);
            } else if (create) {
                document = builder.newDocument();
            }
        } catch (IOException | SAXException | ParserConfigurationException e) {
            PluginLogger.error(Messages.getString("UnoidlProject.GetComponentsFileError"), e); //$NON-NLS-1$
        }
        return document;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element getComponentsElement(Document document) {
        Element components = null;
        File file = getComponentsFile().getLocation().toFile();
        if (file.exists()) {
            components = document.getDocumentElement();
        } else {
            components = document.createElement("components"); //$NON-NLS-1$
            components.setAttribute("xmlns", "http://openoffice.org/2010/uno-components"); //$NON-NLS-1$ //$NON-NLS-2$
            document.appendChild(components);
        }
        return components;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeImplementation(Element components, Element component, String implementation) {
        boolean removed = false;
        NodeList nodes = component.getElementsByTagName("implementation"); //$NON-NLS-1$
        int length = nodes.getLength();
        int i = length - 1;
        // Since some entries may be deleted, we need to iterate the nodes in reverse order
        while (i >= 0 && !removed) {
            removed = removeImplementation(components, component, nodes.item(i), implementation, length);
            i--;
        }
        return removed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element createImplementation(Document document, Element component,
                                        String implementation, String[] services) {
        Element element = document.createElement("implementation"); //$NON-NLS-1$
        setNameAttribute(element, implementation);
        for (String service : services) {
            element.appendChild(createServiceElement(document, service));
        }
        component.appendChild(element);
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element getImplementationElement(Element component, String implementation) {
        Element element = null;
        int i = 0;
        NodeList nodes = component.getElementsByTagName("implementation"); //$NON-NLS-1$
        while (i < nodes.getLength() && element == null) {
            element = getImplementation(nodes.item(i), implementation);
            i++;
        }
        return element;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addServiceElements(Document document, Element implementation, String[] services) {
        boolean changed = true;
        NodeList nodes = implementation.getElementsByTagName("service"); //$NON-NLS-1$
        int count = nodes.getLength();
        if (count > 0 && count == services.length) {
            changed = isServiceNamesChanged(nodes, count, services);
        }
        if (changed) {
            // Since some entries may be deleted, we need to iterate the nodes in reverse order
            for (int i = count - 1; i >= 0; i--) {
                implementation.removeChild(nodes.item(i));
            }
            for (String service : services) {
                implementation.appendChild(createServiceElement(document, service));
            }
        }
        return changed;
    }

    private boolean isServiceNamesChanged(NodeList nodes, int count, String[] services) {
        boolean changed = false;
        int i = 0;
        while (i < count && !changed) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (!hasNameAttribute(element) || !getNameAttribute(element).equals(services[i])) {
                    changed = true;
                }
            }
            i++;
        }
        return changed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeImplementationElements(Element component, Element implementation) {
        NodeList nodes = component.getChildNodes();
        // Since some entries may be deleted, we need to iterate the nodes in reverse order
        for (int i = nodes.getLength() - 1; i >= 0; i--) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (!element.equals(implementation)) {
                    component.removeChild(element);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeComponentsFile(Document document) {
        // Check presence of package.components in META-INF/manifest.xml file
        checkManifestComponents();
        writeXmlFile(document, getComponentsFile().getLocation().toFile());
    }

    /**
     * Check the META-INF/manifest.xml file
     * for the application/vnd.sun.star.uno-typelibrary;type=RDB file entry.
     */
    @Override
    public void checkManifestTypes() {
        // Check presence of type.rdb in META-INF/manifest.xml file
        File file = getFile(MANIFEST_FILE).getLocation().toFile();
        if (!file.exists()) {
            createManifestFile();
        }
        Document document = getManifestXmlDocument(file);
        if (document != null) {
            String mediatype = "application/vnd.sun.star.uno-typelibrary;type=RDB"; //$NON-NLS-1$
            checkManifestFileEntry(document, file, mediatype, IDLTYPES_FILE);
        }
    }

    // *************************************************************************
    // IProjectNature Implementation
    // *************************************************************************

    /**
     * {@inheritDoc}
     */
    @Override
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deconfigure() throws CoreException {
        dispose();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IProject getProject() {
        return mProject;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public String toString() {
        return "UNO Project " + getName(); //$NON-NLS-1$
    }

    /**
     * Save the .unoproject file.
     *
     */
    private void saveUnoProject() {
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
            properties.setProperty(SDK_NAME, mSdk.getName());
            properties.setProperty(IDL_DIR, mIdlDir);
            properties.setProperty(SRC_DIRECTORY, mSourcesDir);
            properties.setProperty(COMPANY_PREFIX, mCompanyPrefix);
            properties.setProperty(OUTPUT_EXT, mOutputExtension);

            out = new FileOutputStream(configFile);
            properties.store(out, Messages.getString("UnoidlProject.ConfigFileComment")); //$NON-NLS-1$

            // Refresh the configuration file
            getFile(CONFIG_FILE).refreshLocal(IResource.DEPTH_ZERO, null);

        } catch (Exception e) {
            PluginLogger.warning(Messages.getString("UnoidlProject.ConfigFileError"), e); //$NON-NLS-1$
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
     * Toggle an error marker on the project indicating that the there is either no LibreOffice nor SDK set.
     *
     * @param enabled
     *            <code>true</code> if the error marker should be set, <code>false</code> otherwise.
     */
    private void setErrorMarker(boolean enabled) {

        IProject prjRes = getProject();

        try {
            if (enabled) {
                IMarker marker = prjRes.createMarker(IMarker.PROBLEM);
                marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
                marker.setAttribute(IMarker.MESSAGE, Messages.getString("UnoidlProject.NoOOoSdkError")); //$NON-NLS-1$
            } else {
                prjRes.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
            }
        } catch (CoreException e) {
            if (enabled) {
                PluginLogger.error(Messages.getString("UnoidlProject.CreateMarkerError") + //$NON-NLS-1$
                    getProjectPath().toString(), e);
            } else {
                PluginLogger.error(Messages.getString("UnoidlProject.RemoveMarkerError"), e); //$NON-NLS-1$
            }
        }
    }

    /**
     * Get the build.properties project file.
     *
     * @return the build.properties project file.
     */
    private File getBuildFile() {
        return new File(getProjectPath().toOSString(), BUILD_FILE); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private void saveBuildProperties(Properties properties, File buildFile) {
        // Save the build.properties file
        FileWriter writer = null;

        try {
            writer = new FileWriter(buildFile);
            properties.store(writer, Messages.getString("UnoidlProject.BuildFileComment")); //$NON-NLS-1$
            writer.close();

        } catch (IOException e) {
            PluginLogger.warning(Messages.getString("UnoidlProject.BuildFileError"), e); //$NON-NLS-1$
        }
    }

    /**
     * Set the build.properties project properties.
     *
     * @param properties
     *          the properties of build.properties file
     */
    private void setProjectBuildProperties(Properties properties) {
        properties.put("office.install.dir", getOOo().getHome()); //$NON-NLS-1$ //$NON-NLS-2$
        properties.put("sdk.dir", getSdk().getHome()); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Get the build.properties project properties.
     *
     * @param buildFile
     *          the build.properties file
     *
     * @return the build.properties project properties.
     */
    private Properties getBuildProperties(File buildFile) {
        Properties properties = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader(buildFile);
            properties.load(reader);
            reader.close();

        } catch (IOException e) {
            PluginLogger.warning(Messages.getString("UnoidlProject.BuildFileError"), e); //$NON-NLS-1$
        }
        return properties;
    }

    private Element getImplementation(Node node, String implementation) {
        Element element = null;
        if (node.getNodeType() == Node.ELEMENT_NODE && hasNameAttribute((Element) node, implementation)) {
            element = (Element) node;
        }
        return element;
    }

    private boolean removeImplementation(Element components, Element component,
                                         Node node, String implementation, int length) {
        boolean removed = false;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            if (hasNameAttribute((Element) node, implementation)) {
                component.removeChild(node);
                // If this is the latest implementation then we also need to remove the component.
                if (length == 1) {
                    components.removeChild(component);
                }
                removed = true;
            }
        }
        return removed;
    }

    private Element createServiceElement(Document document, String service) {
        Element element = document.createElement("service"); //$NON-NLS-1$
        setNameAttribute(element, service);
        return element;
    }

    private boolean hasNameAttribute(Element element, String value) {
        return hasNameAttribute(element) && getNameAttribute(element).equals(value);
    }

    private boolean hasNameAttribute(Element element) {
        return element.hasAttribute("name"); //$NON-NLS-1$
    }

    private String getNameAttribute(Element element) {
        return element.getAttribute("name"); //$NON-NLS-1$
    }

    private void setNameAttribute(Element element, String value) {
        element.setAttribute("name", value); //$NON-NLS-1$
    }

    /**
     * Check the META-INF/manifest.xml file
     * for the application/vnd.sun.star.uno-components file entry.
     */
    private void checkManifestComponents() {
        File file = getFile(MANIFEST_FILE).getLocation().toFile();
        if (!file.exists()) {
            createManifestFile();
        }
        Document document = getManifestXmlDocument(file);
        if (document != null) {
            String mediatype = "application/vnd.sun.star.uno-components"; //$NON-NLS-1$
            checkManifestFileEntry(document, file, mediatype, COMPONENTS_FILE);
        }
    }

    private void createManifestFile() {
        File file = getFile(MANIFEST_FILE).getLocation().toFile();
        if (!file.exists()) {
            Document document = createManifestXmlDocument(file);
            if (document != null) {
                writeXmlFile(document, file);
            }
        }
    }

    private Document getManifestXmlDocument(File file) {
        Document document = null;
        if (file.exists()) {
            try {
                document = getNewDocumentBuilder(true).parse(file);
            } catch (IOException | SAXException | ParserConfigurationException e) {
                PluginLogger.error(Messages.getString("UnoidlProject.GetManifestXmlDocumentError"), e); //$NON-NLS-1$
            }
        } else {
            document = createManifestXmlDocument(file);
        }
        return document;
    }

    private Document createManifestXmlDocument(File file) {
        Document document = null;
        file.getParentFile().mkdir();
        String prefix = "manifest"; //$NON-NLS-1$
        String uri = "http://openoffice.org/2001/manifest"; //$NON-NLS-1$
        try {
            document = getNewDocumentBuilder(true).newDocument();
            document.appendChild(document.createElementNS(uri, prefix + ":manifest")); //$NON-NLS-1$
        } catch (ParserConfigurationException e) {
            PluginLogger.error(Messages.getString("UnoidlProject.CreateManifestXmlDocumentError"), e); //$NON-NLS-1$
        }
        return document;
    }

    private DocumentBuilder getNewDocumentBuilder() throws ParserConfigurationException {
        return getNewDocumentBuilder(false, false);
    }

    private DocumentBuilder getNewDocumentBuilder(boolean namespace) throws ParserConfigurationException {
        return getNewDocumentBuilder(namespace, false);
    }

    private DocumentBuilder getNewDocumentBuilder(boolean namespace, boolean dtd) throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(namespace);
        if (dtd) {
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
        }
        return factory.newDocumentBuilder();
    }

    private void checkManifestFileEntry(Document document, File file, String mediaType, String fullpath) {
        boolean changed = true;
        Element entry = null;
        Element root = document.getDocumentElement();
        String uri = root.getNamespaceURI();
        String prefix = root.getPrefix();
        int i = 0;
        NodeList nodes = root.getElementsByTagNameNS(uri, "file-entry"); //$NON-NLS-1$
        while (i < nodes.getLength() && entry == null) {
            Node node = nodes.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;
                if (element.hasAttributeNS(uri, "media-type") && //$NON-NLS-1$
                    element.getAttributeNS(uri, "media-type").equals(mediaType)) { //$NON-NLS-1$
                    entry = element;
                }
            }
            i++;
        }
        if (entry == null) {
            entry = document.createElementNS(uri, prefix + ":file-entry"); //$NON-NLS-1$
            entry.setAttributeNS(uri, prefix + ":media-type", mediaType); //$NON-NLS-1$
            entry.setAttributeNS(uri, prefix + ":full-path", fullpath); //$NON-NLS-1$
            root.appendChild(entry);
        } else if (!entry.hasAttributeNS(uri, "full-path") || //$NON-NLS-1$
                   !entry.getAttributeNS(uri, "full-path").equals(fullpath)) { //$NON-NLS-1$
            entry.setAttributeNS(uri, prefix + ":full-path", fullpath); //$NON-NLS-1$
        } else {
            changed = false;
        }
        if (changed) {
            writeXmlFile(document, file);
        }
    }

    private void writeXmlFile(Document document, File file) {
        writeXmlFile(document, file, false);
    }

    private void writeXmlFile(Document document, File file, boolean dtd) {
        // Write dom document to a file
        try {
            // Hide the standalone="no"
            document.setXmlStandalone(true);
            // Remove whitespaces outside tags
            document.normalize();
            XPath path = XPathFactory.newInstance().newXPath();
            NodeList nodes = (NodeList) path.evaluate("//text()[normalize-space()='']", //$NON-NLS-1$
                                                      document,
                                                      XPathConstants.NODESET);

            for (int i = nodes.getLength() - 1; i >= 0; i--) {
                Node node = nodes.item(i);
                node.getParentNode().removeChild(node);
            }

            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setAttribute("indent-number", 2); //$NON-NLS-1$
            Transformer transformer = factory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8"); //$NON-NLS-1$

            if (dtd) {
                String doctype = "-//OpenOffice.org//DTD Manifest 1.0//EN"; //$NON-NLS-1$
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, doctype);
                transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "Manifest.dtd"); //$NON-NLS-1$
            } else {
                // XML declaration on its own line
                transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes"); //$NON-NLS-1$
            }

            // Pretty print XML
            transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

            try  (FileOutputStream stream = new FileOutputStream(file)) {
                transformer.transform(new DOMSource(document), new StreamResult(stream));
            }
            String msg = Messages.getString("UnoidlProject.WriteXmlFile"); //$NON-NLS-1$
            PluginLogger.debug(MessageFormat.format(msg, file.toString()));
        } catch (IOException | TransformerException | XPathExpressionException e) {
            String msg = Messages.getString("UnoidlProject.WriteXmlFileError"); //$NON-NLS-1$
            PluginLogger.error(MessageFormat.format(msg, file.toString()), e);
        }
    }

}
