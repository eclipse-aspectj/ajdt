/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal logging - if a logger hasn't been set, dump to sdout
 */
public class AJLog {

	private static IAJLogger logger;
	
	// support for logging the start and end of activies
	private static Map timers = new HashMap();
	
	public static void log(String msg) {
		if (logger != null) {
			logger.log(msg);
		} else {
			System.out.println(msg);
		}
	}
	
	public static void logStart(String event) {
		Long now = new Long(System.currentTimeMillis());
		timers.put(event,now);
	}
	
	public static void logEnd(String event) {
		logEnd(event, null);
	}
	
	public static void logEnd(String event, String optional_msg) {
		Long then = (Long)timers.get(event);
		if (then != null) {
			long now = System.currentTimeMillis();
			long elapsed = now - then.longValue();
			if ((optional_msg != null) && (optional_msg.length() > 0)) {
				log("Timer event: "+elapsed + "ms: "+event+" ("+optional_msg+")");
			} else {
				log("Timer event: "+elapsed + "ms: "+event);
			}
			timers.remove(event);
		}
	}
	
	public static void setLogger(IAJLogger l) {
		logger = l;
	}
}
