package figures.support;

import figures.Line;

/**
 * @author colyer, 19-Feb-2003
 *
 */
public aspect LineLogger {

	pointcut lineCreation() : 
		execution(Line+.new(..));
		
	after( Line l ) returning : lineCreation() &&
		target(l) {
		Log.log( l.toString() );		
	}

}
