package other;

import java.util.List;
import java.util.HashMap;

public class Gen <T extends Number & List<?>, 
        F extends HashMap<String, String> & Comparable<?>> {
 
    void doNothing(T bar, F baz) {
        bar.floatValue();
        bar.clear();
        baz.clear();
        baz.compareTo(null);
    }
}
