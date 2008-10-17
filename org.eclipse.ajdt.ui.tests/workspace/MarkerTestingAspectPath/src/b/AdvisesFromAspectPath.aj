package b;


public abstract aspect AdvisesFromAspectPath {
	
	public abstract pointcut amain();
	before() : amain() {
		System.out.println("Advised from aspect path!");
	} 
	  
	before() : execution(public static void a.TargetClass.main(String[])) {
		System.out.println("Advised from project!");
	}

}