
public class HasPrivateMembers {
	private int x = 3;
	{ 
		x++;
		getNothing();
	}

	private class Nothing {
		
	}
	
	private HasPrivateMembers() {
		
	}
	
	private Nothing getNothing() {
		return new Nothing();
	}
	
}
 