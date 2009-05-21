package pushin2;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import pushin.OtherClass;

public aspect A {
	int B.x = 9;
	int B.y = 9;

	OtherClass.new(List<Comparable<Comparator<String>>> h) {
	    this();
		Set<String> y = null;
		System.out.print(y);
	}
	
}
 