package test;

import p1.Test;

public aspect MyAspect {

	before() : execution(void Test.foo(..)) {
		System.out.println("Hello");
	}
}
