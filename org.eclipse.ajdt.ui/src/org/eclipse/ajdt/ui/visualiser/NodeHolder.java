
package org.eclipse.ajdt.ui.visualiser;

import org.aspectj.asm.IProgramElement;

public class NodeHolder {

	public IProgramElement node;
	public boolean runtimeTest;
	
	public NodeHolder(IProgramElement ipe, boolean b) {
		node = ipe;
		runtimeTest = b;
	}

}
