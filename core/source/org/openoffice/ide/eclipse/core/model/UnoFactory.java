/**
 * 
 */
package org.openoffice.ide.eclipse.core.model;

import java.util.Hashtable;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.openoffice.ide.eclipse.core.internal.model.UnoComposite;

/**
 * This class provides static methods to generate well-formed
 * uno composites. Thus there is no need to know how to create them to
 * use them.
 * 
 * <p><b>Note:</b> This class has to be extended to add new UNO-IDL
 * code generation.</p>
 * 
 * @author cbosdonnat
 *
 */
public final class UnoFactory {
	
	
	/**
	 * Creates a file node from the fully qualified name of the type which
	 * should be described inside.
	 * 
	 * @param fullName  is the fully qualified name of the
	 * 			type described in the file to create 
	 * 			(eg: <code>org::openoffice::foo</code>)
	 * @param project is the uno project in which to add the type.
	 * 
	 * @return a Uno composite representing a file, or <code>null</code>
	 * 		if the fullName is null or an empty string
	 */
	public static IUnoComposite createTypeFile(String fullName, 
			IUnoidlProject project) {
		
		IUnoComposite file = null;
		
		if (fullName != null && !fullName.equals("")) {
			String fileName = fullName.replace("::", "/") + ".idl";
			
			IFile filehandle = project.getFile(
					project.getIdlPath().append(fileName));
			
			file = createFile(filehandle);
		}
		
		return file;
	}
	
	/**
	 * Creates a Uno composite representing a file from its filename
	 * 
	 * @param filehandle the relative filename
	 * 
	 * @see #createTypeFile(String, IUnoidlProject) for file creation from 
	 * 		Type name
	 * 
	 * @return a Uno composite of FILE type
	 */
	public static IUnoComposite createFile(IFile filehandle) {
		
		IUnoComposite file = null;
		
		if (filehandle != null) {
			file = new UnoComposite();
			file.setType(IUnoComposite.COMPOSITE_TYPE_FILE);
			file.configure(filehandle.getLocation().toString());
		}
		
		return file;
	}
	
	/**
	 * Creates a uno composite representing the file content skeleton from
	 * the type name it should contain. All the file contents has to be added
	 * in this composite.
	 * 
	 * <p>The produced result will be of the following form: 
	 * <pre>#ifndef __define_name_idl__
	 * #define __define_name_idl__
	 * [...]
	 * #endif
	 * </pre></p>
	 * 
	 * @param fullname the type fully qualified name (eg: <code>org::foo</code>)
	 * @return a uno composite representing the file content
	 */
	public static IUnoComposite createFileContent(String fullname) {
		
		IUnoComposite content = null;
		
		if (fullname != null && !fullname.equals("")) {
			content = new UnoComposite();
			content.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
			
			Hashtable properties = new Hashtable();
			String define = fullname.replace("::", "_");
			define = "__" + define + "_idl__";
			properties.put("define", define.toLowerCase());
			
			String template = "#ifndef ${define}\n"+
			                  "#define ${define}\n" + 
			                  "\n${children}\n" +
							  "#endif\n";
			
			content.configure(properties, template);
		}
		
		return content;
	}
	
	/**
	 * Creates a Uno composite representing an include line. It uses the
	 * type's fully qualified name to recompose the file name. It supposes
	 * that the type is contained in a file of it's name.
	 * 
	 * <p>If the method is called with the type <code>foo::XFoo</code>, the
	 * file <code>foo/XFoo.idl</code> will be returned. The resulting include
	 * line is always in &lt;,  &gt; characters.</p>
	 * 
	 * @param fullName the fully qualified name of the type to include
	 * 
	 * @return a parametrized uno composite
	 */
	public static IUnoComposite createInclude(String fullName) {
		
		IUnoComposite include = null;
		
		if (fullName != null && !fullName.equals("")) {
			String fileName = fullName.replace("::", "/") + ".idl";
			
			include = new UnoComposite();
			include.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
			Hashtable properties = new Hashtable();
			properties.put("file", fileName);
			String template = "#include <${file}>\n";
			include.configure(properties, template);
		}
		
		return include;
	}
	
