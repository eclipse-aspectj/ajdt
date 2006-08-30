package pkg;

public aspect Registry {

    void dummy() {
        register(getObjects()[0]);
    }
    
    Object[] getObjects() {
    	return new String[1];
    }
    
    void register(Object o) {
    }
    
}
