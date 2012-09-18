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
package org.eclipse.ajdt.pointcutdoctor.core.relaxer;

import org.aspectj.weaver.patterns.Pointcut;

public class RelaxData {
	
	@Override
	public String toString() {
		return "rplmnt="+replacement;
	}
	RelaxOp relaxOp;
	Pointcut pointcut;
	
	// reflection will be used to replace some field of an object (e.g. signaturePattern, HandlerPointcut etc.)
	Object replacement;
	private Relaxer relaxer;
	
	public RelaxData(RelaxOp op, Relaxer relaxer, Pointcut pointcut) {
		this(op, null, relaxer, pointcut);
	}
	public RelaxData(RelaxOp op, Object replacement, Relaxer relaxer, Pointcut pointcut) {
		relaxOp = op;
		this.replacement = replacement;
		this.relaxer = relaxer;
		this.pointcut = pointcut;
	}
	public Pointcut getPointcut() {
		return pointcut;
	}
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}
	public RelaxOp getRelaxOp() {
		return relaxOp;
	}
	public void setRelaxOp(RelaxOp relaxOp) {
		this.relaxOp = relaxOp;
	}
	public String getFieldName() {
		if (relaxer instanceof AnnotationRelaxer)
			return "annotationPattern";
		else if (relaxer instanceof ArgsRelaxer)
			return "arguments";
		else if (relaxer instanceof DeclaringTypeRelaxer)
			return "declaringType";
		else if (relaxer instanceof HandlerRelaxer)
			return "exceptionType";
		else if (relaxer instanceof ModifierRelaxer)
			return "modifiers";
		else if (relaxer instanceof NameRelaxer)
			return "name";
		else if (relaxer instanceof NotRelaxer)
			return "";
		else if (relaxer instanceof ParamsRelaxer)
			return "parameterTypes";
		else if (relaxer instanceof ReturnTypeRelaxer)
			return "returnType";
		else if (relaxer instanceof ThisOrTargetRelaxer)
			return "type";
		else if (relaxer instanceof ThrowRelaxer)
			return "throwsPattern";
		else if (relaxer instanceof WithincodeRelaxer)
			return "signature";
		else if (relaxer instanceof WithinRelaxer)
			return "typePattern";
		else return "";

	}
	public Object getReplacement() {
		return replacement;
	}
	public void setReplacement(Object replacement) {
		this.replacement = replacement;
	}
	
}