	/**
	 * Creates a Uno composite directory.
	 * 
	 * @param fullName is the fully qualified name of the
	 * 			module to create (eg: <code>org::openoffice::foo</code>)
	 * @param project is the unoidl project in which to generate the 
	 * 			module
	 * 
	 * @return a Uno composite directory.
	 */
	public static IUnoComposite createModuleDir(String fullName, 
			IUnoidlProject project){
		
		UnoComposite module = new UnoComposite();
		module.setType(IUnoComposite.COMPOSITE_TYPE_FOLDER);
		String path = fullName.replace("::", "/");
		
		IFolder filehandle = project.getFolder(
				project.getIdlPath().append(path));
		
		module.configure(filehandle.getLocation().toString());
		
		return module;
	}
	
	/**
	 * Creates a simple module namespace use.
	 * 
	 * <p>For example, the produced text for the "foo" module name will be
	 * the following: </p>
	 * 
	 * <p><pre>module foo { };</pre></p>
	 * 
	 * @param name is the module name (eg <code>foo</code>)
	 * @return the uno composite corresponding to the module.
	 */
	public static IUnoComposite createModuleSpace(String name){
		
		UnoComposite module = new UnoComposite();
		module.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
		Hashtable properties = new Hashtable();
		properties.put("name", name);
		String template = "module ${name} { ${children} };"; 
		module.configure(properties, template);
		
		return module;
	}
	
	/**
	 * Simple convenient method calling {@link #createModuleSpace(String)} to
	 * create cascading modules namespaces.
	 * 
	 * @param fullName is the fully qualified name of the
	 * 			module to create (eg: <code>org::openoffice::foo</code>)
	 * @return the top-most composite corresponding to the top-most module
	 */
	public static IUnoComposite createModulesSpaces(String fullName){
		
		IUnoComposite topModule = null;
		
		if (fullName != null && !fullName.equals("")){
			String[] modules = fullName.split("::");
			
			if (modules.length > 0) {
				
				topModule = createModuleSpace(modules[0]);
				IUnoComposite lastModule = topModule;
				
				for (int i=1; i<modules.length; i++) {
					IUnoComposite currentModule = createModuleSpace(modules[i]);
					lastModule.addChild(currentModule);
					
					lastModule = currentModule;
				}
			}
		}
		return topModule;
	}
	
	/**
	 * Creates a Uno composite representing a Uno service with an interface 
	 * inheritance.
	 * 
	 * <p>The text produced by this method corresponds to the following one</p>
	 * <p><pre>\n[published ]service NAME : INTERFACE {
	 * [...]
	 * };</pre></p>
	 * 
	 * @param name is the service name
	 * @param isPublished <code>true</code> if the module is a published one.
	 * @param interfaceFullName is the interface inheritance fully qualified
	 * 			name
	 * 
	 * @see #createModulesSpaces(String) to get all the module declarations
	 * @see #createService(String, boolean) for optional interface inheritance
	 * @see #createService(String) for a very basic service creation method
	 * 
	 * @return the created service composite
	 */
	public static IUnoComposite createService(String name, boolean isPublished,
			String interfaceFullName){
		
		IUnoComposite service = null;
		
		if (name != null && !name.equals("")){
			service = new UnoComposite();
			service.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
			service.setIndented(true);  // A non indented service is unreadable
			Hashtable properties = new Hashtable();
			properties.put("name", name);
			
			if (interfaceFullName != null && !interfaceFullName.equals("")){
				properties.put("interface", ": " + interfaceFullName + " ");
			} else {
				properties.put("interface", "");
			}
			
			if (isPublished) {
				properties.put("published", "published ");
			} else {
				properties.put("published", "");
			}
			
			String template = "\n\n${published}service ${name} ${interface}{\n"+
							  "${children}\n};\n\n";
			
			service.configure(properties, template);
		}
		
		return service;
	}
	/**
	 * Creates a service declaration text with the default interface. This one
	 * doesn't need to be mentionned in order to be used by UNO: 
	 * <code>com::sun::star::uno::XInterface</code>.
	 * 
	 * <p>The text produced by this method corresponds to the following one</p>
	 * <p><pre>[published ]service NAME {
	 * [...]
	 * };</pre></p>
	 * 
	 * @param name is the service name
	 * @param isPublished <code>true</code> if the module is a published one.
	 *
	 * @see #createModulesSpaces(String) to get all the module declarations
	 * @see #createService(String, boolean, String) for complex service 
	 * 			creation method
	 * @see #createService(String) for a very basic service creation method
	 * 
	 * @return the created service composite
	 */
	public static IUnoComposite createService(String name, boolean isPublished) {
		return createService(name, isPublished, null);
	}
	
