/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.launching;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.ajdt.internal.core.AJDTEventTrace;
import org.eclipse.ajdt.internal.ui.ajde.BuildOptionsAdapter;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.internal.debug.ui.classpath.ClasspathEntry;
import org.eclipse.jdt.internal.debug.ui.classpath.IClasspathEntry;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Utitlites for manipulating the runtime classpaths for launch configurations
 */
public class LaunchConfigurationClasspathUtils {

	/**
	 * Returns the classpath entries currently specified by the given model
	 */
	public static IRuntimeClasspathEntry[] getCurrentClasspath(
			AJClasspathModel fModel) {
		IClasspathEntry[] boot = fModel.getEntries(AJClasspathModel.BOOTSTRAP);
		IClasspathEntry[] user = fModel.getEntries(AJClasspathModel.USER);
		IClasspathEntry[] aspectPath = fModel
				.getEntries(AJClasspathModel.ASPECTPATH);
		List entries = new ArrayList(boot.length + user.length
				+ aspectPath.length);
		IClasspathEntry bootEntry;
		IRuntimeClasspathEntry entry;
		for (int i = 0; i < boot.length; i++) {
			bootEntry = boot[i];
			entry = null;
			if (bootEntry instanceof ClasspathEntry) {
				entry = ((ClasspathEntry) bootEntry).getDelegate();
			} else if (bootEntry instanceof IRuntimeClasspathEntry) {
				entry = (IRuntimeClasspathEntry) boot[i];
			}
			if (entry != null) {
				if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
					entry
							.setClasspathProperty(IRuntimeClasspathEntry.BOOTSTRAP_CLASSES);
				}
				entries.add(entry);
			}
		}
		IClasspathEntry userEntry;
		for (int i = 0; i < user.length; i++) {
			userEntry = user[i];
			entry = null;
			if (userEntry instanceof ClasspathEntry) {
				entry = ((ClasspathEntry) userEntry).getDelegate();
			} else if (userEntry instanceof IRuntimeClasspathEntry) {
				entry = (IRuntimeClasspathEntry) user[i];
			}
			if (entry != null) {
				entry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
				entries.add(entry);
			}
		}
		IClasspathEntry aspectEntry;
		for (int i = 0; i < aspectPath.length; i++) {
			aspectEntry = aspectPath[i];
			entry = null;
			if (aspectEntry instanceof ClasspathEntry) {
				entry = ((ClasspathEntry) aspectEntry).getDelegate();
			} else if (aspectEntry instanceof IRuntimeClasspathEntry) {
				entry = (IRuntimeClasspathEntry) aspectPath[i];
			}
			if (entry != null) {
				entry.setClasspathProperty(IRuntimeClasspathEntry.USER_CLASSES);
				entries.add(entry);
			}
		}
		return (IRuntimeClasspathEntry[]) entries
				.toArray(new IRuntimeClasspathEntry[entries.size()]);
	}

	/**
	 * Create an AJClasspath model from the given configuration
	 */
	public static AJClasspathModel createClasspathModel(
			ILaunchConfiguration configuration) throws CoreException {
		boolean configIsDirty = false;
		AJClasspathModel fModel = new AJClasspathModel();
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);
		IRuntimeClasspathEntry entry;

		String projectName = configuration.getAttribute(
				IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				(String) null);
		IRuntimeClasspathEntry[] aspectEntries = null;
		if (projectName != null && !projectName.trim().equals("")) {
			IProject project = AspectJUIPlugin.getWorkspace().getRoot()
					.getProject(projectName);
			aspectEntries = LaunchConfigurationClasspathUtils
					.getAspectpath(project);
		}
		if (aspectEntries != null) {
			for (int i = 0; i < aspectEntries.length; i++) {
				fModel.addEntry(AJClasspathModel.ASPECTPATH, aspectEntries[i]);
			}
		} else {
			aspectEntries = new IRuntimeClasspathEntry[0];
		}
		boolean[] allAspectEntriesOnClasspath = new boolean[aspectEntries.length];
		for (int i = 0; i < entries.length; i++) {
			entry = entries[i];
			switch (entry.getClasspathProperty()) {
			case IRuntimeClasspathEntry.USER_CLASSES:
				boolean isAspectPathEntry = false;
				for (int j = 0; j < aspectEntries.length; j++) {
					if (aspectEntries[j].equals(entry)) {
						isAspectPathEntry = true;
						allAspectEntriesOnClasspath[j] = true;
						break;
					}
				}
				if (!isAspectPathEntry) {
					fModel.addEntry(AJClasspathModel.USER, entry);
				}
				break;
			default:
				fModel.addEntry(AJClasspathModel.BOOTSTRAP, entry);
				break;
			}
		}
		return fModel;
	}

	/**
	 * Get the AspectPath for a project
	 */
	public static IRuntimeClasspathEntry[] getAspectpath(IProject project)
			throws CoreException {
		List result = new ArrayList();
		String paths = project
				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH);
		String cKinds = project
				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH_CON_KINDS);
		String eKinds = project
				.getPersistentProperty(BuildOptionsAdapter.ASPECTPATH_ENT_KINDS);

		if ((paths != null && paths.length() > 0)
				&& (cKinds != null && cKinds.length() > 0)
				&& (eKinds != null && eKinds.length() > 0)) {
			StringTokenizer sTokPaths = new StringTokenizer(paths,
					File.pathSeparator);
			StringTokenizer sTokCKinds = new StringTokenizer(cKinds,
					File.pathSeparator);
			StringTokenizer sTokEKinds = new StringTokenizer(eKinds,
					File.pathSeparator);
			if ((sTokPaths.countTokens() == sTokCKinds.countTokens())
					&& (sTokPaths.countTokens() == sTokEKinds.countTokens())) {
				while (sTokPaths.hasMoreTokens()) {
					org.eclipse.jdt.core.IClasspathEntry entry = new org.eclipse.jdt.internal.core.ClasspathEntry(
							Integer.parseInt(sTokCKinds.nextToken()), // content
							// kind
							Integer.parseInt(sTokEKinds.nextToken()), // entry
							// kind
							new Path(sTokPaths.nextToken()), // path
							new IPath[] {}, // inclusion patterns
							new IPath[] {}, // exclusion patterns
							null, // src attachment path
							null, // src attachment root path
							null, // output location
							false); // is exported ?
					result.add(new RuntimeClasspathEntry(entry));
				}// end while
			}// end if string token counts tally
		}// end if we have something valid to work with

		if (result.size() > 0) {
			return (IRuntimeClasspathEntry[]) result
					.toArray(new IRuntimeClasspathEntry[0]);
		} else {
			return null;
		}
	}

	/**
	 * Returns whether the specified classpath is equivalent to the default
	 * classpath for this configuration.
	 * 
	 * @param classpath
	 *            classpath to compare to default
	 * @param configuration
	 *            original configuration
	 * @return whether the specified classpath is equivalent to the default
	 *         classpath for this configuration
	 */
	public static boolean isDefaultClasspath(
			IRuntimeClasspathEntry[] classpath,
			ILaunchConfiguration configuration) {
		try {
			ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
			wc.setAttribute(
					IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
					true);
			IRuntimeClasspathEntry[] entries = JavaRuntime
					.computeUnresolvedRuntimeClasspath(wc);
			if (classpath.length == entries.length) {
				for (int i = 0; i < entries.length; i++) {
					IRuntimeClasspathEntry entry = entries[i];
					if (!entry.equals(classpath[i])) {
						return false;
					}
				}
				return true;
			}
			return false;
		} catch (CoreException e) {
			return false;
		}
	}

	/**
	 * Updates the classpath for a launch configuration to ensure that it
	 * contains the aspectpath. NB. Will not add the aspect path a second time
	 * if the classpath already contains it.
	 */
	public static void addAspectPathToClasspath(
			ILaunchConfiguration configuration) {
		ILaunchConfigurationWorkingCopy wc;
		try {
			wc = configuration.getWorkingCopy();
			AJClasspathModel model = createClasspathModel(configuration);
			IRuntimeClasspathEntry[] classpath = getCurrentClasspath(model);
			boolean def = isDefaultClasspath(classpath, wc);
			if (def) {
				wc.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
						(String) null);
				wc.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
						(String) null);
			} else {
				wc.setAttribute(
						IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH,
						false);
				try {
					List mementos = new ArrayList(classpath.length);
					for (int i = 0; i < classpath.length; i++) {
						IRuntimeClasspathEntry entry = classpath[i];
						mementos.add(entry.getMemento());
					}
					wc.setAttribute(
							IJavaLaunchConfigurationConstants.ATTR_CLASSPATH,
							mementos);
				} catch (CoreException e) {
					AJDTEventTrace.generalEvent(e.getMessage());
				}
				wc.doSave();
			}
		} catch (CoreException e1) {
			AJDTEventTrace.generalEvent(e1.getMessage());
		}
	}

}
