package p;

public aspect AdviceCalls {
	
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
