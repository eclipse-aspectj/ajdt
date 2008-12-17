package ships;

public privileged aspect ShipAccessor {
	final static int MAX_X = 100;
	final static int MAX_Y = 100;
	
	public int Ship.getX() {
		return this.x;
	}
	
	public int Ship.getY() {
		return this.y;
	}
	
	public void Ship.setX(int newX) {
		this.x = newX;
	}
	
	public void Ship.setY(int newY) {
		this.y = newY;
	}
	
	before(int newX, int newY) : execution(public void Ship.moveTo(int, int)) && args(newX, newY) {
		if (newX < 0 || newX > MAX_X || newY < 0 || newY > MAX_Y) {
			throw new RuntimeException("Invalid move");
		}
	}
	after(Ship s) returning: execution(public void Ship.moveTo(int, int)) && this(s) {
		System.out.println(s.whereAmI());
	}
	after(Ship s) : execution(public Ship.new(int, int)) && this(s) {
		System.out.println(s.whereAmI());
	}
	
	public static class FloatingThing { } 
	
	declare parents : Ship extends FloatingThing;
}
