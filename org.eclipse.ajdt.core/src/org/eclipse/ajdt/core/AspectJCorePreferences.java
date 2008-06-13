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
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Project specific settings. These used to be in the UI plugin, so the UI node
 * names have been kept for compatibility.
 */
public class AspectJCorePreferences {
			
    public static final String OPTION_AutobuildSuppressed = "org.eclipse.ajdt.ui.builder.autobuildSuppressed"; //$NON-NLS-1$

    
	private static final String ASPECTPATH_ATTRIBUTE_NAME = "org.eclipse.ajdt.aspectpath";
	
	private static final String INPATH_ATTRIBUTE_NAME = "org.eclipse.ajdt.inpath";

	public static final IClasspathAttribute ASPECTPATH_ATTRIBUTE = JavaCore.newClasspathAttribute(
			ASPECTPATH_ATTRIBUTE_NAME, "true"); //$NON-NLS-1$

	public static final IClasspathAttribute INPATH_ATTRIBUTE = JavaCore.newClasspathAttribute(
			INPATH_ATTRIBUTE_NAME, "true"); //$NON-NLS-1$

	public static final String OUT_JAR = "org.eclipse.ajdt.ui.outJar"; //$NON-NLS-1$

	public static final String ASPECTPATH = "org.eclipse.ajdt.ui.aspectPath"; //$NON-NLS-1$

	public static final String ASPECTPATH_CON_KINDS = "org.eclipse.ajdt.ui.aspectPath.contentKind"; //$NON-NLS-1$

	public static final String ASPECTPATH_ENT_KINDS = "org.eclipse.ajdt.ui.aspectPath.entryKind"; //$NON-NLS-1$

	public static final String INPATH = "org.eclipse.ajdt.ui.inPath"; //$NON-NLS-1$

	public static final String INPATH_CON_KINDS = "org.eclipse.ajdt.ui.inPath.contentKind"; //$NON-NLS-1$

	public static final String INPATH_ENT_KINDS = "org.eclipse.ajdt.ui.inPath.entryKind"; //$NON-NLS-1$

	public static String getProjectOutJar(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		return projectNode.get(OUT_JAR, ""); //$NON-NLS-1$
	}

	public static void setProjectOutJar(IProject project, String value) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		projectNode.put(OUT_JAR, value);
		if (value.length() == 0) {
			projectNode.remove(OUT_JAR);
		}
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public static void setProjectAspectPath(IProject project, String path,
			String cKinds, String eKinds) {
		IJavaProject javaProject = JavaCore.create(project);		
		removeAttribute(javaProject, ASPECTPATH_ATTRIBUTE);
		
		StringTokenizer pathTok = new StringTokenizer(path, File.pathSeparator);
		StringTokenizer eKindsTok = new StringTokenizer(eKinds, File.pathSeparator);
		int index = 1;
		while (pathTok.hasMoreTokens() && eKindsTok.hasMoreTokens()) {
			String entry = pathTok.nextToken();
			int eKind = Integer.parseInt(eKindsTok.nextToken());
			addToAspectPath(project,entry, eKind);
			index++;
		}
	}

