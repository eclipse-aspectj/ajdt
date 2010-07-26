public privileged aspect MyAspect {
	
	public MyClass.new(String arg) {
		this.msg = arg;
	}
	
	public void MyClass.method(String arg) {
		this.msg = arg;
	}
	
}