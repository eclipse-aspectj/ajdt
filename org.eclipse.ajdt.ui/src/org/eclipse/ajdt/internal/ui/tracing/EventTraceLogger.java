/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.tracing;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.ajdt.core.IAJLogger;

/**
 * This logger simply outputs to the event trace view
 */
public class EventTraceLogger implements IAJLogger {

	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.internal.core.AJLogger#log(java.lang.String)
	 */
	public void log(String msg) {
		if (DebugTracing.DEBUG) {
			EventTrace.postEvent(msg, AJLog.DEFAULT);
		}
	}

	public void log(int category, String msg) {
		if (DebugTracing.DEBUG) {
			boolean doit = true;
			if (category==AJLog.COMPILER) {
				doit = DebugTracing.DEBUG_COMPILER;
			} else if (category==AJLog.BUILDER) {
				doit = DebugTracing.DEBUG_BUILDER;
			} else if (category==AJLog.BUILDER_CLASSPATH) {
				doit = DebugTracing.DEBUG_BUILDER_CLASSPATH;
			} else if (category==AJLog.COMPILER_PROGRESS) {
				doit = DebugTracing.DEBUG_COMPILER_PROGRESS;
			} else if (category==AJLog.COMPILER_MESSAGES) {
				doit = DebugTracing.DEBUG_COMPILER_MESSAGES;
            } else if (category==AJLog.PARSER) {
                doit = DebugTracing.DEBUG_PARSER;
            } else if (category==AJLog.MODEL) {
                doit = DebugTracing.DEBUG_MODEL;
			}
			if (doit) {
				EventTrace.postEvent(msg, category);
			}
		}
	}

}
