/*
 * Created on 14-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package tracing;


/**
 * @author Sian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public aspect Trace {

	pointcut methodExecution(): execution(* *.*(..));
	
	before(): methodExecution() {
		System.out.println("Entering a method");
	}
	
}
