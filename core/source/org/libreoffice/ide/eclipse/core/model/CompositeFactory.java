/*************************************************************************
 *
 * $RCSfile: CompositeFactory.java,v $
 *
 * $Revision: 1.8 $
 *
 * last change: $Author: cedricbosdo $ $Date: 2008/12/13 13:42:50 $
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
package org.libreoffice.ide.eclipse.core.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.libreoffice.ide.eclipse.core.internal.model.UnoComposite;

/**
 * This class provides static methods to generate well-formed uno composites. Thus there is no need to know how to
 * create them to use them.
 *
 * <p>
 * <b>Note:</b> This class has to be extended to add new UNO-IDL code generation.
 * </p>
 */
public final class CompositeFactory {

    /**
     * Creates a file node from the fully qualified name of the type which should be described inside.
     *
     * @param pFullName
     *            is the fully qualified name of the type described in the file to create (eg:
     *            <code>org::libreoffice::foo</code>)
     * @param pProject
     *            is the uno project in which to add the type.
     *
     * @return a Uno composite representing a file, or <code>null</code> if the fullName is null or an empty string
     */
    public static IUnoComposite createTypeFile(String pFullName, IUnoidlProject pProject) {

        IUnoComposite file = null;

        if (pFullName != null && !pFullName.equals("")) { //$NON-NLS-1$
            String fileName = pFullName.replace("::", "/") + ".idl"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            IFile filehandle = pProject.getFile(pProject.getIdlPath().append(fileName));

            file = createFile(filehandle);
        }

        return file;
    }

    /**
     * Creates a Uno composite representing a file from its filename.
     *
     * @param pFilehandle
     *            the relative filename
     *
     * @see #createTypeFile(String, IUnoidlProject) for file creation from Type name
     *
     * @return a Uno composite of FILE type
     */
    public static IUnoComposite createFile(IFile pFilehandle) {

        IUnoComposite file = null;

        if (pFilehandle != null) {
            file = new UnoComposite();
            file.setType(IUnoComposite.COMPOSITE_TYPE_FILE);
            file.configure(pFilehandle.getLocation().toString());
        }

        return file;
    }

    /**
     * Creates a uno composite representing the file content skeleton from the type name it should contain. All the file
     * contents has to be added in this composite.
     *
     * <p>
     * The produced result will be of the following form:
     *
     * <pre>
     * #ifndef __define_name_idl__
     * #define __define_name_idl__
     * [...]
     * #endif
     * </pre>
     * </p>
     *
     * @param pFullname
     *            the type fully qualified name (eg: <code>org::foo</code>)
     * @return a uno composite representing the file content
     */
    public static IUnoComposite createFileContent(String pFullname) {

        IUnoComposite content = null;

        if (pFullname != null && !pFullname.equals("")) { //$NON-NLS-1$
            content = new UnoComposite();
            content.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);

            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            String define = pFullname.replace("::", "_"); //$NON-NLS-1$ //$NON-NLS-2$
            define = "__" + define + "_idl__"; //$NON-NLS-1$ //$NON-NLS-2$
            properties.put("define", define.toLowerCase()); //$NON-NLS-1$

            String template = "#ifndef ${define}\n" + //$NON-NLS-1$
                "#define ${define}\n" + //$NON-NLS-1$
                "\n${children}\n" + //$NON-NLS-1$
                "#endif\n"; //$NON-NLS-1$

            content.configure(properties, template);
        }

