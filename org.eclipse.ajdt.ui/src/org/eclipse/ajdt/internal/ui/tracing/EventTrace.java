/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
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

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class EventTrace {
	
	public static interface EventListener {
		public void ajdtEvent(String msg, int category, Date time);
	};

	private static ArrayList listeners = new ArrayList();

	public static void postEvent(String msg, int category) {
		Date time = new Date();
		if (!listeners.isEmpty()) {
			for (Iterator it = listeners.iterator(); it.hasNext();) {
				((EventListener) it.next()).ajdtEvent(msg, category, time);
			}
		}
	}

	public static void addListener(EventListener l) {
		listeners.add(l);
	}

	public static void removeListener(EventListener l) {
		listeners.remove(l);
	}
}
