package org.openoffice.ide.eclipse.core.unittests;


import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for UnoCompositeTest project");
		//$JUnit-BEGIN$
		suite.addTestSuite(CompositeTest.class);
		suite.addTestSuite(UnoFactoryTest.class);
		//$JUnit-END$
		return suite;
	}

}
