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
package org.libreoffice.ide.eclipse.core.model;

import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.libreoffice.ide.eclipse.core.model.config.IOOo;
import org.libreoffice.ide.eclipse.core.model.config.ISdk;
import org.libreoffice.ide.eclipse.core.model.language.AbstractLanguage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Interface for a UNO project.
 */
public interface IUnoidlProject {

    public static final String DESCRIPTION_FILENAME = "description.xml"; //$NON-NLS-1$

    /**
     * <code>org.libreoffice.ide.eclipse.idlfolder</code> is a persistent folder property that determines whether the
     * folder can contain unoidl files or not.
     */
    public static final String IDL_FOLDER = "idlfolder"; //$NON-NLS-1$

    public static final String XCS_EXTENSION = "xcs"; //$NON-NLS-1$

    public static final String XCU_EXTENSION = "xcu"; //$NON-NLS-1$

    public static final String IDL_EXTENSION = "idl"; //$NON-NLS-1$

    public static final String IDLTYPES_FILE = "types.rdb"; //$NON-NLS-1$

    public static final String COMPONENTS_FILE = "package.components"; //$NON-NLS-1$

    /**
     * Cleans up the project before destroying it.
     */
    public void dispose();

    // ---------------------------------------------------- Properties accessors

    /**
     * @return the project implementation language.
     */
    public AbstractLanguage getLanguage();

    /**
     * @return the project name.
     */
    public String getName();

    /**
     * @return the selected LibreOffice
     */
    public IOOo getOOo();

    /**
     * @return the selected SDK
     */
    public ISdk getSdk();

    /**
     * Set the language of the project implementation. This method can be called only once on a project to avoid project
     * nature problems.
     *
     * @param language
     *            the new language
     */
    public void setLanguage(AbstractLanguage language);

    /**
     * Sets the selected LibreOffice.
     *
     * @param ooo
     *            the selected LibreOffice
     */
    public void setOOo(IOOo ooo);

    /**
     * Sets the selected SDK.
     *
     * @param sdk
     *            the selected SDK
     */
    public void setSdk(ISdk sdk);

    /**
     * Set a property to the project.
     *
     * <p>
     * This can be used by plugins to set their own properties on the project.
     * </p>
     *
     * @param name
     *            the property name
     * @param value
     *            the property value
     */
    public void setProperty(String name, String value);

    /**
     * Get a project's property.
     *
     * <p>
     * This can be used by plugins to get their own properties from the project.
     * </p>
     *
     * @param name
     *            the property name
     * @return the value of the property or <code>null</code> if it doesn't exists
     */
    public String getProperty(String name);

    // -------------------------------------------------------- Config accessors

    /**
     * Gets the root module of the project.
     *
     * <p>
     * It corresponds to the prefix transformed as an idl scoped name. For example, if the company prefix is set to
     * <code>foo.bar</code>, the root module will be <code>foo::bar</code>.
     * </p>
     *
     * @return the root module of the project
     */
    public String getRootModule();

    /**
     * Gets the root module path of the project.
     *
     * <p>
     * It corresponds to the path to the root module definition. For example, if the company prefix is set to
     * <code>foo.bar</code>, the root module path will be <code>idl/foo/bar</code>.
     * </p>
     *
     * @return the root module path of the project
     */
    public IPath getRootModulePath();

    /**
     * Sets the company prefix.
     *
     * @param prefix
     *            new company prefix
     */
    public void setCompanyPrefix(String prefix);

    /**
     * Returns the company prefix used in the idl modules and implementation trees. For example, it could be
     * <code>org.libreoffice</code> for any code created by the LibreOffice community.
     *
     * @return the company prefix
     */
    public String getCompanyPrefix();

    /**
     * Sets the output extension.
     *
     * @param outputExt
     *            new output extension to set
     */
    public void setOutputExtension(String outputExt);

    /**
     * Returns the package or namespace name used for the implementation.
     *
     * <p>
     * If the company prefix is <code>org.libreoffice</code> and the output extension is <code>comp</code>, then the
     * implementation namespace will be: <code>org.libreoffice.comp</code>.
     * </p>
     *
     * @return the implementation namespace
     */
    public String getOutputExtension();

    // ------------------------------------------------------------ Path getters

    /**
     * @return the path to the project directory containing the temporary build files. This path is relative to the
     *         project folder.
     */
    public IPath getBuildPath();

    /**
     * @return the path to the project directory containing the idl files. This path is relative to the project folder.
     */
    public String getIdlDir();

    /**
     * @return the path to the project directory containing the idl files. This path is relative to the project folder.
     */
    public IPath getIdlPath();

    /**
     * @return the path to the project implementation directory. This path is relative to the project folder.
     */
    public IPath getImplementationPath();

    /**
     * @return the IProject to the project
     */
    public IProject getProject();

    /**
     * @return the full path to the project
     */
    public IPath getProjectPath();

    /**
     * @return the path to the sources directory: that is "source". This path is relative to the project folder.
     */
    public IPath getSourcePath();

    /**
     * @return the path to the project <code>types.rdb</code> file. This path is relative to the project folder.
     */
    public IPath getTypesPath();

    /**
     * @return the path to the project <code>services.rdb</code> file. This path is relative to the project folder.
     */
    public IPath getServicesPath();

    /**
     * @return the path to the project directory containing the generated urd files. This path is relative to the
     *         project folder.
     */
    public IPath getUrdPath();

    /**
     * @return the path to the project's folder containing the distribution .oxt file.
     */
    public IPath getDistPath();

