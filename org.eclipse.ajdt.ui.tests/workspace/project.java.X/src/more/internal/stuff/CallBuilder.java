/*
 * Created on 24-Aug-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package more.internal.stuff;
 
import internal.stuff.MyBuilder;

/**
 * @author hawkinsh
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CallBuilder {

	public CallBuilder() {
		System.out.println("in constructor for callbuilder");
		MyBuilder mb = new MyBuilder();
		mb.doBuild();
	}
	
	public static void main(String[] args) {
		CallBuilder cb = new CallBuilder();
	}
}
