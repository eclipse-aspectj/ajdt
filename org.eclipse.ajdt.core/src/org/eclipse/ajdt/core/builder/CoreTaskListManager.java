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

import org.aspectj.ajde.TaskListManager;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.IMessage.Kind;
import org.eclipse.ajdt.core.AJLog;

public class CoreTaskListManager implements TaskListManager {

	public void addSourcelineTask(String message,
			ISourceLocation sourceLocation, Kind kind) {
		AJLog.log(AJLog.COMPILER,"AJC: "+message); //$NON-NLS-1$
	}

	public void addSourcelineTask(IMessage message) {
		AJLog.log(AJLog.COMPILER,"AJC: "+message); //$NON-NLS-1$
	}

	public boolean hasWarning() {
		return false;
	}

	public void addProjectTask(String message, Kind kind) {
		AJLog.log(AJLog.COMPILER,"project task: "+message); //$NON-NLS-1$
	}

	public void clearTasks() {
	}

}
