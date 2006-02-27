package foo;

public aspect MyAspect {
	
	declare warning: call(void Orange.update(..)): 
		"call to update";

	pointcut p1() : execution(void setX(..))
		|| execution(void setY(..));
	
	after() : p1() {
		System.out.println("after p1");
	}
	
	before() : execution(void set*(..)) {
		System.out.println("before set*");
	}
}
