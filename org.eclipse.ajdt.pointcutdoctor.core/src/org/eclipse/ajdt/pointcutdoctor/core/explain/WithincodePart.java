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
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.WithincodePointcut;
import org.eclipse.ajdt.pointcutdoctor.core.utils.StringUtils;
import org.eclipse.ajdt.pointcutdoctor.core.utils.TypeUtil;
import org.eclipse.ajdt.pointcutdoctor.core.virtual.VirtualShadow;



public class WithincodePart extends AtomicPart {

	@Override
	protected String explainMatchTextual(Shadow shadow) {
		if (!(shadow instanceof VirtualShadow)) {
			World world = shadow.getIWorld();
			Member method = shadow.getEnclosingCodeSignature(); 
			ResolvedType dt = method.getDeclaringType().resolve(world);

			WithincodePointcut wptc = (WithincodePointcut)pointcut;
			TypePattern dtp = wptc.getSignature().getDeclaringType();

			if (dtp instanceof ExactTypePattern) {
				ResolvedType rdtp = ((ExactTypePattern)dtp).getType().resolve(world);
				if (TypeUtil.isSubTypeOf(dt, rdtp) && 
						TypeUtil.isApplicableToType(method, dt)	&& TypeUtil.isApplicableToType(method, rdtp)) {
					String methodDtp = String.format("%s.%s%s", rdtp.getClassName(), method.getName(), 
							StringUtils.arrayToString(method.getParameterTypes()));
					return formatAndUpdateExplainMessage(ExplainMessage.M_MSGWithincode0,method.toString(), methodDtp);
				}
			}
		}
		return super.explainMatchTextual(shadow);
	}

	public WithincodePart(Pointcut pointcut) {
		super(pointcut, pointcut);
	}

	@Override
	protected String getJoinPointPartName() {
		return "enclosing method";
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
			return shadow.getEnclosingCodeSignature().toString(); //TODO??
	}
}
