package aspectPath;

public aspect ContainerAspect {

	before() : execution(public static void *..fromContainer(String[])) {
		System.out.println("from container aspect");
	}
}
