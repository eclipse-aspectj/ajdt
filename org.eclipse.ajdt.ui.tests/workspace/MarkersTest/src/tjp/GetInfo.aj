/*
 * Copyright (c) Xerox Corporation 1998-2002. All rights reserved.
 * 
 * Use and copying of this software and preparation of derivative works based
 * upon this software are permitted. Any distribution of this software or
 * derivative works must comply with all applicable United States export control
 * laws.
 * 
 * This software is made available AS IS, and Xerox Corporation makes no
 * warranty about the software, its performance or its conformity to any
 * specification.
 */

package tjp;

import java.io.Serializable;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;

// loads of nonsense advice added to test for different
// markers
aspect GetInfo {
	declare warning : set(int Demo.x) : "field set";
	declare parents : Demo implements Serializable;
	
	declare soft : DemoException : execution(void go());

	public void Demo.itd(int t) {
		println("itd");
	}

	private int Demo.f = 5;

	static final void println(String s) {
		System.out.println(s);
	}
	
	before(): handler(Throwable+) {
		println("handler");
	}
	
	pointcut goCut(): cflow(this(Demo) && execution(void go()));

	pointcut demoExecs(): within(Demo) && execution(* *(..));

	pointcut fieldSet(): set(int Demo.x);
	
	before(): demoExecs() {
		println("before");
	}

	before(): execution(* foo(..)) && cflow(this(Demo)) {
		println("before2");
	}

	after(): fieldSet() {
		println("set");
	}
	
	Object around(): demoExecs() && !execution(* go()) && goCut() {
		println("Intercepted message: "
				+ thisJoinPointStaticPart.getSignature().getName());
		println("in class: "
				+ thisJoinPointStaticPart.getSignature().getDeclaringType()
						.getName());
		printParameters(thisJoinPoint);
		println("Running original method: \n");
		Object result = proceed();
		println("  result: " + result);
		return result;
	}

	after(): execution(void printParameters(..)) {
		println("advises aspect");
	}
	
	static private void printParameters(JoinPoint jp) {
		println("Arguments: ");
		Object[] args = jp.getArgs();
		String[] names = ((CodeSignature) jp.getSignature())
				.getParameterNames();
		Class[] types = ((CodeSignature) jp.getSignature()).getParameterTypes();
		for (int i = 0; i < args.length; i++) {
			println("  " + i + ". " + names[i] + " : " + types[i].getName()
					+ " = " + args[i]);
		}
	}
	
	
}