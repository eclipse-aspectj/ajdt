/*
 * Created on 02-Aug-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package helloWorld;


/**
 * @author Sian
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public aspect HelloAspect {

	String around () : call(String getMessage()) {
		return proceed() + " and hello from AspectJ";
	}
	
}
