/*
 * 
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
 *  
 */
package tjp;

public class Demo {
	static Demo d;
	int x;
	
	public static void main(String[] args) {
		new Demo().go();
	}

	void go() {
		d = new Demo();
		d.foo(1, d);
		x = 5;
		System.out.println(d.bar(new Integer(3)));
	}

	void foo(int i, Object o) {
		try {
			System.out.println("Demo.foo(" + i + ", " + o + ")\n");
			throw new DemoException();
		} catch (DemoException e) {

		}
	}

	String bar(Integer j) {
		System.out.println("Demo.bar(" + j + ")\n");
		return "Demo.bar(" + j + ")";
	}
}