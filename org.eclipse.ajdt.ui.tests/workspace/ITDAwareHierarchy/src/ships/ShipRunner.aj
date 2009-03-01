package ships;

public class ShipRunner {
	public static void main(String[] args) {
		Ship s = new Ship(5, 6); 
		System.out.println(s.whereAmI());
		s.moveTo(2, 2);
		System.out.println(s.whereAmI());
		System.out.println(s.getX() + " " + s.getY());
	}
}
