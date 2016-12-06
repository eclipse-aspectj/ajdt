/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaProject;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Project specific settings. These used to be in the UI plugin, so the UI node
 * names have been kept for compatibility.
 */
public class AspectJCorePreferences {

	public static final String OPTION_IncrementalCompilationOptimizations = "org.eclipse.ajdt.core.builder.incrementalCompilationOptimizations"; //$NON-NLS-1$

	public static final String ASPECTPATH_ATTRIBUTE_NAME = "org.eclipse.ajdt.aspectpath"; //$NON-NLS-1$
	public static final String INPATH_ATTRIBUTE_NAME = "org.eclipse.ajdt.inpath"; //$NON-NLS-1$

	// see ajdt.ui's plugin.xml and the
	// org.eclipse.jdt.ui.classpathAttributeConfiguration extension point
	public static final String INPATH_RESTRICTION_ATTRIBUTE_NAME = "org.eclipse.ajdt.inpath.restriction"; //$NON-NLS-1$
	public static final String ASPECTPATH_RESTRICTION_ATTRIBUTE_NAME = "org.eclipse.ajdt.aspectpath.restriction"; //$NON-NLS-1$

	/**
	 * The value may be filled in with the container that contains this
	 * classpath entry So when checking to see if a classpath entry has this
	 * attribute, use {@link #isOnAspectpath(IClasspathEntry)}
	 */
	public static final IClasspathAttribute ASPECTPATH_ATTRIBUTE = JavaCore
			.newClasspathAttribute(ASPECTPATH_ATTRIBUTE_NAME, ASPECTPATH_ATTRIBUTE_NAME); // $NON-NLS-1$

	/**
	 * The value may be filled in with the container that contains this
	 * classpath entry So when checking to see if a classpath entry has this
	 * attribute, use {@link #isOnInpath(IClasspathEntry)}
	 */
	public static final IClasspathAttribute INPATH_ATTRIBUTE = JavaCore.newClasspathAttribute(INPATH_ATTRIBUTE_NAME,
			INPATH_ATTRIBUTE_NAME); // $NON-NLS-1$

	public static final String OUT_JAR = "org.eclipse.ajdt.ui.outJar"; //$NON-NLS-1$

	public static final String INPATH_OUT_FOLDER = "org.eclipse.ajdt.ui.inpathOutFolder"; //$NON-NLS-1$

	public static final String ASPECTPATH = "org.eclipse.ajdt.ui.aspectPath"; //$NON-NLS-1$

	public static final String ASPECTPATH_CON_KINDS = "org.eclipse.ajdt.ui.aspectPath.contentKind"; //$NON-NLS-1$

	public static final String ASPECTPATH_ENT_KINDS = "org.eclipse.ajdt.ui.aspectPath.entryKind"; //$NON-NLS-1$

	public static final String INPATH = "org.eclipse.ajdt.ui.inPath"; //$NON-NLS-1$

	public static final String INPATH_CON_KINDS = "org.eclipse.ajdt.ui.inPath.contentKind"; //$NON-NLS-1$

	public static final String INPATH_ENT_KINDS = "org.eclipse.ajdt.ui.inPath.entryKind"; //$NON-NLS-1$

