package pack;

public aspect A {
	after(): all(){
		System.out.println("xxx");
	}
	
	pointcut all(): execution(* *(..));
}
