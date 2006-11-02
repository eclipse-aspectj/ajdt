package aspects;

import library.UserLibraryClass;

/**
 * Aspect that attempts to advise a class which has a dependant class that 
 * is not on the aspect path.
 */
public aspect UserAspect {

	/* 
	 * Advising the constructor for UserLibraryClass does results
	 */
	before() : call(UserLibraryClass.new(..)) {  
		System.out.println("UserLibraryClass constructor()");
	} 
}
