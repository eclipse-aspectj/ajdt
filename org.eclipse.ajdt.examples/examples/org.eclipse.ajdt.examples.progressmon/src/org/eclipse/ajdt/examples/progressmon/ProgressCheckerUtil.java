/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.examples.progressmon;

public class ProgressCheckerUtil {
		
	private static IProgressCheckerReporter reporter;
	
	public static void setReporter(IProgressCheckerReporter report) {
		reporter = report;
	}
	
	public static IProgressCheckerReporter getReporter() {
		return reporter;
	}
	
	public static void report(RuleViolations code, String msg, String loc1, String loc2) {
		if (getReporter() != null) {
			getReporter().reportRuleViolation(code, msg, loc1, loc2);
		} else {
			System.err.println(msg);
			System.err.println(loc1);
			System.err.println(loc2);
		}
	}
}
