package m;

public aspect AspectOnAspectPath {

    before() : execution(* *..main(String[])) {
        System.out.println("Aspect has been woven!");
    }
}
