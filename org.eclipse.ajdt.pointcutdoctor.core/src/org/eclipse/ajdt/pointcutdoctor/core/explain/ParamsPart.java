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
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.patterns.ExactTypePattern;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.TypePatternList;
import org.eclipse.ajdt.pointcutdoctor.core.utils.StringUtils;
import org.eclipse.ajdt.pointcutdoctor.core.utils.TypeUtil;



public class ParamsPart extends SigPart {

	TypePatternList parameterTypes;
	
	public ParamsPart(KindedPointcut pointcut, TypePatternList pms) {
		super(pointcut, pms);
		parameterTypes = pms;
	}

	@Override
	protected FuzzyBoolean matchesExactly(JoinPointSignature aMethod, World world) {
		if (!parameterTypes.canMatchSignatureWithNParameters(aMethod.getParameterTypes().length)) return FuzzyBoolean.NO;
		ResolvedType[] resolvedParameters = world.resolve(aMethod.getParameterTypes());
//		return parameterTypes.matches(resolvedParameters, TypePattern.STATIC);		
		
		if (!parameterTypes.matches(resolvedParameters, TypePattern.STATIC).alwaysTrue()) {
			// It could still be a match based on the generic sig parameter types of a parameterized type
			if (!parameterTypes.matches(world.resolve(aMethod.getGenericParameterTypes()),TypePattern.STATIC).alwaysTrue()) {
				return FuzzyBoolean.MAYBE;
				// It could STILL be a match based on the erasure of the parameter types??
				// to be determined via test cases...
			}
		}
		return FuzzyBoolean.YES;
	}

	public String toString() {
		return "params.matches("+parameterTypes+")"+super.toString();
	}

	@Override
	protected void computeOffsetLengthInSource() {
		String ptcSource = readPointcutSource();
		if (ptcSource!=null) {
			// [^()]*([^()]*([^()]*)[^()]*)[^()]*
			String nb = "[^\\(\\)]*";
			Pattern p = Pattern.compile(String.format(
					"%s\\(%s(\\(%s\\))%s\\)%s", nb, nb, nb,nb,nb));
			Matcher m = p.matcher(ptcSource);
			if (m.find()) {
				String matched = m.group(1);
				offset = pointcut.getStart() + ptcSource.indexOf(matched);
				length = matched.length();
			} 
		} 
	}

	@Override
	protected String getJoinPointPartName() {
		return "parameters";
	}

	@Override
	protected String explainMisMatchTextual(Shadow shadow) {
		TypePatternList argsPattern = ((KindedPointcut)pointcut).getSignature().getParameterTypes();
		UnresolvedType[] args = shadow.getArgTypes();
		String s = "";
		if (!argsPattern.canMatchSignatureWithNParameters(args.length)) {
			s = formatAndUpdateExplainMessage(ExplainMessage.MSGArgs0,
					argsPattern.toString(), args.length);
			return s;
		} else {
			World world = shadow.getIWorld();
			int[] indices = findFirstMismatchedIndices(argsPattern, shadow.getArgTypes(), world, false);
			TypePattern mismPattern = argsPattern.get(indices[0]);
			ResolvedType mismType = shadow.getArgType(indices[1]).resolve(world);
			if (mismPattern instanceof ExactTypePattern) {
				ResolvedType paramPattern = ((ExactTypePattern)mismPattern).getType().resolve(world);
				if (TypeUtil.isSubTypeOf(paramPattern, mismType))
					return formatAndUpdateExplainMessage(ExplainMessage.MSGParam0,
							mismType.getClassName(), paramPattern.getClassName());
			}
			return super.explainMisMatchTextual(shadow);
		}
	}

	private int[] findFirstMismatchedIndices(TypePatternList typePatterns, UnresolvedType[] types, 
			World world, boolean dynamic) {
		int[] result = new int[2]; //the first one is the pattern, the second one is the argType
		
    	int nameLength = types.length;
		int patternLength = typePatterns.getTypePatterns().length;
		
		int nameIndex = 0;
		int patternIndex = 0;
		
		int ellipsisCount=0;
		for(TypePattern tp:typePatterns.getTypePatterns()) if (tp==TypePattern.ELLIPSIS) ellipsisCount++;
		TypePattern.MatchKind kind = dynamic? TypePattern.DYNAMIC : TypePattern.STATIC;
		
		if (ellipsisCount == 0) {
			while (patternIndex < patternLength) {
				FuzzyBoolean ret = typePatterns.get(patternIndex++).matches(types[nameIndex++].resolve(world), kind);
				if (ret == FuzzyBoolean.NO) {
					break;
				}
			}
		} else if (ellipsisCount == 1) {
			while (patternIndex < patternLength) {
				TypePattern p = typePatterns.get(patternIndex++);
				if (p == TypePattern.ELLIPSIS) {
					nameIndex = nameLength - (patternLength-patternIndex);
				} else {
					FuzzyBoolean ret = p.matches(types[nameIndex++].resolve(world), kind);
				    if (ret == FuzzyBoolean.NO) break;
				}
			}
		} 
		result[0]=patternIndex-1;
		result[1]=nameIndex-1;
		return result;
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return StringUtils.arrayToString(shadow.getArgTypes());
	}
	
	
}
