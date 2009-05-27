package pushin4;


public aspect A {
	int B.y = 9; 
	int C.y = 9; 
	
	declare @type: B : @Deprecated;

	int y;
}
 