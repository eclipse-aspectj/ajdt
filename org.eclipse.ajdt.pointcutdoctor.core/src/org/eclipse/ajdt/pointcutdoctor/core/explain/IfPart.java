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

public class IfPart extends AtomicPart {

	public IfPart(Pointcut pointcut) {
		super(pointcut, pointcut);
	}

	@Override
	protected String getJoinPointPartName() {
		return "If";
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		return FuzzyBoolean.MAYBE;
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return "??";
	}

}
