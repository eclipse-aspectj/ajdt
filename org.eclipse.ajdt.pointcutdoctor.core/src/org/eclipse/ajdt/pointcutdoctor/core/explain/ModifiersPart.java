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

import java.lang.reflect.Modifier;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.JoinPointSignature;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ModifiersPattern;
import org.aspectj.weaver.patterns.Pointcut;

public class ModifiersPart extends SigPart {

//	private ModifiersPattern modifiers;

	public ModifiersPart(Pointcut pointcut, ModifiersPattern modifiers) {
		super(pointcut, modifiers);
	}

	@Override
	protected void computeOffsetLengthInSource() {
		String ms = node.toString();
		if (ms.length()>0) {
		    length = ms.length();
			String sptc = readPointcutSource();
			offset = pointcut.getStart()+sptc.indexOf(ms);
		}
	}

	@Override
	protected FuzzyBoolean matchesExactly(JoinPointSignature sig, World world) {
		ModifiersPattern modifiers = (ModifiersPattern) node;
		return modifiers.matches(sig.getModifiers()) ? FuzzyBoolean.YES : FuzzyBoolean.NO;
	}

	@Override
	protected String getJoinPointPartName() {
		return "modifiers";
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return Modifier.toString(shadow.getSignature().getModifiers());
	}

}
