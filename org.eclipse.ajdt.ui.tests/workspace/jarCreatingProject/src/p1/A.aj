/*
 * Created on Jul 29, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package p1;


/**
 * @author User
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract aspect A {
	public abstract pointcut myPC();
	
	declare warning: myPC(): "warning";
	
	after(): myPC(){
		int x = 0;
	}
}