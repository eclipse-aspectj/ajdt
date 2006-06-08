/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.ui.tests.javamodel;

import org.eclipse.ajdt.codeconversion.AspectsConvertingParser;
import org.eclipse.ajdt.codeconversion.ConversionOptions;

/**
 * Test 
 * 
 * 
 * @author Luzius Meisser
 */
public class AspectsConvertingParserTest extends AbstractTestCase {
	


	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		unit.requestOriginalContentMode();
		char[] content = (char[])unit.getContents().clone();
		unit.discardOriginalContentMode();
		myParser = new AspectsConvertingParser(content);

	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testConvert() {
//		int len = myParser.content.length;
		
		myParser.convert(ConversionOptions.STANDARD);
//		if (myParser.content.length !=  len + 4)
//			fail("Reference to C has not been added (?).");
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed."); //$NON-NLS-1$
	}
	
	public void testConvert2() {
 		myParser.convert(new ConversionOptions(true, true, false));
 		int pos = new String(myParser.content).indexOf("org.aspectj.lang.JoinPoint thisJoinPoint;"); //$NON-NLS-1$
 		if (pos < 0)
 			fail("tjp has not been added."); //$NON-NLS-1$
 		
 		pos = new String(myParser.content).indexOf("org.aspectj.lang.JoinPoint.StaticPart thisJoinPointStaticPart;"); //$NON-NLS-1$
 		if (pos < 0)
 			fail("tjpsp has not been added."); //$NON-NLS-1$
 		
//		if (myParser.content.length != 1086)
//			fail("tjp and tjpsp have not been added correctly.");
	}
	
	public void testConvert3() {
		int len = myParser.content.length;
 		myParser.convert(ConversionOptions.CONSTANT_SIZE);
		if (myParser.content.length !=  len)
			fail("Length of content has changed."); //$NON-NLS-1$
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed."); //$NON-NLS-1$
	}
	
	public void testConvert4() {
 		myParser.convert(ConversionOptions.CODE_COMPLETION);
		if (myParser.content.length != 1094)
			fail("Wrong size of content."); //$NON-NLS-1$
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed."); //$NON-NLS-1$
	}

	public void testBug93248() {
		String statement = "System.out.println(true?\"foo\":\"bar\");"; //$NON-NLS-1$
		char[] testContent = ("public aspect ABC {\npublic static void main(String[] args) {\n" //$NON-NLS-1$
				+ statement + "\n}\n}").toCharArray(); //$NON-NLS-1$
		AspectsConvertingParser pars = new AspectsConvertingParser(testContent);
		pars.convert(ConversionOptions.STANDARD);
		String converted = new String(pars.content);
		if (converted.indexOf(statement) == -1) {
			fail("Regression of bug 93248: tertiary operator breaks organise imports"); //$NON-NLS-1$
		}
	}
	
	public void testBug93248again() {
		// nested conditional statements
		String statement = "System.out.println(true?true?\"foo\":\"foobar\":\"bar\");"; //$NON-NLS-1$
		char[] testContent = ("public aspect ABC {\npublic static void main(String[] args) {\n" //$NON-NLS-1$
				+ statement + "\n}\n}").toCharArray(); //$NON-NLS-1$
		AspectsConvertingParser pars = new AspectsConvertingParser(testContent);
		pars.convert(ConversionOptions.STANDARD);
		String converted = new String(pars.content);
		if (converted.indexOf(statement) == -1) {
			fail("Regression of bug 93248: tertiary operator breaks organise imports"); //$NON-NLS-1$
		}
	}	
	
	
	/*
	 * Class under test for int findPrevious(char, char[], int)
	 */
	public void testFindPreviouscharcharArrayint() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		char target = 'b';
		myParser.content = testContent;
		if (myParser.findPrevious(target, 3) != 1)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPrevious(target, 0) != -1)
			fail("Find previous failed."); //$NON-NLS-1$
		
	}

	/*
	 * Class under test for int findPrevious(char[], char[], int)
	 */
	public void testFindPreviouscharArraycharArrayint() {
		
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		char[] target = "bx".toCharArray(); //$NON-NLS-1$
		myParser.content = testContent;
		if (myParser.findPrevious(target, 3) != 1)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPrevious(target, 0) != -1)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPrevious(target, 13) != 12)
			fail("Find previous failed."); //$NON-NLS-1$
	}

	public void testFindPreviousNonSpace() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		myParser.content = testContent;
		if (myParser.findPreviousNonSpace(3) != 2)
			fail("Find previous failed."); //$NON-NLS-1$
		if (myParser.findPreviousNonSpace(0) != 0)
			fail("Find previous failed, returns " + myParser.findPreviousNonSpace(0)); //$NON-NLS-1$
		
	}

	public void testFindNext() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray(); //$NON-NLS-1$
		char[] target = "bx".toCharArray(); //$NON-NLS-1$
		myParser.content = testContent;
		if (myParser.findNext(target, 0) != 1)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 100) != -1)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 7) != 9)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 17) != -1)
			fail("Find next failed."); //$NON-NLS-1$
		if (myParser.findNext(target, 12) != 12)
			fail("Find next failed."); //$NON-NLS-1$
		
	}
	

//	public void testReplace() {
//		AspectsConvertingParser pars = new AspectsConvertingParser("It's not the long fall that kills you, it's the sudden stop.".toCharArray());
//		pars.addReplacement(2, 2, " is".toCharArray());
//		pars.convert(true, true);
//		System.out.println(new String(pars.content));		
//	}
}
