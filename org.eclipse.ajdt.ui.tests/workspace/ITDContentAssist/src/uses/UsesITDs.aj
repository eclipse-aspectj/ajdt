package uses;

import hasitds.HasITDs;
import java.util.List;

class UsesITDs {
	public void tryItOut() {
		HasITDs h = new HasITDs(6);
		h.list.addAll(null);
		h.makeList(4);
		h.value = 9;
		h.inside = 9; 
	} 
}
