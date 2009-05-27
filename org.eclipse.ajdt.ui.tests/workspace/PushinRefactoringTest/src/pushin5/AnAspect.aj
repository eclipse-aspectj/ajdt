package pushin5;

import pushin.OtherClass;

public aspect AnAspect {
	
	declare parents : (AClass* || AnInterface*) implements I, J; 

	interface I {
		
	}
	interface J {
		
	}

	declare parents : AClass implements H; 

    static class H {
    	
    }
    
    
    // this is OK
    declare parents : NoErrorFullyQualified implements java.io.Serializable;
    
    // this will result fully qualified type in target type
    declare parents : ErrorNoImport extends OtherClass;
    
}
