package org.openoffice.ide.eclipse.core.model;

import java.util.Hashtable;

/** is an interface to handle Uno composites. 
 * 
 *  <p>Their goal is to provide a simple tree structure to generate UNO-IDL
 *  files. A Uno composite could be of several types:
 *  <ul>
 *     <li><code>COMPOSITE_TYPE_NOTSET</code>: the type isn't set 
 *  			(very bad)</li>
 *     <li><code>COMPOSITE_TYPE_FILE</code>: the node is representing a 
 *     		file</li>
 *     <li><code>COMPOSITE_TYPE_FOLDER</code>: the node is representing 
 *     		a directory</li>
 *     <li><code>COMPOSITE_TYPE_TEXT</code>: The node is representing a
 *          piece of text</li>
 *     <li></li>
 *  </ul></p>
 *  
 *   <p>The logical use of a UNO composite will respect the following steps:
 *   <ol>
 *     <li>setting up the composite type</li>
 *     <li>configuring the composite depending on its type</li>
 *     <li>adding children to the composite</li>
 *     <li>creating the composite file, folder or text</li>
 *   </ol></p>
 * 
 *  @author cbosdonnat
 *
 */
public interface IUnoComposite {

	public static final int COMPOSITE_TYPE_NOTSET = -1;
	
	/** configures the composite as a file. The filename
	 *  has to be filed in order to create the composite.
	 * 
	 */
	public static final int COMPOSITE_TYPE_FILE = 0;
	
	/** configures the composite as a folder. The property filename
	 *  has to be filed in order to create the composite.
	 * 
	 */
	public static final int COMPOSITE_TYPE_FOLDER = 1;
	
	/** configures the composite as a UNO-IDL object with a textual 
	 *  representation. Thus the properties and template have to be filled
	 *  in order to create the composite.
	 * 
	 */
	public static final int COMPOSITE_TYPE_TEXT = 2;
	
	/**
	 * Release the references held by the object
	 */
	public void dispose();
	
    //------------------------------------------------- Tree structural methods
	
	
	/** return all the node children if any.
	 * 
	 * @return an array of zero or more IUnoComposite nodes
	 */
	public IUnoComposite[] getChildren();
	
	/** adds a child to the node. No name uniqueness will be checked. 
	 * 
	 * @param aChild the child to add
	 */
	public void addChild(IUnoComposite aChild);
	
	/** Removes all the children nodes.
	 * 
	 */
	public void removeAll();
	
	//----------------------------------------------------- Uno objects methods
	
	/** sets the type of the composite. The value has to be chosen among the
	 *  <code>COMPOSITE_TYPE_*</code> types. 
	 *  
	 *  <p>This method can be called only once to avoid strange reconfigurations
	 *  of the node. Moreover, it should be called first to setup the node 
	 *  before to set the properties or template. Any other operation done with
	 *  the type unset will be simply ignored.</p>
	 *  
	 *  <p>Please note that a <code>COMPOSITE_TYPE_TEXT</code> node can only 
	 *  contain <code>COMPOSITE_TYPE_TEXT</code> children. Otherwise they won't
	 *  be taken into account for the node <code>toString()</code> execution.</p>
	 * 
	 * @param aType the COMPOSITE_TYPE_XXX type of the node
	 */
	public void setType(int aType);
	
	/** returns the type of the composite. The value is one of the 
	 * <code>COMPOSITE_TYPE_*</code> types.
	 * 
	 * @return the type of the composite
	 */
	public int getType();
	
	/** set the node for a COMPOSITE_TYPE_TEXT only. The template uses some
	 *  properties defined inthe properties parameter.
	 * 
	 *  <p>The template is a string where <code>\n</code> is the end of line,
	 *  and the properties are written using the form <code>${prop_name}</code>.
	 *  The property name has to correspond to one of the properties given in 
	 *  attribute, otherwises the empty string will be used instead. The 
	 *  special property <code>${children}</code> will be replaced by the
	 *  children <code>toString()</code> result</p>
	 *  
	 *  <p>Example of template:</p>
	 *  <p><pre>module ${name} { ${children} 
	 *  };</pre></p>
	 *  
	 *  <p>Example of properties associated:
	 *    <ul>
	 *      <li>name = mymodule</li>
	 *    </ul>
	 *  </p>
	 * 
	 * @param properties properties table. The name is associated to the value.
	 * @param template the string template used in the 
	 *       <code>toString()</code> method.
	 */
	public void configure(Hashtable properties, String template);
	
	/** sets the <code>COMPOSITE_TYPE_FILE</code> or 
	 * <code>COMPOSITE_TYPE_FOLDER</code> filename.
	 * 
	 * <p>There is no need to have a very deep folder and file tree, because
	 * only one will be sufficient to create all the parents.</p>
	 * 
	 * @param filename the composite filename.
	 */
	public void configure(String filename);
	
	/** sets whether the output string of the text composite will be indented
	 *  or not. The method has no effect if the type is different from
	 *  <code>COMPOSITE_TYPE_TEXT</code>
	 * 
	 * @param toIndent <code>true</code> will add indentation.
	 */
	public void setIndented(boolean toIndent);
	
	/** creates the file or folder with its non-existing parents.
	 * 
	 * @param force <code>true</code> let the method overwrite the existing
	 * 			file if needed.
	 * @throws Exception
	 * 		If there is any problem during the file or folder creation
	 */
	public void create(boolean force) throws Exception;
	
	/** returns the string representation of the node is it has a textual
	 *  representation. The string will be a reference for the files 
	 *  and folders.
	 * 
	 * @return the string representing the node.
	 */
	public String toString();
}
