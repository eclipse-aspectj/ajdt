
public aspect Client extends Library {

	pointcut stuff(): execution(* *.main(..));

	after(): stuff() { }

}

