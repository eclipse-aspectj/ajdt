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
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.HandlerPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;
import org.eclipse.ajdt.pointcutdoctor.core.utils.TypeUtil;



public class HandlerPart extends AtomicPart {

	public HandlerPart(Pointcut pointcut) {
		super(pointcut, pointcut);
	}

	@Override
	protected String getJoinPointPartName() {
		return "exception type";
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		return pointcut.match(shadow);
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return shadow.getArgType(0).getClassName();
	}

	@Override
	protected String explainMisMatchTextual(Shadow shadow) {
		HandlerPointcut hptc = (HandlerPointcut)pointcut;
		TypePattern exTp = PointcutUtil.getExceptionType(hptc);
		World world = shadow.getIWorld();
		if(exTp instanceof ExactTypePattern) {
			ResolvedType etp = ((ExactTypePattern)exTp).getType().resolve(world);
			ResolvedType et = shadow.getArgType(0).resolve(world);
			if (TypeUtil.isSubTypeOf(et, etp))
				return this.formatAndUpdateExplainMessage(ExplainMessage.MSGHandler0, 
						etp.getClassName());
		}
		return super.explainMisMatchTextual(shadow);
	}

}
