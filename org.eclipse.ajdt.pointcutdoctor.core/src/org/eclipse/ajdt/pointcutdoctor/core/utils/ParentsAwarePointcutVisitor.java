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

import java.util.Stack;

import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.AndPointcut2;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.FlatternedPointcut;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.NotPointcut2;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.OrPointcut2;



//TODO this visitor has side effect on And/Or/NotPointcuts since it will change the original pointcut
public abstract class ParentsAwarePointcutVisitor extends PointcutVisitor {
	private Stack<Pointcut> parents = new Stack<Pointcut>();

	public Pointcut getParent() {
		return parents.isEmpty()? null: parents.peek();
	}

	@Override
	protected Object visitFlatternedPointcut(FlatternedPointcut pointcut, Object data) {
		parents.push(pointcut);
		Object result = super.visitFlatternedPointcut(pointcut, data);
		parents.pop();
		return result;
	}

	@Override
	protected Object visitNotPointcut(NotPointcut pointcut, Object data) {
		Pointcut oldNPtc = pointcut.getNegatedPointcut();
		NotPointcut2 newPtc = new NotPointcut2(oldNPtc);
		parents.push(newPtc);
		Pointcut nnptc = (Pointcut) visit(oldNPtc, data);
		newPtc.replaceChild(oldNPtc, nnptc);
		parents.pop();
		return newPtc;
	}

	@Override
	protected Object visitOrPointcut(OrPointcut pointcut, Object data) {
		Pointcut oldLeft = pointcut.getLeft();
		Pointcut oldRight = pointcut.getRight();
		OrPointcut2 newPtc = new OrPointcut2(oldLeft,oldRight);
		parents.push(newPtc);
		Pointcut left = (Pointcut) visit(oldLeft, data);
		Pointcut right = (Pointcut) visit(oldRight, data);
		newPtc.replaceChild(oldLeft, left);
		newPtc.replaceChild(oldRight, right);
		parents.pop();
		return newPtc;
	}

	@Override
	protected Object visitAndPointcut(AndPointcut pointcut, Object data) {
		Pointcut oldLeft = pointcut.getLeft();
		Pointcut oldRight = pointcut.getRight();
		AndPointcut2 newPtc = new AndPointcut2(oldLeft,oldRight);
		parents.push(newPtc);
		Pointcut left = (Pointcut) visit(oldLeft, data);
		Pointcut right = (Pointcut) visit(oldRight, data);
		newPtc.replaceChild(oldLeft, left);
		newPtc.replaceChild(oldRight, right);
		parents.pop();
		return newPtc;
	}
}
