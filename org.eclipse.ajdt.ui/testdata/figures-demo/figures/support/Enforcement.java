package figures.support;

import figures.FigureElement;
import figures.Point;

/**
 * @author colyer, 19-Feb-2003
 *
 */
public aspect Enforcement {

	pointcut privateFieldUpdate() :
		set(private * FigureElement+.*);
		
	pointcut inSetMethod() :
		withincode(* FigureElement+.set*(..));
		
	pointcut insideConstructor() :
		withincode(FigureElement+.new(..));
		
	declare warning : privateFieldUpdate() &&
	 !(inSetMethod() || insideConstructor()) :
	 "Only update private fields through setters";

	pointcut pointCoordUpdate() :
		set(int Point+.*);
		
	before(int coord) : pointCoordUpdate() &&
		args(coord) {
		if (coord < 0 ) {
			throw new IllegalArgumentException( "too small" );	
		}		
	}		

}
