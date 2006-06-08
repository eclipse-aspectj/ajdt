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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Project specific settings. These used to be in the UI plugin, so the UI node
 * names have been kept for compatibility.
 */
public class AspectJCorePreferences {
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

	public static String[] getProjectAspectPath(IProject project) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		String pathString = ""; //$NON-NLS-1$
		int index = 1;
		String value = projectNode.get(ASPECTPATH + index, ""); //$NON-NLS-1$
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

	public static void setProjectAspectPath(IProject project, String path,
			String cKinds, String eKinds) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);

		StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
		int index = 1;
		while (tok.hasMoreTokens()) {
			projectNode.put(ASPECTPATH + index, tok.nextToken());
			index++;
		}
		while (projectNode.get(ASPECTPATH + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH + index);
			index++;
		}

		tok = new StringTokenizer(cKinds, File.pathSeparator);
		index = 1;
		while (tok.hasMoreTokens()) {
			projectNode.put(ASPECTPATH_CON_KINDS + index, fromContentKind(tok
					.nextToken()));
			index++;
		}
		while (projectNode.get(ASPECTPATH_CON_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(ASPECTPATH_CON_KINDS + index);
			index++;
		}

		tok = new StringTokenizer(eKinds, File.pathSeparator);
		index = 1;
		while (tok.hasMoreTokens()) {
			projectNode.put(ASPECTPATH_ENT_KINDS + index, fromEntryKind(tok
					.nextToken()));
			index++;
		}
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
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);
		String pathString = ""; //$NON-NLS-1$
		int index = 1;
		String value = projectNode.get(INPATH + index, ""); //$NON-NLS-1$
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

	public static void setProjectInPath(IProject project, String path,
			String cKinds, String eKinds) {
		IScopeContext projectScope = new ProjectScope(project);
		IEclipsePreferences projectNode = projectScope
				.getNode(AspectJPlugin.UI_PLUGIN_ID);

		StringTokenizer tok = new StringTokenizer(path, File.pathSeparator);
		int index = 1;
		while (tok.hasMoreTokens()) {
			projectNode.put(INPATH + index, tok.nextToken());
			index++;
		}
		while (projectNode.get(INPATH + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH + index);
			index++;
		}

		tok = new StringTokenizer(cKinds, File.pathSeparator);
		index = 1;
		while (tok.hasMoreTokens()) {
			projectNode.put(INPATH_CON_KINDS + index, fromContentKind(tok
					.nextToken()));
			index++;
		}
		while (projectNode.get(INPATH_CON_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH_CON_KINDS + index);
			index++;
		}

		tok = new StringTokenizer(eKinds, File.pathSeparator);
		index = 1;
		while (tok.hasMoreTokens()) {
			projectNode.put(INPATH_ENT_KINDS + index, fromEntryKind(tok
					.nextToken()));
			index++;
		}
		while (projectNode.get(INPATH_ENT_KINDS + index, "").length() > 0) { //$NON-NLS-1$
			projectNode.remove(INPATH_ENT_KINDS + index);
			index++;
		}

		try {
			projectNode.flush();
		} catch (BackingStoreException e) {
		}
	}

	private static String fromContentKind(String cKinds) {
		String contentStr = "unknown"; //$NON-NLS-1$
		try {
			int content = Integer.parseInt(cKinds);
			if (content == IPackageFragmentRoot.K_SOURCE) {
				contentStr = "SOURCE"; //$NON-NLS-1$
			} else if (content == IPackageFragmentRoot.K_BINARY) {
				contentStr = "BINARY"; //$NON-NLS-1$
			}
		} catch (NumberFormatException e) {
		}
		return contentStr;
	}

	private static String fromEntryKind(String eKinds) {
		String entryStr = "unknown"; //$NON-NLS-1$
		try {
			int entry = Integer.parseInt(eKinds);
			if (entry == IClasspathEntry.CPE_SOURCE) {
				entryStr = "SOURCE"; //$NON-NLS-1$
			} else if (entry == IClasspathEntry.CPE_LIBRARY) {
				entryStr = "LIBRARY"; //$NON-NLS-1$
			} else if (entry == IClasspathEntry.CPE_PROJECT) {
				entryStr = "PROJECT"; //$NON-NLS-1$
			} else if (entry == IClasspathEntry.CPE_VARIABLE) {
				entryStr = "VARIABLE"; //$NON-NLS-1$
			} else if (entry == IClasspathEntry.CPE_CONTAINER) {
				entryStr = "CONTAINER"; //$NON-NLS-1$
			}
		} catch (NumberFormatException e) {
		}
		return entryStr;
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
}
