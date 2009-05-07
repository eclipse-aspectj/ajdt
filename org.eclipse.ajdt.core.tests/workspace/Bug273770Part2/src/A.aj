
public aspect A {

	@Deprecated
	before() : execution(* main()) {}
	
	before() : adviceexecution() && cflow(withincode(@Deprecated * *.*(..))) {
		
	}
}
