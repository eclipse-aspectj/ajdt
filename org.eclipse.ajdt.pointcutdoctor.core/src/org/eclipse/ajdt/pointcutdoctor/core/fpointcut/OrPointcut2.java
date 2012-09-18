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
package org.eclipse.ajdt.pointcutdoctor.core.fpointcut;

import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;

//TODO this class is a hack because left and right in AndPointcut/OrPointcut are not accesible, but we 
// want to replace it's children sometimes.
public class OrPointcut2 extends OrPointcut {

	private Pointcut left2;
	private Pointcut right2;

	public OrPointcut2(Pointcut left, Pointcut right) {
		super(left, right);
		this.left2 = left;
		this.right2 = right;
	}
	
	public void replaceChild(Pointcut oldChild, Pointcut newChild) {
		if (left2==oldChild)
			left2 = newChild;
		else if (right2==oldChild)
			right2 = newChild;
	}

	@Override
	public Pointcut getLeft() {
		return left2;
	}

	@Override
	public Pointcut getRight() {
		return right2;
	}
	
	public String toString(){
		return "(" + getLeft().toString() + " || " + getRight().toString() + ")";
	}
}
