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

import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.Pointcut;

//TODO similar to AndPointcut2/OrPointcut2
public class NotPointcut2 extends NotPointcut {

	private Pointcut negated2;

	public NotPointcut2(Pointcut other) {
		super(other);
		negated2 = other;
	}
	
	public void replaceChild(Pointcut oldChild, Pointcut newChild) {
		if (negated2==oldChild)
			negated2 = newChild;
	}

	@Override
	public Pointcut getNegatedPointcut() {
		return negated2;
	}

	public String toString() {
		return "!" + getNegatedPointcut().toString();

	}
}
