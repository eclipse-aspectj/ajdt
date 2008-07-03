package aspectPath;

public aspect ProjectAspect {

	before() : execution(public static void *..fromProject(String[])) {
		System.out.println("from project aspect");
	}
}
