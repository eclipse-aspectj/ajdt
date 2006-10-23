package lib;

public aspect AnnotationBeanConfigurerAspect extends
		AbstractBeanConfigurerAspect {
	// pointcut is on line 6
	protected pointcut beanCreation()
		: initialization(*.new(..)) && !within(lib.*);

	before() : execution(* main(..)) {
		
	}
}