	public static String getProjectOutJar(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.UI_PLUGIN_ID);
		return projectNode.get(OUT_JAR, ""); //$NON-NLS-1$
	}

	public static String getProjectInpathOutFolder(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.UI_PLUGIN_ID);
		return projectNode.get(INPATH_OUT_FOLDER, null);
	}

	public static void setProjectOutJar(IProject project, String value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.UI_PLUGIN_ID);
		projectNode.put(OUT_JAR, value);
		if (value.length() == 0) {
			projectNode.remove(OUT_JAR);
		}
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public static void setProjectInpathOutFolder(IProject project, String value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.UI_PLUGIN_ID);
		if (value == null || value.length() == 0) {
			projectNode.remove(INPATH_OUT_FOLDER);
		} else {
			projectNode.put(INPATH_OUT_FOLDER, value);
		}
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public static void setProjectAspectPath(IProject project, String path, String cKinds, String eKinds) {
		setProjectPath(project, path, cKinds, eKinds, ASPECTPATH_ATTRIBUTE);
	}

	public static String[] getRawProjectAspectPath(IProject project) {
		return internalGetProjectPath(project, ASPECTPATH_ATTRIBUTE, false);
	}

	public static String[] getResolvedProjectAspectPath(IProject project) {
		return internalGetProjectPath(project, ASPECTPATH_ATTRIBUTE, true);
	}

	public static void addToAspectPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		addAttribute(jp, entry, AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
	}

	public static void removeFromAspectPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		removeAttribute(jp, entry, AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
	}

	public static void addToAspectPath(IProject project, String jarPath, int eKind) {
		addAttribute(project, jarPath, eKind, ASPECTPATH_ATTRIBUTE);
	}

	public static boolean isOnAspectpath(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (int j = 0; j < attributes.length; j++) {
			if (isAspectPathAttribute(attributes[j])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isAspectPathAttribute(IClasspathAttribute attribute) {
		return attribute.getName().equals(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE.getName());
	}

	/**
	 * determines if an element is on the aspect path taking into account the
	 * restrictions of the classpath container entry
	 */
	public static boolean isOnAspectpathWithRestrictions(IClasspathEntry entry, String item) {
		if (!isOnAspectpath(entry)) {
			return false;
		}

		Set<String> restrictions = findContainerRestrictions(entry, true);
		if (restrictions == null) {
			// no restrictions, assume the jar entry is on the path
			return true;
		} else {
			for (String restriction : restrictions) {
				if (item.indexOf(restriction) != -1) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * determines if an element is on the aspect path taking into account the
	 * restrictions of the classpath container entry
	 */
	public static boolean isOnInpathWithRestrictions(IClasspathEntry entry, String item) {
		if (!isOnInpath(entry)) {
			return false;
		}

		Set<String> restrictions = findContainerRestrictions(entry, false);
		if (restrictions == null || restrictions.isEmpty()) {
			// no restrictions, assume the jar entry is on the path
			return true;
		} else {
			for (String restriction : restrictions) {
				if (item.indexOf(restriction) != -1) {
					return true;
				}
			}
			return false;
		}
	}

	/**
	 * Checks to see if an entry is already on the aspect path
	 */
	public static boolean isOnAspectpath(IProject project, String path) {
		IJavaProject jp = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_PROJECT)) {
					IClasspathEntry resolvedClasspathEntry = JavaCore.getResolvedClasspathEntry(cp[i]);
					if (resolvedClasspathEntry != null) {
						String entry = resolvedClasspathEntry.getPath().toPortableString();
						if (entry.equals(path)) {
							if (isOnAspectpath(cp[i])) {
								return true;
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}

	public static String[] getRawProjectInpath(IProject project) {
		return internalGetProjectPath(project, INPATH_ATTRIBUTE, false);
	}

	public static String[] getResolvedProjectInpath(IProject project) {
		return internalGetProjectPath(project, INPATH_ATTRIBUTE, true);
	}

	public static List<IClasspathEntry> resolveDependentProjectClasspath(IClasspathEntry projEntry,
			IProject requiredProj) {
		// add all output locations and exported classpath entities
		// AspectJ compiler doesn't understand the concept of a java project
		List<IClasspathEntry> actualEntries = new ArrayList<IClasspathEntry>();

		try {
			JavaProject requiredJavaProj = (JavaProject) JavaCore.create(requiredProj);
			// bug 288395 Do not use the default mechanism for resolving
			// classpath here
			// this will look into jar files at the Classpath header in the
			// jar's manifest
			// and include jar files that are potentially missing, but have no
			// effect on
			// the build.
			Object resolvedClasspath = requiredJavaProj.resolveClasspath(requiredJavaProj.getRawClasspath(), true,
					false);
			IClasspathEntry[] requiredEntries = extractRequiredEntries(resolvedClasspath);
			for (int i = 0; i < requiredEntries.length; i++) {
				IClasspathEntry requiredEntry = requiredEntries[i];
				if (requiredEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {

					// always add source entries even if not explicitly exported
					// don't add the source folder itself, but instead add the
					// outfolder
					IPath outputLocation = requiredEntry.getOutputLocation();
					if (outputLocation != null) {
						IAccessRule[] rules = projEntry.getAccessRules();
						IClasspathAttribute[] attributes = projEntry.getExtraAttributes();

						// only add the out folder if it already exists
						if (requiredProj.getFolder(outputLocation.removeFirstSegments(1)).exists()) {
							IClasspathEntry outFolder = JavaCore.newLibraryEntry(outputLocation,
									requiredEntry.getPath(), requiredProj.getFullPath(), rules, attributes,
									projEntry.isExported());
							actualEntries.add(outFolder);
						}
					}
				} else if (requiredEntry.isExported()) {
					// must recur through this entry and add entries that it
					// contains
					actualEntries.addAll(resolveClasspath(requiredEntry, requiredProj));

				}
			} // for (int i = 0; i < requiredEntries.length; i++)

			IPath outputLocation = requiredJavaProj.getOutputLocation();
			// Output location may not exist. Do not put output location of
			// required project
			// on path unless it exists
			boolean exists = false;
			// bug 244330 check to see if the project folder is also the output
			// folder
			if (outputLocation.segmentCount() == 1) {
				exists = true;
			} else {
				if (requiredProj.getWorkspace().getRoot().getFolder(outputLocation).exists()) {
					exists = true;
				}
			}

			if (exists) {
				IClasspathEntry outFolder = JavaCore.newLibraryEntry(outputLocation, null, requiredProj.getFullPath());
				actualEntries.add(outFolder);
			}
		} catch (JavaModelException e) {
		}
		return actualEntries;
	}

	/**
	 * resolvedClasspath is a package protected static class inside JavaProject
	 * must use reflection to access it
	 */
	@SuppressWarnings({ "unchecked" })
	private static IClasspathEntry[] extractRequiredEntries(Object resolvedClasspath) {

		try {
			Class resolvedClasspathClass = Class.forName("org.eclipse.jdt.internal.core.JavaProject$ResolvedClasspath");
			return (IClasspathEntry[]) ReflectionUtils.getPrivateField(resolvedClasspathClass, "resolvedClasspath",
					resolvedClasspath);
		} catch (Exception e) {
			return new IClasspathEntry[0];
		}
	}

	public static List<IClasspathEntry> resolveClasspathContainer(IClasspathEntry classpathContainerEntry,
			IProject thisProject) throws JavaModelException {
		IJavaProject thisJavaProject = JavaCore.create(thisProject);
		IClasspathContainer container = JavaCore.getClasspathContainer(classpathContainerEntry.getPath(),
				thisJavaProject);
		if (container != null) {
			List<IClasspathEntry> actualEntries = new ArrayList<IClasspathEntry>();
			IClasspathEntry[] containerEntries = container.getClasspathEntries();
			for (int i = 0; i < containerEntries.length; i++) {
				// projects must be resolved specially since the AspectJ doesn't
				// understand the
				// concept of project
				switch (containerEntries[i].getEntryKind()) {
				case IClasspathEntry.CPE_PROJECT:
					IProject requiredProj = thisProject.getWorkspace().getRoot()
							.getProject(containerEntries[i].getPath().makeRelative().toPortableString());
					if (!requiredProj.getName().equals(thisProject.getName()) && requiredProj.exists()) {
						actualEntries.addAll(resolveDependentProjectClasspath(containerEntries[i], requiredProj));
					}
					break;

				case IClasspathEntry.CPE_VARIABLE:
					IClasspathEntry resolvedClasspathEntry = JavaCore.getResolvedClasspathEntry(containerEntries[i]);
					if (resolvedClasspathEntry != null) {
						actualEntries.add(resolvedClasspathEntry);
					}
					break;

				case IClasspathEntry.CPE_CONTAINER:
					// not sure if we can have this, but try anyway
					actualEntries.addAll(resolveClasspathContainer(containerEntries[i], thisProject));
					break;
				case IClasspathEntry.CPE_LIBRARY:
					actualEntries.add(containerEntries[i]);
					break;
				default:
					// do nothing
				}
			}
			return actualEntries;
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Resolves a single classpath entry
	 * 
	 * @param entry
	 *            the classpath entry to resolve
	 * @param thisProject
	 *            the java project that has this entry
	 * @return the resolved list of classpath entries
	 * @throws JavaModelException
	 */
	public static List<IClasspathEntry> resolveClasspath(IClasspathEntry entry, IProject thisProject)
			throws JavaModelException {
		switch (entry.getEntryKind()) {
		case IClasspathEntry.CPE_CONTAINER:
			return resolveClasspathContainer(entry, thisProject);

		case IClasspathEntry.CPE_LIBRARY:
			return Collections.singletonList(entry);

		case IClasspathEntry.CPE_PROJECT:
			IProject containedProj = thisProject.getWorkspace().getRoot()
					.getProject(entry.getPath().makeRelative().toPortableString());
			if (!containedProj.getName().equals(thisProject.getName()) && containedProj.exists()) {
				return resolveDependentProjectClasspath(entry, containedProj);
			} else {
				return Collections.emptyList();
			}

		case IClasspathEntry.CPE_VARIABLE:
			IClasspathEntry resolvedClasspathEntry = JavaCore.getResolvedClasspathEntry(entry);
			if (resolvedClasspathEntry != null) {
				return Collections.singletonList(resolvedClasspathEntry);
			} else {
				return Collections.emptyList();
			}
		default:
			return Collections.emptyList();
		}
	}

	public static void setProjectInPath(IProject project, String path, String cKinds, String eKinds) {
		setProjectPath(project, path, cKinds, eKinds, INPATH_ATTRIBUTE);
	}

	public static void addToInPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		addAttribute(jp, entry, AspectJCorePreferences.INPATH_ATTRIBUTE);
	}

	public static void removeFromInPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		removeAttribute(jp, entry, AspectJCorePreferences.INPATH_ATTRIBUTE);
	}

	public static void addToInPath(IProject project, String jarPath, int eKind) {
		addAttribute(project, jarPath, eKind, INPATH_ATTRIBUTE);
	}

	public static boolean isOnInpath(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (int j = 0; j < attributes.length; j++) {
			if (isInPathAttribute(attributes[j])) {
				return true;
			}
		}
		return false;
	}

	public static boolean isOnPath(IClasspathEntry entry, boolean aspectpath) {
		return aspectpath ? isOnAspectpath(entry) : isOnInpath(entry);
	}

	/**
	 * Checks to see if an entry is already on the Inpath
	 */
	public static boolean isOnInpath(IProject project, String jarPath) {
		IJavaProject jp = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_PROJECT)) {
					IClasspathEntry resolvedClasspathEntry = JavaCore.getResolvedClasspathEntry(cp[i]);
					if (resolvedClasspathEntry != null) {
						String entry = resolvedClasspathEntry.getPath().toPortableString();
						if (entry.equals(jarPath)) {
							if (isOnInpath(cp[i])) {
								return true;
							}
						}
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}

	public static boolean isInPathAttribute(IClasspathAttribute attribute) {
		return attribute.getName().equals(AspectJCorePreferences.INPATH_ATTRIBUTE.getName());
	}

	public static void setIncrementalCompilationOptimizationsEnabled(boolean value) {
		IEclipsePreferences store = AspectJPlugin.getDefault().getPreferences();
		store.putBoolean(OPTION_IncrementalCompilationOptimizations, value);
	}

	/**
	 * Searches the raw classpath for entries whose paths contain the strings in
	 * putOnPath.
	 * 
	 * Then ensures that these classpath entries are on the aspect path
	 */
	public static void augmentAspectPath(IProject project, String[] putOnAspectPath) {
		if (putOnAspectPath.length == 0) {
			// nothing to do!
			return;
		}
		IJavaProject jp = JavaCore.create(project);
		List<IClasspathEntry> toPutOnAspectPath = new ArrayList<IClasspathEntry>();
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				String path = cp[i].getPath().toPortableString();
				for (int j = 0; j < putOnAspectPath.length; j++) {
					if (path.indexOf(putOnAspectPath[j]) != -1) {
						toPutOnAspectPath.add(cp[i]);
					}
				}
			}

			for (IClasspathEntry entry : toPutOnAspectPath) {
				if (!isOnAspectpath(entry)) {
					addToAspectPath(project, entry);
				}
			}
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Checks to see if the compiler option for incremental build optimizations
	 * is on or off
	 * 
	 * On by default
	 * 
	 * @return
	 */
	public static boolean isIncrementalCompilationOptimizationsEnabled() {
		IEclipsePreferences store = AspectJPlugin.getDefault().getPreferences();
		return store.getBoolean(OPTION_IncrementalCompilationOptimizations, true);
	}

	private static void setProjectPath(IProject project, String path, String cKinds, String eKinds,
			IClasspathAttribute attribute) {
		IJavaProject javaProject = JavaCore.create(project);
		removeAttribute(javaProject, attribute);

		StringTokenizer pathTok = new StringTokenizer(path, File.pathSeparator);
		StringTokenizer eKindsTok = new StringTokenizer(eKinds, File.pathSeparator);
		int index = 1;
		while (pathTok.hasMoreTokens() && eKindsTok.hasMoreTokens()) {
			String entry = pathTok.nextToken();
			int eKind = Integer.parseInt(eKindsTok.nextToken());
			if (ASPECTPATH_ATTRIBUTE.equals(attribute)) {
				addToAspectPath(project, entry, eKind);
			} else if (INPATH_ATTRIBUTE.equals(attribute)) {
				addToInPath(project, entry, eKind);
			}
			index++;
		}
	}

	private static boolean shouldCheckOldStylePath(IProject project, String pathKind) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.UI_PLUGIN_ID);
		return projectNode.get(pathKind, "").length() == 0 && projectNode.get(pathKind + "1", "").length() > 0;
	}

	private static void markOldStylePathAsRead(IProject project, String pathKind) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.UI_PLUGIN_ID);
		projectNode.put(pathKind, "visited");
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	private static String[] getOldProjectPath(IProject project, boolean aspectPath) {
		String pathName;
		String pathConKinds;
		String pathEntKinds;
		if (aspectPath) {
			pathName = ASPECTPATH;
			pathConKinds = ASPECTPATH_CON_KINDS;
			pathEntKinds = ASPECTPATH_ENT_KINDS;
		} else {
			pathName = INPATH;
			pathConKinds = INPATH_CON_KINDS;
			pathEntKinds = INPATH_ENT_KINDS;
		}

		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope.getNode(AspectJPlugin.UI_PLUGIN_ID);
		String pathString = ""; //$NON-NLS-1$
		int index = 1;
		String value = projectNode.get(pathName + index, ""); //$NON-NLS-1$
		if (value.length() == 0) {
			return null;
		}
		while (value.length() > 0) {
			pathString += value;
			pathString += File.pathSeparator;
			index++;
			value = projectNode.get(pathName + index, ""); //$NON-NLS-1$
		}

		String contentString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(pathConKinds + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			contentString += toContentKind(value.toUpperCase());
			contentString += File.pathSeparator;
			index++;
			value = projectNode.get(pathConKinds + index, ""); //$NON-NLS-1$
		}

		String entryString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(pathEntKinds + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			entryString += toEntryKind(value.toUpperCase());
			entryString += File.pathSeparator;
			index++;
			value = projectNode.get(pathEntKinds + index, ""); //$NON-NLS-1$
		}
		return new String[] { pathString, contentString, entryString };
	}

	/**
	 * Firstly, add library to the Java build path if it's not there already,
	 * then mark the entry as being on the aspect path
	 * 
	 * @param project
	 * @param path
	 */
	private static void addAttribute(IProject project, String jarPath, int eKind, IClasspathAttribute attribute) {
		IJavaProject jp = JavaCore.create(project);

		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			int cpIndex = getIndexInBuildPathEntry(cp, jarPath);
			if (cpIndex >= 0) { // already on classpath
				// add attribute to classpath entry
				// if it doesn't already exist
				IClasspathEntry pathAdd = cp[cpIndex];
				// only add attribute if this element is not already on the path
				if (isAspectPathAttribute(attribute) ? !isOnAspectpath(pathAdd) : !isOnInpath(pathAdd)) {
					IClasspathAttribute[] attributes = pathAdd.getExtraAttributes();
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length + 1];
					System.arraycopy(attributes, 0, newattrib, 0, attributes.length);
					newattrib[attributes.length] = attribute;
					switch (pathAdd.getEntryKind()) {
					case IClasspathEntry.CPE_LIBRARY:
						pathAdd = JavaCore.newLibraryEntry(pathAdd.getPath(), pathAdd.getSourceAttachmentPath(),
								pathAdd.getSourceAttachmentRootPath(), pathAdd.getAccessRules(), newattrib,
								pathAdd.isExported());
						break;

					case IClasspathEntry.CPE_VARIABLE:
						pathAdd = JavaCore.newVariableEntry(pathAdd.getPath(), pathAdd.getSourceAttachmentPath(),
								pathAdd.getSourceAttachmentRootPath(), pathAdd.getAccessRules(), newattrib,
								pathAdd.isExported());
						break;

					case IClasspathEntry.CPE_CONTAINER:
						pathAdd = JavaCore.newContainerEntry(pathAdd.getPath(), pathAdd.getAccessRules(), newattrib,
								pathAdd.isExported());
						break;

					case IClasspathEntry.CPE_PROJECT:
						pathAdd = JavaCore.newProjectEntry(pathAdd.getPath(), pathAdd.getAccessRules(), true, newattrib,
								pathAdd.isExported());
						break;
					}

					cp[cpIndex] = pathAdd;
					jp.setRawClasspath(cp, null);
				}
			} else {
				addEntryToJavaBuildPath(jp, attribute, jarPath, eKind);
			}
		} catch (JavaModelException e) {
		}
	}

	private static String[] internalGetProjectPath(IProject project, IClasspathAttribute attribute,
			boolean useResolvedPath) {
		if (isAspectPathAttribute(attribute)) {
			if (shouldCheckOldStylePath(project, ASPECTPATH)) {
				String[] old = getOldProjectPath(project, true);
				if (old != null) {
					AJLog.log("Migrating aspect path settings for project " + project.getName()); //$NON-NLS-1$
					setProjectAspectPath(project, old[0], old[1], old[2]);
				}
				markOldStylePathAsRead(project, ASPECTPATH);
			}
		} else { // INPATH_ATTRIBUTE
			if (shouldCheckOldStylePath(project, INPATH)) {
				String[] old = getOldProjectPath(project, false);
				if (old != null) {
					AJLog.log("Migrating aspect path settings for project " + project.getName()); //$NON-NLS-1$
					setProjectInPath(project, old[0], old[1], old[2]);
				}
				markOldStylePathAsRead(project, INPATH);
			}
		}
		String pathString = ""; //$NON-NLS-1$
		String contentString = ""; //$NON-NLS-1$
		String entryString = ""; //$NON-NLS-1$

		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = javaProject.getRawClasspath();
			AJLog.log("internalGetProjectPath: iterating over classpath entries, size #"+(cp==null?0:cp.length));
			for (int i = 0; i < cp.length; i++) {
				IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
				AJLog.log("internalGetProjectPath: attributes on "+cp[i].getPath()+" are "+toString(attributes));
				boolean attributeFound = false;
				for (int j = 0; j < attributes.length; j++) {
					if (attributes[j].getName().equals(attribute.getName())) {
						attributeFound = true;
						List<IClasspathEntry> actualEntries = new ArrayList<IClasspathEntry>();

						if (useResolvedPath) {
							// this entry is on the path. must resolve it
							if (cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
								List<IClasspathEntry> containerEntries = resolveClasspathContainer(cp[i], project);
								// Bug 273770 - look for the
								// XXXPATH_RESTRICTION_ATTRIBUTE_NAME classpath
								// attribute
								Set<String> extraPathElements = findContainerRestrictions(cp[i],
										isAspectPathAttribute(attribute));
								if (extraPathElements != null && extraPathElements.size() > 0) {
									// must filter
									for (Iterator<IClasspathEntry> cpIter = containerEntries.iterator(); cpIter
											.hasNext();) {
										IClasspathEntry containerEntry = cpIter.next();
										if (!containsAsPathFragment(extraPathElements, containerEntry)) {
											cpIter.remove();
										}
									}
								}
								actualEntries.addAll(containerEntries);
							} else if (cp[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
								IProject requiredProj = project.getWorkspace().getRoot()
										.getProject(cp[i].getPath().makeRelative().toPortableString());
								if (!requiredProj.getName().equals(project.getName()) && requiredProj.exists()) {
									actualEntries.addAll(resolveDependentProjectClasspath(cp[i], requiredProj));
								}
							} else { // resolve the classpath variable
								IClasspathEntry resolved = JavaCore.getResolvedClasspathEntry(cp[i]);
								if (resolved != null) {
									if (resolved.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
										// must resolve the project
										actualEntries.addAll(resolveDependentProjectClasspath(resolved, project
												.getWorkspace().getRoot().getProject(resolved.getPath().toString())));
									} else {
										actualEntries.add(resolved);
									}
								}
							} // cp[i].getEntryKind()
						} else {
							actualEntries.add(cp[i]);
						} // useResolvedEntry

						for (IClasspathEntry actualEntry : actualEntries) {
							// we can get null for actualEntry if the raw entry
							// corresponds to
							// an unbound classpath variable
							if (actualEntry != null) {
								pathString += actualEntry.getPath().toPortableString() + File.pathSeparator;
								contentString += actualEntry.getContentKind() + File.pathSeparator;
								entryString += actualEntry.getEntryKind() + File.pathSeparator;
							}
						}
					} // attributes[j].equals(attribute)
				} // for (int j = 0; j < attributes.length; j++)

				// there is a special case that we must look inside the
				// classpath container for entries with
				// attributes if we are returning the resolved path and the
				// container itself isn't already
				// on the path.
				if (!attributeFound && useResolvedPath && cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
					List<IClasspathEntry> containerEntries = resolveClasspathContainer(cp[i], project);
					AJLog.log("internalGetProjectPath: Couldn't find it on first look, so looking here: "+containerEntries);
					for (IClasspathEntry containerEntry : containerEntries) {
						if (isOnPath(containerEntry, isAspectPathAttribute(attribute))) {
							AJLog.log("internalGetProjectPath: isOnPath  for "+containerEntry);
							pathString += containerEntry.getPath().toPortableString() + File.pathSeparator;
							contentString += containerEntry.getContentKind() + File.pathSeparator;
							entryString += containerEntry.getEntryKind() + File.pathSeparator;
						}
					} // for (Iterator cpIter = containerEntries.iterator();
						// cpIter.hasNext(); )
				} // !attributeFound && useResolvedPath && cp[i].getEntryKind()
					// == IClasspathEntry.CPE_CONTAINER
			} // for (int i = 0; i < cp.length; i++)
		} catch (JavaModelException e) {
		}
		return new String[] { pathString, contentString, entryString };
	}

	private static String toString(IClasspathAttribute[] attributes) {
		StringBuilder s = new StringBuilder();
		if (attributes != null) {
			for (IClasspathAttribute ca: attributes) {
				s.append(ca.getName()+"="+ca.getValue()+" ");
			}
		}
		return s.toString().trim();
	}

	public static boolean containsAsPathFragment(Set<String> extraPathElements, IClasspathEntry containerEntry) {
		if (extraPathElements.size() == 0) {
			return false;
		}
		String pathStr = containerEntry.getPath().toString();
		for (String extraPathStr : extraPathElements) {
			if (pathStr.indexOf(extraPathStr) != -1) {
				return true;
			}
		}
		return false;
	}

	private static Set<String> findContainerRestrictions(IClasspathEntry containerEntry,
			boolean isAspectPathAttribute) {
		if (containerEntry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
			return Collections.emptySet();
		}
		Set<String> restrictionPaths = new HashSet<String>();
		String restrictions = getRestriction(containerEntry,
				isAspectPathAttribute ? ASPECTPATH_RESTRICTION_ATTRIBUTE_NAME : INPATH_RESTRICTION_ATTRIBUTE_NAME);
		if (restrictions != null) {
			String[] restrictionsArr = restrictions.split(",");
			for (int j = 0; j < restrictionsArr.length; j++) {
				restrictionPaths.add(restrictionsArr[j].trim());
			}
			return restrictionPaths;
		} else {
			return null;
		}
	}

	private static void addAttribute(IJavaProject jp, IClasspathEntry entry, IClasspathAttribute attr) {
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].equals(entry)) {
					IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length + 1];
					System.arraycopy(attributes, 0, newattrib, 0, attributes.length);
					newattrib[attributes.length] = attr;
					switch (cp[i].getEntryKind()) {
					case IClasspathEntry.CPE_LIBRARY:
						cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(), cp[i].getSourceAttachmentPath(),
								cp[i].getSourceAttachmentRootPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_VARIABLE:
						cp[i] = JavaCore.newVariableEntry(cp[i].getPath(), cp[i].getSourceAttachmentPath(),
								cp[i].getSourceAttachmentRootPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_CONTAINER:
						cp[i] = JavaCore.newContainerEntry(cp[i].getPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_PROJECT:
						cp[i] = JavaCore.newProjectEntry(cp[i].getPath(), cp[i].getAccessRules(), true, newattrib,
								cp[i].isExported());
						break;

					}
				}
			}
			jp.setRawClasspath(cp, null);
		} catch (JavaModelException e) {
		}
	}

	public static void removeAttribute(IJavaProject jp, IClasspathEntry entry, IClasspathAttribute attr) {
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].equals(entry)) {
					IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length - 1];
					int count = 0;
					for (int j = 0; j < attributes.length; j++) {
						if (!attributes[j].getName().equals(attr.getName())) {
							newattrib[count++] = attributes[j];
						}
					}
					switch (cp[i].getEntryKind()) {
					case IClasspathEntry.CPE_LIBRARY:
						cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(), cp[i].getSourceAttachmentPath(),
								cp[i].getSourceAttachmentRootPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_VARIABLE:
						cp[i] = JavaCore.newVariableEntry(cp[i].getPath(), cp[i].getSourceAttachmentPath(),
								cp[i].getSourceAttachmentRootPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_CONTAINER:
						cp[i] = JavaCore.newContainerEntry(cp[i].getPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_PROJECT:
						cp[i] = JavaCore.newProjectEntry(cp[i].getPath(), cp[i].getAccessRules(), true, newattrib,
								cp[i].isExported());
						break;
					}
				}
			}
			jp.setRawClasspath(cp, null);
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Remove all occurrences of an attribute
	 * 
	 * @param javaProject
	 * @param attribute
	 */
	private static void removeAttribute(IJavaProject javaProject, IClasspathAttribute attribute) {
		try {
			IClasspathEntry[] cp = javaProject.getRawClasspath();
			boolean changed = false;
			for (int i = 0; i < cp.length; i++) {
				IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
				boolean found = false;
				for (int j = 0; !found && (j < attributes.length); j++) {
					if (attributes[j].getName().equals(attribute.getName())) {
						found = true;
					}
				}
				if (found) {
					changed = true;
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length - 1];
					int count = 0;
					for (int j = 0; j < attributes.length; j++) {
						if (!attributes[j].getName().equals(attribute.getName())) {
							newattrib[count++] = attributes[j];
						}
					}
					switch (cp[i].getEntryKind()) {
					case IClasspathEntry.CPE_LIBRARY:
						cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(), cp[i].getSourceAttachmentPath(),
								cp[i].getSourceAttachmentRootPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_VARIABLE:
						cp[i] = JavaCore.newVariableEntry(cp[i].getPath(), cp[i].getSourceAttachmentPath(),
								cp[i].getSourceAttachmentRootPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_CONTAINER:
						cp[i] = JavaCore.newContainerEntry(cp[i].getPath(), cp[i].getAccessRules(), newattrib,
								cp[i].isExported());
						break;

					case IClasspathEntry.CPE_PROJECT:
						cp[i] = JavaCore.newProjectEntry(cp[i].getPath(), cp[i].getAccessRules(), true, newattrib,
								cp[i].isExported());
						break;
					}
				}
			}
			if (changed) {
				javaProject.setRawClasspath(cp, null);
			}
		} catch (JavaModelException e) {
		}
	}

	private static int getIndexInBuildPathEntry(IClasspathEntry[] cp, String jarPath) {
		for (int i = 0; i < cp.length; i++) {
			String entry = cp[i].getPath().toPortableString();
			if (entry.equals(jarPath)) {
				return i;
			}
		}
		return -1;
	}

	private static void addEntryToJavaBuildPath(IJavaProject jp, IClasspathAttribute attribute, String path,
			int eKind) {
		IClasspathAttribute[] attributes = new IClasspathAttribute[] { attribute };
		try {
			IClasspathEntry[] originalCP = jp.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
			IClasspathEntry cp = null;
			if (eKind == IClasspathEntry.CPE_LIBRARY) {
				cp = JavaCore.newLibraryEntry(new Path(path), null, null, new IAccessRule[0], attributes, false);
			} else if (eKind == IClasspathEntry.CPE_VARIABLE) {
				cp = JavaCore.newVariableEntry(new Path(path), null, null, new IAccessRule[0], attributes, false);
			} else if (eKind == IClasspathEntry.CPE_CONTAINER) {
				cp = JavaCore.newContainerEntry(new Path(path), null, attributes, false);
			} else if (eKind == IClasspathEntry.CPE_PROJECT) {
				cp = JavaCore.newProjectEntry(new Path(path), null, true, attributes, false);
			}

			// Update the raw classpath with the new entry.
			if (cp != null) {
				System.arraycopy(originalCP, 0, newCP, 0, originalCP.length);
				newCP[originalCP.length] = cp;
				jp.setRawClasspath(newCP, new NullProgressMonitor());
			}
		} catch (JavaModelException e) {
		} catch (NumberFormatException e) {
		}
	}

	private static String toContentKind(String contentStr) {
		int content = 0;
		if (contentStr.equals("SOURCE")) { //$NON-NLS-1$
			content = IPackageFragmentRoot.K_SOURCE;
		} else if (contentStr.equals("BINARY")) { //$NON-NLS-1$
			content = IPackageFragmentRoot.K_BINARY;
		}
		return new Integer(content).toString();
	}

	private static String toEntryKind(String entryStr) {
		int entry = 0;
		if (entryStr.equals("SOURCE")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_SOURCE;
		} else if (entryStr.equals("LIBRARY")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_LIBRARY;
		} else if (entryStr.equals("PROJECT")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_PROJECT;
		} else if (entryStr.equals("VARIABLE")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_VARIABLE;
		} else if (entryStr.equals("CONTAINER")) { //$NON-NLS-1$
			entry = IClasspathEntry.CPE_CONTAINER;
		}
		return new Integer(entry).toString();
	}

	public static String getRestriction(IClasspathEntry pathEntry, String attributeName) {
		IClasspathAttribute[] attributes = pathEntry.getExtraAttributes();
		for (int i = 0; i < attributes.length; i++) {
			IClasspathAttribute attribute = attributes[i];
			if (attribute.getName().equals(attributeName)) {
				String extraStr = attribute.getValue();
				if (extraStr != null) {
					return extraStr;
				}
			}
		}

		return null;
	}

	/**
	 * adds the classpath attribute to the entry with the default value if it
	 * doesn't already exist else does nothing
	 */
	public static IClasspathEntry ensureHasAttribute(IClasspathEntry curr, String attributeName, String defaultVal) {
		int index = indexOfAttribute(curr.getExtraAttributes(), attributeName);
		if (index < 0) {
			IClasspathAttribute[] attrs = curr.getExtraAttributes();
			// must create a new entry with more extra attributes
			IClasspathAttribute newAttr = JavaCore.newClasspathAttribute(attributeName, defaultVal);
			IClasspathAttribute[] newAttrs;
			if (attrs == null || attrs.length == 0) {
				newAttrs = new IClasspathAttribute[] { newAttr };
			} else {
				newAttrs = new IClasspathAttribute[attrs.length + 1];
				System.arraycopy(attrs, 0, newAttrs, 0, attrs.length);
				newAttrs[attrs.length] = newAttr;
			}
			return copyContainerEntry(curr, newAttrs);
		} else {
			return curr;
		}
	}

	/**
	 * adds the classpath attribute to the entry with the default value if it
	 * doesn't already exist else does nothing
	 */
	public static IClasspathEntry ensureHasNoAttribute(IClasspathEntry curr, String attributeName) {
		int index = indexOfAttribute(curr.getExtraAttributes(), attributeName);
		if (index < 0) {
			return curr;
		} else {
			IClasspathAttribute[] attrs = curr.getExtraAttributes();
			// must create a new entry with more extra attributes
			IClasspathAttribute[] newAttrs = new IClasspathAttribute[attrs.length - 1];
			for (int i = 0, j = 0; i < newAttrs.length; i++) {
				if (i != index) {
					newAttrs[j] = attrs[i];
					j++;
				}
			}
			return copyContainerEntry(curr, newAttrs);
		}
	}

	public static IClasspathEntry copyContainerEntry(IClasspathEntry containerEntry, IClasspathAttribute[] extraAttrs) {
		return JavaCore.newContainerEntry(containerEntry.getPath(), containerEntry.getAccessRules(), extraAttrs,
				containerEntry.isExported());
	}

	private static int indexOfAttribute(IClasspathAttribute[] attrs, String attrName) {
		for (int i = 0; i < attrs.length; i++) {
			if (attrs[i].getName().equals(attrName)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Adds the classpath restriction to the given classpath entry. Returns the
	 * new classpath entry
	 */
	public static IClasspathEntry updatePathRestrictions(IClasspathEntry entry, String restrictionStr,
			String restrictionKind) {
		IClasspathAttribute[] attrs = entry.getExtraAttributes();
		int index = indexOfAttribute(attrs, restrictionKind);
		IClasspathAttribute newAttr = JavaCore.newClasspathAttribute(restrictionKind, restrictionStr);
		if (index >= 0) {
			// just replace
			attrs[index] = newAttr;
		} else {
			// must create a new entry with more extra attributes
			IClasspathAttribute[] newAttrs;
			if (attrs == null || attrs.length == 0) {
				newAttrs = new IClasspathAttribute[] { newAttr };
			} else {
				newAttrs = new IClasspathAttribute[attrs.length + 1];
				System.arraycopy(attrs, 0, newAttrs, 0, attrs.length);
				newAttrs[attrs.length] = newAttr;
			}
			entry = copyContainerEntry(entry, newAttrs);
		}
		return entry;
	}

	/**
	 * If this classpath entry's path already exists on the classpath, then it
	 * is replaced else it is added
	 */
	public static void updateClasspathEntry(IProject project, IClasspathEntry newEntry) {
		IJavaProject jProject = JavaCore.create(project);
		try {
			IClasspathEntry[] entries = jProject.getRawClasspath();
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (newEntry.getPath().equals(entry.getPath())) {
					entries[i] = newEntry;
					jProject.setRawClasspath(entries, null);
					return;
				}
			}

			// entry not found on classpath...add it
			IClasspathEntry[] newEntries = new IClasspathEntry[entries.length + 1];
			System.arraycopy(entries, 0, newEntries, 0, entries.length);
			newEntries[entries.length] = newEntry;
			jProject.setRawClasspath(newEntries, null);
		} catch (JavaModelException e) {
		}
	}
}
