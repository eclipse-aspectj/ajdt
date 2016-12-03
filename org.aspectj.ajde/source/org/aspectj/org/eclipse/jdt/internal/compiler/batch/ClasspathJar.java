// ASPECTJ
/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *								Bug 440687 - [compiler][batch][null] improve command line option for external annotations
 *******************************************************************************/
package org.aspectj.org.eclipse.jdt.internal.compiler.batch;

// this file has a number of changes made by ASC to prevent us from
// leaking OS resources by keeping jars open longer than needed.

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.aspectj.org.eclipse.jdt.core.compiler.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationDecorator;
import org.aspectj.org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.aspectj.org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.ManifestAnalyzer;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.aspectj.org.eclipse.jdt.internal.compiler.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ClasspathJar extends ClasspathLocation {

	// AspectJ Extension
	/**
	 * ASC 23112004
	 * These fields and related logic throughout the class enable us to cope with very long
	 * classpaths.  Normally all the archives on the classpath were openede and left open
	 * for the duration of a compile - this doesn't scale since you will start running out
	 * of file handles when you have extremely long classpaths (e.g. 4000 jars).  These
	 * fields enable us to keep track of how many are currently open and if you attempt to
	 * open more than some defined limit, it will close some that you haven't used for a
	 * while before opening the new one.  The limit is tailorable through the
	 *   org.aspectj.weaver.openarchives
	 * system property, the default is 1000 which means most users will never exercise
	 * this logic.  The only change outside of this class related to this feature is that
	 * the FileSystem class now constructs a ClasspathJar object by passing in a File
	 * rather than a ZipFile - it is then the responsibility of this class to
	 * open and manage the ZipFile.
	 */
	private static int maxOpenArchives = 1000;
	private final static int MAXOPEN_DEFAULT = 1000;
    private static List openArchives = new ArrayList();
	// End AspectJ Extension
	
protected File file;
protected ZipFile zipFile;
protected ZipFile annotationZipFile;
protected boolean closeZipFileAtEnd;
protected Hashtable packageCache;
protected List<String> annotationPaths;


// AspectJ Extension	
static {
	String openarchivesString = getSystemPropertyWithoutSecurityException("org.aspectj.weaver.openarchives",Integer.toString(MAXOPEN_DEFAULT));
	maxOpenArchives=Integer.parseInt(openarchivesString);
	if (maxOpenArchives<20) maxOpenArchives=1000;
}
// End AspectJ Extension

public ClasspathJar(File file, boolean closeZipFileAtEnd,
		AccessRuleSet accessRuleSet, String destinationPath) {
	super(accessRuleSet, destinationPath);
	this.file = file;
	this.closeZipFileAtEnd = closeZipFileAtEnd;
}

public List fetchLinkedJars(FileSystem.ClasspathSectionProblemReporter problemReporter) {
	// expected to be called once only - if multiple calls desired, consider
	// using a cache
	InputStream inputStream = null;
	try {
		initialize();
		ArrayList result = new ArrayList();
		ZipEntry manifest = this.zipFile.getEntry("META-INF/MANIFEST.MF"); //$NON-NLS-1$
		if (manifest != null) { // non-null implies regular file
			inputStream = this.zipFile.getInputStream(manifest);
			ManifestAnalyzer analyzer = new ManifestAnalyzer();
			boolean success = analyzer.analyzeManifestContents(inputStream);
			List calledFileNames = analyzer.getCalledFileNames();
			if (problemReporter != null) {
				if (!success || analyzer.getClasspathSectionsCount() == 1 &&  calledFileNames == null) {
					problemReporter.invalidClasspathSection(getPath());
				} else if (analyzer.getClasspathSectionsCount() > 1) {
					problemReporter.multipleClasspathSections(getPath());
				}
			}
			if (calledFileNames != null) {
				Iterator calledFilesIterator = calledFileNames.iterator();
				String directoryPath = getPath();
				int lastSeparator = directoryPath.lastIndexOf(File.separatorChar);
				directoryPath = directoryPath.substring(0, lastSeparator + 1); // potentially empty (see bug 214731)
				while (calledFilesIterator.hasNext()) {
					result.add(new ClasspathJar(new File(directoryPath + (String) calledFilesIterator.next()), this.closeZipFileAtEnd, this.accessRuleSet, this.destinationPath));
				}
			}
		}
		return result;
	} catch (IOException e) {
		return null;
	} finally {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				// best effort
			}
		}
	}
}
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
	return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
}
public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly) {
	if (!isPackage(qualifiedPackageName))
		return null; // most common case

	try {
	    ensureOpen(); // AspectJ Extension 
		IBinaryType reader = ClassFileReader.read(this.zipFile, qualifiedBinaryFileName);
		if (reader != null) {
			if (this.annotationPaths != null) {
				String qualifiedClassName = qualifiedBinaryFileName.substring(0, qualifiedBinaryFileName.length()-SuffixConstants.EXTENSION_CLASS.length()-1);
				for (String annotationPath : this.annotationPaths) {
					try {
						if (this.annotationZipFile == null) {
							this.annotationZipFile = ExternalAnnotationDecorator.getAnnotationZipFile(annotationPath, null);
						}
						reader = ExternalAnnotationDecorator.create(reader, annotationPath, qualifiedClassName, this.annotationZipFile);

						if (reader.getExternalAnnotationStatus() == ExternalAnnotationStatus.TYPE_IS_ANNOTATED) {
							break;
						}
					} catch (IOException e) {
						// don't let error on annotations fail class reading
					}
				}
			}
			return new NameEnvironmentAnswer(reader, fetchAccessRestriction(qualifiedBinaryFileName));
		}
	} catch(ClassFormatException e) {
		// treat as if class file is missing
	} catch (IOException e) {
		// treat as if class file is missing
	}
	return null;
}
@Override
public boolean hasAnnotationFileFor(String qualifiedTypeName) {
	return this.zipFile.getEntry(qualifiedTypeName+ExternalAnnotationProvider.ANNOTATION_FILE_SUFFIX) != null; 
}
public char[][][] findTypeNames(String qualifiedPackageName) {
	if (!isPackage(qualifiedPackageName))
		return null; // most common case

	// AspectJ Extension
	try {
        ensureOpen();
	} catch (IOException ioe) {
		// Doesn't normally occur - probably means since starting the compile 
		// you have removed one of the jars.
		ioe.printStackTrace();
		return null;
	}
	// End AspectJ Extension
	ArrayList answers = new ArrayList();
	nextEntry : for (Enumeration e = this.zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();

		// add the package name & all of its parent packages
		int last = fileName.lastIndexOf('/');
		while (last > 0) {
			// extract the package name
			String packageName = fileName.substring(0, last);
			if (!qualifiedPackageName.equals(packageName))
				continue nextEntry;
			int indexOfDot = fileName.lastIndexOf('.');
			if (indexOfDot != -1) {
				String typeName = fileName.substring(last + 1, indexOfDot);
				char[] packageArray = packageName.toCharArray();
				answers.add(
					CharOperation.arrayConcat(
						CharOperation.splitOn('/', packageArray),
						typeName.toCharArray()));
			}
		}
	}
	int size = answers.size();
	if (size != 0) {
		char[][][] result = new char[size][][];
		answers.toArray(result);
		return null;
	}
	return null;
}
public void initialize() throws IOException {
	if (this.zipFile == null) {
		this.zipFile = new ZipFile(this.file);
	}
}
public boolean isPackage(String qualifiedPackageName) {
	if (this.packageCache != null)
		return this.packageCache.containsKey(qualifiedPackageName);

	this.packageCache = new Hashtable(41);
	this.packageCache.put(Util.EMPTY_STRING, Util.EMPTY_STRING);

	// AspectJ Extension
	try {
        ensureOpen();
	} catch (IOException ioe) {
		// Doesn't normally occur - probably means since starting the compile 
		// you have removed one of the jars.
		ioe.printStackTrace();
		return false;
	}
	// End AspectJ Extension
	nextEntry : for (Enumeration e = this.zipFile.entries(); e.hasMoreElements(); ) {
		String fileName = ((ZipEntry) e.nextElement()).getName();

		// add the package name & all of its parent packages
		int last = fileName.lastIndexOf('/');
		while (last > 0) {
			// extract the package name
			String packageName = fileName.substring(0, last);
			if (this.packageCache.containsKey(packageName))
				continue nextEntry;
			this.packageCache.put(packageName, packageName);
			last = packageName.lastIndexOf('/');
		}
	}
	return this.packageCache.containsKey(qualifiedPackageName);
}
public void reset() {
	if (this.closeZipFileAtEnd) {
		if (this.zipFile != null) {
		// AspectJ Extension
		/*old code:{
			try {
				this.zipFile.close();
			} catch(IOException e) {
				// ignore
			}
			this.zipFile = null;
		*/// new code:
		close();
		// End AspectJ Extension
		}
		if (this.annotationZipFile != null) {
			try {
				this.annotationZipFile.close();
			} catch(IOException e) {
				// ignore
			}
			this.annotationZipFile = null;
		}
	}
	this.packageCache = null;
}
public String toString() {
	return "Classpath for jar file " + this.file.getPath(); //$NON-NLS-1$
}
public char[] normalizedPath() {
	if (this.normalizedPath == null) {
		String path2 = this.getPath();
		char[] rawName = path2.toCharArray();
		if (File.separatorChar == '\\') {
			CharOperation.replace(rawName, '\\', '/');
		}
		this.normalizedPath = CharOperation.subarray(rawName, 0, CharOperation.lastIndexOf('.', rawName));
	}
	return this.normalizedPath;
}
public String getPath() {
	if (this.path == null) {
		try {
			this.path = this.file.getCanonicalPath();
		} catch (IOException e) {
			// in case of error, simply return the absolute path
			this.path = this.file.getAbsolutePath();
		}
	}
	return this.path;
}
public int getMode() {
	return BINARY;
}

// AspectJ Extension
private void ensureOpen() throws IOException {
	if (zipFile != null) return; // If its not null, the zip is already open
	if (openArchives.size()>=maxOpenArchives) {
		closeSomeArchives(openArchives.size()/10); // Close 10% of those open
	}
	zipFile = new ZipFile(file);
	openArchives.add(this);
}

private void closeSomeArchives(int n) {
	for (int i=n-1;i>=0;i--) {
		ClasspathJar zf = (ClasspathJar)openArchives.get(0);
		zf.close();
	}
}

public void close() {
	if (zipFile == null) return;
	try {
		openArchives.remove(this);
		zipFile.close();
	} catch (IOException ioe) {
		ioe.printStackTrace();
	} finally {
		zipFile = null;
	}
}

// Copes with the security manager
private static String getSystemPropertyWithoutSecurityException (String aPropertyName, String aDefaultValue) {
	try {
		return System.getProperty(aPropertyName, aDefaultValue);
	} catch (SecurityException ex) {
		return aDefaultValue;
	}
}

// End AspectJ Extension
}
