package recursive;

public aspect RecursiveCatcher {

	pointcut recur() : 
    	   call(public void 
    			   RecursiveCatcher.recursiveCall(String));
	before(): recur() {
	}

	pointcut setRecursiveCall() : set(String RecursiveCatcher.recursiveCall);
	before() : setRecursiveCall() {
	}

	pointcut getRecursiveCall() : get(String RecursiveCatcher.recursiveCall);
	before() : getRecursiveCall() {
	}

	String recursiveCall;

	public void recursiveCall(String i) {
		recursiveCall(i);
		recursiveCall = "boo!";
		recursiveCall.toString();
	}

}
