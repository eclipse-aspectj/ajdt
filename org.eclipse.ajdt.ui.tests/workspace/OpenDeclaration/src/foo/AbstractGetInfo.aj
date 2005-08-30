package foo;

public abstract aspect AbstractGetInfo {
	public pointcut executeGo(): execution(void go());
}
