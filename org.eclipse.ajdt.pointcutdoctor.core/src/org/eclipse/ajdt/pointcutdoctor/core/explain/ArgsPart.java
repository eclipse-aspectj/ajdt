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
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.patterns.ArgsPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePatternList;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;
import org.eclipse.ajdt.pointcutdoctor.core.utils.StringUtils;




public class ArgsPart extends AtomicPart {

	private ArgsPointcut argsPointcut;
	
	public ArgsPart(Pointcut pointcut) {
		super(pointcut, pointcut);
		argsPointcut = (ArgsPointcut)pointcut;
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		return argsPointcut.match(shadow);//.maybeTrue(); //TODO we treat maybe as true here
	}

	public String toString() {
		ArgsPointcut aptc = (ArgsPointcut)pointcut;
		return "args.matches("+aptc.getArguments()+")"+super.toString();
	}

//	@Override
//	protected void computeOffsetLengthInSource() {
//		offset = pointcut.getStart();
//		length = pointcut.getEnd()-pointcut.getStart()+1;
//	}
//
//	  <1> - numbers don't agree [MSG] args(int) doesn't match join points with 2 arguments
//	  <2> pointcut: call(* Foo.foo(..))&&args(int)
//	      joinpoint sig: void Foo.foo(String)
//	  		- [MSG] "String" cannot be matched by pattern "int", nor a subtype of "int"
//	  <3> matched because numbers agree, AND type matches
//	      pointcut: call(* Foo.foo(..))&&args(Foo)
//	      joinpoint sig: void Foo.foo(SubFoo)
//	      - [MSG] Matched because SubFoo is a subtype of Foo, and the runtime type of the argument will always be of type Foo.      
//	  <4> matched and has runtime testing
//	      pointcut: call(* Foo.foo(..))&&args(SubFoo)
//	      joinpoint sig: void Foo.foo(Foo)
//	      - [MSG] Matched because SubFoo is a subtype of Foo.  Runtime test is needed because the runtime type of the argument could, but not necessarily, be SubFoo.

	@Override
	protected String explainMisMatchTextual(Shadow shadow) {
		TypePatternList argsPattern = ((ArgsPointcut)pointcut).getArguments();
		UnresolvedType[] args = shadow.getArgTypes();
		String s = "";
		if (!argsPattern.canMatchSignatureWithNParameters(args.length)) {
			s = formatAndUpdateExplainMessage(ExplainMessage.MSGArgs0,
					PointcutUtil.getPointcutAsString(pointcut, false), args.length);
		} else {
			s = formatAndUpdateExplainMessage(ExplainMessage.MSGArgs1,
					StringUtils.arrayToString(args), PointcutUtil.typePatternListToString(argsPattern));//TODO we should really use single type here, rather than type list
		}
		return s;
	}
	
	

	@Override
	protected String explainMatchTextual(Shadow shadow) {
//		UnresolvedType[] args = shadow.getArgTypes();
//		argsPointcut.getBindingTypePatterns();
		//TODO skip it for now...
		return super.explainMatchTextual(shadow);
	}

	@Override
	protected String getJoinPointPartName() {
		return "parameters";
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		return StringUtils.arrayToString(shadow.getArgTypes());
	}

}
