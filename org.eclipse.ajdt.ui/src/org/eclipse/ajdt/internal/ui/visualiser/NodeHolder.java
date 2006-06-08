/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ajdt.internal.ui.visualiser;

import org.aspectj.asm.IProgramElement;

public class NodeHolder {

	public IProgramElement node;
	public boolean runtimeTest;
	
	public String adviceType;
	
	public NodeHolder(IProgramElement ipe, boolean b, String type) {
		node = ipe;
		runtimeTest = b;
		if (type == null) {
			type = ""; //$NON-NLS-1$
		}
		adviceType = type;
	}

}
