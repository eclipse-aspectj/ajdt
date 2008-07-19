package aspectPath;

public aspect JarAspect {

	before() : execution(public static void *..fromJar(String[])) {
		System.out.println("from jar aspect");
	}
}
