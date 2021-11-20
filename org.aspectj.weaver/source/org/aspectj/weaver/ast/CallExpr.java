/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


package org.aspectj.weaver.ast;

import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;

public class CallExpr extends Expr {
	// assert m.return value is boolean
	private final Member method;
	private final Expr[] args;
	private final ResolvedType returnType; // yes, stored in method as well, but that one isn't resolved

	public CallExpr(Member m, Expr[] args, ResolvedType returnType) {
		super();
		this.method = m;
		this.args = args;
		this.returnType = returnType;
	}

	public void accept(IExprVisitor v) {
		v.visit(this);
	}

	public Expr[] getArgs() {
		return args;
	}

	public Member getMethod() {
		return method;
	}

	public ResolvedType getType() {
		return returnType;
	}

}
