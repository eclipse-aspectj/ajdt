/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial version
 *     Ian McGrath  - added additional tests
 *******************************************************************************/
package org.eclipse.contribution.visualiser.tests;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.interfaces.simpleImpl.SimpleMarkupKind;

import junit.framework.TestCase;

/**
 * @author AndyClement
 */
public class StripeTest extends TestCase {

	Stripe testStripe1;
	Stripe testStripe2;
	Stripe testStripe3;
	Stripe testStripe4;
	IMarkupKind kind = new SimpleMarkupKind("KIND");
	IMarkupKind kind0 = new SimpleMarkupKind("KIND0");
	IMarkupKind kind1 = new SimpleMarkupKind("KIND1");
	IMarkupKind kind2 = new SimpleMarkupKind("KIND2");
	IMarkupKind kind3 = new SimpleMarkupKind("KIND3");
	
	/**
	 * Constructor for StripeTest.
	 * @param name
	 */
	public StripeTest(String name) {
		super(name);
	}

	
	/*
	 * sets up the stripes required for testing
	 */
	public void setUp() {
		testStripe1 = new Stripe();
		testStripe2 = new Stripe(kind, 10);
		testStripe3 = new Stripe(kind0, 15, 2);
		testStripe4 = new Stripe(buildKindsListForTesting(), 20, 3);
	}

	/*
	 * tests the equals method
	 */		
	public void testStripeEqualsMethods() {
		assertFalse("testStripe1 is equal to testStripe2", 
				testStripe1.equals(testStripe2) );
		assertFalse("testStripe2 is equal to testStripe3", 
				testStripe2.equals(testStripe3) );
		assertFalse("testStripe3 is equal to testStripe4", 
				testStripe3.equals(testStripe4) );
		assertFalse("testStripe4 is equal to the String hello", 
				testStripe4.equals("hello") );
		assertEquals("testStripe1 is not equal to itself", 
				testStripe1, testStripe1 );
		assertEquals("testStripe2 is not equal to itself", 
				testStripe2, testStripe2 );		
		assertEquals("testStripe4 is not equal to itself", 
				testStripe4, testStripe4 );
	}
	
	public void testStripe() {
		
	}

	/*
	 * Test for void Stripe(String, int)
	 */
	public void testStripeStringint() {
		Stripe s = new Stripe(kind1,10);
		assertTrue("Too many kinds in the stripe, expected one",s.getKinds().size()==1);
		assertTrue("Kind incorrect",((IMarkupKind)s.getKinds().get(0)).getName().equals("KIND1"));
		assertTrue("Offset wrong",s.getOffset()==10);
		assertTrue("Depth wrong, should be defaulting to one",s.getDepth()==1);
	}



	public void testGetKinds() {
		Stripe s = new Stripe(kind1,10);
		assertTrue("1. getKinds() returns list of wrong size: "+s.getKinds().size(),s.getKinds().size()==1);
		assertTrue("2. Kind (KIND1) missing from getKinds() return value",s.getKinds().contains(kind1));
		s = new Stripe(buildKindsListForTesting(),10,5);
		assertTrue("3. getKinds() returns list of wrong size: "+s.getKinds().size(),s.getKinds().size()==3);
		assertTrue("4. Kind (KIND1) missing from getKinds() return value",s.getKinds().contains(kind1));
		assertTrue("5. Kind (KIND2) missing from getKinds() return value",s.getKinds().contains(kind2));
		assertTrue("6. Kind (KIND3) missing from getKinds() return value",s.getKinds().contains(kind3));
	}



	/*
	 * Test for void Stripe(String, int, int)
	 */
	public void testStripeStringintint() {
		Stripe s = new Stripe(kind2 ,10,25);
		assertTrue("Too many kinds in the stripe, expected one",s.getKinds().size()==1);
		assertTrue("Kind incorrect",s.getKinds().get(0).equals(kind2));
		assertTrue("Offset wrong, should be 10",s.getOffset()==10);
		assertTrue("Depth wrong, should be 25",s.getDepth()==25);
	}


	/*
	 * Test for void Stripe(List, int, int)
	 */
	public void testStripeListintint() {
		Stripe s = new Stripe(buildKindsListForTesting(),100,12);
		assertTrue("Incorrect number of kinds in the stripe, expected three",s.getKinds().size()==3);
		assertTrue("Kind (KIND1) missing from stripe",s.hasKind(kind1));
		assertTrue("Kind (KIND2) missing from stripe",s.hasKind(kind2));
		assertTrue("Kind (KIND3) missing from stripe",s.hasKind(kind3));
		assertTrue("Offset wrong",s.getOffset()==100);
		assertTrue("Depth wrong",s.getDepth()==12);
	}



	public void testGetOffset() {
		Stripe s = new Stripe(kind1,10);
		assertTrue("Offset is wrong: "+s.getOffset(),s.getOffset()==10);
		s.setOffset(-1);
		assertTrue("Offset is wrong: "+s.getOffset(),s.getOffset()==-1);
		s.setOffset(100000);
		assertTrue("Offset is wrong: "+s.getOffset(),s.getOffset()==100000);
	}



