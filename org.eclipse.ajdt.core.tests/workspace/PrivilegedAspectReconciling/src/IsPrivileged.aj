 
public privileged aspect IsPrivileged {
	public static void doSomething() {
		new HasPrivateMembers().x++;
		HasPrivateMembers.Nothing nothing = new HasPrivateMembers().getNothing();
		nothing.toString();
	}
}
