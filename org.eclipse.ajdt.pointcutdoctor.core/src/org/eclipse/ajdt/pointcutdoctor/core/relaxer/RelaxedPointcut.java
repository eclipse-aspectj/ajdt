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


/**
 * A wrapper class for pointcuts and applied relaxers
 * @author Linton
 * 
 */
public class RelaxedPointcut {
	private Pointcut pointcut;
	public RelaxedPointcut(Pointcut newPtc) {
		pointcut  = newPtc;
	}
	public Pointcut getPointcut() {
		return pointcut;
	}
	public void setPointcut(Pointcut pointcut) {
		this.pointcut = pointcut;
	}
	public String toString() {
		return pointcut.toString();
	}
}
