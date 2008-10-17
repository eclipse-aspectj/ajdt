package a;

import b.AdvisesFromAspectPath;

public aspect AdvisesFromProject extends AdvisesFromAspectPath {
	 
	public pointcut amain() : execution(public static void a.TargetClass.main2(String[]));
} 