    /**
     * @return the folder containing the distribution .oxt file. If the folder does not exist then it is created.
     * @throws CoreException
     *             if we were unable to create the folder.
     */
    public IFolder getDistFolder() throws CoreException;

    /**
     * @return the path to the project's folder used to store the user profile when running/debugging LibreOffice in a
     *         clean environment. This way we do not mangle with the system wide installed, LibreOffice settings.
     */
    public IPath getOfficeUserProfilePath();

    /**
     * @return the folder used to store the user profile when running/debugging LibreOffice in a clean environment. This
     *         way we do not mangle with the system wide installed, LibreOffice settings. If the folder does not exist
     *         then it is created.
     * @throws CoreException
     *             if we were unable to create the folder.
     */
    public IFolder getOfficeUserProfileFolder() throws CoreException;

    // ----------------------------------------------- Project resources getters

    /**
     * Returns the file handle for the given project relative path. If the file doesn't exists, the handle will be
     * <code>null</code>.
     *
     * @param path
     *            the path to the folder to get
     *
     * @return the folder handle or <code>null</code>
     *
     * @see org.eclipse.core.resources.IProject#getFile(java.lang.String)
     */
    public IFile getFile(IPath path);

    /**
     * Returns the file handle for the given project relative path. If the file doesn't exists, the handle will be
     * <code>null</code>.
     *
     * @param path
     *            the path to the folder to get
     *
     * @return the folder handle or <code>null</code>
     *
     * @see org.eclipse.core.resources.IProject#getFile(java.lang.String)
     */
    public IFile getFile(String path);

    /**
     * Returns the file handle for project component XML file.
     *
     * @return the file handle or <code>null</code>
     */
    public IFile getComponentsFile();

    /**
     * Returns the file handle for idl types file.
     *
     * @return the file handle or <code>null</code>
     */
    public IFile getTypesFile();

    /**
     * Returns the XML document for project component XML file.
     *
     * @return the XML document or <code>null</code>
     */
    public Document getComponentsDocument();

    /**
     * Returns the XML document for project component XML file.
     *
     * @param create create the XML document if not exist
     *
     * @return the XML document or <code>null</code>
     */
    public Document getComponentsDocument(boolean create);

    /**
     * Returns the components XML element.
     *
     * @param document the XML document
     *
     * @return the components XML element.
     */
    public Element getComponentsElement(Document document);

    /**
     * Remove the implementation XML element.
     *
     * @param components the XML document
     * @param component the XML document
     * @param implementation the XML document
     *
     * @return <code>true</code> if implementation has been removed or <code>false</code>
     */
    public boolean removeImplementation(Element components, Element component, String implementation);

    /**
     * Check manifest types file entry.
     * @throws ParserConfigurationException 
     */
    public void checkManifestTypes();

    /**
     * Get the implementation XML element.
     *
     * @param element the component XML element
     * @param implementation the implementation name
     *
     * @return the implementation XML element
     */
    public Element getImplementationElement(Element element, String implementation);

    /**
     * Create a new implementation.
     *
     * @param document the XML document
     * @param component the XML component
     * @param implName the implementation name
     * @param serviceName the service name
     *
     * @return the created implementation element
     */
    public Element createImplementation(Document document, Element component, String implName, String serviceName);

    /**
     * Check if service exist.
     *
     * @param document the XML document
     * @param element the implementation XML element
     * @param service the service name
     *
     * @return <code>true</code> if service exist or <code>false</code>
     */
    public boolean addServiceElement(Document document, Element element, String service);

    /**
     * Remove all implementation elements from component except the given implementation.
     *
     * @param element the component XML element
     * @param implementation the implementation element to keep
     *
     */
    public void removeImplementationElements(Element element, Element implementation);

    /**
     * Write the components XML file.
     *
     * @param document the XML document
     *
     */
    public void writeComponentsFile(Document document);

    /**
     * Returns the folder handle for the given project relative path. If the folder doesn't exists, the handle will be
     * <code>null</code>.
     *
     * @param path
     *            the path to the folder to get
     *
     * @return the folder handle or <code>null</code>
     *
     * @see org.eclipse.core.resources.IProject#getFolder(java.lang.String)
     */
    public IFolder getFolder(IPath path);

    /**
     * Returns the folder handle for the given project relative path. If the folder doesn't exists, the handle will be
     * <code>null</code>.
     *
     * @param path
     *            the path to the folder to get
     *
     * @return the folder handle or <code>null</code>
     *
     * @see org.eclipse.core.resources.IProject#getFolder(java.lang.String)
     */
    public IFolder getFolder(String path);

    /**
     * Defines the directory containing the IDL files.
     *
     * @param idlDir
     *            the IDL directory
     */
    public void setIdlDir(String idlDir);

    /**
     * Defines the directory containing the sources.
     *
     * @param sourcesDir
     *            the sources directory
     */
    public void setSourcesDir(String sourcesDir);

    /**
     * Saves the UNO project configuration in a hidden file.
     */
    public void saveAllProperties();

    /**
     * Create the Ant build.properties configuration file.
     *
     */
    public void createBuildProperties();

    /**
     * @return the language dependent binaries folders.
     */
    public IFolder[] getBinFolders();

    /**
     * @return if the build.properties project file exist.
     */
    public boolean hasBuildFile();

    /**
     * Save in the Ant build.properties the Java class path.
     *
     * @param libs
     *            the files list composing the class path
     */
    public void saveJavaBuildProperties(List<IResource> libs);

}
