/**
 * 
 */
package pack;

public aspect A1 {
	before() : execution(void foo()) {
		
	}
	
	void foo() {
		
	}
}
