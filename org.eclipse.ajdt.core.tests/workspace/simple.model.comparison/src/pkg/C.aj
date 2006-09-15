package pkg;

public class C {

	pointcut p() : execution(void cMethod());
	
	public void cMethod() {
		
	}
	
}
