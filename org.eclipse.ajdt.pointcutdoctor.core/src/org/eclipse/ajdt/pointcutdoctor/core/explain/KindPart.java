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
import org.aspectj.weaver.Shadow.Kind;
import org.aspectj.weaver.patterns.KindedPointcut;

public class KindPart extends AtomicPart {

	private Kind kind; //TODO constructor initiation

	public KindPart(KindedPointcut pointcut, Kind kind2) {
		super(pointcut, null);
		kind = kind2;
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		return kind==shadow.getKind()?FuzzyBoolean.YES: FuzzyBoolean.NO;
	}

	public String toString() {
		return "kind="+kind+super.toString();
	}

	@Override
	protected void computeOffsetLengthInSource() {
		offset = pointcut.getStart();
		length = kind.getSimpleName().length();
	}

	@Override
	protected String getJoinPointPartName() {
		return "kind";
	}

	@Override
	protected String patternAsString() {
		return kind.toString();
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return shadow.getKind().getName();
	}
}
