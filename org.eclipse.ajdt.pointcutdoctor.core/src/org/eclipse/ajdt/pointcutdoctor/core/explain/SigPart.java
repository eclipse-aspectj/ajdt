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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.JoinPointSignature;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.PatternNode;
import org.aspectj.weaver.patterns.Pointcut;

public abstract class SigPart extends AtomicPart {

	public SigPart(Pointcut pointcut, PatternNode node) {
		super(pointcut, node);
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		//TODO multiple signature not considered!
		Member jp = shadow.getMatchingSignature();
		World world = shadow.getIWorld();
		Iterator<JoinPointSignature> candidateMatches = jp.getJoinPointSignatures(world);
		while(candidateMatches.hasNext()) {
			JoinPointSignature aSig = candidateMatches.next();
			FuzzyBoolean matchResult = matchesExactly(aSig,world);
			if (matchResult.alwaysTrue() || matchResult.alwaysFalse()) {
			    return matchResult;
			}
		}
		return FuzzyBoolean.NO;
	}

	protected abstract FuzzyBoolean matchesExactly(JoinPointSignature sig, World world);

	protected String readPointcutSource() {
		File file = pointcut.getSourceLocation().getSourceFile();
		int pLen = pointcut.getEnd() - pointcut.getStart();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			char[] chars = new char[pLen];
			reader.skip(pointcut.getStart());
			reader.read(chars);
			return new String(chars);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader!=null)
				try {
					reader.close();
				} catch (IOException e) {}
		}
	}

}
