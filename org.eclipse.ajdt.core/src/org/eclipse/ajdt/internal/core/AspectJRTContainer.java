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
package org.eclipse.ajdt.internal.core;

import java.io.IOException;
import java.net.URL;

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

	public AspectJRTContainer() {
	}

	public IClasspathEntry[] getClasspathEntries() {
		if (fClasspathEntries == null) {
			String[] path = getAspectjrtClasspath();
			fClasspathEntries = new IClasspathEntry[path.length];
			for (int i = 0; i < path.length; i++) {
				IPath p = new Path(path[i]);
				fClasspathEntries[i] = JavaCore.newLibraryEntry(p, null, null,
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
	 * plugins/org.aspectj.ajde_ <VERSION>/aspectjrt.jar
	 */
	public static String[] getAspectjrtClasspath() {
		if (aspectjrtPath == null) {
			aspectjrtPath = new String[1];
			Bundle runtime = Platform
					.getBundle(AspectJPlugin.RUNTIME_PLUGIN_ID);
			if (runtime != null) {
				URL installLoc = runtime.getEntry("aspectjrt.jar"); //$NON-NLS-1$
				if (installLoc == null) {
					// maybe it's a JARed bundle
					IPath path = new Path(runtime.getLocation().split("@")[1]); //$NON-NLS-1$
					IPath full = new Path(Platform.getInstallLocation()
							.getURL().getFile()).append(path);
					aspectjrtPath[0] = full.toString();
				} else {
					try {
						aspectjrtPath[0] = FileLocator.resolve(installLoc)
								.getFile();
					} catch (IOException e) {
					}
				}
			}
		}
		return aspectjrtPath;
	}
}
