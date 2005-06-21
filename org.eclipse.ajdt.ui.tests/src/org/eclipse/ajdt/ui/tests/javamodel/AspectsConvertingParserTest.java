/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
			fail("Some pointcut designators have not been removed.");
	}
	
	public void testConvert2() {
 		myParser.convert(new ConversionOptions(true, true, false));
 		int pos = new String(myParser.content).indexOf("org.aspectj.lang.JoinPoint thisJoinPoint;");
 		if (pos < 0)
 			fail("tjp has not been added.");
 		
 		pos = new String(myParser.content).indexOf("org.aspectj.lang.JoinPoint.StaticPart thisJoinPointStaticPart;");
 		if (pos < 0)
 			fail("tjpsp has not been added.");
 		
//		if (myParser.content.length != 1086)
//			fail("tjp and tjpsp have not been added correctly.");
	}
	
	public void testConvert3() {
		int len = myParser.content.length;
 		myParser.convert(ConversionOptions.CONSTANT_SIZE);
		if (myParser.content.length !=  len)
			fail("Length of content has changed.");
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed.");
	}
	
	public void testConvert4() {
 		myParser.convert(ConversionOptions.CODE_COMPLETION);
		if (myParser.content.length != 1094)
			fail("Wrong size of content.");
		if (new String(myParser.content).indexOf(':') != -1)
			fail("Some pointcut designators have not been removed.");
	}

	public void testBug93248() {
		String statement = "System.out.println(true?\"foo\":\"bar\");";
		char[] testContent = ("public aspect ABC {\npublic static void main(String[] args) {\n"
				+ statement + "\n}\n}").toCharArray();
		AspectsConvertingParser pars = new AspectsConvertingParser(testContent);
		pars.convert(ConversionOptions.STANDARD);
		String converted = new String(pars.content);
		if (converted.indexOf(statement) == -1) {
			fail("Regression of bug 93248: tertiary operator breaks organise imports");
		}
	}
	
	public void testBug93248again() {
		// nested conditional statements
		String statement = "System.out.println(true?true?\"foo\":\"foobar\":\"bar\");";
		char[] testContent = ("public aspect ABC {\npublic static void main(String[] args) {\n"
				+ statement + "\n}\n}").toCharArray();
		AspectsConvertingParser pars = new AspectsConvertingParser(testContent);
		pars.convert(ConversionOptions.STANDARD);
		String converted = new String(pars.content);
		if (converted.indexOf(statement) == -1) {
			fail("Regression of bug 93248: tertiary operator breaks organise imports");
		}
	}	
	
	
	/*
	 * Class under test for int findPrevious(char, char[], int)
	 */
	public void testFindPreviouscharcharArrayint() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray();
		char target = 'b';
		myParser.content = testContent;
		if (myParser.findPrevious(target, 3) != 1)
			fail("Find previous failed.");
		if (myParser.findPrevious(target, 0) != -1)
			fail("Find previous failed.");
		
	}

	/*
	 * Class under test for int findPrevious(char[], char[], int)
	 */
	public void testFindPreviouscharArraycharArrayint() {
		
		char[] testContent = "abc abc abc xyz xyz".toCharArray();
		char[] target = "bx".toCharArray();
		myParser.content = testContent;
		if (myParser.findPrevious(target, 3) != 1)
			fail("Find previous failed.");
		if (myParser.findPrevious(target, 0) != -1)
			fail("Find previous failed.");
		if (myParser.findPrevious(target, 13) != 12)
			fail("Find previous failed.");
	}

	public void testFindPreviousNonSpace() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray();
		myParser.content = testContent;
		if (myParser.findPreviousNonSpace(3) != 2)
			fail("Find previous failed.");
		if (myParser.findPreviousNonSpace(0) != 0)
			fail("Find previous failed, returns " + myParser.findPreviousNonSpace(0));
		
	}

	public void testFindNext() {
		char[] testContent = "abc abc abc xyz xyz".toCharArray();
		char[] target = "bx".toCharArray();
		myParser.content = testContent;
		if (myParser.findNext(target, 0) != 1)
			fail("Find next failed.");
		if (myParser.findNext(target, 100) != -1)
			fail("Find next failed.");
		if (myParser.findNext(target, 7) != 9)
			fail("Find next failed.");
		if (myParser.findNext(target, 17) != -1)
			fail("Find next failed.");
		if (myParser.findNext(target, 12) != 12)
			fail("Find next failed.");
		
	}
	

//	public void testReplace() {
//		AspectsConvertingParser pars = new AspectsConvertingParser("It's not the long fall that kills you, it's the sudden stop.".toCharArray());
//		pars.addReplacement(2, 2, " is".toCharArray());
//		pars.convert(true, true);
//		System.out.println(new String(pars.content));		
//	}
}
