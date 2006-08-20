package org.openoffice.ide.eclipse.core.unittests;

import org.openoffice.ide.eclipse.core.model.IUnoComposite;
import org.openoffice.ide.eclipse.core.model.CompositeFactory;

import junit.framework.TestCase;

/**
 * JUnit tests for the Uno composite factory
 * 
 * @author cbosdonnat
 */
public class UnoFactoryTest extends TestCase {
	
	/*
	 * Test method for 'createFileContent(String)'
	 */
	public void testcreateFileContent() {
		
		IUnoComposite content = CompositeFactory.createFileContent("foo::Foo");
		String expected = "#ifndef __foo_foo_idl__\n"+
						  "#define __foo_foo_idl__\n"+
						  "\n\n#endif\n";
		assertEquals(expected, content.toString());
		
	}
	
	/*
	 * Test method for 'createInclude(String)'
	 */
	public void testcreateInclude() {
		
		// Standard test
		IUnoComposite include = CompositeFactory.createInclude("foo::XFoo");
		assertEquals("#include <foo/XFoo.idl>\n", include.toString());
		
		// Null test
		include = CompositeFactory.createInclude(null);
		assertNull(include);
	}
	
	/*
	 * Test method for 'UnoFactory.createModuleSpace(String)'
	 */
	public void testCreateModuleSpace() {
		
		IUnoComposite module = CompositeFactory.createModuleSpace("foo");
		String expected = "module foo {  };";
		assertEquals("basic module creation failed", 
				expected, module.toString());
	}

	/*
	 * Test method for 'UnoFactory.createModulesSpaces(String)'
	 */
	public void testCreateModulesSpaces() {

		IUnoComposite topmodule = CompositeFactory.createModulesSpaces("foo::bar");
		String expected = "module foo { module bar {  }; };";
		assertEquals("modules in modules test failed", expected, topmodule.toString());
	}

	/*
	 * Test method for 'UnoFactory.createService(String, boolean, String)'
	 */
	public void testCreateService() {
		
		// Basic test
		IUnoComposite service = CompositeFactory.createService("foo");
		String expected = "\t\n\n\tservice foo {\n\n\t};\n\n";
		assertEquals("basic service creation failed", 
				expected, service.toString());
		
		// Test with published
		service = CompositeFactory.createService("foo", true);
		expected = "\t\n\n\tpublished service foo {\n\n\t};\n\n";
		assertEquals("Service test with published failed", 
				expected, service.toString());
		
		// Test with published and interface
		service = CompositeFactory.createService("foo", true,
				"foo::XTest");
		expected = "\t\n\n\tpublished service foo : foo::XTest {\n\n\t};\n\n";
		assertEquals("Service normal use test failed", expected, service.toString());
		
		// Test with null name
		service = CompositeFactory.createService(null, true,
				"foo::XTest");
		assertNull("service not null with null name", service);
		
		//Test with empty name
		service = CompositeFactory.createService("", true,
				"foo::XTest");
		assertNull("service not null with empty name", service);
	}
	
	/*
	 * Test method for 'UnoFactory.createInterfaceInheritance(...)'
	 */
	public void testCreateInterfaceInheritance() {
		
		IUnoComposite intf = CompositeFactory.createInterfaceInheritance("foo::XFoo", true);
		String expected = "\t[optional] interface foo::XFoo;\n";
		assertEquals(expected, intf.toString());
		
		intf = CompositeFactory.createInterfaceInheritance("foo::XFoo", false);
		expected = "\tinterface foo::XFoo;\n";
		assertEquals(expected, intf.toString());
	}
	
	/*
	 * Test method for 'UnoFactory.createInterface(...)'
	 */
	public void testCreateInterface() {
		
		// One parent interface
		String[] parents = new String[]{"XBar"};
		IUnoComposite intf = CompositeFactory.createInterface("XFoo", true, parents);
		String expected = "\t\n\n\tpublished interface XFoo : XBar {\n\n\t};\n\n";
		assertEquals(expected, intf.toString());
		
		// More than one parent interface
		parents = new String[]{"XBar", "XFooBar"};
		intf = CompositeFactory.createInterface("XFoo", false, parents);
		expected = "\t\n\n\tinterface XFoo {\n\t\tinterface XBar;\n\t\t" +
				"interface XFooBar;\n\n\t};\n\n";
		assertEquals(expected, intf.toString());
		
		// Name is null
		intf = CompositeFactory.createInterface(null, false, parents);
		assertNull(intf);
	}
}
