/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tal Lev-Ami - added package cache for zip files
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.builder;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.util.SimpleLookupTable;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

/**
 * Copied from org.eclipse.jdt.internal.core.builder.ClasspathJar.
 * Changes marked with // AspectJ Change
 */
public class ClasspathJar extends ClasspathLocation {

	static class PackageCacheEntry {
		long lastModified;
		long fileSize;
		SimpleSet packageSet;

		PackageCacheEntry(long lastModified, long fileSize, SimpleSet packageSet) {
			this.lastModified = lastModified;
			this.fileSize = fileSize;
			this.packageSet = packageSet;
		}
	}

	static SimpleLookupTable PackageCache = new SimpleLookupTable();

	/**
	 * Calculate and cache the package list available in the zipFile.
	 * @param zipFile The zip file to use
	 * @return A SimpleSet with the all the package names in the zipFile.
	 */
	static SimpleSet findPackageSet(ZipFile zipFile) {
		String zipFileName = zipFile.getName();
		File zipFileObject = new File(zipFileName);
		long lastModified = zipFileObject.lastModified();
		long fileSize = zipFileObject.length();
		PackageCacheEntry cacheEntry = (PackageCacheEntry) PackageCache.get(zipFileName);
		if (cacheEntry != null && cacheEntry.lastModified == lastModified && cacheEntry.fileSize == fileSize)
			return cacheEntry.packageSet;

		SimpleSet packageSet = new SimpleSet(41);
		packageSet.add(""); //$NON-NLS-1$
		nextEntry : for (Enumeration<? extends ZipEntry> e = zipFile.entries(); e.hasMoreElements(); ) {
			String fileName = (e.nextElement()).getName();

			// add the package name & all of its parent packages
			int last = fileName.lastIndexOf('/');
			while (last > 0) {
				// extract the package name
				String packageName = fileName.substring(0, last);
				if (packageSet.includes(packageName))
					continue nextEntry;
				packageSet.add(packageName);
				last = packageName.lastIndexOf('/');
			}
		}

		PackageCache.put(zipFileName, new PackageCacheEntry(lastModified, fileSize, packageSet));
		return packageSet;
	}


	String zipFilename; // keep for equals
	IFile resource;
	ZipFile zipFile;
	boolean closeZipFileAtEnd;
	SimpleSet knownPackageNames;
    AccessRuleSet accessRuleSet;

	ClasspathJar(String zipFilename, AccessRuleSet accessRuleSet) {
		this.zipFilename = zipFilename;
		this.zipFile = null;
		this.knownPackageNames = null;
		this.accessRuleSet = accessRuleSet;
	}

	ClasspathJar(IFile resource, AccessRuleSet accessRuleSet) {
		this.resource = resource;
		IPath location = resource.getLocation();
		this.zipFilename = location != null ? location.toString() : ""; //$NON-NLS-1$
		this.zipFile = null;
		this.knownPackageNames = null;
		this.accessRuleSet = accessRuleSet;
	}

	public ClasspathJar(ZipFile zipFile, AccessRuleSet accessRuleSet) {
		this.zipFilename = zipFile.getName();
		this.zipFile = zipFile;
		this.closeZipFileAtEnd = false;
		this.knownPackageNames = null;
		this.accessRuleSet = accessRuleSet;
	}

	public void cleanup() {
		if (this.zipFile != null && this.closeZipFileAtEnd) {
			try {
				this.zipFile.close();
			} catch(IOException e) { // ignore it
			}
			this.zipFile = null;
		}
		this.knownPackageNames = null;
	}

	public int hashCode() {
	    return this.zipFilename == null ? super.hashCode() : this.zipFilename.hashCode();
	}

	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ClasspathJar)) return false;

		ClasspathJar jar = (ClasspathJar) o;
		if (this.accessRuleSet != jar.accessRuleSet)
			if (this.accessRuleSet == null || !this.accessRuleSet.equals(jar.accessRuleSet))
				return false;
		return this.zipFilename.equals(((ClasspathJar) o).zipFilename);
	}

	public NameEnvironmentAnswer findClass(String binaryFileName, String qualifiedPackageName, String qualifiedBinaryFileName) {
		if (!isPackage(qualifiedPackageName)) return null; // most common case

		try {
			ClassFileReader reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
			if (reader != null) {
				if (this.accessRuleSet == null)
					return new NameEnvironmentAnswer(reader, null);
				return new NameEnvironmentAnswer(reader, this.accessRuleSet.getViolatedRestriction(qualifiedBinaryFileName.toCharArray()));
			}
		} catch (Exception e) { // treat as if class file is missing
		}
		return null;
	}

	public IPath getProjectRelativePath() {
		if (this.resource == null) return null;
		return	this.resource.getProjectRelativePath();
	}

	public boolean isPackage(String qualifiedPackageName) {
		if (this.knownPackageNames != null)
			return this.knownPackageNames.includes(qualifiedPackageName);

		try {
			if (this.zipFile == null) {
				if (org.eclipse.jdt.internal.core.JavaModelManager.ZIP_ACCESS_VERBOSE) {
				    AJLog.log(AJLog.BUILDER_CLASSPATH, "(" + Thread.currentThread() + ") [ClasspathJar.isPackage(String)] Creating ZipFile on " + zipFilename); //$NON-NLS-1$   //$NON-NLS-2$
				}
				this.zipFile = new ZipFile(zipFilename);
				this.closeZipFileAtEnd = true;
			}
			this.knownPackageNames = findPackageSet(this.zipFile);
		} catch(Exception e) {
			this.knownPackageNames = new SimpleSet(); // assume for this build the zipFile is empty
		}
		return this.knownPackageNames.includes(qualifiedPackageName);
	}

	public String toString() {
		return "Classpath jar file " + zipFilename; //$NON-NLS-1$
	}

//AspectJ Change Begin
/* (non-Javadoc)
 * @see org.eclipse.ajdt.internal.ui.ajde.ClasspathLocation#toOSString()
 */
public String toOSString() {
	return zipFilename;
}
//AspectJ Change End
}