	public static String[] getProjectAspectPath(IProject project) {
		String[] old = getOldProjectAspectPath(project);
		if (old != null) {
			AJLog.log("Migrating aspect path settings for project "+project.getName()); //$NON-NLS-1$
			setProjectAspectPath(project,old[0],old[1],old[2]);
			removeOldAspectPathSetting(project);
		}
		String pathString = ""; //$NON-NLS-1$
		String contentString = ""; //$NON-NLS-1$
		String entryString = ""; //$NON-NLS-1$
	
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = javaProject.getResolvedClasspath(true);
			for (int i = 0; i < cp.length; i++) {
				IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
				for (int j = 0; j < attributes.length; j++) {
					if (attributes[j].equals(ASPECTPATH_ATTRIBUTE)) { //$NON-NLS-1$
						pathString += cp[i].getPath().toPortableString()
								+ File.pathSeparator;
						contentString += cp[i].getContentKind()
								+ File.pathSeparator;
						entryString += cp[i].getEntryKind()
								+ File.pathSeparator;
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return new String[] { pathString, contentString, entryString };
	}

	public static void addToAspectPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		addAttribute(jp,entry,AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
	}

	public static void removeFromAspectPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		removeAttribute(jp,entry,AspectJCorePreferences.ASPECTPATH_ATTRIBUTE);
	}

	/**
	 * Firstly, add library to the Java build path if it's not there already,
	 * then mark the entry as being on the aspect path
	 * @param project
	 * @param path
	 */
	public static void addToAspectPath(IProject project, String jarPath, int eKind) {
		IJavaProject jp = JavaCore.create(project);
		if (isOnBuildPath(jp, jarPath)) { // already on classpath
			// add attribute to classpath entry
			try {
				IClasspathEntry[] cp = jp.getRawClasspath();
				for (int i = 0; i < cp.length; i++) {
					if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
							|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)) {
						String entry = JavaCore
								.getResolvedClasspathEntry(cp[i]).getPath()
								.toPortableString();
						if (entry.equals(jarPath)) {
							IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
							IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length + 1];
							System.arraycopy(attributes, 0, newattrib, 0, attributes.length);
							newattrib[attributes.length] = AspectJCorePreferences.ASPECTPATH_ATTRIBUTE;
							if (cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
								cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(),
									cp[i].getSourceAttachmentPath(), cp[i]
											.getSourceAttachmentRootPath(),
									cp[i].getAccessRules(), newattrib, cp[i]
											.isExported());
							} else { // kind=variable
								cp[i] = JavaCore.newVariableEntry(cp[i].getPath(),
										cp[i].getSourceAttachmentPath(), cp[i]
												.getSourceAttachmentRootPath(),
										cp[i].getAccessRules(), newattrib, cp[i]
												.isExported());
							}
						}
					}
				}
				jp.setRawClasspath(cp, null);
			} catch (JavaModelException e) {
			}
		} else {
			addEntryToJavaBuildPath(jp, ASPECTPATH_ATTRIBUTE, jarPath, eKind); //$NON-NLS-1$
		}
	}

	public static boolean isOnAspectpath(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (int j = 0; j < attributes.length; j++) {
			if (attributes[j].equals(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE)) {
				return true;								
			}
		}
		return false;
	}
	
	public static boolean isOnAspectpath(IProject project, String jarPath) {
		IJavaProject jp = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)) {
					String entry = JavaCore.getResolvedClasspathEntry(cp[i])
							.getPath().toPortableString();
					if (entry.equals(jarPath)) {
						IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
						for (int j = 0; j < attributes.length; j++) {
							if (attributes[j].equals(AspectJCorePreferences.ASPECTPATH_ATTRIBUTE)) {
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

	private static String[] getOldProjectAspectPath(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		String pathString = ""; //$NON-NLS-1$
		int index = 1;
		String value = projectNode.get(ASPECTPATH + index, ""); //$NON-NLS-1$
		if (value.length() == 0) {
			return null;
		}
		while (value.length() > 0) {
			pathString += value;
			pathString += File.pathSeparator;
			index++;
			value = projectNode.get(ASPECTPATH + index, ""); //$NON-NLS-1$
		}
	
		String contentString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(ASPECTPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			contentString += toContentKind(value.toUpperCase());
			contentString += File.pathSeparator;
			index++;
			value = projectNode.get(ASPECTPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		}
	
		String entryString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(ASPECTPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			entryString += toEntryKind(value.toUpperCase());
			entryString += File.pathSeparator;
			index++;
			value = projectNode.get(ASPECTPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		}
		return new String[] { pathString, contentString, entryString };
	}

	private static void removeOldAspectPathSetting(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		int index = 1;
		while (projectNode.get(ASPECTPATH + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH + index);
			index++;
		}
		index = 1;
		while (projectNode.get(ASPECTPATH_CON_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH_CON_KINDS + index);
			index++;
		}
		index = 1;
		while (projectNode.get(ASPECTPATH_ENT_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH_ENT_KINDS + index);
			index++;
		}
	
		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	public static String[] getProjectInPath(IProject project) {
		String[] old = getOldProjectInPath(project);
		if (old != null) {
			AJLog.log("Migrating inpath settings for project "+project.getName()); //$NON-NLS-1$
			setProjectInPath(project,old[0],old[1],old[2]);
			removeOldInPathSetting(project);
		}
		String pathString = ""; //$NON-NLS-1$
		String contentString = ""; //$NON-NLS-1$
		String entryString = ""; //$NON-NLS-1$
	
		IJavaProject javaProject = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = javaProject.getResolvedClasspath(true);
			for (int i = 0; i < cp.length; i++) {
				IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
				for (int j = 0; j < attributes.length; j++) {
					if (attributes[j].equals(INPATH_ATTRIBUTE)) {
						pathString += cp[i].getPath().toPortableString() + File.pathSeparator;
						contentString += cp[i].getContentKind() + File.pathSeparator;
						entryString += cp[i].getEntryKind() + File.pathSeparator;
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return new String[] { pathString, contentString, entryString };
	}

	public static void setProjectInPath(IProject project, String path,
			String cKinds, String eKinds) {
		IJavaProject javaProject = JavaCore.create(project);
		removeAttribute(javaProject, INPATH_ATTRIBUTE);
	
		StringTokenizer pathTok = new StringTokenizer(path, File.pathSeparator);
		StringTokenizer eKindsTok = new StringTokenizer(eKinds, File.pathSeparator);
		int index = 1;
		while (pathTok.hasMoreTokens() && eKindsTok.hasMoreTokens()) {
			String entry = pathTok.nextToken();
			int eKind = Integer.parseInt(eKindsTok.nextToken());
			addToInPath(project,entry,eKind);
			index++;
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
					if (cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(),
								cp[i].getSourceAttachmentPath(), cp[i]
										.getSourceAttachmentRootPath(),
								cp[i].getAccessRules(), newattrib, cp[i]
										.isExported());
					} else if (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
						cp[i] = JavaCore.newVariableEntry(cp[i].getPath(),
								cp[i].getSourceAttachmentPath(), cp[i]
										.getSourceAttachmentRootPath(),
								cp[i].getAccessRules(), newattrib, cp[i]
										.isExported());						
					}
				}
			}
			jp.setRawClasspath(cp, null);
		} catch (JavaModelException e) {
		}
	}
	
	private static void removeAttribute(IJavaProject jp, IClasspathEntry entry, IClasspathAttribute attr) {
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].equals(entry)) {
					IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
					IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length - 1];
					int count = 0;
					for (int j = 0; j < attributes.length; j++) {
						if (!attributes[j].equals(attr)) {
							newattrib[count++] = attributes[j];
						}
					}
					if (cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(),
								cp[i].getSourceAttachmentPath(), cp[i]
										.getSourceAttachmentRootPath(),
								cp[i].getAccessRules(), newattrib, cp[i]
										.isExported());
					} else if (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
						cp[i] = JavaCore.newVariableEntry(cp[i].getPath(),
								cp[i].getSourceAttachmentPath(), cp[i]
										.getSourceAttachmentRootPath(),
								cp[i].getAccessRules(), newattrib, cp[i]
										.isExported());						
					}
				}
			}
			jp.setRawClasspath(cp, null);
		} catch (JavaModelException e) {
		}
	}
	
	public static void addToInPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		addAttribute(jp,entry,AspectJCorePreferences.INPATH_ATTRIBUTE);
	}
	
	public static void removeFromInPath(IProject project, IClasspathEntry entry) {
		IJavaProject jp = JavaCore.create(project);
		removeAttribute(jp,entry,AspectJCorePreferences.INPATH_ATTRIBUTE);
	}
	
	/**
	 * Firstly, add library to the Java build path if it's not there already,
	 * then mark the entry as being on the aspect path
	 * @param project
	 * @param path
	 */
	public static void addToInPath(IProject project, String jarPath, int eKind) {
		IJavaProject jp = JavaCore.create(project);
		if (isOnBuildPath(jp, jarPath)) { // already on classpath
			// add attribute to classpath entry
			try {
				IClasspathEntry[] cp = jp.getRawClasspath();
				for (int i = 0; i < cp.length; i++) {
					if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)) {
						String entry = JavaCore
								.getResolvedClasspathEntry(cp[i]).getPath()
								.toPortableString();
						if (entry.equals(jarPath)) {
							IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
							IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length + 1];
							System.arraycopy(attributes, 0, newattrib, 0, attributes.length);
							newattrib[attributes.length] = AspectJCorePreferences.INPATH_ATTRIBUTE;
							if (cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
								cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(),
									cp[i].getSourceAttachmentPath(), cp[i]
											.getSourceAttachmentRootPath(),
									cp[i].getAccessRules(), newattrib, cp[i]
											.isExported());
							} else { // kind=variable
								cp[i] = JavaCore.newVariableEntry(cp[i].getPath(),
										cp[i].getSourceAttachmentPath(), cp[i]
												.getSourceAttachmentRootPath(),
										cp[i].getAccessRules(), newattrib, cp[i]
												.isExported());
							}
						}
					}
				}
				jp.setRawClasspath(cp, null);
			} catch (JavaModelException e) {
			}
		} else {
			addEntryToJavaBuildPath(jp, INPATH_ATTRIBUTE, jarPath, eKind); //$NON-NLS-1$
		}
	}

	public static boolean isOnInpath(IClasspathEntry entry) {
		IClasspathAttribute[] attributes = entry.getExtraAttributes();
		for (int j = 0; j < attributes.length; j++) {
			if (attributes[j].equals(AspectJCorePreferences.INPATH_ATTRIBUTE)) {
				return true;								
			}
		}
		return false;
	}
	
	public static boolean isOnInpath(IProject project, String jarPath) {
		IJavaProject jp = JavaCore.create(project);
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)) {
					String entry = JavaCore.getResolvedClasspathEntry(cp[i])
							.getPath().toPortableString();
					if (entry.equals(jarPath)) {
						IClasspathAttribute[] attributes = cp[i].getExtraAttributes();
						for (int j = 0; j < attributes.length; j++) {
							if (attributes[j].equals(AspectJCorePreferences.INPATH_ATTRIBUTE)) {
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

	private static String[] getOldProjectInPath(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		String pathString = ""; //$NON-NLS-1$
		int index = 1;
		String value = projectNode.get(INPATH + index, ""); //$NON-NLS-1$
		if (value.length() == 0) {
			return null;
		}
		while (value.length() > 0) {
			pathString += value;
			pathString += File.pathSeparator;
			index++;
			value = projectNode.get(INPATH + index, ""); //$NON-NLS-1$
		}
	
		String contentString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(INPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			contentString += toContentKind(value.toUpperCase());
			contentString += File.pathSeparator;
			index++;
			value = projectNode.get(INPATH_CON_KINDS + index, ""); //$NON-NLS-1$
		}
	
		String entryString = ""; //$NON-NLS-1$
		index = 1;
		value = projectNode.get(INPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		while (value.length() > 0) {
			entryString += toEntryKind(value.toUpperCase());
			entryString += File.pathSeparator;
			index++;
			value = projectNode.get(INPATH_ENT_KINDS + index, ""); //$NON-NLS-1$
		}
		return new String[] { pathString, contentString, entryString };
	}

	private static void removeOldInPathSetting(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		int index = 1;
		while (projectNode.get(INPATH + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH + index);
			index++;
		}
		index = 1;
		while (projectNode.get(INPATH_CON_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH_CON_KINDS + index);
			index++;
		}
		index = 1;
		while (projectNode.get(INPATH_ENT_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH_ENT_KINDS + index);
			index++;
		}

		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	/**
	 * Remove all occurrences of an attribute
	 * @param javaProject
	 * @param attribute
	 */
	private static void removeAttribute(IJavaProject javaProject,
			IClasspathAttribute attribute) {
		try {
			IClasspathEntry[] cp = javaProject.getRawClasspath();
			boolean changed = false;
			for (int i = 0; i < cp.length; i++) {
				if (cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IClasspathAttribute[] attributes = cp[i]
							.getExtraAttributes();
					boolean found = false;
					for (int j = 0; !found && (j < attributes.length); j++) {
						if (attributes[j].equals(attribute)) {
							found = true;
						}
					}
					if (found) {
						IClasspathAttribute[] newattrib = new IClasspathAttribute[attributes.length - 1];
						int count = 0;
						for (int j = 0; j < attributes.length; j++) {
							if (!attributes[j]
									.equals(attribute)) {
								newattrib[count++] = attributes[j];
							}
						}
						cp[i] = JavaCore.newLibraryEntry(cp[i].getPath(), cp[i]
								.getSourceAttachmentPath(), cp[i]
								.getSourceAttachmentRootPath(), cp[i]
								.getAccessRules(), newattrib, cp[i]
								.isExported());
						changed = true;
					}
				}
			}
			if (changed) {
				javaProject.setRawClasspath(cp, null);
			}
		} catch (JavaModelException e) {
		}
	}
	
	private static boolean isOnBuildPath(IJavaProject jp, String jarPath) {
		try {
			IClasspathEntry[] cp = jp.getRawClasspath();
			for (int i = 0; i < cp.length; i++) {
				if ((cp[i].getEntryKind() == IClasspathEntry.CPE_LIBRARY)
						|| (cp[i].getEntryKind() == IClasspathEntry.CPE_VARIABLE)) {
					String entry = JavaCore
							.getResolvedClasspathEntry(cp[i]).getPath()
							.toPortableString();
					if (entry.equals(jarPath)) {
						return true;
					}
				}
			}
		} catch (JavaModelException e) {
		}
		return false;
	}
	
	private static void addEntryToJavaBuildPath(IJavaProject jp,
			IClasspathAttribute attribute, String path, int eKind) {
		IClasspathAttribute[] attributes = new IClasspathAttribute[] { attribute };
		try {
			IClasspathEntry[] originalCP = jp.getRawClasspath();
			IClasspathEntry[] newCP = new IClasspathEntry[originalCP.length + 1];
			IClasspathEntry cp = null;
			if (eKind == IClasspathEntry.CPE_LIBRARY) {
				cp = JavaCore.newLibraryEntry(
						new Path(path), null, null, new IAccessRule[0], attributes, false);
			} else if (eKind == IClasspathEntry.CPE_VARIABLE) {
				cp = JavaCore.newVariableEntry(
						new Path(path), null, null, new IAccessRule[0], attributes, false);
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
	
	

    public static boolean isAutobuildSuppressed() {
        Preferences store = AspectJPlugin.getDefault()
                .getPluginPreferences();
        return store.getBoolean(OPTION_AutobuildSuppressed);
    }

    static public void setAutobuildSuppressed(boolean done) {
        Preferences store = AspectJPlugin.getDefault()
                .getPluginPreferences();
        store.setValue(OPTION_AutobuildSuppressed, done);
    }

}
