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

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.launching.DefaultEntryResolver;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver;
import org.eclipse.jdt.launching.IVMInstall;

/**
 * This class wraps DefaultEntryResolver and caches its result as long as
 * enabled. Please don't forget to disable the cache again after having
 * enabled it, otherwise the cache will never get refreshed.
 * <p>
 * Potential problem: jdt allows only one ClasspathEntryResolver per entry
 * type. -> conflict if another plugin registers for the same type. (But
 * I don't know of any that do so.)
 * <p>
 * Not necessary any more as soon as jdt bug 70000 is resolved.
 */
public class CachedRuntimeClasspathEntryResolver implements IRuntimeClasspathEntryResolver {
	IRuntimeClasspathEntryResolver defaultRes;

	private static HashMap<String, IRuntimeClasspathEntry[]> cache;

	public static void setEnabled(boolean enabled) {
		cache = enabled ? new HashMap<>() : null;
	}

	public CachedRuntimeClasspathEntryResolver() {
		defaultRes = new DefaultEntryResolver();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, ILaunchConfiguration configuration)
		throws CoreException
	{
		if (cache != null) {
			String key = entry.toString();
			IRuntimeClasspathEntry[] classpathEntries = cache.get(key);
			if (classpathEntries == null) {
				classpathEntries = defaultRes.resolveRuntimeClasspathEntry(entry, configuration);
				cache.put(key, classpathEntries);
			}
			return classpathEntries;
		}
		else
			return defaultRes.resolveRuntimeClasspathEntry(entry, configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveRuntimeClasspathEntry(org.eclipse.jdt.launching.IRuntimeClasspathEntry, org.eclipse.jdt.core.IJavaProject)
	 */
	public IRuntimeClasspathEntry[] resolveRuntimeClasspathEntry(IRuntimeClasspathEntry entry, IJavaProject project)
		throws CoreException
	{
		if (cache != null) {
			String key = entry.toString();
			IRuntimeClasspathEntry[] classpathEntries = cache.get(key);
			if (classpathEntries == null) {
				classpathEntries = defaultRes.resolveRuntimeClasspathEntry(entry, project);
				cache.put(key, classpathEntries);
			}
			return classpathEntries;
		}
		else
			return defaultRes.resolveRuntimeClasspathEntry(entry, project);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.launching.IRuntimeClasspathEntryResolver#resolveVMInstall(org.eclipse.jdt.core.IClasspathEntry)
	 */
	public IVMInstall resolveVMInstall(IClasspathEntry entry) throws CoreException {
		return defaultRes.resolveVMInstall(entry);
	}

}
