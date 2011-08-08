package p;

public class HandleTestingClass {
	void foo(int x, long y) { 
		foo1(x,y);
		foo1(x,y);

		foo2(x,y);

		class MyClass { 
		    int x = 9;
		}
		new MyClass() {};
		new MyClass() {};
		
		yCall(1);
		zCall(1);
		
	}
	void foo1(int x, long y) { }
	void foo2(int x, long y) { }
	
	int yCall(int y) throws RuntimeException {
		return y;
	}
	int zCall(int y) throws RuntimeException {
		return y;
	}
	
	class MyClass {
	    int x = 9;
	}
	
	{ }
	static { }
}
