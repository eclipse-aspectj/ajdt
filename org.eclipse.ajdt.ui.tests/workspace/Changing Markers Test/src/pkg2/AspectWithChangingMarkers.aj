package pkg2;

import pkg.ClassWithChangingMarkers;
import java.io.Serializable;


public aspect AspectWithChangingMarkers {

	int ClassWithChangingMarkers.x;

	declare parents : ClassWithChangingMarkers implements Serializable;
	
	declare warning : execution(public void ClassWithChangingMarkers.declareWarning()) : "Warning!";
	
	before() : execution(public void ClassWithChangingMarkers.advised()) {
		
	}
}
