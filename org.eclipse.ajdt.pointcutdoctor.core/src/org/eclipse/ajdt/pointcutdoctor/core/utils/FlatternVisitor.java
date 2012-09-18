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
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;
import org.eclipse.ajdt.pointcutdoctor.core.fpointcut.FlatternedPointcut;



/**
 * @author linton
 *
 */
public class FlatternVisitor extends ParentsAwarePointcutVisitor {

	@Override
	protected Object visitAndPointcut(AndPointcut pointcut, Object data) {
		AndPointcut oldPtc = (AndPointcut) super.visitAndPointcut(pointcut, data);
		Pointcut left = oldPtc.getLeft();
		Pointcut right = oldPtc.getRight();
		if (FlatternedPointcut.flatternable(FlatternedPointcut.Kind.AND, left, right))
			return new FlatternedPointcut(FlatternedPointcut.Kind.AND, left, right);
		else
			return oldPtc;
	}

	@Override
	protected Object visitOrPointcut(OrPointcut pointcut, Object data) {
		OrPointcut oldPtc = (OrPointcut) super.visitOrPointcut(pointcut, data);
		Pointcut left = oldPtc.getLeft();
		Pointcut right = oldPtc.getRight();
		if (FlatternedPointcut.flatternable(FlatternedPointcut.Kind.OR, left, right))
			return new FlatternedPointcut(FlatternedPointcut.Kind.OR, left, right);
		else
			return oldPtc;
	}


}
