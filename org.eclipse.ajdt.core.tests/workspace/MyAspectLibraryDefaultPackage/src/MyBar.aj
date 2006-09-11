/**
 * 
 */
public aspect MyBar {
	before() : execution(* main(..)) {
		System.out.println("about to call a main method");
	}

	public void main() {
		
	}
}
