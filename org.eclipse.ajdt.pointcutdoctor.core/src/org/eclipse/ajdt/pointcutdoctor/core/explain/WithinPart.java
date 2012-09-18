/*******************************************************************************
 * Copyright (c) 2007 Linton Ye.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Linton Ye - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.pointcutdoctor.core.explain;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.ajdt.pointcutdoctor.core.virtual.VirtualShadow;


public class WithinPart extends AtomicPart {

	public WithinPart(Pointcut pointcut) {
		super(pointcut, pointcut);
	}

	@Override
	protected String getJoinPointPartName() {
		return "enclosing type";
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		if (shadow instanceof VirtualShadow) return FuzzyBoolean.YES;
		else
			return pointcut.match(shadow);
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		if (shadow instanceof VirtualShadow)
			return "(virtual)";
		else
			return shadow.getEnclosingType().getClassName();
	}

}
