 
public privileged aspect IsPrivilegedWithError {
	public static void doSomething() {
		new HasPrivateMembers().errorMethod();  // should be an error
		new HasPrivateMembers().getNothing(6);  // should be an error
	}
}
