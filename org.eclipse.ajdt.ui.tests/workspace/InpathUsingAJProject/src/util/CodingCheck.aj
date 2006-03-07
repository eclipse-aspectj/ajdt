package util;

public aspect CodingCheck {
	public pointcut callSystemOut():
		get( * System.out );
	
	declare error: callSystemOut(): "System.out should not be called";
}
