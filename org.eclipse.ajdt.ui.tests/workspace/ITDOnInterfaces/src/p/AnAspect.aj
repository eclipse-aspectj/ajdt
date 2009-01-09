package p;

public aspect AnAspect {
	public boolean AnInterface.running;

    public boolean AnInterface.isRunning() {
	    this.running = false;
	    this.isRunning2();
        return running;
    }
    
    declare parents : ASubInterface3 extends AnInterface;
    
    declare parents : ASubInterface3 extends ASubInterface2;
    
    declare parents : AClass implements ASubInterface3;
    
    public boolean ASubInterface.isRunning2() {
    	return running;
    }    
    public boolean ASubInterface2.isRunning2() {
    	return running;
    }    
    public boolean ASubInterface3.isRunning2() {
    	return running;
    }    
    public boolean ASubInterface2.isRunning3() {
    	return running;
    }    
}
