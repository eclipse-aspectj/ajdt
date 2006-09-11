/**
 * 
 */
package bar.foo;

public aspect MyBar2 {
	
	after() returning : execution(* main(..)) {
		System.out.println("after calling a main method");
	}

//	public void main() {
//		
//	}
}
