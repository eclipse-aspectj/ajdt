package test;
import java.util.List;

public privileged aspect MyAspect {
    java.util.List<String> Demo.list = null;
	int Demo.x = 5;
	
	void Demo.foo(java.util.List<String> x) {
		
	}
	
	public Demo.new(int x) {
		this();
	}
	
    declare warning : execution(* *.nothing(..)) : "blah";
    
    declare error : execution(* *.nothing(..)) : "blah";
    
	declare soft : Exception : execution(* *.nothing(..));
	
	declare @type: (Demo): @Deprecated;
//	declare @field: (int Demo.x): @Deprecated;
//	declare @method: (void Demo.foo(..): @Deprecated;
//	declare @constructor: (public Demo.new(int)): @Deprecated;

	
	   protected pointcut s():
	        execution(String Object+.toString(..));
	   protected pointcut t():
	        execution(void Demo.g(..));

    before (): s() {
    }
    after (): s() {
    }
    void around (Demo d): t() && target(d) {
        proceed(d);
        return;
    }
    after () returning(): s() {
    }
    after () throwing(): s() {
    }
}
