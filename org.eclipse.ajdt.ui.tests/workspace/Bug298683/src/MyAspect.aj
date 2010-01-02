public aspect MyAspect {
    MyAspect() {
        super();
    }

    declare parents: MyObject implements MyInterface;

    public boolean MyInterface.instanceOf(Class<? extends Object> c) {
        return c.isInstance(this);
    }
}

interface MyInterface { }
class MyObject { 
    MyObject() {
        super();
    }
}

class Main {
    Main() {
        super();
    }
    public static void main(String[] args) {
        new MyObject().instanceOf(MyObject.class);
    }
}
