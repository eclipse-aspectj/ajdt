
package helloWorld;

public aspect HelloAspect {

	String around () : call(String getMessage()) {
		return proceed() + " and hello from AspectJ";
	}
	
}
