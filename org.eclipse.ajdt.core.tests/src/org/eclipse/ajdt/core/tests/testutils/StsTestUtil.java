/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.testutils;

import java.util.Map;

public class StsTestUtil {

	public static StringBuffer getStackDumps() {
		StringBuffer sb = new StringBuffer();
		Map<Thread, StackTraceElement[]> traces = Thread.getAllStackTraces();
		for (Map.Entry<Thread, StackTraceElement[]> entry : traces.entrySet()) {
			sb.append(entry.getKey().toString());
			sb.append("\n");
			for (StackTraceElement element : entry.getValue()) {
				sb.append("  ");
				sb.append(element.toString());
				sb.append("\n");
			}
			sb.append("\n");
		}
		return sb;
	}
	
}
