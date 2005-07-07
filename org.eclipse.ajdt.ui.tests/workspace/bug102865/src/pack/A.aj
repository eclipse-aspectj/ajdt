package pack;

public aspect A {

	pointcut p() : call(* *(..));
	
	before() : p() {
		
	}
	
}
