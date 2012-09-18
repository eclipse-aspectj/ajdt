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

public class ReferencePart extends AtomicPart {

	private Pointcut concretePointcut;

	public ReferencePart(Pointcut pointcut, Pointcut concretPointcut) {
		super(pointcut, pointcut);
		this.concretePointcut = concretPointcut;
	}

	@Override
	protected String getJoinPointPartName() {
		return null;//not used since we overrided explainTextual 
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		return concretePointcut.match(shadow);
	}

	@Override
	protected String explainMisMatchTextual(Shadow shadow) {
		String ptc = pointcut.toString();
		return String.format("%s doesn't match this join point, see the definition of %s for detailed reason.", 
				ptc, ptc);
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return "??";
	}
	
	

}
