package q;

public aspect AnAspect {
	before() : execution(* *.*(..)) {
		System.out.println("Hi!");
	}
}
