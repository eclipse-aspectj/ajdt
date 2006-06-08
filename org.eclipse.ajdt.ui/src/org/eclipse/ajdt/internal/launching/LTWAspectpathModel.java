/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.internal.launching;

import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathModel;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;

public class LTWAspectpathModel extends ClasspathModel {

	
	public IRuntimeClasspathEntry[] getAllEntries() {
		IClasspathEntry[] user = getEntries(USER);
		IRuntimeClasspathEntry[] all = new IRuntimeClasspathEntry[user.length];
		if (user.length > 0) {
			System.arraycopy(user, 0, all, 0, user.length);
		}
		return all;
	}
	
	public IClasspathEntry getBootstrapEntry() {
		return null;
	}
}
