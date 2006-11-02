package library;

import thirdparty.ThirdPartyLibrary;

/**
 * User library class that references a third party library
 */
public class UserLibraryClass { 

	public void doStuff() {
		new ThirdPartyLibrary().thirdPartyOperation();
	}
}
