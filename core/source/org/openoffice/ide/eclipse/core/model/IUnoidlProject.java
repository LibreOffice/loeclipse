package org.openoffice.ide.eclipse.core.model;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.openoffice.ide.eclipse.core.preferences.IOOo;
import org.openoffice.ide.eclipse.core.preferences.ISdk;

public interface IUnoidlProject {

	/**
	 * <code>org.openoffice.ide.eclipse.idlfolder</code> is a
	 * persistent folder property that determines whether the
	 * folder can contain unoidl files or not. 
	 */
	public static final String IDL_FOLDER = "idlfolder"; //$NON-NLS-1$
	
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
	 * Add a new persistent property to the project. This can be used by
	 * plugins to set their own properties on the project.
	 * 
	 * @param name the property qualified name
	 * @param value the property value
	 */
	public void addProperty(QualifiedName name, String value);
	
	/**
	 * Get a project's persistent property. This can be used by
	 * plugins to get their own properties from the project.
	 * 
	 * @param name the property name
	 * @return the value of the property or <code>null</code> if it doesn't
	 * 		exists
	 */
	public String getProperty(QualifiedName name);
	
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
	 * Sets the output extension
	 * 
	 * @param outputExt new output extension to set
	 */
	public void setOutputExtension(String outputExt);
	
	//------------------------------------------------------------ Path getters

	/**
	 * Returns the path to the project directory containing the temporary
	 * build files.
	 */
	public IPath getBuildPath();
	
	/**
	 * Returns the path to the project directory containing the idl files.
	 */
	public IPath getIdlPath();
	
	/**
	 * Returns the path to the project implementation directory
	 */
	public IPath getImplementationPath();
	
	/**
	 * Returns the full path to the project 
	 */
	public IPath getProjectPath();
	
	/**
	 * @return the path to the sources directory: that is "source".
	 */
	public IPath getSourcePath();
	
	/**
	 * @return the path to the project <code>types.rdb</code> file.
	 */
	public IPath getTypesPath();
	
	/**
	 * @return the path to the project <code>services.rdb</code> file.
	 */
	public IPath getServicesPath();
	
	/**
	 * @return the path to the project directory containing the generated 
	 * urd files.
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
}
