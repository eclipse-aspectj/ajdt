/**
 * 
 */

public aspect MyBar3 {
	
	void around() : execution(* main(..)) {
		System.out.println("around: about to call a main method");
		proceed();
		System.out.println("around: after calling a main method");
	}

//	public void main() {
//		
//	}
}
