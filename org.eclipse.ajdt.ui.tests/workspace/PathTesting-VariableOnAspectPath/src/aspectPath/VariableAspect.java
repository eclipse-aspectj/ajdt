package aspectPath;

public aspect VariableAspect {

	before() : execution(public static void *..fromVariable(String[])) {
		System.out.println("from variable aspect");
	}
}
