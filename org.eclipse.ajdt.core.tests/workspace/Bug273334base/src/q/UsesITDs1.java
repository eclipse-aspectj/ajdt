package q;

import p.HasITDs1;
import r.HasITDs2;

public class UsesITDs1 {

	void n() {
		new HasITDs1().aField++; 
		new HasITDs2().aField++;
		new HasITDs1().nothing(1, 1, 1);
		new HasITDs2().nothing(1, 1, 1);
	}
	
} 