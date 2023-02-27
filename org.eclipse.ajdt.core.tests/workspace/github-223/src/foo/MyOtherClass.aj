/*
 * Created on 05-May-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package foo;
/**
 * @author Sian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyOtherClass {

	public static class MyInnerClass {
		
		public static aspect MyInnerInnerAspect {
			
			before(): execution(* MyClass.method1()) {
				System.out.println("Before method1..");
			}
		
		}
		
	}
	
}
