
public aspect Advises {
	before() : execution(public static void main(String[])) /*&& cflow(execution(public String toString()))*/ {
		thisJoinPoint.toLongString(); 
	}
}   
  