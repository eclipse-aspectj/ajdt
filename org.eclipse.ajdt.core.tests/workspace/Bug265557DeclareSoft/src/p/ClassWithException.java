package p;

import java.io.IOException;

public class ClassWithException {

	// no error here
	public void p() {
		thrower();
	}

	public void thrower() throws IOException {
		throw new IOException();
	}

	public void thrower2() throws IOException {
		throw new IOException();
	}
}