
public abstract aspect Library {

	abstract pointcut stuff();
	
	after(): stuff() {
		System.out.println("> after stuff");
	}

}
