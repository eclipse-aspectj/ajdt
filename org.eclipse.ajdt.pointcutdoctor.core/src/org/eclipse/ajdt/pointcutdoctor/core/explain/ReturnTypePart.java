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
import org.aspectj.weaver.JoinPointSignature;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.TypePattern;

public class ReturnTypePart extends SigPart {

	TypePattern returnType;
	
	public ReturnTypePart(KindedPointcut pointcut, TypePattern returnType2) {
		super(pointcut, returnType2);
		returnType = returnType2;
	}

	@Override
	protected void computeOffsetLengthInSource() {
		// dont know why, but when the return type is *, its offset will become very large number
		if (returnType.isStar()) {
			String ptcSrc = readPointcutSource();
			offset = pointcut.getStart()+ptcSrc.indexOf("*");
			length = 1;
		} else
			super.computeOffsetLengthInSource();
	}

	@Override
	protected FuzzyBoolean matchesExactly(JoinPointSignature aMethod, World world) {
		if (!returnType.matchesStatically(aMethod.getReturnType().resolve(world))) {
			// looking bad, but there might be parameterization to consider...
			if (!returnType.matchesStatically(aMethod.getGenericReturnType().resolve(world))) {
				// ok, it's bad.
				return FuzzyBoolean.MAYBE;
			} else return FuzzyBoolean.YES; 
		} else return FuzzyBoolean.YES;
	}

	public String toString() {
		return "rt.matches("+returnType+")"+super.toString();
	}
	
	@Override
	protected String getJoinPointPartName() {
		//TODO how about field joinpoints?
		return "return type";
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return shadow.getSignature().getReturnType().getClassName();
	}
}
