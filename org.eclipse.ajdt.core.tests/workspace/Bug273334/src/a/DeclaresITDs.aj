package a;

public aspect DeclaresITDs {
    void doNothing() {
        new HasAnITD().method();
        new HasAnITD().field++;
    }
	int HasAnITD.field = 9;
	void HasAnITD.method() {
		
	}
	
}
