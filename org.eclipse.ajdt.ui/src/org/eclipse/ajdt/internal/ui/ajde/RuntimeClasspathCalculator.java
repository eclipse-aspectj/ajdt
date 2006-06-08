/**********************************************************************
Copyright (c) 2004 IBM Corporation and others.
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Luzius Meisser - initial version
**********************************************************************/
package org.eclipse.ajdt.internal.ui.ajde;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * This class provides a method which is similar to JavaRuntime.resolveRuntimeClasspathEntry
 * from Eclipse 3.0, but activates the CachedRuntimeClasspathEntryResolver which is several
 * orders of magnitude faster than jdts version.
 * 
 * (see jdt bug 70000)
 * 
 **/
public class RuntimeClasspathCalculator {

	/*
	 * Method similar to JavaRuntime.resolveRuntimeClasspathEntry from eclipse 3.0, but faster
	 */
	public static IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(
			IRuntimeClasspathEntry rtcp, IJavaProject jp) throws CoreException{
		CachedRuntimeClasspathEntryResolver.setEnabled(true);
		IRuntimeClasspathEntry[] res = JavaRuntime.resolveRuntimeClasspathEntry(rtcp, jp);
		CachedRuntimeClasspathEntryResolver.setEnabled(false);
		return res;
	}
}
