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
package org.eclipse.ajdt.pointcutdoctor.core.utils;

import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.ArgsPointcut;
import org.aspectj.weaver.patterns.CflowPointcut;
import org.aspectj.weaver.patterns.HandlerPointcut;
import org.aspectj.weaver.patterns.IfPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.ReferencePointcut;
import org.aspectj.weaver.patterns.ThisOrTargetPointcut;
import org.aspectj.weaver.patterns.WithinPointcut;
import org.aspectj.weaver.patterns.WithincodePointcut;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.FlatternedPointcut;



public class PointcutVisitor {

	public PointcutVisitor() {
		super();
	}

	public Object visit(Pointcut ptc, Object data) {
		Object result = ptc;
		if (ptc instanceof AndPointcut) {
			result = visitAndPointcut((AndPointcut)ptc, data);
		} else if (ptc instanceof OrPointcut) {
			result = visitOrPointcut((OrPointcut)ptc, data);
		} else if (ptc instanceof NotPointcut) {
			result = visitNotPointcut((NotPointcut)ptc, data);
		}else if (ptc instanceof KindedPointcut) {
			result = visitKindedPointcut((KindedPointcut)ptc, data);
		} else if (ptc instanceof WithinPointcut) {
			result = visitWitinPointcut((WithinPointcut)ptc, data);
		} else if (ptc instanceof FlatternedPointcut) {
			result = visitFlatternedPointcut((FlatternedPointcut)ptc, data);
		} else if (ptc instanceof ReferencePointcut) {
			result = visitReferencedPointcut((ReferencePointcut)ptc, data);			
		} else if (ptc instanceof ArgsPointcut) {
			result = visitArgsPointcut((ArgsPointcut)ptc, data);
		} else if (ptc instanceof ThisOrTargetPointcut) {
			result = visitThisOrTargetPointcut((ThisOrTargetPointcut)ptc, data);
		} else if (ptc instanceof WithincodePointcut) {
			result = visitWithincodePointcut((WithincodePointcut)ptc, data);
		} else if (ptc instanceof HandlerPointcut) {
			result = visitHandlerPointcut((HandlerPointcut)ptc, data);
		} else if (ptc instanceof CflowPointcut) {
			result = visitCflowPointcut((CflowPointcut)ptc, data);
		} else if (ptc instanceof IfPointcut) {
			result = visitIfPointcut((IfPointcut)ptc, data);
		} else {
			result = visitOtherPointcut(ptc, data);
		}
		return result;
	}

	protected Object visitIfPointcut(IfPointcut pointcut, Object data) {
		return pointcut;
	}

	protected Object visitCflowPointcut(CflowPointcut pointcut, Object data) {
		return pointcut;
	}

	protected Object visitHandlerPointcut(HandlerPointcut pointcut, Object data) {
		return pointcut;
	}

	protected Object visitReferencedPointcut(ReferencePointcut pointcut, Object data) {
		return pointcut;
	}

	protected Object visitOtherPointcut(Pointcut ptc, Object data) {
		return ptc;
	}

	protected Object visitWitinPointcut(WithinPointcut pointcut, Object data) {
		return pointcut;
	}

	protected Object visitKindedPointcut(KindedPointcut pointcut, Object data) {
		return pointcut;
	}
	
	protected Object visitArgsPointcut(ArgsPointcut pointcut, Object data) {
		return pointcut;
	}
	
	protected Object visitThisOrTargetPointcut(ThisOrTargetPointcut pointcut, Object data) {
		return pointcut;
	}
	protected Object visitWithincodePointcut(WithincodePointcut pointcut, Object data) {
		return pointcut;
	}
	
	protected Object visitFlatternedPointcut(FlatternedPointcut pointcut, Object data) {
		for (Pointcut p:pointcut.getChildren())
			visit(p,data);
		return pointcut;
	}
	protected Object visitNotPointcut(NotPointcut pointcut, Object data) {
		return visit(pointcut.getNegatedPointcut(),data);
	}
	protected Object visitOrPointcut(OrPointcut pointcut, Object data) {
		visit(pointcut.getLeft(),data);
		visit(pointcut.getRight(),data);
		return pointcut;
	}
	protected Object visitAndPointcut(AndPointcut pointcut, Object data) {
		visit(pointcut.getLeft(),data);
		visit(pointcut.getRight(),data);
		return pointcut;
	}


}
