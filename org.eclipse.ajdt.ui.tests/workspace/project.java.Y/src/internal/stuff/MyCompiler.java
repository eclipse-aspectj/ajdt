/*
 * Created on 24-Aug-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package internal.stuff;

/**
 * @author hawkinsh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyCompiler {

	public MyCompiler() {};
	
	public void compile() {
		System.out.println("I'm compiling!");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
}
