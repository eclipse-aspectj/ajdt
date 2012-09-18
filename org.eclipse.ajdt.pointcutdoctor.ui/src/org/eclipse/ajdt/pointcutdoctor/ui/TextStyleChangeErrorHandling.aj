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
package org.eclipse.ajdt.pointcutdoctor.ui;

import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

public aspect TextStyleChangeErrorHandling {
	pointcut getStyle(int start, int len):call(* StyledText.getStyleRanges(..))
		&& args(start, len);
	
	StyleRange[] around(int start,int len):getStyle(start,len) {
		try {
			return proceed(start,len);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(String.format("start=%d,len=%d",
					start,len), e);
		}
	}
	
	
	pointcut changeStyle():within(ReasonHighlighter)&&
	(call(* StyledText.setStyleRange(..))||call(* StyledText.replaceStyleRanges(..)));
	
	void around():changeStyle() {
		try {
			proceed();
		} catch (IllegalArgumentException e) {
			Object[] args = thisJoinPoint.getArgs();
			Object lastArg = args[args.length-1];
			String errMsg;
			if (lastArg instanceof StyleRange) {
				errMsg = produceErrMsg((StyleRange)lastArg);
			} else {
				StyleRange[] ranges = (StyleRange[])lastArg;
				errMsg = produceErrMsg(ranges);
			}
			throw new RuntimeException(errMsg, e);
		}
	}
	
	private String produceErrMsg(StyleRange... ranges) {
		String s = "Range(s)=";
		for (StyleRange r:ranges) {
			if (s.length()>0) s+="; ";
			s+=String.format("start=%d,len=%d", 
					r.start, r.length);
		}
		return s;
	}
}
