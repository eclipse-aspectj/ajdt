/*
 * Created on 04-Nov-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package bar;

import foo.HelloWorld;

/**
 * @author mchapman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public aspect AspectHelloWorld {
	pointcut helloWorldJarFile():
		execution (public void HelloWorld.printHelloWorld());
	
	void around(): helloWorldJarFile() {
		System.out.println("Hello world from aspect");
	}
}
