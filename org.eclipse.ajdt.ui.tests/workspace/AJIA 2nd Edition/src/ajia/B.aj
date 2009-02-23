package ajia;

aspect B {
    private int I.i;
    
    after(I instance): execution(* *(..)) && target(instance) {
        instance.i = 5;
    } 
}

interface I {
    int x = 9;
}