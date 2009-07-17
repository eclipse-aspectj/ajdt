package f;

public aspect MyAspect {
	before() : execution(* f.*.*(..)) {}
	
}
