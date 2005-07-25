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
package org.eclipse.ajdt.core.builder;

import org.aspectj.ajde.ErrorHandler;
import org.eclipse.ajdt.core.AJLog;

public class CoreErrorHandler implements ErrorHandler {

	public void handleWarning(String message) {
		AJLog.log("AJC: Compiler warning: "+message);
	}

	public void handleError(String message) {
		AJLog.log("AJC: Compiler error: "+message);
	}

	public void handleError(String message, Throwable t) {
		AJLog.log("AJC: Compiler error: "+message);
		t.printStackTrace();
	}

}
