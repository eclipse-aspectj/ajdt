package pushin4;


public aspect D {
	int B.z = 9; 
	int C.z = 9; 
	
	declare @field: int B.a : @Deprecated;

	int y;
}
 