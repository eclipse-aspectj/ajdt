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

import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.TypePatternList;
import org.eclipse.ajdt.pointcutdoctor.core.utils.PointcutUtil;


public aspect PointcutToString {

	pointcut pointcutToStringCall(Pointcut ptc):
		call(String toString()) && target(ptc) && within(org.eclipse.ajdt.core.explain..*);
	
	String around(Pointcut ptc):pointcutToStringCall(ptc) {
		return PointcutUtil.getPointcutAsString(ptc, false);
	}
	
	pointcut typePatternListToStringCall(TypePatternList tpl):
		call(String toString()) && target(tpl) && within(org.eclipse.ajdt.core.explain..*);
	
	String around(TypePatternList tpl):typePatternListToStringCall(tpl) {
		return "("+PointcutUtil.typePatternListToString(tpl)+")";
	}

}
