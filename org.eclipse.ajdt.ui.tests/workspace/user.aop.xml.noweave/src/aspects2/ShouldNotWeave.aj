package aspects2;

public aspect ShouldNotWeave {
	before() : execution(* main(..)) {
		System.out.println("Before advise called.");
		
	}

	public void main() {
		
	}
}
