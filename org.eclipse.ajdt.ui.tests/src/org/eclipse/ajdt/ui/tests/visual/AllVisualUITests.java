package org.eclipse.ajdt.ui.tests.visual;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.ui.tests.AllUITests;

public class AllVisualUITests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllVisualUITests.class.getName());
		
		AllUITests.setupAJDTPlugin();
		
		suite.addTest(new TestSuite(AspectJBreakpointKeyboardActionTest.class));
		
		return suite;
	}
}
