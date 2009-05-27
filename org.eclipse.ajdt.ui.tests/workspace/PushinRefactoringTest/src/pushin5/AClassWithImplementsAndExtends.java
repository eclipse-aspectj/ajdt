package pushin5;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AClassWithImplementsAndExtends extends HashMap<String, List<String>> implements Iterable<Object> {

	private static final long serialVersionUID = 1L;

	public Iterator<Object> iterator() {
		return null;
	}

}
