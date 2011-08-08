package p;

public aspect AdviceCalls {
	
    // must have this or AspectJ minimalModel will drop some elements
    void around() : within(*) { }
    
	static void doNothing() {
		doNothing();
		doNothing();
		doNothing();
	}

	before() : call(static void doNothing()) {
		doNothing();
		doNothing();
		doNothing();
		doNothing();
	}
	after() : call(static void doNothing()) {
		doNothing();
		doNothing();
		doNothing();
		doNothing();
	}
}
