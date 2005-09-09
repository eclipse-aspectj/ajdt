/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.internal.launching;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.launching.LaunchingMessages;
import org.eclipse.jdt.launching.ExecutionArguments;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.IVMRunner;
import org.eclipse.jdt.launching.JavaLaunchDelegate;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.launching.VMRunnerConfiguration;

/**
 *
 */
public class LTWApplicationLaunchConfigurationDelegate 
		extends JavaLaunchDelegate {
	
	private static final String classLoaderOption = "-Djava.system.class.loader"; //$NON-NLS-1$
	private static final String ajClasspathOption = "-Daj.class.path"; //$NON-NLS-1$
	

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		monitor.beginTask(MessageFormat.format("{0}...", new String[]{configuration.getName()}), 3); //$NON-NLS-1$
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}
		
		monitor.subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Verifying_launch_attributes____1);
						
		String mainTypeName = verifyMainTypeName(configuration);
		IVMRunner runner = getVMRunner(configuration, mode);

		File workingDir = verifyWorkingDirectory(configuration);
		String workingDirName = null;
		if (workingDir != null) {
			workingDirName = workingDir.getAbsolutePath();
		}
		
		// Environment variables
		String[] envp= getEnvironment(configuration);
		
		// Classpath
		String[] classpath = getClasspath(configuration);
		
		// Program & VM args
		String pgmArgs = getProgramArguments(configuration);
		String vmArgs = getVMArguments(configuration);
		vmArgs = addExtraVMArgs(vmArgs, classpath);
		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
		
		// VM-specific attributes
		Map vmAttributesMap = getVMSpecificAttributesMap(configuration);
		
		String[] ltwClasspath = getLTWClasspath();
		
		// Create VM config
		VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, ltwClasspath);
		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
		runConfig.setEnvironment(envp);
		runConfig.setVMArguments(execArgs.getVMArgumentsArray());
		runConfig.setWorkingDirectory(workingDirName);
		runConfig.setVMSpecificAttributesMap(vmAttributesMap);

		// Bootpath
		runConfig.setBootClassPath(getBootpath(configuration));
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}		
		
		// stop in main
		prepareStopInMain(configuration);
		
		// done the verification phase
		monitor.worked(1);
		
		monitor.subTask(LaunchingMessages.JavaLocalApplicationLaunchConfigurationDelegate_Creating_source_locator____2);
		// set the default source locator if required
		setDefaultSourceLocator(launch, configuration);
		monitor.worked(1);		
		
		// Launch the configuration - 1 unit of work
		runner.run(runConfig, launch, monitor);
		
		// check for cancellation
		if (monitor.isCanceled()) {
			return;
		}	
		
		monitor.done();
	}

	/**
	 * Override getClasspath so that we also include the load time weaving
	 * aspectpath
	 */
	public String[] getClasspath(ILaunchConfiguration configuration)
			throws CoreException {
		IRuntimeClasspathEntry[] entries = JavaRuntime
				.computeUnresolvedRuntimeClasspath(configuration);
		IRuntimeClasspathEntry[] aspectPathEntries = getAspectPathEntries(configuration);
		entries = JavaRuntime.resolveRuntimeClasspath(entries, configuration);
		List userEntries = new ArrayList(entries.length + aspectPathEntries.length);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getClasspathProperty() == IRuntimeClasspathEntry.USER_CLASSES) {
				String location = entries[i].getLocation();
				if (location != null) {
					userEntries.add(location);
				}
			}
		}
		for (int i = 0; i < aspectPathEntries.length; i++) {
			String location = aspectPathEntries[i].getLocation();
			if (location != null) {
				userEntries.add(location);
			}
		}
		return (String[]) userEntries.toArray(new String[userEntries.size()]);
	}
	
	/**
	 * Get the load time weaving aspectpath from the given configuration
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private IRuntimeClasspathEntry[] getAspectPathEntries(ILaunchConfiguration configuration) throws CoreException {
		List entries = configuration.getAttribute(LTWAspectPathTab.ATTR_ASPECTPATH, Collections.EMPTY_LIST);
		IRuntimeClasspathEntry[] rtes = new IRuntimeClasspathEntry[entries.size()];
		Iterator iter = entries.iterator();
		int i = 0;
		while (iter.hasNext()) {
			rtes[i] = JavaRuntime.newRuntimeClasspathEntry((String)iter.next());
			i++;
		}
		return rtes;
	}

	private String[] getLTWClasspath() {
		// TODO: Fill in this method adding the appropriate jar to the classpath
		return null;
	}

	private String addExtraVMArgs(String vmArgs, String[] ajClasspath) {
		StringBuffer sb = new StringBuffer(vmArgs);
		sb.append(' '); //$NON-NLS-1$
		sb.append(classLoaderOption);
		sb.append('='); //$NON-NLS-1$
		sb.append("org.aspectj.weaver.WeavingURLClassLoader"); //$NON-NLS-1$
		sb.append(' '); //$NON-NLS-1$
		sb.append(ajClasspathOption);
		sb.append('='); //$NON-NLS-1$
		for (int i = 0; i < ajClasspath.length; i++) {
			if(i != 0) {
				sb.append(';'); //$NON-NLS-1$
			}
			sb.append(ajClasspath[i]);			
		}
		
		return sb.toString();
	}
}
