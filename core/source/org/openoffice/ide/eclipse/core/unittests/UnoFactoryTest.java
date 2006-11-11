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
		
		IUnoComposite content = CompositeFactory.createFileContent("foo::Foo"); //$NON-NLS-1$
		String expected = "#ifndef __foo_foo_idl__\n"+ //$NON-NLS-1$
						  "#define __foo_foo_idl__\n"+ //$NON-NLS-1$
						  "\n\n#endif\n"; //$NON-NLS-1$
		assertEquals(expected, content.toString());
		
	}
	
	/*
	 * Test method for 'createInclude(String)'
	 */
	public void testcreateInclude() {
		
		// Standard test
		IUnoComposite include = CompositeFactory.createInclude("foo::XFoo"); //$NON-NLS-1$
		assertEquals("#include <foo/XFoo.idl>\n", include.toString()); //$NON-NLS-1$
		
		// Null test
		include = CompositeFactory.createInclude(null);
		assertNull(include);
	}
	
	/*
	 * Test method for 'UnoFactory.createModuleSpace(String)'
	 */
	public void testCreateModuleSpace() {
		
		IUnoComposite module = CompositeFactory.createModuleSpace("foo"); //$NON-NLS-1$
		String expected = "module foo {  };"; //$NON-NLS-1$
		assertEquals("basic module creation failed",  //$NON-NLS-1$
				expected, module.toString());
	}

	/*
	 * Test method for 'UnoFactory.createModulesSpaces(String)'
	 */
	public void testCreateModulesSpaces() {

		IUnoComposite topmodule = CompositeFactory.createModulesSpaces("foo::bar"); //$NON-NLS-1$
		String expected = "module foo { module bar {  }; };"; //$NON-NLS-1$
		assertEquals("modules in modules test failed", expected, topmodule.toString()); //$NON-NLS-1$
	}

	/*
	 * Test method for 'UnoFactory.createService(String, boolean, String)'
	 */
	public void testCreateService() {
		
		// Basic test
		IUnoComposite service = CompositeFactory.createService("foo"); //$NON-NLS-1$
		String expected = "\t\n\n\tservice foo {\n\n\t};\n\n"; //$NON-NLS-1$
		assertEquals("basic service creation failed",  //$NON-NLS-1$
				expected, service.toString());
		
		// Test with published
		service = CompositeFactory.createService("foo", true); //$NON-NLS-1$
		expected = "\t\n\n\tpublished service foo {\n\n\t};\n\n"; //$NON-NLS-1$
		assertEquals("Service test with published failed",  //$NON-NLS-1$
				expected, service.toString());
		
		// Test with published and interface
		service = CompositeFactory.createService("foo", true, //$NON-NLS-1$
				"foo::XTest"); //$NON-NLS-1$
		expected = "\t\n\n\tpublished service foo : foo::XTest {\n\n\t};\n\n"; //$NON-NLS-1$
		assertEquals("Service normal use test failed", expected, service.toString()); //$NON-NLS-1$
		
		// Test with null name
		service = CompositeFactory.createService(null, true,
				"foo::XTest"); //$NON-NLS-1$
		assertNull("service not null with null name", service); //$NON-NLS-1$
		
		//Test with empty name
		service = CompositeFactory.createService("", true, //$NON-NLS-1$
				"foo::XTest"); //$NON-NLS-1$
		assertNull("service not null with empty name", service); //$NON-NLS-1$
	}
	
	/*
	 * Test method for 'UnoFactory.createInterfaceInheritance(...)'
	 */
	public void testCreateInterfaceInheritance() {
		
		IUnoComposite intf = CompositeFactory.createInterfaceInheritance("foo::XFoo", true); //$NON-NLS-1$
		String expected = "\t[optional] interface foo::XFoo;\n"; //$NON-NLS-1$
		assertEquals(expected, intf.toString());
		
		intf = CompositeFactory.createInterfaceInheritance("foo::XFoo", false); //$NON-NLS-1$
		expected = "\tinterface foo::XFoo;\n"; //$NON-NLS-1$
		assertEquals(expected, intf.toString());
	}
	
	/*
	 * Test method for 'UnoFactory.createInterface(...)'
	 */
	public void testCreateInterface() {
		
		// One parent interface
		String[] parents = new String[]{"XBar"}; //$NON-NLS-1$
		IUnoComposite intf = CompositeFactory.createInterface("XFoo", true, parents); //$NON-NLS-1$
		String expected = "\t\n\n\tpublished interface XFoo : XBar {\n\n\t};\n\n"; //$NON-NLS-1$
		assertEquals(expected, intf.toString());
		
		// More than one parent interface
		parents = new String[]{"XBar", "XFooBar"}; //$NON-NLS-1$ //$NON-NLS-2$
		intf = CompositeFactory.createInterface("XFoo", false, parents); //$NON-NLS-1$
		expected = "\t\n\n\tinterface XFoo {\n\t\tinterface XBar;\n\t\t" + //$NON-NLS-1$
				"interface XFooBar;\n\n\t};\n\n"; //$NON-NLS-1$
		assertEquals(expected, intf.toString());
		
		// Name is null
		intf = CompositeFactory.createInterface(null, false, parents);
		assertNull(intf);
	}
}
