import org.aspectj.lang.annotation.DeclareWarning;
import org.aspectj.lang.annotation.Aspect;
@Aspect
public aspect A {
	@DeclareWarning("execution(void *.foo(..))")
	static final String h = "barf";
	void foo() { }
	
	declare error : execution(public void *.x(..)) : "barf";
	void bar() {
		
	}
}
