/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins - initial version
 *******************************************************************************/
package wpstest.aspectj;


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
