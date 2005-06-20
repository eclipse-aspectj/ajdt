/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - iniital version
 *******************************************************************************/
package org.eclipse.ajdt.internal.builder;

import java.io.File;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.test.AspectJTestPlugin;
import org.eclipse.ajdt.test.utils.BlockingProgressMonitor;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;

/**
 * This contains useful methods for checking classpaths for various entries.
 * 
 * @author hawkinsh
 *  
 */
public class ProjectDependenciesUtils {

//	public static void waitForJobsToComplete(IProject pro) {
//		Job job = new Job("Dummy Job") {
//			public IStatus run(IProgressMonitor m) {
//				return Status.OK_STATUS;
//			}
//		};
//		job.setPriority(Job.DECORATE);
//		job.setRule(pro);
//		job.schedule();
//		try {
//			job.join();
//		} catch (InterruptedException e) {
//			// Do nothing
//		}
//	}

	public static boolean projectHasProjectDependency(IProject project,
			IProject projectDependedOn) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null)
			return false;
		IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
		for (int j = 0; j < cpEntry.length; j++) {
			IClasspathEntry entry = cpEntry[j];
			int entryKind = entry.getEntryKind();
			IPath entryPath = entry.getPath();
			if (entryKind == IClasspathEntry.CPE_PROJECT
					&& entryPath.equals(projectDependedOn.getFullPath())) {
				return true;
			}
		}
		return false;
	}

	public static boolean projectHasClassFolderDependency(IProject project,
			IProject projectDependedOn) throws JavaModelException {
		IJavaProject javaProject = JavaCore.create(project);
		IJavaProject depProject = JavaCore.create(projectDependedOn);
		if (javaProject == null || depProject == null)
			return false;

		// first get the output location for projectDependedOn
		IPath outputLocation = null;
		IClasspathEntry[] cp = depProject.getRawClasspath();
		for (int j = 0; j < cp.length; j++) {
			IClasspathEntry entry = cp[j];
			int contentKind = entry.getContentKind();
			if (contentKind == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getOutputLocation();
			}
			if (entry.getEntryKind() == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getOutputLocation();
			}
		}
		if (outputLocation == null) {
			outputLocation = depProject.getOutputLocation();
		}
		// now check the classpath entries of project for the output
		// location just calculated
		IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
		for (int j = 0; j < cpEntry.length; j++) {
			IClasspathEntry entry = cpEntry[j];
			int entryKind = entry.getEntryKind();
			IPath entryPath = entry.getPath();
			if (entryKind == IClasspathEntry.CPE_LIBRARY
					&& entryPath.equals(outputLocation)) {
				return true;
			}
		}
		return false;
	}

	public static boolean projectIsMarkedWithError(IProject project,
			String errorMessage) throws CoreException {
		Utils.waitForJobsToComplete();
		boolean projectIsMarked = false;

		IMarker[] problemMarkers = project.findMarkers(IMarker.PROBLEM, false,
				IResource.DEPTH_INFINITE);
		if (problemMarkers.length > 0) {
			for (int j = 0; j < problemMarkers.length; j++) {
				IMarker marker = problemMarkers[j];
				int markerSeverity = marker.getAttribute(IMarker.SEVERITY, -1);
				String markerMessage = marker.getAttribute(IMarker.MESSAGE,
						"no message");
				int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
				if (markerSeverity == IMarker.SEVERITY_ERROR) {
					if (errorMessage == null
							|| markerMessage.equals(errorMessage)) {
						return true;
					}
				}
			}
		}
		IMarker[] javaModelMarkers = project.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
				IResource.DEPTH_INFINITE);
		if (javaModelMarkers.length > 0) {
			for (int j = 0; j < javaModelMarkers.length; j++) {
				IMarker marker = javaModelMarkers[j];
				int markerSeverity = marker.getAttribute(IMarker.SEVERITY, -1);
				String markerMessage = marker.getAttribute(IMarker.MESSAGE,
						"no message");
				int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, -1);
				if (markerSeverity == IMarker.SEVERITY_ERROR) {
					if (errorMessage == null
							|| markerMessage.equals(errorMessage)) {
						System.out.println("TEST: error message: " + markerMessage);
						projectIsMarked = true;
					}
				}
			}
		}

		Utils.waitForJobsToComplete();
		return projectIsMarked;
	}

	public static boolean projectHasNoSrc(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return false;
		}

		boolean foundSrc = false;
		try {
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
					foundSrc = true;
					if (entry.getPath().equals(javaProject.getPath())) {
						return true;
					}
				}
			}
			if (!foundSrc)
				return true;
		} catch (JavaModelException e) {
			AspectJTestPlugin.log(e);
		}
		return false;
	}

	public static boolean projectHasAnExportedClasspathEntry(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		if (javaProject == null) {
			return false;
		}

		try {
			IClasspathEntry[] cpEntry = javaProject.getRawClasspath();
			for (int j = 0; j < cpEntry.length; j++) {
				IClasspathEntry entry = cpEntry[j];
				if (entry.isExported()) {
					// we don't want to export it in the new classpath.
					if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
						return true;
					}
				}
			}
		} catch (JavaModelException e) {
			AspectJTestPlugin.log(e);
		}
		return false;
	}

	public static boolean projectHasJarOnClasspath(IProject projectToHaveJar,
			IProject projectWhichExportedJar) throws JavaModelException {
		IJavaProject javaProject1 = JavaCore.create(projectToHaveJar);
		IJavaProject javaProject2 = JavaCore.create(projectWhichExportedJar);
		if (javaProject1 == null || javaProject2 == null) {
			return false;
		}

		IClasspathEntry[] cpEntry2 = javaProject2.getRawClasspath();
		for (int j = 0; j < cpEntry2.length; j++) {
			IClasspathEntry entry = cpEntry2[j];
			if (entry.isExported()) {
				// we don't want to export it in the new classpath.
				if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					IClasspathEntry[] cpEntry1 = javaProject1.getRawClasspath();
					for (int i = 0; i < cpEntry1.length; i++) {
						IClasspathEntry entry1 = cpEntry1[i];
						if (entry1.getEntryKind() == entry.getEntryKind()
								&& entry1.getPath().equals(entry.getPath())) {
							if (entry1.isExported()) {
								// don't want it to be exported
								return false;
							} else {
								return true;
							}
						}
					}
					return false;
				}
			}
		}
		return false;
	}

	public static boolean projectHasOutJarOnClasspath(
			IProject projectToHaveJar, IProject projectWhichHasOutJar,
			String outJar) throws JavaModelException {
		IJavaProject javaProject1 = JavaCore.create(projectToHaveJar);
		if (javaProject1 == null) {
			return false;
		}
		System.out.println("TEST: outjar = " + outJar);
		StringBuffer sb = new StringBuffer(outJar);
		int indexOfProject = sb.lastIndexOf(projectWhichHasOutJar.getName());
		IPath newPath;
		if (indexOfProject < 0) {
			newPath = new Path(outJar);
		} else {
			newPath = new Path(sb.substring(indexOfProject));
		}

		IClasspathEntry[] cpEntry = javaProject1.getRawClasspath();
		for (int j = 0; j < cpEntry.length; j++) {
			IClasspathEntry entry = cpEntry[j];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				if (entry.getPath().equals(new Path(outJar))
						|| entry.getPath().equals(newPath.makeAbsolute())) {
					return true;
				}
			}
		}
		return false;
	}

	public static int numberOfTimesOutJarOnClasspath(IProject projectToHaveJar,
			IProject projectWhichHasOutJar, String outJar)
			throws JavaModelException {
		IJavaProject javaProject1 = JavaCore.create(projectToHaveJar);
		if (javaProject1 == null) {
			return -1;
		}
		int counter = 0;
		System.out.println("TEST: outjar = " + outJar);
		StringBuffer sb = new StringBuffer(outJar);
		int indexOfProject = sb.lastIndexOf(projectWhichHasOutJar.getName());
		String jar = sb.substring(indexOfProject);
		IPath newPath = new Path(jar);

		IClasspathEntry[] cpEntry = javaProject1.getRawClasspath();
		for (int j = 0; j < cpEntry.length; j++) {
			IClasspathEntry entry = cpEntry[j];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				if (entry.getPath().equals(new Path(outJar))
						|| entry.getPath().equals(newPath.makeAbsolute())) {
					counter++;
				}
			}
		}
		return counter;
	}

	public static String setupOutJar(String jarName,
			IProject projectToOutputToJarFile) {
		String outJar = jarName;
		if (outJar != null) {
			if (!outJar.startsWith(File.separator)) {
				IJavaProject jp = JavaCore.create(projectToOutputToJarFile);
				IPath projectPath = jp.getPath();
				IPath full = AspectJPlugin.getWorkspace().getRoot()
				        .getLocation().append(projectPath);
				outJar = full.toOSString() + File.separator + outJar;
			}
		}
		System.out.println("TEST: outJar = " + outJar);
		return outJar;
	}

	/**
	 * checks whether the project has been marked as requiring prerequisite
	 * projects to be built. This marker comes from the java builder which is
	 * why check for a JAVA_MODEL_PROBLEM_MARKER.
	 */
	public static boolean projectMarkedWithPrereqMessage(IProject project,
			IProject prereqProject) {
		Utils.waitForJobsToComplete();
		try {
			String errorMessage = "The project cannot be built until its prerequisite "
					+ prereqProject.getName()
					+ " is built. Cleaning and rebuilding all projects is recommended";
			IMarker[] javaModelMarkers = project.findMarkers(
					IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false,
					IResource.DEPTH_INFINITE);
			if (javaModelMarkers.length > 0) {
				for (int j = 0; j < javaModelMarkers.length; j++) {
					IMarker marker = javaModelMarkers[j];
					int markerSeverity = marker.getAttribute(IMarker.SEVERITY,
							-1);
					String markerMessage = marker.getAttribute(IMarker.MESSAGE,
							"no message");
					if (markerSeverity == IMarker.SEVERITY_ERROR
							&& markerMessage.equals(errorMessage)) {
						return true;
					}
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			Utils.waitForJobsToComplete();
		}
		return false;
	}

	public static void addProjectDependency(
			IJavaProject projectToHaveDependency, IProject projectDependedOn) {
		try {
			IClasspathEntry[] cpEntry = projectToHaveDependency
					.getRawClasspath();
			IClasspathEntry newProjectEntry = JavaCore
					.newProjectEntry(projectDependedOn.getFullPath());

			IClasspathEntry[] newEntries = new IClasspathEntry[cpEntry.length + 1];
			for (int i = 0; i < cpEntry.length; i++) {
				newEntries[i] = cpEntry[i];
			}
			newEntries[cpEntry.length] = newProjectEntry;
			projectToHaveDependency.setRawClasspath(newEntries, null);
			Utils.waitForJobsToComplete();
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	public static void addPluginDependency(IProject projectToHaveDependency,
			String importID, BlockingProgressMonitor monitor) {
		ManifestEditor manEd = AJDTUtils
				.getPDEManifestEditor(projectToHaveDependency);
		AJDTUtils.getAndPrepareToChangePDEModel(projectToHaveDependency);
		if (manEd != null) {
			IPluginModel model = (IPluginModel) manEd.getAggregateModel();
			try {
				addImportToPDEModel(model, importID, monitor);
				monitor.reset();
				manEd.doSave(monitor);
				monitor.waitForCompletion();
				monitor.reset();
				projectToHaveDependency.build(
						IncrementalProjectBuilder.FULL_BUILD, "Java Builder",
						null, monitor);
				monitor.waitForCompletion();
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	private static void addImportToPDEModel(IPluginModel model,
			String importId, BlockingProgressMonitor monitor)
			throws CoreException {

		IPluginImport importNode = model.getPluginFactory().createImport();
		importNode.setId(importId);
		model.getPluginBase().getImports();
		model.getPluginBase().add(importNode);

		IFile manifestFile = (IFile) model.getUnderlyingResource();
		//monitor.reset();
		manifestFile.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		//monitor.waitForCompletion();
	}
	
	public static boolean projectHasPluginDependency(IProject projectWhichHasDependency,
			String pluginIdOfRequiredProject) throws JavaModelException {
		ManifestEditor manEd = AJDTUtils.getPDEManifestEditor(projectWhichHasDependency);
		AJDTUtils.getAndPrepareToChangePDEModel(projectWhichHasDependency);
		if (manEd != null) {
			IPluginModel model = (IPluginModel) manEd.getAggregateModel();
			IPluginImport[] imports = model.getPluginBase().getImports();
			for (int i = 0; i < imports.length; i++) {
				IPluginImport import1 = imports[i];
				if (import1.getId().equals(pluginIdOfRequiredProject)) {
					return true;
				}
			}
		}

		return false;
	}
}

