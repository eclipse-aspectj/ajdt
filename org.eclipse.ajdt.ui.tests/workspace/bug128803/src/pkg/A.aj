package pkg;

public aspect A {

	pointcut p() : execution(* somemethod());
	
	before() : p() {
	}
	
}

