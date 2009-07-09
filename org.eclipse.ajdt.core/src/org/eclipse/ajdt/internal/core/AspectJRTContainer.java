/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.text.CoreMessages;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.framework.Bundle;

public class AspectJRTContainer implements IClasspathContainer {

	private IClasspathEntry[] fClasspathEntries;

	private static String[] aspectjrtPath = null;

	private static String[] aspectjrtSourcePath = null;

	public IClasspathEntry[] getClasspathEntries() {
		if (fClasspathEntries == null) {
			String[] path = getAspectjrtClasspath();
			String[] sourcePath = getAspectjrtSourcePath();
			fClasspathEntries = new IClasspathEntry[path.length];
			for (int i = 0; i < path.length; i++) {
				IPath p = new Path(path[i]);
				IPath sp = null;
				if ((sourcePath != null) && (i < sourcePath.length)) {
					sp = new Path(sourcePath[i]);
				}
				fClasspathEntries[i] = JavaCore.newLibraryEntry(p, sp, null,
						false);
			}
		}
		return fClasspathEntries;
	}

	public String getDescription() {
		return CoreMessages.ajRuntimeContainerName;
	}

	public int getKind() {
		return IClasspathContainer.K_APPLICATION;
	}

	public IPath getPath() {
		return new Path(AspectJPlugin.ASPECTJRT_CONTAINER);
	}

	/**
	 * Get the aspectjrt.jar classpath entry. This is usually in
	 * plugins/org.aspectj.runtime_<VERSION>/aspectjrt.jar
	 * <p>
	 * Synchronized method because static field aspectjrtPath is initialized here.
	 */
	public synchronized static String[] getAspectjrtClasspath() {
		if (aspectjrtPath == null) {
			List pathList = new LinkedList();
			Bundle runtime = Platform
					.getBundle(AspectJPlugin.RUNTIME_PLUGIN_ID);
			if (runtime != null) {
				Enumeration enu = runtime.findEntries("/", "*.jar", false); //$NON-NLS-1$  //$NON-NLS-2$
				if (enu != null) {
					while (enu.hasMoreElements()) {
						URL installLoc = (URL) enu.nextElement();
						try {
							pathList.add(FileLocator.resolve(installLoc)
									.getFile());
						} catch (IOException e) {
						}
					}
				}
				if (pathList.size() == 0) {
					// maybe it's a JARed bundle
					IPath path = new Path(runtime.getLocation().split("@")[1]); //$NON-NLS-1$
					IPath full = new Path(Platform.getInstallLocation()
							.getURL().getFile()).append(path);
					pathList.add(full.toString());
				}
			}
			aspectjrtPath = new String[pathList.size()];
			pathList.toArray(aspectjrtPath);
		}
		return aspectjrtPath;
	}

	private synchronized static String[] getAspectjrtSourcePath() {
		if (aspectjrtSourcePath == null) {
			List pathList = new LinkedList();
			Bundle source = Platform.getBundle("org.eclipse.ajdt.source"); //$NON-NLS-1$
			if (source != null) {
				Enumeration enu = source.findEntries(
						"/", "aspectjrtsrc.zip", true); //$NON-NLS-1$  //$NON-NLS-2$
				if (enu != null) {
					while (enu.hasMoreElements()) {
						URL installLoc = (URL) enu.nextElement();
						try {
							pathList.add(FileLocator.resolve(installLoc)
									.getFile());
						} catch (IOException e) {
						}
					}
				}
			}
			aspectjrtSourcePath = new String[pathList.size()];
			pathList.toArray(aspectjrtSourcePath);
		}
		return aspectjrtSourcePath;
	}
}