        return content;
    }

    /**
     * Creates a Uno composite representing an include line. It uses the type's fully qualified name to recompose the
     * file name. It supposes that the type is contained in a file of it's name.
     *
     * <p>
     * If the method is called with the type <code>foo::XFoo</code>, the file <code>foo/XFoo.idl</code> will be
     * returned. The resulting include line is always in &lt;, &gt; characters.
     * </p>
     *
     * @param pFullName
     *            the fully qualified name of the type to include
     *
     * @return a parametrized uno composite
     */
    public static IUnoComposite createInclude(String pFullName) {

        IUnoComposite include = null;

        if (pFullName != null && !pFullName.equals("")) { //$NON-NLS-1$
            String fileName = pFullName.replace("::", "/") + ".idl"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

            include = new UnoComposite();
            include.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("file", fileName); //$NON-NLS-1$
            String template = "#include <${file}>\n"; //$NON-NLS-1$
            include.configure(properties, template);
        }

        return include;
    }

    /**
     * Creates a Uno composite directory.
     *
     * @param pFullName
     *            is the fully qualified name of the module to create (eg: <code>org::libreoffice::foo</code>)
     * @param pProject
     *            is the unoidl project in which to generate the module
     *
     * @return a Uno composite directory.
     */
    public static IUnoComposite createModuleDir(String pFullName, IUnoidlProject pProject) {

        UnoComposite module = new UnoComposite();
        module.setType(IUnoComposite.COMPOSITE_TYPE_FOLDER);
        String path = pFullName.replace("::", "/"); //$NON-NLS-1$ //$NON-NLS-2$

        IFolder filehandle = pProject.getFolder(pProject.getIdlPath().append(path));

        module.configure(filehandle.getLocation().toString());

        return module;
    }

    /**
     * Creates a simple module namespace use.
     *
     * <p>
     * For example, the produced text for the "foo" module name will be the following:
     * </p>
     *
     * <p>
     *
     * <pre>
     * module foo { };
     * </pre>
     * </p>
     *
     * @param pName
     *            is the module name (eg <code>foo</code>)
     * @return the uno composite corresponding to the module.
     */
    public static IUnoComposite createModuleSpace(String pName) {

        UnoComposite module = new UnoComposite();
        module.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
        Hashtable<String, Object> properties = new Hashtable<String, Object>();
        properties.put("name", pName); //$NON-NLS-1$
        String template = "module ${name} { ${children} };"; //$NON-NLS-1$
        module.configure(properties, template);

        return module;
    }

    /**
     * Simple convenient method calling {@link #createModuleSpace(String)} to create cascading modules namespaces.
     *
     * @param pFullName
     *            is the fully qualified name of the module to create (eg: <code>org::libreoffice::foo</code>)
     * @return the top-most composite corresponding to the top-most module
     */
    public static IUnoComposite createModulesSpaces(String pFullName) {

        IUnoComposite topModule = null;

        if (pFullName != null && !pFullName.equals("")) { //$NON-NLS-1$
            String[] modules = pFullName.split("::"); //$NON-NLS-1$

            if (modules.length > 0) {

                topModule = createModuleSpace(modules[0]);
                IUnoComposite lastModule = topModule;

                for (int i = 1; i < modules.length; i++) {
                    IUnoComposite currentModule = createModuleSpace(modules[i]);
                    lastModule.addChild(currentModule);

                    lastModule = currentModule;
                }
            }
        }
        return topModule;
    }

    /**
     * Creates a UNO composite representing a UNO service with an interface inheritance.
     *
     * <p>
     * The text produced by this method corresponds to the following one
     * </p>
     * <p>
     *
     * <pre>
     * \n[published ]service NAME : INTERFACE {
     * [...]
     * };
     * </pre>
     * </p>
     *
     * @param pName
     *            is the service name
     * @param pIsPublished
     *            <code>true</code> if the module is a published one.
     * @param pInterfaceFullName
     *            is the interface inheritance fully qualified name
     *
     * @see #createModulesSpaces(String) to get all the module declarations
     * @see #createService(String, boolean) for optional interface inheritance
     * @see #createService(String) for a very basic service creation method
     *
     * @return the created service composite
     */
    public static IUnoComposite createService(String pName, boolean pIsPublished, String pInterfaceFullName) {

        IUnoComposite service = null;

        if (pName != null && !pName.equals("")) { //$NON-NLS-1$
            service = new UnoComposite();
            service.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
            service.setIndented(true);
            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("name", pName); //$NON-NLS-1$

            if (pInterfaceFullName != null && !pInterfaceFullName.equals("")) { //$NON-NLS-1$
                properties.put("interface", ": " + pInterfaceFullName + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                properties.put("interface", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (pIsPublished) {
                properties.put("published", "published "); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                properties.put("published", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }

            String template = "\n\n${published}service ${name} ${interface}{\n" + //$NON-NLS-1$
                "${children}\n};\n\n"; //$NON-NLS-1$

            service.configure(properties, template);
        }

        return service;
    }

    /**
     * Creates a service declaration text with the default interface. This one doesn't need to be mentioned in order to
     * be used by UNO: <code>com::sun::star::uno::XInterface</code>.
     *
     * <p>
     * The text produced by this method corresponds to the following one
     * </p>
     * <p>
     *
     * <pre>
     * [published ]service NAME {
     * [...]
     * };
     * </pre>
     * </p>
     *
     * @param pName
     *            is the service name
     * @param pIsPublished
     *            <code>true</code> if the module is a published one.
     *
     * @see #createModulesSpaces(String) to get all the module declarations
     * @see #createService(String, boolean, String) for complex service creation method
     * @see #createService(String) for a very basic service creation method
     *
     * @return the created service composite
     */
    public static IUnoComposite createService(String pName, boolean pIsPublished) {
        return createService(pName, pIsPublished, null);
    }

    /**
     * Creates a default very basic service declaration only using the name and not published.
     *
     * <p>
     * The text produced by this method corresponds to the following one
     * </p>
     * <p>
     *
     * <pre>
     * service NAME {
     * [...]
     * };
     * </pre>
     * </p>
     *
     * @param pName
     *            is the service name
     *
     * @see #createModulesSpaces(String) to get all the module declarations
     * @see #createService(String, boolean, String) for complex service creation method
     * @see #createService(String, boolean) for a basic service creation method that lets specify if th service is
     *      published or not.
     *
     * @return the created service composite
     */
    public static IUnoComposite createService(String pName) {
        return createService(pName, false, null);
    }

    /**
     * Creates a Uno composite corresponding to an interface with its mandatory parent interfaces.
     *
     * <p>
     * This method returns two kind of texts depending on the number of parent interfaces name in the array. The first
     * writing is used when there is only one parent interface:
     *
     * <pre>
     * [published ]interface name [: parent::name ]{
     * [...]
     * };
     * </pre>
     * </p>
     *
     * <p>
     * The other way is used when there are more than one parent interfaces:
     *
     * <pre>
     * \n[published ]interface name {
     *     [[optional] ]interface parent::name; // for each parent
     *  [...]
     * };
     * </pre>
     * </p>
     *
     * @param pName
     *            is the name of the interface (eg: <code>foo</code>)
     * @param pIsPublished
     *            <code>true</code> if the interface is published
     * @param pParentIntfNames
     *            array of all the mandatory parent interfaces
     *
     * @see #createInterfaceInheritance(String, boolean) for the interfaces inheritances. This method should be called
     *      for each new interface to add or for optional interfaces. It is used in this method thought.
     *
     * @return a uno composite representing an interface declaration
     */
    public static IUnoComposite createInterface(String pName, boolean pIsPublished, String[] pParentIntfNames) {

        IUnoComposite intf = null;

        if (pName != null && !pName.equals("")) { //$NON-NLS-1$
            intf = new UnoComposite();
            intf.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
            intf.setIndented(true);

            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("name", pName); //$NON-NLS-1$
            if (pParentIntfNames.length == 1) {
                properties.put("interface", ": " + pParentIntfNames[0] + " "); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            } else {
                properties.put("interface", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            if (pIsPublished) {
                properties.put("published", "published "); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                properties.put("published", ""); //$NON-NLS-1$ //$NON-NLS-2$
            }
            String template = "\n\n${published}interface ${name} ${interface}{\n" + //$NON-NLS-1$
                "${children}\n};\n\n"; //$NON-NLS-1$
            intf.configure(properties, template);

            // Adds the interfaces if more than 1
            if (pParentIntfNames.length > 1) {

                for (int i = 0; i < pParentIntfNames.length; i++) {
                    IUnoComposite parent = createInterfaceInheritance(pParentIntfNames[i], false);
                    if (parent != null) {
                        intf.addChild(parent);
                    }
                }
            }
        }
        return intf;
    }

    /**
     * Creates a UNO composite representing an interface inheritance.
     *
     * <p>
     * The generated text is formatted as following:
     *
     * <pre>
     * \t[[optional] ]interface inheritance::name;\n
     * </pre>
     * </p>
     *
     * @param pName
     *            the fully qualified name of the interface to inherit from
     * @param pOptional
     *            <code>true</code> if the interface is optional
     *
     * @return a UNO composite
     */
    public static IUnoComposite createInterfaceInheritance(String pName, boolean pOptional) {

        IUnoComposite intf = null;
        if (pName != null && !pName.equals("")) { //$NON-NLS-1$
            intf = new UnoComposite();
            intf.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);

            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("name", pName); //$NON-NLS-1$
            String optional = ""; //$NON-NLS-1$
            if (pOptional) {
                optional = "[optional] "; //$NON-NLS-1$
            }
            properties.put("optional", optional); //$NON-NLS-1$
            String template = "\t${optional}interface ${name};\n"; //$NON-NLS-1$
            intf.configure(properties, template);
        }
        return intf;
    }

    /**
     * Creates an interface attribute.
     *
     * @param pName
     *            the attribute name
     * @param pType
     *            the type of the attribute
     * @param pFlags
     *            the well-formatted string of flags.
     *
     * @return the attribute UNO composite
     */
    public static IUnoComposite createAttribute(String pName, String pType, String pFlags) {

        IUnoComposite attribute = null;
        if (pType == null) {
            pType = "void"; //$NON-NLS-1$
        }

        if (pFlags == null) {
            pFlags = ""; //$NON-NLS-1$
        }

        pFlags.trim();
        if (pFlags == null || pFlags.equals("")) { //$NON-NLS-1$
            pFlags = ""; //$NON-NLS-1$
        } else {
            pFlags = pFlags.replace(" ", ", "); //$NON-NLS-1$ //$NON-NLS-2$
            pFlags = ", " + pFlags; //$NON-NLS-1$

        }

        if (pName != null && !pName.equals("")) { //$NON-NLS-1$
            attribute = new UnoComposite();
            attribute.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);

            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("name", pName); //$NON-NLS-1$
            properties.put("type", pType); //$NON-NLS-1$
            properties.put("flags", pFlags); //$NON-NLS-1$
            String template = "\t[attribute${flags}] ${type} ${name};\n"; //$NON-NLS-1$
            attribute.configure(properties, template);
        }
        return attribute;
    }

    /**
     * Creates an interface method.
     *
     * @param pName
     *            the method name
     * @param pType
     *            the method return type
     *
     * @return the UNO composite representing the method
     *
     * @see #createMethodArgument(String, String, String) for informations on how to add parameters to the method
     */
    public static IUnoComposite createMethod(String pName, String pType) {

        IUnoComposite method = null;
        if (pType == null || pType.trim().equals("")) { //$NON-NLS-1$
            pType = "void"; //$NON-NLS-1$
        }

        if (pName != null && !pName.equals("")) { //$NON-NLS-1$
            method = new UnoComposite();
            method.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
            method.setChildrenSeparator(", "); //$NON-NLS-1$

            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("name", pName); //$NON-NLS-1$
            properties.put("type", pType); //$NON-NLS-1$
            String template = "\t${type} ${name}(${children});\n"; //$NON-NLS-1$
            method.configure(properties, template);
        }
        return method;
    }

    /**
     * Create a method argument to be added to a method UNO composite.
     *
     * @param pName
     *            the argument name
     * @param pType
     *            the argument type
     * @param pDirection
     *            the argument direction among <code>in</code>, <code>out</code>, <code>inout</code>.
     *
     * @return the UNO composite representing the parameter
     */
    public static IUnoComposite createMethodArgument(String pName, String pType, String pDirection) {

        IUnoComposite argument = null;
        if (pType == null) {
            pType = "any"; //$NON-NLS-1$
        }

        if (pDirection == null) {
            pDirection = "inout"; //$NON-NLS-1$
        }

        if (pName != null && !pName.equals("")) { //$NON-NLS-1$
            argument = new UnoComposite();
            argument.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);

            Hashtable<String, Object> properties = new Hashtable<String, Object>();
            properties.put("name", pName); //$NON-NLS-1$
            properties.put("type", pType); //$NON-NLS-1$
            properties.put("direction", pDirection); //$NON-NLS-1$
            String template = "[${direction}] ${type} ${name}"; //$NON-NLS-1$
            argument.configure(properties, template);
        }
        return argument;
    }
}
