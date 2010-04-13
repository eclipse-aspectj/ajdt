package a;

public class HasAnITD {

	void doNothing() {
		this.field++;
		field++;
		this.method();
		method();
	}
	
	void regularMethod() { }
	int regularField = 9;
}
