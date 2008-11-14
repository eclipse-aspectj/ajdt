package a;

public aspect AnAspect {

	before() : execution(int b.AClass.nothing()) {
		
	}
}
