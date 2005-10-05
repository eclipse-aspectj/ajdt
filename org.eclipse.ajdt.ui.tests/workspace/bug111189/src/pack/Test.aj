package pack;

class Test {
	
	public void testMethod() {
		class C {
			public void m(){			
			}
		}
	}
	
	class C1 {
		public void m() {
			method();
		}
	}
	
	public void method() {
		
	}
}

aspect A {
	
	declare warning : execution(* m(..)) : "blah";

	declare warning : call(* method(..)) : "shouldn't call method()";
}
