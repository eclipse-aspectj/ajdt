/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.builder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;

/**
 * Copied from org.eclipse.jdt.internal.core.builder.ClasspathDirectory.
 * Changes marked with // AspectJ Change
 */
class ClasspathDirectory extends ClasspathLocation {


	IContainer binaryFolder; // includes .class files for a single directory
	boolean isOutputFolder;
	String binaryLocation;
	SimpleLookupTable directoryCache;
	String[] missingPackageHolder = new String[1];
    AccessRuleSet accessRuleSet;

	ClasspathDirectory(IContainer binaryFolder, boolean isOutputFolder, AccessRuleSet accessRuleSet) {
		this.binaryFolder = binaryFolder;
		this.isOutputFolder = isOutputFolder;
		IPath location = binaryFolder.getLocation();
		this.binaryLocation = location != null ? location.addTrailingSeparator().toString() : ""; //$NON-NLS-1$
		this.directoryCache = new SimpleLookupTable(5);
		this.accessRuleSet = accessRuleSet;
	}

	public void cleanup() {
		this.directoryCache = null;
	}

	String[] directoryList(String qualifiedPackageName) {
		String[] dirList = (String[]) directoryCache.get(qualifiedPackageName);
		if (dirList == missingPackageHolder) return null; // package exists in another classpath directory or jar
		if (dirList != null) return dirList;

		try {
			IResource container = binaryFolder.findMember(qualifiedPackageName); // this is a case-sensitive check
			if (container instanceof IContainer) {
				IResource[] members = ((IContainer) container).members();
				dirList = new String[members.length];
				int index = 0;
				for (int i = 0, l = members.length; i < l; i++) {
					IResource m = members[i];
					if (m.getType() == IResource.FILE && org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(m.getName()))
						// add exclusion pattern check here if we want to hide .class files
						dirList[index++] = m.getName();
				}
				if (index < dirList.length)
					System.arraycopy(dirList, 0, dirList = new String[index], 0, index);
				directoryCache.put(qualifiedPackageName, dirList);
				return dirList;
			}
		} catch(CoreException ignored) {
			// ignore
		}
		directoryCache.put(qualifiedPackageName, missingPackageHolder);
		return null;
	}

	boolean doesFileExist(String fileName, String qualifiedPackageName) {
		String[] dirList = directoryList(qualifiedPackageName);
		if (dirList == null) return false; // most common case

		for (int i = dirList.length; --i >= 0;)
			if (fileName.equals(dirList[i]))
				return true;
		return false;
	}
	
	
	public int hashCode() {
	    int num1 = accessRuleSet == null ? 0 : accessRuleSet.hashCode();
	    int num2 = binaryFolder == null ? 0 : binaryFolder.hashCode();
	    return num1 * num2;
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClasspathDirectory)) return false;

		ClasspathDirectory dir = (ClasspathDirectory) o;
		if (this.accessRuleSet != dir.accessRuleSet)
			if (this.accessRuleSet == null || !this.accessRuleSet.equals(dir.accessRuleSet))
				return false;
		return this.binaryFolder.equals(dir.binaryFolder);
	} 

	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
		if (!doesFileExist(binaryFileName, qualifiedPackageName)) return null; // most common case

		try {
			ClassFileReader reader = ClassFileReader.read(binaryLocation + qualifiedBinaryFileName);
			if (reader != null) {
				if (this.accessRuleSet == null)
					return new NameEnvironmentAnswer(reader, null);
				return new NameEnvironmentAnswer(reader, this.accessRuleSet.getViolatedRestriction(qualifiedBinaryFileName.toCharArray()));
			}
		} catch (Exception e) {
			// handle the case when the project is the output folder and the top-level package is a linked folder
			if (binaryFolder instanceof IProject) {
				IResource file = binaryFolder.findMember(qualifiedBinaryFileName);
				if (file instanceof IFile) {
					IPath location = file.getLocation();
					if (location != null) {
						try {
							ClassFileReader reader = ClassFileReader.read(location.toString());
							if (reader != null) {
								if (this.accessRuleSet == null)
									return new NameEnvironmentAnswer(reader, null);
								return new NameEnvironmentAnswer(reader, this.accessRuleSet.getViolatedRestriction(qualifiedBinaryFileName.toCharArray()));
							}
						} catch (Exception ignored) { // treat as if class file is missing
						}
					}
				}
			}
		}
		return null;
	}

	public IPath getProjectRelativePath() {
		return binaryFolder.getProjectRelativePath();
	}


	public boolean isOutputFolder() {
		return isOutputFolder;
	}

	public boolean isPackage(String qualifiedPackageName) {
		return directoryList(qualifiedPackageName) != null;
	}

	public void reset() {
		this.directoryCache = new SimpleLookupTable(5);
	}

	public String toString() {
		return "Binary classpath directory " + binaryFolder.getFullPath().toString(); //$NON-NLS-1$
	}

	//AspectJ Change Begin
/* (non-Javadoc)
 * @see org.eclipse.ajdt.internal.ui.ajde.ClasspathLocation#toOSString()
 */
public String toOSString() {
	return binaryFolder.getLocation().toOSString();
}
//AspectJ Change End
}
