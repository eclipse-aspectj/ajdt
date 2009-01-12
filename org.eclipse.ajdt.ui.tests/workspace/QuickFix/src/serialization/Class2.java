package serialization;

import java.io.Serializable;

//serialVersionUID = -8766141914748822473L
public class Class2 implements Serializable {
	
	// serialVersionUID = -6071201175706894658L
	public class Class3 implements Serializable {

		
	}

	// fails because not implemented
	Serializable s = new Serializable() {
		
	};


	
	public void doNothing() {
		// fails because not implemented
		class Class4 implements Serializable {
			
		}
	}
	public void doNothing1() {
		// fails because not implemented
		class Class4 implements Serializable {

			
		}
	}
}
