package ships;

public class Ship {
	 
	public Ship(int x, int y) {
		this.x = x;
		this.y = y;
	}
	private int x;
	 
	private int y; 
 
	public void moveTo(int newX, int newY) {
		x = newX;
		y = newY;
	}
	
	public String whereAmI() {
		return "I am at " + x + ", " + y;
	}
}
