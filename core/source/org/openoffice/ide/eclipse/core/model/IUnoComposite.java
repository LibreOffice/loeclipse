/*************************************************************************
 *
 * $RCSfile: IUnoComposite.java,v $
 *
 * $Revision: 1.6 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2007/11/25 20:32:30 $
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
package org.openoffice.ide.eclipse.core.model;

import java.util.Hashtable;

/** is an interface to handle Uno composites.
 *
 *  <p>Their goal is to provide a simple tree structure to generate UNO-IDL
 *  files. A Uno composite could be of several types:
 *  <ul>
 *     <li><code>COMPOSITE_TYPE_NOTSET</code>: the type isn't set
 *              (very bad)</li>
 *     <li><code>COMPOSITE_TYPE_FILE</code>: the node is representing a
 *             file</li>
 *     <li><code>COMPOSITE_TYPE_FOLDER</code>: the node is representing
 *             a directory</li>
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
 *  @author cedricbosdo
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
     * Release the references held by the object.
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
     * @param pChild the child to add
     */
    public void addChild(IUnoComposite pChild);

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
     * @param pType the COMPOSITE_TYPE_XXX type of the node
     */
    public void setType(int pType);

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
     * @param pProperties properties table. The name is associated to the value.
     * @param pTemplate the string template used in the
     *       <code>toString()</code> method.
     */
    public void configure(Hashtable<String, Object> pProperties, String pTemplate);

    /** sets the <code>COMPOSITE_TYPE_FILE</code> or
     * <code>COMPOSITE_TYPE_FOLDER</code> filename.
     *
     * <p>There is no need to have a very deep folder and file tree, because
     * only one will be sufficient to create all the parents.</p>
     *
     * @param pFilename the composite filename.
     */
    public void configure(String pFilename);

    /** sets whether the output string of the text composite will be indented
     *  or not. The method has no effect if the type is different from
     *  <code>COMPOSITE_TYPE_TEXT</code>
     *
     * @param pToIndent <code>true</code> will add indentation.
     */
    public void setIndented(boolean pToIndent);

    /**
     * defines the string which has to be inserted between two children. If no
     * separator is specified, the children will simply be concatenated.
     *
     * @param pSeparator the separator to add between the children
     */
    public void setChildrenSeparator(String pSeparator);

    /** creates the file or folder with its non-existing parents.
     *
     * @param pForce <code>true</code> let the method overwrite the existing
     *             file if needed.
     * @throws Exception
     *         If there is any problem during the file or folder creation
     */
    public void create(boolean pForce) throws Exception;

    /** returns the string representation of the node is it has a textual
     *  representation. The string will be a reference for the files
     *  and folders.
     *
     * @return the string representing the node.
     */
    @Override
    public String toString();
}
