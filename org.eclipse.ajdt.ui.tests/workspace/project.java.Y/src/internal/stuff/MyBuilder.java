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
public class MyBuilder {
	
	public MyBuilder() {};
	
	public void doBuild() {
		System.out.println("I am building.....");
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
