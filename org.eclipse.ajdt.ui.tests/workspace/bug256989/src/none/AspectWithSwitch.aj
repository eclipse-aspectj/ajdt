/**
 * 
 */
package none;
 
/**
 * @author Dawid Pytel
 * 
 */
public aspect AspectWithSwitch {
	public void foo() {
		int i = 1;
		switch (i) {
		case 1:
			break;
		}
	}
}
