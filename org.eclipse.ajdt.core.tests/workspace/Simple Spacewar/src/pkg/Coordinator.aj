package pkg;

public aspect Coordinator {

	protected pointcut synchronizationPoint():
		call(void Registry.register(..)) ||
	    call(Object[] Registry.getObjects(..));

    before (): synchronizationPoint() {
    }
	
}
