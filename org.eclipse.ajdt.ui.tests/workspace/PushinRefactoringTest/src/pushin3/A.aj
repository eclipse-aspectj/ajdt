package pushin3;


public aspect A {
	int B.y = 9; 
	
	declare @type: B : @Deprecated;

	int y;
}
 