> 1 public class ErrorClass {
	> 1 	public static aspect ProfilingAspect {
		> 1 	private long AccessTracked.lastAccessedTime;

		> 1 		public void AccessTracked.updateLastAccessedTime() {
			> 1 			lastAccessedTime = System.nanoTime();
			> 1 		}

		> 1 		public long AccessTracked.getLastAccessedTime() {
			> 1 		return lastAccessedTime;
			> 1 	}

		> 1 		before(AccessTracked accessTracked)
		> 1     : execution(* AccessTracked+.*(..)) 
> 1       && !execution(* AccessTracked.*(..)) 
    > 1   && this(accessTracked){
    	> 1 	accessTracked.updateLastAccessedTime(); // ERROR reported here
    	> 1 	}
    > 1 
    > 1 	}
	> 1 
	> 1 	interface AccessTracked {
		> 1 	}

	> 1  }
