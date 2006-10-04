package test;

public aspect MyAspect {
	int Demo.x = 5;
	
	void Demo.foo() {
		
	}
	
	declare warning : execution(* *.*(..)) : "blah";
}
