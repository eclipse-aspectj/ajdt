package foo;

public class Orange {

	public void setX() {
		update();
	}
		
	public void setup() {
		System.out.println("setup");
	}
	
	public static void update() {
		System.out.println("update");
	}
}
