
public aspect Aspect {
	declare error : call(* *(..)): "test message";
}
