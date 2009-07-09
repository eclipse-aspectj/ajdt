import java.util.List;

public aspect RR {
	
	class Inner<T extends Object, H> {
		List<List<? extends Comparable<?>>> foo;
		List<List<List<String>>> bar;
	}  

	
	static abstract aspect InnerAspect<T> {
		after(R<String> r) : call(R.new(List<String>)) && args(r)  {
	    }
	}
	
	boolean R.f() { return false ; }
	
	// <<< 
    public R<List<List<?>>>   R.c() {
    	if (4 < 5) {  }  
    	int x = f() ? 5 : 8;
    	x++;
    	R<String> h = new R<String>();
    	h.toString();
    	System.out.println(78 >>> 2);
        return null;
    }
    public R<List<?>> R.c2() {
    	if (4 < 5) {  }
    	
     	int x = f() ? (f() ? 5: 8) : 8;
     	x++;
    	R<String> h = new R<String>();
    	h.toString();
        return null;
    }
    
    after(R<String> r) : call(R.new(List<String>)) && args(r)  {
    }
    
    
    
}

class R<T> { } 