package p1;

public abstract aspect A {
	public abstract pointcut myPC();
	
	declare warning: myPC(): "warning";
	
	after(): myPC(){
		int x = 0;
	}
}