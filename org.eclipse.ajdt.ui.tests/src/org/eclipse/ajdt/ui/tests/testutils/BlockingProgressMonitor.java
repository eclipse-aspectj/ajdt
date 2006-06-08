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
package org.eclipse.ajdt.ui.tests.testutils;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Helper class which creates a blocking progress monitor for use
 * in the tests to wait for an action to finish.
 * 
 * @author hawkinsh
 *
 */
public class BlockingProgressMonitor implements IProgressMonitor {

	private Boolean isDone = Boolean.FALSE;

	public boolean isDone() {
		boolean ret = false;
		synchronized (isDone) {
			ret = (isDone == Boolean.TRUE);
		}
		return ret;
	}

	public void reset() {
		synchronized (isDone) {
			isDone = Boolean.FALSE;
		}
	}

	public void waitForCompletion() {
		while (!isDone()) {
			try {
				synchronized (this) {
					wait(500);
				}
			} catch (InterruptedException intEx) {
				// no-op
			}
		}
	}

	public void beginTask(String name, int totalWork) {
		if (name != null)
			System.out.println(name);
		reset();
	}

	public void done() {
		synchronized (isDone) {
			isDone = Boolean.TRUE;
		}
		synchronized (this) {
			notify();
		}
	}

	public void internalWorked(double work) {
	}

	public boolean isCanceled() {
		return false;
	}

	public void setCanceled(boolean value) {
	}

	public void setTaskName(String name) {
	}

	public void subTask(String name) {
	}

	public void worked(int work) {
	}
}

