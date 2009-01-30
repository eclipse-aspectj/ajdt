package ajfiles;


public class C2 {
	// missing a }
	// should only see one reconcile error
	public static void main(String[] args) {
		int x = 7;
		if (true) {
		} else 
			x--;
		} 
	}    
      
	private static Object x(Object project, boolean b) {
		return null;
	}
}
 