/*
 * Created on 24-Jan-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.ibm.wpstest.aspectj;


/**
 * @author hawkinsh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public aspect A {

	pointcut tracedPrint(String s): call(void java.io.PrintStream.println(*)) &&
		args(s) && !within(A);
		
	before(String s): tracedPrint(s) {
		System.out.println("got you: " + s + " ;)");
	}	
		
	after(String s): tracedPrint(s) {
		System.out.println("hehe, finished: " + s + " :(");
	}
}
