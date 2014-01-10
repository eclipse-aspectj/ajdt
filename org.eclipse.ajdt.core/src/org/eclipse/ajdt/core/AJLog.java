/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Minimal logging - if a logger hasn't been set, dump to sdout
 */ 
public class AJLog {
  
	public static final int DEFAULT = 0; 

	public static final int COMPILER = 1;

	public static final int BUILDER = 2; 

	public static final int BUILDER_CLASSPATH = 3;	

	public static final int COMPILER_PROGRESS = 4;	

	public static final int COMPILER_MESSAGES = 5;	

	public static final int PARSER = 6;
	
    public static final int MODEL = 7;

    private static IAJLogger logger;
	
	// support for logging the start and end of activies
	private static Map timers = new HashMap();
	
	public static void log(String msg) {
		log(DEFAULT,msg);
	}
	
	public static void log(int category, String msg) {
		String formattedMessage = format(msg);
		if (logger != null) {
			logger.log(category,formattedMessage);
		} else {
		    AspectJPlugin.getDefault().getLog().log(new Status(IStatus.INFO, AspectJPlugin.PLUGIN_ID, formattedMessage));
		}
	}
	
	public static String format(String msg) {
		StringBuilder buf = new StringBuilder();
		for (int i=0;i<indent;i++) {
			buf.append("  ");
		}
		buf.append(msg);
		return buf.toString();
	}
	
	public static int indent = 0;

	public static void logStart(String event, boolean reportAndIndent) {
		logStart(DEFAULT,event,reportAndIndent);
	}
	
	public static void logStart(int category, String event, boolean reportAndIndent) {
		Long now = new Long(System.currentTimeMillis());
		timers.put(event,now);
		if (reportAndIndent) {
			log(category, "> "+event);
			indent++;
		}
	}
	
	public static void logEnd(int category, String event, boolean reportAndIndent) {
		if (reportAndIndent) {
			indent--;
		}
		logEnd(category, event, null, reportAndIndent);
	}
//	public static void logEnd(int category, String event, String optional_msg) {
//		
//	}
		
	public static void logEnd(int category, String event, String optional_msg, boolean reportAndIndent) {
		Long then = (Long)timers.get(event);
		if (then != null) {
			long now = System.currentTimeMillis();
			long elapsed = now - then.longValue();
			if ((optional_msg != null) && (optional_msg.length() > 0)) {
				log(category,(reportAndIndent?"< ":"")+event+" (took "+elapsed + "ms) ("+optional_msg+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} else {
				log(category,(reportAndIndent?"< ":"")+event+" (took "+elapsed + "ms)"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			timers.remove(event);
		}
	}
	
	public static void setLogger(IAJLogger l) {
		logger = l;
	}
}
