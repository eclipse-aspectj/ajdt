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
package org.eclipse.ajdt.internal.ui;

import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.core.AJLogger;

/**
 * This logger simply outputs to the event trace view
 */
public class EventTraceLogger implements AJLogger {

	/* (non-Javadoc)
	 * @see org.eclipse.ajdt.internal.core.AJLogger#log(java.lang.String)
	 */
	public void log(String msg) {
		AJDTEventTrace.generalEvent(msg);
	}

}
