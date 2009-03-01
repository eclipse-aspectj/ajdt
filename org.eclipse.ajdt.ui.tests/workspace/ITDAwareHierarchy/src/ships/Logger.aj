package ships;

public aspect Logger {
	 interface Log {
		 
	 }
	 
	 declare parents : Ship implements Log;
}
