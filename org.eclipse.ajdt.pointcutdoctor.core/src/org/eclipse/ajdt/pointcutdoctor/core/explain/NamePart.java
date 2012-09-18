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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.JoinPointSignature;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NamePattern;

public class NamePart extends SigPart {

	NamePattern name;
	
	public NamePart(KindedPointcut pointcut, NamePattern nm) {
		super(pointcut, nm);
		name = nm;
	}

	@Override
	protected FuzzyBoolean matchesExactly(JoinPointSignature aMethodOrField, World world) {
		if(name.matches(aMethodOrField.getName()))
			return FuzzyBoolean.YES;
		else return FuzzyBoolean.NO;
	}

	public String toString() {
		return "nm.matches("+name+")"+super.toString();
	}

	private int findNameOffsetInSource() {
		String ptcSource = readPointcutSource();
		if (ptcSource!=null) {
			// [\s*|.]name[\s*|(|$]
			String n = name.toString().replaceAll("\\*", "\\\\*");
//			Pattern p = Pattern.compile("execution\\(void\\sFigure\\.set");
			Pattern p = Pattern.compile("([\\s*|\\.]"+n+"[\\s*|\\(|$])");
			Matcher m = p.matcher(ptcSource);
			if (m.find()) {
				String matched = m.group();
				return m.start()+matched.indexOf(name.toString());
			} else return -1;
		} else return -1;
	}

	@Override
	protected void computeOffsetLengthInSource() {
		offset = pointcut.getStart() + findNameOffsetInSource();
		length = name.toString().length();
	}

	@Override
	protected String getJoinPointPartName() {
		return "name";
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return shadow.getSignature().getName();
	}
}
