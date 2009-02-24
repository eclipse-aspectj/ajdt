package be.cronos.aop.aspects;

import java.util.List;

import be.cronos.aop.InterTypeAspectSupport;

public aspect InterTypeAspect {

	public interface InterTypeAspectInterface {
	}

	declare parents : (@InterTypeAspectSupport *) implements InterTypeAspectInterface;

	public String InterTypeAspectInterface.foo() {
		return "bar";
	}

	public Log InterTypeAspectInterface.getLogger(List<?> h) {
		return new Log();
	}
}