	/**
	 * Creates a default very basic service declaration only using the name
	 * and not published.
	 * 
	 * <p>The text produced by this method corresponds to the following one</p>
	 * <p><pre>service NAME {
	 * [...]
	 * };</pre></p>
	 * 
	 * @param name is the service name
	 * 
	 * @see #createModulesSpaces(String) to get all the module declarations
	 * @see #createService(String, boolean, String) for complex service 
	 * 			creation method
	 * @see #createService(String, boolean) for a basic service creation method
	 * 			that lets specify if th service is published or not.
	 * 
	 * @return the created service composite
	 */
	public static IUnoComposite createService(String name) {
		return createService(name, false, null);
	}
	
	/**
	 * Creates a Uno composite corresponding to an interface with its 
	 * mandatory parent interfaces.
	 * 
	 * <p>This method returns two kind of texts depending on the number
	 * of parent interfaces name in the array. The first writing is used
	 * when there is only one parent interface:
	 * <pre>[published ]interface name [: parent::name ]{
	 * [...]
	 * };</pre></p>
	 * 
	 * <p>The other way is used when there are more than one parent 
	 * interfaces:
	 * <pre>\n[published ]interface name {
	 * 	[[optional] ]interface parent::name; // for each parent
	 *  [...]
	 * };</pre></p>
	 * 
	 * @param name is the name of the interface (eg: <code>foo</code>)
	 * @param isPublished <code>true</code> if the interface is published
	 * @param parentIntfNames array of all the mandatory parent interfaces
	 * 
	 * @see #createInterfaceInheritance(String, boolean) for the interfaces 
	 * 		inheritances. This method should be called for each new interface
	 * 		to add or for optional interfaces. It is used in this method thought.
	 * 
	 * @return a uno composite representing an interface declaration
	 */
	public static IUnoComposite createInterface(String name, 
			boolean isPublished, String[] parentIntfNames) {
		
		IUnoComposite intf = null;
		
		if (name != null && !name.equals("")) {
			intf = new UnoComposite();
			intf.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
			intf.setIndented(true);
			
			Hashtable properties = new Hashtable();
			properties.put("name", name);
			if (parentIntfNames.length == 1) {
				properties.put("interface", ": " + parentIntfNames[0] + " ");
			} else {
				properties.put("interface", "");
			}
			if (isPublished) {
				properties.put("published", "published ");
			} else {
				properties.put("published", "");
			}
			String template = "\n\n${published}interface ${name} ${interface}{\n" +
				"${children}\n};\n\n";
			intf.configure(properties, template);
			
			// Adds the interfaces if more than 1
			if (parentIntfNames.length > 1){
				
				for (int i=0; i<parentIntfNames.length; i++) {
					IUnoComposite parent = createInterfaceInheritance(
							parentIntfNames[i], false);
					if (parent != null) {
						intf.addChild(parent);
					}
				}
			}
		}
		return intf;
	}
	
	/**
	 * Creates a uno composite representing an interface inheritance.
	 * 
	 * <p>The generated text is formatted as following:
	 * <pre>\t[[optional] ]interface inheritance::name;\n</pre></p>
	 * 
	 * @param name the fully qualified name of the interface to inherit from
	 * @param optional <code>true</code> if the interface is optional
	 * 
	 * @return a uno composite
	 */
	public static IUnoComposite createInterfaceInheritance(
			String name, boolean optional){
		
		IUnoComposite intf = null;
		if (name != null && !name.equals("")) {
			intf = new UnoComposite();
			intf.setType(IUnoComposite.COMPOSITE_TYPE_TEXT);
			
			Hashtable properties = new Hashtable();
			properties.put("name", name);
			properties.put("optional", optional?"[optional] ":"");
			String template = "\t${optional}interface ${name};\n";
			intf.configure(properties, template);
		}
		return intf;
	}
}
