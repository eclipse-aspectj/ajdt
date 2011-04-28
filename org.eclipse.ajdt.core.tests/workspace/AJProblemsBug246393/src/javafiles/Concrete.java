package javafiles;

// test that anonymous inner classes properly receive 
// ITDs from implemented interfaces
public class Concrete {
	public static void main(String[] args) {
		Interface i = new Interface() { };
		i.doNothing();
	}
}
