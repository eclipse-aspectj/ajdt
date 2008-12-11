package tmp.b;

import java.util.List;

import tmp.a.AbstractAspect;

public aspect ConcreteAspect2 extends AbstractAspect {
    List<String> l = new LinkedList<String>();
}
