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

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.JoinPointSignature;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ThrowsPattern;
import org.aspectj.weaver.patterns.TypePattern;

public class ThrowsPart extends SigPart {

	private ThrowsPattern throwsPattern;

	public ThrowsPart(Pointcut pointcut, ThrowsPattern pattern) {
		super(pointcut, pattern);
		this.throwsPattern = pattern;
	}

	@Override
	protected FuzzyBoolean matchesExactly(JoinPointSignature sig, World world) {
		return throwsPattern.matches(sig.getExceptions(), world) ? FuzzyBoolean.YES
				:FuzzyBoolean.NO;
	}

	@Override
	protected String getJoinPointPartName() {
		return "throws";
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return Arrays.toString(shadow.getSignature().getExceptions(shadow.getIWorld()));
	}

	@Override
	protected String explainMisMatchTextual(Shadow shadow) {
		TypePattern[] excepionTypes = throwsPattern.getRequired().getTypePatterns();
		for (TypePattern tp:excepionTypes) {
			ResolvedType et = tp.getExactType().resolve(shadow.getIWorld());
			if (isOrSubClassOfUncheckedException(et))
				return formatAndUpdateExplainMessage(ExplainMessage.MSGThrow0, et.getClassName());
		}
		return super.explainMisMatchTextual(shadow);
	}

	private boolean isOrSubClassOfUncheckedException(ResolvedType et) {
		String[] unchecked = {RuntimeException.class.getName(), Error.class.getName()};
		ResolvedType superclass = et.getSuperclass();
		while(superclass!=null) {
			for (String un:unchecked)
				if (superclass.getName().equals(un))
					return true;
			superclass = superclass.getSuperclass();
		}
		return false;
	}

	@Override
	protected void computeOffsetLengthInSource() {
		// throwsPattern.getStart()==throwsPattern.getEnd()==-1, so we have to read it from the source code :(
		String ptcSource = readPointcutSource();
		if (ptcSource!=null) {
			// \(.*\s*(throws[^\)]*)\)
			Pattern p = Pattern.compile("\\(.*\\s*(throws[^\\)]*)\\)");
			Matcher m = p.matcher(ptcSource);
			if (m.find()) {
				String matched = m.group(1);
				offset = pointcut.getStart() + ptcSource.indexOf(matched);
				length = matched.length();
			} 
		} 
	}

	
}