	public void testGetDepth() {
		Stripe s = new Stripe(kind1,10);
		assertTrue("Depth is wrong: "+s.getDepth(),s.getDepth()==1);
		s.setDepth(-1);
		assertTrue("Depth is wrong: "+s.getDepth(),s.getDepth()==-1);
		s.setDepth(100000);
		assertTrue("Depth is wrong: "+s.getDepth(),s.getDepth()==100000);
	}



	/*
	 * Test for String toString()
	 */
	public void testToString() {
		assertEquals("toString is not working for testStripe1", 
				"Stripe: [0:0:0:]", testStripe1.toString());
		assertEquals("toString is not working for testStripe2", 
				"Stripe: [10:1:11: KIND ]", testStripe2.toString());
		assertEquals("toString is not working for testStripe3", 
				"Stripe: [15:2:17: KIND0 ]", testStripe3.toString());
		assertEquals("toString is not working for testStripe4", 
				"Stripe: [20:3:23: KIND1  KIND2  KIND3 ]", testStripe4.toString());
	}
	
	public void testHasKind() {
		assertFalse("testStripe1 incorrectly contains KIND1", 
				testStripe1.hasKind(kind1));
		assertFalse("testStripe2 incorrectly contains KIND3", 
				testStripe2.hasKind(kind3));
		assertTrue("testStripe3 does not contain KIND0", 
				testStripe3.hasKind(kind0));
		assertTrue("testStripe4 does not contain KIND1", 
				testStripe4.hasKind(kind1));
	}

	public void testCompareTo() {
		
		//an exception is expected when a non stripe object is used (not certain why it isnt defined in the constructor)
		try {
			int compareResult1 = testStripe2.compareTo("hello");	
			
			//if the test gets here something is wrong because no exception is thrown
			fail("The method is trying to compare to a non stripe type");
		}
		catch (final Exception success) {
			//The method correctly complains
		}
		
		
		//two identical stripes should be equal
		int compareResult2 = testStripe1.compareTo(testStripe1);
		if (compareResult2 != 0) {
			fail("When compared to itself, testStripe1 is not equal to itself");
		}

		Stripe tempTestStripe = new Stripe(new SimpleMarkupKind("KIND6"), 15, 1);
		
		int compareResult3 = testStripe3.compareTo(tempTestStripe);
		if (compareResult3 != 0) {
			fail("When two stripes with the same offSet are compared, 0 should be the result");
		}
		
		int compareResult4 = testStripe4.compareTo(testStripe3);
		if (compareResult4 != 1) {
			fail("testStripe4 has a greater offset than testStripe3 but the result isnt 1");
		}
		
		int compareResult5 = testStripe2.compareTo(testStripe3);
		if (compareResult5 != -1) {
			fail("testStripe2 has a lesser offset than testStripe3 but the result isnt -1");
		}
		
	}

	public void testStringifyKinds() {
		assertEquals("stringifyKinds is not working on testStripe1", 
				"", testStripe1.stringifyKinds());
		assertEquals("stringifyKinds is not working on testStripe2", 
				" KIND ", testStripe2.stringifyKinds());
		assertEquals("stringifyKinds is not working on testStripe3", 
				" KIND0 ", testStripe3.stringifyKinds());
		assertEquals("stringifyKinds is not working on testStripe4", 
				" KIND1  KIND2  KIND3 ", testStripe4.stringifyKinds());
	}

	//a few basic tests are required to satisfy code coverage
	public void testBasicMethods() {
		
		assertEquals("The getToolTip or stringifyKinds is returning the wrong string", 
				testStripe4.stringifyKinds(), testStripe4.getToolTip());
		
		testStripe2.setDepth(5);
		if(testStripe2.getDepth() != 5) {
			fail("Either setDepth or getDepth hasnt worked correctly");
		}
		
		testStripe2.setOffset(3);
		if(testStripe2.getOffset() != 3) {
			fail("Either setOffset or getOffset hasnt worked correctly");			
		}
		
		
		ArrayList lst = new ArrayList();
		lst.add("KIND7");
		lst.add("KIND8");
		testStripe2.setKinds(lst);
		if(!testStripe2.getKinds().equals(lst)) {
			fail("Either setKinds or getKinds hasnt worked correctly");			
		}
		
		
		testStripe1.addKinds(lst);
		if(!testStripe1.getKinds().equals(lst)) {
			fail("Either addKinds or getKinds hasnt worked correctly");
		}
	}

	public void testSetOffset() {
		Stripe s = new Stripe(kind1,15);
		s.setOffset(15);
		assertTrue("Offset is wrong: "+s.getOffset(),s.getOffset()==15);
		s.setOffset(10000);
		assertTrue("Offset is wrong: "+s.getOffset(),s.getOffset()==10000);
	}

	public void testSetDepth() {
		Stripe s = new Stripe(kind1,10);
		s.setDepth(35);
		assertTrue("Depth is wrong: "+s.getDepth(),s.getDepth()==35);
		s.setDepth(1000);
		assertTrue("Depth is wrong: "+s.getDepth(),s.getDepth()==1000);
	}


	private List buildKindsListForTesting() {
		List kinds = new ArrayList();
		kinds.add(kind1);
		kinds.add(kind2);
		kinds.add(kind3);
		return kinds;
	}
}
