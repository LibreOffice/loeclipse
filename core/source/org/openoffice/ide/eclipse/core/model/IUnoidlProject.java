package org.openoffice.ide.eclipse.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.openoffice.ide.eclipse.core.model.language.ILanguage;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

public interface IUnoidlProject {

	/**
	 * <code>org.openoffice.ide.eclipse.idlfolder</code> is a
	 * persistent folder property that determines whether the
	 * folder can contain unoidl files or not. 
	 */
	public static final String IDL_FOLDER = "idlfolder"; //$NON-NLS-1$
	
	/**
	 * Cleans up the project before destroying it
	 */
	public void dispose();
	
	//---------------------------------------------------- Properties accessors

	/**
	 * Gets the project implementation language.
	 */
	public ILanguage getLanguage();
	
	/**
	 * Gets the project name.
	 */
	public String getName();
	
	/**
	 * Gets the selected OOo
	 */
	public IOOo getOOo();
	
	/**
	 * Gets the selected SDK
	 */
	public ISdk getSdk();
	
	/**
	 * Set the language of the project implementation. This method can
	 * be called only once on a project to avoid project nature problems.
	 * 
	 * @param aLanguage the new language
	 */
	public void setLanguage(ILanguage aLanguage);
	
	/**
	 * Sets the selected OOo
	 */
	public void setOOo(IOOo aOOo);
	
	/**
	 * Sets the selected SDK
	 */
	public void setSdk(ISdk aSdk);
	
	/**
	 * Set a property to the project. 
	 * 
	 * <p>This can be used by plugins to set their own properties on the project.</p>
	 * 
	 * @param name the property name
	 * @param value the property value
	 */
	public void setProperty(String name, String value);
	
	/**
	 * Get a project's property. 
	 * 
	 * <p>This can be used by plugins to get their own properties from the project.</p>
	 * 
	 * @param name the property name
	 * @return the value of the property or <code>null</code> if it doesn't
	 * 		exists
	 */
	public String getProperty(String name);
	
	//-------------------------------------------------------- Config accessors
	
	/**
	 * Gets the root module of the project. It corresponds to the prefix
	 * transformed as an idl scoped name. For example, if the company
	 * prefix is set to <code>foo.bar</code>, the root module will be
	 * <code>foo::bar</code>.
	 */
	public String getRootModule();
	
	/**
	 * Gets the root module path of the project. It corresponds to the path
	 * to the root module definition. For example, if the company prefix
	 * is set to <code>foo.bar</code>, the root module path will be
	 * <code>idl/foo/bar</code>
	 */
	public IPath getRootModulePath();
	
	/**
	 * Sets the company prefix
	 * 
	 * @param prefix new company prefix 
	 */
	public void setCompanyPrefix(String prefix);
	
	/**
	 * Returns the company prefix used in the idl modules and implementation 
	 * trees. For example, it could be <code>org.openoffice</code> for any code
	 * created by the OpenOffice.org community. 
	 * 
	 * @return the company prefix
	 */
	public String getCompanyPrefix();
	
	/**
	 * Sets the output extension
	 * 
	 * @param outputExt new output extension to set
	 */
	public void setOutputExtension(String outputExt);
	
	/**
	 * Returns the package or namespace name used for the implementation. If
	 * the company prefix is <code>org.openoffice</code> and the output extension
	 * is <code>comp</code>, then the implementation namespace will be:
	 * <code>org.openoffice.comp</code>
	 *  
	 * @return the implementation namespace
	 */
	public String getOutputExtension();
	
	//------------------------------------------------------------ Path getters

	/**
	 * @return the path to the project directory containing the temporary
	 * build files. This path is relative to the project folder.
	 */
	public IPath getBuildPath();
	
	/**
	 * @return the path to the project directory containing the idl files.
	 * This path is relative to the project folder.
	 */
	public IPath getIdlPath();
	
	/**
	 * @return the path to the project implementation directory. This path is
	 * relative to the project folder.
	 */
	public IPath getImplementationPath();
	
	/**
	 * @return the full path to the project 
	 */
	public IPath getProjectPath();
	
	/**
	 * @return the path to the sources directory: that is "source". This path is
	 * relative to the project folder.
	 */
	public IPath getSourcePath();
	
	/**
	 * @return the path to the project <code>types.rdb</code> file. This path is
	 * relative to the project folder.
	 */
	public IPath getTypesPath();
	
	/**
	 * @return the path to the project <code>services.rdb</code> file. This path
	 * is relative to the project folder.
	 */
	public IPath getServicesPath();
	
	/**
	 * @return the path to the project directory containing the generated 
	 * urd files. This path is relative to the project folder.
	 */
	public IPath getUrdPath();
	
	//----------------------------------------------- Project resources getters
	
	/**
	 * Returns the file handle for the given project relative path. If the
	 * file doesn't exists, the handle won't be null.
	 * 
	 * @see org.eclipse.core.resources.IProject#getFile(java.lang.String)
	 */
	public IFile getFile(IPath path);
	
	/**
	 * Returns the file handle for the given project relative path. If the
	 * file doesn't exists, the handle won't be null.
	 * 
	 * @see org.eclipse.core.resources.IProject#getFile(java.lang.String)
	 */
	public IFile getFile(String path);
	
	/**
	 * Returns the folder handle for the given project relative path. If the
	 * folder doesn't exists, the handle won't be null.
	 * 
	 * @see org.eclipse.core.resources.IProject#getFolder(java.lang.String)
	 */
	public IFolder getFolder(IPath path);
	
	/**
	 * Returns the folder handle for the given project relative path. If the
	 * folder doesn't exists, the handle won't be null.
	 * 
	 * @see org.eclipse.core.resources.IProject#getFolder(java.lang.String)
	 */
	public IFolder getFolder(String path);

	/**
	 * Defines the directory containing the IDL files
	 * 
	 * @param idlDir the IDL directory
	 */
	public void setIdlDir(String idlDir);
	
	/**
	 * Defines the directory containing the sources
	 * 
	 * @param sourcesDir the sources directory
	 */
	public void setSourcesDir(String sourcesDir);

	/**
	 * Saves the UNO project configuration in a hidden file.
	 */
	public void saveAllProperties();
}
