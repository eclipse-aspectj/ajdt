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

import java.io.File;
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

	private static String aspectjrtPath = null;

	private static String aspectjrtSourcePath = null;
	
	private static boolean sourceCheckDone = false;

	public IClasspathEntry[] getClasspathEntries() {
		if (fClasspathEntries == null) {
			try {
                String path = getAspectjrtClasspath();
                String sourcePath = getAspectjrtSourcePath();
                fClasspathEntries = new IClasspathEntry[1];
                IPath p = new Path(path);
                IPath sp;
                if ((sourcePath != null)) {
                	sp = new Path(sourcePath);
                } else {
                    sp = null;
                }
                fClasspathEntries[0] = JavaCore.newLibraryEntry(p, sp, null, false);
            } catch (IOException e) {
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
	 * plugins/org.aspectj.runtime_<VERSION>.jar
	 * <p>
	 * Synchronized method because static field aspectjrtPath is initialized here.
	 * @throws IOException 
	 */
	private synchronized static String getAspectjrtClasspath() throws IOException {
		if (aspectjrtPath == null) {
			Bundle runtime = Platform.getBundle(AspectJPlugin.RUNTIME_PLUGIN_ID);
			if (runtime != null) {
			    URL location = runtime.getEntry("/");
			    File file = new File(FileLocator.resolve(location).getFile());
			    if (file.isDirectory()) {
			        // in a runtime workbench
			        file = new File(file, "classes");
			    }
			    aspectjrtPath = file.getCanonicalPath();
			}
		}
		return aspectjrtPath;
	}

	private synchronized static String getAspectjrtSourcePath() throws IOException {
        if (aspectjrtSourcePath == null && !sourceCheckDone) {
            sourceCheckDone = true;
            
            Bundle runtime = Platform.getBundle(AspectJPlugin.RUNTIME_PLUGIN_ID + ".source");
            if (runtime != null) {
                URL location = runtime.getEntry("/");
                File file = new File(FileLocator.resolve(location).getFile());
                if (file.isDirectory()) {
                    // in a runtime workbench
                    file = new File(file, "classes");
                }
                aspectjrtSourcePath = file.getCanonicalPath();
            }
        }
        return aspectjrtSourcePath;
	}
}
