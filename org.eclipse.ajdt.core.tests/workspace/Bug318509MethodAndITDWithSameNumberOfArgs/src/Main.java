public class Main {
	public static void main(String[] args) {
		//In the class
		MyClass cls = new MyClass(123);
		cls.method(123);    
		
		//In the aspect
		cls = new MyClass("Hello");
		cls.method("Hello"); 
	}
}