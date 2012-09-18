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
import org.aspectj.weaver.Member;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.Shadow.Kind;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
import org.eclipse.ajdt.pointcutdoctor.core.virtual.VirtualShadow;



public class ThisOrTargetPart extends AtomicPart {

	private boolean isThis;

	public ThisOrTargetPart(Pointcut pointcut) {
		super(pointcut, pointcut);
		this.isThis = ((ThisOrTargetPointcut)pointcut).isThis();
	}

	@Override
	protected FuzzyBoolean isMatched(Shadow shadow) {
		ThisOrTargetPointcut tptc = (ThisOrTargetPointcut)pointcut;
//		return tptc.match(shadow).maybeTrue(); //TODO we treat maybe as true here
		return tptc.match(shadow);
	}

	public String toString() {
		ThisOrTargetPointcut tptc = (ThisOrTargetPointcut)pointcut;
		return tptc.isThis()?"this":"target"+".matches("+tptc.getType()+")"+super.toString();
	}

	@Override
	protected String getJoinPointPartName() {
		//TODO better description for "this context"?
		return isThis? "\"this\"":"\"target\"";
	}

	@Override
	protected String explainMisMatchTextual(Shadow shadow) {
		Kind kind = shadow.getKind();
		// the following will return a more specific explanation for this shadow
		//TODO maybe there are better ways to express this ugly if...then..?
		if (!isThis) {
			if (kind==Shadow.ConstructorCall)
				return formatAndUpdateExplainMessage(ExplainMessage.MSGTarget0, "Constructor calls");
			if (kind==Shadow.ConstructorExecution)
				return formatAndUpdateExplainMessage(ExplainMessage.MSGTarget0, "Constructor executions");
			
			String type = null;
			if (kind==Shadow.MethodCall && isStaticShadow(shadow))
				type = "Calls to static methods";
			else if (kind==Shadow.FieldGet && isStaticShadow(shadow))
				type = "Static field-gets";
			else if (kind==Shadow.FieldSet && isStaticShadow(shadow))
				type = "Static field-sets";
			else if (kind==Shadow.Initialization)
				return formatAndUpdateExplainMessage(ExplainMessage.MSGTarget0, "Object initializations");
			else if (kind==Shadow.PreInitialization)
				type = "Object pre-initializations";
			else if (kind==Shadow.StaticInitialization)
				type = "Class initializations";
			if (type!=null)
				return formatAndUpdateExplainMessage(ExplainMessage.MSGTarget1,type);
		} else {
			if ((kind==Shadow.MethodExecution && isStaticShadow(shadow)) ||
					kind==Shadow.ConstructorExecution || kind == Shadow.StaticInitialization)
				return formatAndUpdateExplainMessage(ExplainMessage.MSGThis0);
		}
		return super.explainMisMatchTextual(shadow);
	}

	private boolean isStaticShadow(Shadow shadow) {
		return Modifier.isStatic(shadow.getResolvedSignature().getModifiers());
	}

	@Override
	protected String getJoinPointPartValue(Shadow shadow) {
		if (isThis) {
			if (inStaticContext(shadow))
				return "no \"this\"";
			else
				return shadow.getThisType().getClassName()+" or its subtype";
		}
		else {
			if (isStaticShadow(shadow))
				return "no \"target\"";
			else
				return shadow.getTargetType().getClassName()+" or its subtype";
		}
	}

	private boolean inStaticContext(Shadow shadow) {
		if (shadow instanceof VirtualShadow) return true;
			
		Member enclosingCodeSignature = shadow.getEnclosingCodeSignature();
		if (enclosingCodeSignature==null) return true;
		else
			return Modifier.isStatic(enclosingCodeSignature.getModifiers());
	}
	
}
