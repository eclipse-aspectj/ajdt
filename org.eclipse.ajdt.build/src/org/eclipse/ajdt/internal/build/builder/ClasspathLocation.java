/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.build.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;

/**
 * Copied from org.eclipse.jdt.internal.core.builder.ClasspathLocation.
 * Changes marked with // AspectJ Change
 */
public abstract class ClasspathLocation {

static ClasspathLocation forSourceFolder(IContainer sourceFolder, IContainer outputFolder, char[][] inclusionPatterns, char[][] exclusionPatterns) {
	return new ClasspathMultiDirectory(sourceFolder, outputFolder, inclusionPatterns, exclusionPatterns);
}

public static ClasspathLocation forBinaryFolder(IContainer binaryFolder, boolean isOutputFolder) {
	return new ClasspathDirectory(binaryFolder, isOutputFolder);
}

static ClasspathLocation forLibrary(String libraryPathname) {
	return new ClasspathJar(libraryPathname);
}

static ClasspathLocation forLibrary(IFile library) {
	return new ClasspathJar(library);
}

public abstract NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName);

public abstract IPath getProjectRelativePath();

public boolean isOutputFolder() {
	return false;
}

public abstract boolean isPackage(String qualifiedPackageName);

public void cleanup() {
	// free anything which is not required when the state is saved
}
public void reset() {
	// reset any internal caches before another compile loop starts
}

//AspectJ Change Begin
// method to convert classpath item into string format
public abstract String toOSString();
//AspectJ Change End

}
