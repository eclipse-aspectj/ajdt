/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.contribution.xref.internal.core.policy;

import org.eclipse.core.runtime.Platform;
import org.eclipse.contribution.xref.core.XReferencePlugin;

import java.io.PrintStream;
import java.util.Set;
import java.util.HashSet;

/**
 * A tracing policy aspect
 */
public aspect TracingPolicy {
	
	private static final String XREF_CORE_TRACE =
		"org.eclipse.contribution.xref.core/debug"; //$NON-NLS-1$
	
	private final static boolean traceEnabled =
		isTraceEnabled();
	
	public static boolean isTraceEnabled() {
		String option = Platform.getDebugOption(XREF_CORE_TRACE);
		return ( (option != null) &&
				(option.equalsIgnoreCase("true")) ); //$NON-NLS-1$
	}
	
	/**
	 * Eclipse debug output just goes to sysout, but we wrap it here to give us
	 * more flexibility in the future.
	 */
	public interface ITraceListener {
		void newTraceLine(String text);
	}

	/**
	 * Eclipse debug output just goes to sysout, but we wrap it here to give us
	 * more flexibility in the future.
	 */
	public static class TraceConsole {
		
		private static PrintStream destination = System.out;
		private static Set<ITraceListener> listeners = new HashSet<ITraceListener>();
		private static StringBuffer buff = new StringBuffer();
		
		private static final String sig = XReferencePlugin.PLUGIN_ID + ": "; //$NON-NLS-1$
		public static void setDestination(PrintStream stream) {
			destination = stream;
		}
		
		public static void addListener(ITraceListener l) {
			listeners.add(l);
		}
		
		public static void removeListener(ITraceListener l) {
			listeners.remove(l);
		}
		
		public static void print(String s) { 
			destination.print(sig);
			destination.print(s);
			buff.append(s);
		}
		
		public static void println(String s) {
			destination.print(sig);
			printlnAnonymous(s);
		}
		
		public static void printlnAnonymous(String s) {
			destination.println(s);
			buff.append(s);
			for (ITraceListener listener : listeners) {
			    listener.newTraceLine(buff.toString());
			}
			buff = new StringBuffer();			
		}
	}
	
	
	pointcut coreStartup() : execution(void XReferencePlugin.startup());
	pointcut coreShutdown() : execution(void XReferencePlugin.shutdown());

	/**
	 * trace version information on startup
	 */
	after() returning : coreStartup() && if(traceEnabled) {
		TraceConsole.print("Cross Reference Core startup: v."); //$NON-NLS-1$
		TraceConsole.printlnAnonymous(XReferencePlugin.getVersion());
	}
	
	/**
	 * trace plugin shutdown
	 */
	after() returning : coreShutdown() && if(traceEnabled) {
		TraceConsole.println("Cross Reference Core shutdown."); //$NON-NLS-1$
	}
	
	
}
