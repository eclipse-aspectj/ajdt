package aspectPath;

public aspect ContainerAspect {

	before() : execution(public static void main.Main.fromContainer(String[])) {
		System.out.println("from container aspect");
	}
}
