/**
 * 
 */
package TestPackage;

public aspect Asp {
	
	pointcut extendMessage() : call(* Hello.printMessage(..));
	
	before() : extendMessage() {
		System.out.println("Pre Message");
	}

}
