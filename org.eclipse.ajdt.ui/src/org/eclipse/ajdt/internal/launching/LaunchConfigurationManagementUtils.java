/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Sian January - initial version
 * ...
 **********************************************************************/

package org.eclipse.ajdt.internal.launching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ajdt.core.AJLog;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.launching.RuntimeClasspathEntry;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * Utilities for managing AspectJ launch configurations
 */
public class LaunchConfigurationManagementUtils {

	/**
	 * Update the aspect path for launch configurations relating to the given
	 * project
	 * 
	 * @param project -
	 *            the project
	 * @param existingAspectPathEntries -
	 *            current aspect path
	 * @param newAspectPathEntries -
	 *            new aspect path
	 */
	public static void updateAspectPaths(IJavaProject project,
			List existingAspectPathEntries, List newAspectPathEntries) {
		try {
			String projectName = project.getElementName();
			List configs = getLaunchConfigsForProject(projectName);
			for (Iterator iter = configs.iterator(); iter.hasNext();) {
				ILaunchConfiguration configuration = (ILaunchConfiguration) iter
						.next();
				IRuntimeClasspathEntry[] entries = JavaRuntime
						.computeUnresolvedRuntimeClasspath(configuration);
				List entriesAsList = new ArrayList(Arrays.asList(entries));

				for (Iterator iterator = existingAspectPathEntries.iterator(); iterator
						.hasNext();) {
					IClasspathEntry entryToRemove = ((CPListElement) iterator
							.next()).getClasspathEntry();

					for (Iterator iterator2 = entriesAsList.iterator(); iterator2
							.hasNext();) {
						IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator2
								.next();
						if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
							if (entryToRemove.equals(entry.getClasspathEntry())) {
								iterator2.remove();
								break;
							}
						}
					}
				}
				for (Iterator iterator = newAspectPathEntries.iterator(); iterator
						.hasNext();) {
					IClasspathEntry newEntry = ((CPListElement) iterator.next())
							.getClasspathEntry();
					if (newEntry.getEntryKind() != IClasspathEntry.CPE_CONTAINER) {
					    entriesAsList.add(new RuntimeClasspathEntry(newEntry));
					} else {
					    IClasspathContainer container = 
					        JavaCore.getClasspathContainer(newEntry.getPath(), project);
					    if (container != null) {
    					    IClasspathEntry[] containerEntries = container.getClasspathEntries();
    					    for (int i = 0; i < containerEntries.length; i++) {
    					        entriesAsList.add(new RuntimeClasspathEntry(containerEntries[i]));
    					    }
					    }
					}
				}
				IRuntimeClasspathEntry[] updatedEntries = (IRuntimeClasspathEntry[]) entriesAsList
						.toArray(new IRuntimeClasspathEntry[0]);
				updateConfigurationClasspath(configuration, updatedEntries);
			}
		} catch (CoreException cEx) {
			AJLog.log(cEx.getMessage());
		}

	}

	/**
	 * Update the outjar for launch configurations relating to the given
	 * project
	 * 
	 * @param project -
	 *            the project
	 * @param oldOutJar -
	 *            current output jar, or null if none exists
	 * @param newOutJar -
	 *            new output jar or null if none exists
	 */
	public static void updateOutJar(IJavaProject project, IClasspathEntry oldOutJar, IClasspathEntry newOutJar) {
		try {
			String projectName = project.getElementName();
			List configs = getLaunchConfigsForProject(projectName);
			for (Iterator iter = configs.iterator(); iter.hasNext();) {
				ILaunchConfiguration configuration = (ILaunchConfiguration) iter
						.next();
				IRuntimeClasspathEntry[] entries = JavaRuntime
						.computeUnresolvedRuntimeClasspath(configuration);
				List entriesAsList = new ArrayList(Arrays.asList(entries));
	
				if(oldOutJar != null) {
					for (Iterator iterator2 = entriesAsList.iterator(); iterator2
							.hasNext();) {
						IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator2
								.next();
						if (entry.getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
							if (oldOutJar.equals(entry.getClasspathEntry())) {
								iterator2.remove();
								break;
							}
						}
					}
				}
				if(newOutJar != null) {	
					entriesAsList.add(new RuntimeClasspathEntry(newOutJar));
				}
				IRuntimeClasspathEntry[] updatedEntries = (IRuntimeClasspathEntry[]) entriesAsList
						.toArray(new IRuntimeClasspathEntry[0]);
				updateConfigurationClasspath(configuration, updatedEntries);
			}
		} catch (CoreException cEx) {
			AJLog.log(cEx.getMessage());
		}
	}
	
	/**
	 * Update and save a new classpath for the given launch configuration
	 * 
	 * @param configuration
	 * @param updatedEntries
	 */
	private static void updateConfigurationClasspath(
			ILaunchConfiguration configuration,
			IRuntimeClasspathEntry[] classpath) {
		ILaunchConfigurationWorkingCopy wc;
		try {
			wc = configuration.getWorkingCopy();

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
				AJLog.log(e.getMessage());
			}
			wc.doSave();
		} catch (CoreException e1) {
			AJLog.log(e1.getMessage());
		}
	}

	/**
	 * Get all the launch configurations for types in the given project
	 * 
	 * @param projectName
	 */
	private static List getLaunchConfigsForProject(String projectName) {
		ILaunchConfigurationType configType = AspectJApplicationLaunchShortcut
				.getAJConfigurationType();
		List candidateConfigs = new ArrayList();
		try {
			ILaunchConfiguration[] configs = DebugPlugin.getDefault()
					.getLaunchManager().getLaunchConfigurations(configType);
			candidateConfigs = new ArrayList(configs.length);
			for (int i = 0; i < configs.length; i++) {
				ILaunchConfiguration config = configs[i];
				if (config
						.getAttribute(
								IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,
								"").equals(projectName)) { //$NON-NLS-1$
					candidateConfigs.add(config);
				}
			}
		} catch (CoreException cEx) {
			AJLog.log(cEx.getMessage());
		}
		return candidateConfigs;
	}

}
