package a;

public aspect DeclaresITDs {
    void doNothing() {
        new HasAnITD().method();
        new HasAnITD().field++;
    }
	int HasAnITD.field = 9;
	void HasAnITD.method() {
		field = 8;
		this.field = 8;
		method();
		this.method();
		regularMethod();
		this.regularMethod();
		regularField++;
		this.regularField++;
	}
	
}
