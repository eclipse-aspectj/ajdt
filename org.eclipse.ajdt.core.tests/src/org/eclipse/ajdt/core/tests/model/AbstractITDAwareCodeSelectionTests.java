/*******************************************************************************
 * Copyright (c) 2010 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Kris De Volder - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

/**
 * An abstract superclass for Several ITDAwareCodeSelectionTests, which seem to share
 * some code in common. We can pull-up some of that shared code to here as needed.
 * 
 * @author kdvolder
 */
public abstract class AbstractITDAwareCodeSelectionTests extends AJDTCoreTestCase {

	protected final IRegion findRegion(ICompilationUnit unit, String string, int occurrence) {
	    String contents = new String(((CompilationUnit) unit).getContents());
	    int start = 0;
	    while (occurrence-- > 0) {
	        start = contents.indexOf(string, start+1);
	        if (start<0) fail("Too few occurrences of '"+string+"' where found");
	    }
	    System.out.println("Found '"+string+"' at "+start);
	    return new Region(start, string.length());
	}

	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(ITDAwareCodeSelectionTests.class);
		suite.addTestSuite(ITDAwareCodeSelectionTests2.class);
		suite.addTestSuite(ITDAwareCodeSelectionTests3.class);
		return suite;
	}

}
