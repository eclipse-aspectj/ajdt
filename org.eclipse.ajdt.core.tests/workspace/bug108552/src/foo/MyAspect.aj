/**
 * 
 */
package foo;

import java.util.Collection;

/**
 * An ITD with a generic parameterized type - requires 5.0
 */
public aspect MyAspect {
    public void MyClass.foo(Collection<String> bar){
    }
}
