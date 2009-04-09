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
		if (logger != null) {
			logger.log(category,msg);
		} else {
		    AspectJPlugin.getDefault().getLog().log(new Status(IStatus.INFO, AspectJPlugin.PLUGIN_ID, msg));
		}
	}
	
	public static void logStart(String event) {
		Long now = new Long(System.currentTimeMillis());
		timers.put(event,now);
	}
	
	public static void logEnd(int category, String event) {
		logEnd(category, event, null);
	}
	
	public static void logEnd(int category, String event, String optional_msg) {
		Long then = (Long)timers.get(event);
		if (then != null) {
			long now = System.currentTimeMillis();
			long elapsed = now - then.longValue();
			if ((optional_msg != null) && (optional_msg.length() > 0)) {
				log(category,"Timer event: "+elapsed + "ms: "+event+" ("+optional_msg+")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} else {
				log(category,"Timer event: "+elapsed + "ms: "+event); //$NON-NLS-1$ //$NON-NLS-2$
			}
			timers.remove(event);
		}
	}
	
	public static void setLogger(IAJLogger l) {
		logger = l;
	}
}
