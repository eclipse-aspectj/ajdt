public aspect MyAspect {

    declare parents: MyObject implements MyInterface;

    public boolean MyInterface.instanceOf(Class<? extends Object> c) {
        return c.isInstance(this);
    }
}

interface MyInterface { }
class MyObject { }

class Main {
    public static void main(String[] args) {
        new MyObject().instanceOf(MyObject.class);
    }
}
