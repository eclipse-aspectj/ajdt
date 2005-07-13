/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Luzius Meisser - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.javaelements;

import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.internal.core.SourceRange;

public class DeclareElementInfo extends AspectJMemberElementInfo {

	public ISourceRange getSourceRange() {
		return new SourceRange(nameStart, nameEnd - nameStart + 1);
	}
	
}
