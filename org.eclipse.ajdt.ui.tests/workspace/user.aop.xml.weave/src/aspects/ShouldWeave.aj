package aspects;

public aspect ShouldWeave {
	before() : execution(* main(..)) {
		System.out.println("Before advise called.");
		
	}

	public void main() {
		
	}
}
