package p;

import java.io.IOException;

public aspect Soft {
	declare soft : IOException : call(public void ClassWithException.thrower());
}
