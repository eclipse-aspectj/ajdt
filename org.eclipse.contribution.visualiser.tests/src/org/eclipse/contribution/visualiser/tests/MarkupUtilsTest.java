/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Andy Clement - initial version
 *******************************************************************************/
package org.eclipse.contribution.visualiser.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.contribution.visualiser.core.Stripe;
import org.eclipse.contribution.visualiser.interfaces.IMarkupKind;
import org.eclipse.contribution.visualiser.simpleImpl.SimpleMarkupKind;
import org.eclipse.contribution.visualiser.utils.MarkupUtils;

import junit.framework.TestCase;

public class MarkupUtilsTest extends TestCase {
	
  private final static int ANYWHERE = -1;
  private static IMarkupKind A = new SimpleMarkupKind("AAA");  //$NON-NLS-1$
  private static IMarkupKind B = new SimpleMarkupKind("BBB");  //$NON-NLS-1$
  private static IMarkupKind C = new SimpleMarkupKind("CCC");  //$NON-NLS-1$
  private static IMarkupKind D = new SimpleMarkupKind("DDD");  //$NON-NLS-1$

  private List getStripes(int i) {
    List stripes = new ArrayList();
    if (i>0) stripes.add(new Stripe(A,5));
    if (i>1) stripes.add(new Stripe(B,10,4));
    if (i>2) stripes.add(new Stripe(C,15,4));
    if (i>3) stripes.add(new Stripe(D,13,4));
    return stripes;
  }
  
  private static boolean containsStripe(List inputs,IMarkupKind expKind,int expOffset,int expDepth,int expLocationInList) {
  	return containsStripe(inputs,new IMarkupKind[]{expKind},expOffset,expDepth,expLocationInList);
  }
  
  private static boolean containsStripe(List inputs, IMarkupKind[] expKinds, int expOffset, int expDepth, int expLocationInList) {
  	boolean found = false;
  	int i = 0;
  	for (Iterator iter = inputs.iterator(); iter.hasNext();) {
		Stripe stripe = (Stripe) iter.next();
		if (expKinds.length==stripe.getKinds().size()) {
			if (stripe.getOffset()==expOffset) {
			  boolean allTheRightKinds = true;
			  for (int j=0;j<expKinds.length;j++) {
			    if (!stripe.hasKind(expKinds[j])) allTheRightKinds = false;
			  }

			  if (allTheRightKinds) {
		   	    if (expLocationInList==ANYWHERE) found = true;
			    else if (expLocationInList==i)   found = true;
		      }
			}
		}
		i++;
	}
	if (!found) {
		// dump some data we'll need for debugging !
		StringBuffer sb = new StringBuffer();
		for (int j = 0; j < expKinds.length; j++) {
			sb.append(expKinds[j]).append(" "); //$NON-NLS-1$
		}
		System.err.println("Looking for stripe of kind:"+sb.toString()+" offset:"+expOffset+" depth:"+expDepth+" at location:"+expLocationInList); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	    i =0;
		for (Iterator iter = inputs.iterator(); iter.hasNext();) {
			Stripe element = (Stripe) iter.next();
			System.err.println("Stripe "+i+" is "+element.toString());i++; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	return found;
  }
  
  // Give it no data, shouldnt blow up!
  public void testStripeProcessing1() {
  	List inputs = null;
	MarkupUtils.processStripes(inputs);
  }
  
 
  // Give it a list with no data in, shouldnt blow up!
  public void testStripeProcessing2() {
    List inputs = new ArrayList();
    MarkupUtils.processStripes(inputs);
  } 
  
  
  // One stripe in, shouldn't get mangled by the stripe processor
  public void testStripeProcessing3() {
  	List inputs = getStripes(1);
  	MarkupUtils.processStripes(inputs);
  	assertTrue(inputs.size()==1);
  	Stripe s = (Stripe)inputs.get(0);
  	assertTrue(containsStripe(inputs,A,5,1,ANYWHERE));
  	assertTrue(((IMarkupKind)s.getKinds().get(0)).getName().equals("AAA")); //$NON-NLS-1$
  	assertTrue(s.getOffset()==5);
  }
  
  // Three stripes in, shouldn't be 'harmed'
  public void testStripeProcessing4() {
	List inputs = getStripes(3);
	MarkupUtils.processStripes(inputs);
	assertTrue(inputs.size()==3);
	assertTrue(containsStripe(inputs,A,5,1,0));
	assertTrue(containsStripe(inputs,B,10,4,1));
	assertTrue(containsStripe(inputs,C,15,4,2));
  }
  
  // Four stripes, fourth overlays the second and third
  // (1)5----->   (2)10----->    (3)15----->
  //                     (4)13--------->
  // After the call, there should be 6 stripes, two extra stripes are introduced
  // to cover the places where (4) overlays (2) and where (4) overlays (3)
  public void testStripeProcessing5() {
  	List inputs = getStripes(4);
  	MarkupUtils.processStripes(inputs);
	assertTrue(inputs.size()==6);
	assertTrue(containsStripe(inputs,A,5,1,0));
	assertTrue(containsStripe(inputs,B,10,3,ANYWHERE));
	assertTrue(containsStripe(inputs,new IMarkupKind[]{B,D},13,1,ANYWHERE));
	assertTrue(containsStripe(inputs,D,14,1,ANYWHERE));
	assertTrue(containsStripe(inputs,new IMarkupKind[]{C,D},15,2,ANYWHERE));
	assertTrue(containsStripe(inputs,C,17,2,ANYWHERE));
  }
  
  // Stripe overlay testing:
  // Two stripes start at the same location, of different lengths.
  public void testStripeProcessing6() {
  	List inputs = new ArrayList();
  	inputs.add(new Stripe(A,5,10));
  	inputs.add(new Stripe(B,5,8));
  	MarkupUtils.processStripes(inputs);
  	assertTrue(inputs.size()==2);
  	assertTrue(containsStripe(inputs,new IMarkupKind[]{A,B},5,3,ANYWHERE));
  	assertTrue(containsStripe(inputs,A,13,2,ANYWHERE));
  }
  
  // Stripe overlay testing:
  // Two stripes start at the same location, of different lengths (other way round!)
  public void testStripeProcessing7() {
	List inputs = new ArrayList();
	inputs.add(new Stripe(B,5,8));
	inputs.add(new Stripe(A,5,10));
	MarkupUtils.processStripes(inputs);
	assertTrue(inputs.size()==2);
	assertTrue(containsStripe(inputs,new IMarkupKind[]{A,B},5,3,ANYWHERE));
	assertTrue(containsStripe(inputs,A,13,2,ANYWHERE));
  }
}
