/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Helen Hawkins   - initial version
 *******************************************************************************/
package org.eclipse.ajdt.test.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.ajdt.internal.core.AJDTUtils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.internal.resources.ResourceException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * The base for this class was taken from the
 * org.eclipse.contribution.junit.test project (Gamma/Beck) and then edited from
 * there.
 * 
 * This creates an AspectJ project exactly as it would be created by
 * following New --> Project --> AspectJ Project
 * 
 */
public class AJTestProject {

	// projects don't delete reliably whatever we do, so to ensure that
	// every project within a test run is unique, we append a UUID to the
	// project name.
	private static int UNIQUE_ID = 1;

	private String name = "";

	private IProject project;

	private IJavaProject javaProject;

	private IPackageFragmentRoot sourceFolder;

	private IFolder binFolder;

	private BlockingProgressMonitor monitor = new BlockingProgressMonitor();

	public AJTestProject() throws CoreException {
		this("TestProject", "bin", true);
	}

	public AJTestProject(String pname) throws CoreException {
		this(pname, "bin", true);
	}

	public AJTestProject(String pname, boolean createContent)
			throws CoreException {
		this(pname, "bin", createContent);
	}

	public AJTestProject(String pname, String outputfoldername)
			throws CoreException {
		this(pname, outputfoldername, true);
	}

	public AJTestProject(String pname, String outputfoldername,
			boolean createContent) throws CoreException {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		name = pname + UNIQUE_ID++;
		project = root.getProject(name);
		monitor.reset();
		project.create(monitor);
		monitor.waitForCompletion();
		monitor.reset();
		project.open(monitor);
		monitor.waitForCompletion();
		javaProject = JavaCore.create(project);

		waitForJobsToComplete(project);
		binFolder = createOutputFolder(outputfoldername);
		createAJProject();
		waitForJobsToComplete(project);
		createOutputFolder(binFolder);
		monitor.reset();
		javaProject.setRawClasspath(new IClasspathEntry[0], monitor);
		monitor.waitForCompletion();

		if (createContent) {
			addSystemLibraries();
		}
	}

	// this has been lifted from the performFinish() method in
	// AspectJProjectWizard - it therefore goes through the mechanism
	// which creates a brand new AspectJ project
	private void createAJProject() {
		try {
			project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
			monitor.waitForCompletion();
		} catch (CoreException e) {
			System.err.println("Failed initial Java build of new project "
					+ project.getName() + " : " + e);
			AspectJUIPlugin.getDefault().getLog().log(e.getStatus());
		}

		try {
			// The nature to add is the PluginID+NatureID - it is not the
			// name of the class implementing IProjectNature !!
			// When the nature is attached, the project will be driven through
			// INatureProject.configure() which will replace the normal
			// javabuilder
			// with the aspectj builder.
			waitForJobsToComplete(project);
			AJDTUtils.addAspectJNature(project);
			waitForJobsToComplete(project);
		} catch (Throwable e) {
			System.out.println("> Error creating new project: " + e);
			e.printStackTrace();
		}
		AspectJUIPlugin.getDefault().setCurrentProject(project);
	}

	public String getName() {
		return name;
	}

	public IProject getProject() {
		return project;
	}

	public IJavaProject getJavaProject() {
		return javaProject;
	}

	public void addJar(String plugin, String jar) throws MalformedURLException,
			IOException, JavaModelException {
		Path result = findFileInPlugin(plugin, jar);
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newLibraryEntry(result, null,
				null);
		javaProject.setRawClasspath(newEntries, null);
	}

	public IPackageFragment createPackage(String name) throws CoreException {
		if (sourceFolder == null)
			sourceFolder = createSourceFolder();
		monitor.reset();
		IPackageFragment ret = sourceFolder.createPackageFragment(name, false,
				monitor);
		monitor.waitForCompletion();
		return ret;
	}

	public IType createType(IPackageFragment pack, String cuName, String source)
			throws JavaModelException {
		StringBuffer buf = new StringBuffer();
		buf.append("package " + pack.getElementName() + ";\n");
		buf.append("\n");
		buf.append(source);
		monitor.reset();
		ICompilationUnit cu = pack.createCompilationUnit(cuName,
				buf.toString(), false, monitor);
		monitor.waitForCompletion();
		return cu.getTypes()[0];
	}

	public IPackageFragmentRoot getSourceFolder() throws CoreException {
		if (sourceFolder == null)
			sourceFolder = createSourceFolder();
		return sourceFolder;
	}

	public IFolder createFolder(IPackageFragmentRoot root, String name)
			throws JavaModelException, CoreException {
		IFolder folder = (IFolder) root.getCorrespondingResource();
		IFolder ret = folder.getFolder(name);
		if (!folder.exists()) {
			monitor.reset();
			ret.create(true, true, monitor);
			monitor.waitForCompletion();
		}
		return ret;
	}

	public IFile createFile(IFolder inFolder, String name, String content)
			throws CoreException {
		IFile file = inFolder.getFile(name);
		if (file.exists()) {
			file.delete(0, null);
		}
		ByteArrayInputStream source = new ByteArrayInputStream(content
				.getBytes());
		file.create(source, true, null);
		return file;
	}

	public IFolder getOutputFolder() {
		return binFolder;
	}

	public synchronized void dispose() throws CoreException {
		waitForIndexer();
		monitor.reset();
		try {
			project.delete(IResource.ALWAYS_DELETE_PROJECT_CONTENT, monitor);
		} catch (ResourceException re) {
			// ignore this
		}
		monitor.waitForCompletion();
	}

	//	public String run(String className) {
	//		StringBuffer output = new StringBuffer();
	//		try {
	//			File binDir = new File(binFolder.getLocation().toOSString());
	//			Process p = Runtime.getRuntime().exec("java -classpath " +
	//							binDir.getPath() + ":" + ASPECTJRT_JAR + " " + className,
	//							null,new File(binFolder.getLocation().toOSString()));
	//			InputStream is = p.getInputStream();
	//			InputStreamReader isr = new InputStreamReader(is);
	//			p.waitFor();
	//			int c;
	//			while ((c = isr.read()) != -1) {
	//				output.append((char)c);
	//			}
	//		} catch (Exception ex) {
	//			ex.printStackTrace();
	//		}
	//		return output.toString();
	//	}

	private synchronized IFolder createOutputFolder(String outputfoldername)
			throws CoreException {
		IFolder folder = project.getFolder(outputfoldername);
		if (!folder.exists()) {
			monitor.reset();
			folder.create(true, true, monitor);
			monitor.waitForCompletion();
		}
		return folder;
	}

	private void setAspectJNature() throws CoreException {
		IProjectDescription description = project.getDescription();
		description
				.setNatureIds(new String[] { "org.eclipse.ajdt.core.plugin.javanature" });
		monitor.reset();
		project.setDescription(description, monitor);
		monitor.waitForCompletion();
	}

	private void createOutputFolder(IFolder folder) throws JavaModelException {
		IPath outputLocation = folder.getFullPath();
		//monitor.reset(); doesn't always use monitor...
		javaProject.setOutputLocation(outputLocation, monitor);
		monitor.waitForCompletion();
	}

	private IPackageFragmentRoot createSourceFolder() throws CoreException {
		IFolder folder = project.getFolder("src");
		if (!folder.exists()) {
			monitor.reset();
			folder.create(false, true, monitor);
			monitor.waitForCompletion();
		}
		IPackageFragmentRoot root = javaProject.getPackageFragmentRoot(folder);

		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaCore.newSourceEntry(root.getPath());
		monitor.reset();
		javaProject.setRawClasspath(newEntries, monitor);
		monitor.waitForCompletion();
		return root;
	}

	private void addSystemLibraries() throws JavaModelException {
		IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
		IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 2];
		System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
		newEntries[oldEntries.length] = JavaRuntime
				.getDefaultJREContainerEntry();
		//		newEntries[oldEntries.length]= JavaCore.newLibraryEntry(new
		// Path(RT_JAR),null,null);
		//		newEntries[oldEntries.length+1]= JavaCore.newLibraryEntry(new
		// Path(ASPECTJRT_JAR),null,null);
		monitor.reset();
		javaProject.setRawClasspath(newEntries, monitor);
		monitor.waitForCompletion();
	}

	private Path findFileInPlugin(String plugin, String file)
			throws MalformedURLException, IOException {
		org.osgi.framework.Bundle bundle = Platform.getBundle(plugin);
		URL pluginURL = bundle.getEntry("/");
		URL jarURL = new URL(pluginURL, file);
		URL localJarURL = Platform.asLocalURL(jarURL);
		return new Path(localJarURL.getPath());
	}

	private void waitForIndexer() throws JavaModelException {
		new SearchEngine().searchAllTypeNames(null, null,
				SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.CLASS,
				SearchEngine.createJavaSearchScope(new IJavaElement[0]),
				new ITypeNameRequestor() {
					public void acceptClass(char[] packageName,
							char[] simpleTypeName, char[][] enclosingTypeNames,
							String path) {
					}

					public void acceptInterface(char[] packageName,
							char[] simpleTypeName, char[][] enclosingTypeNames,
							String path) {
					}
				}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);

	}

	public static class BlockingProgressMonitor implements IProgressMonitor {

		private Boolean isDone = Boolean.FALSE;

		public boolean isDone() {
			boolean ret = false;
			synchronized (isDone) {
				ret = (isDone == Boolean.TRUE);
			}
			return ret;
		}

		public void reset() {
			synchronized (isDone) {
				isDone = Boolean.FALSE;
			}
		}

		public void waitForCompletion() {
			while (!isDone()) {
				try {
					synchronized (this) {
						wait(500);
					}
				} catch (InterruptedException intEx) {
					// no-op
				}
			}
		}

		public void beginTask(String name, int totalWork) {
			if (name != null)
				System.out.println(name);
			reset();
		}

		public void done() {
			synchronized (isDone) {
				isDone = Boolean.TRUE;
			}
			synchronized (this) {
				notify();
			}
		}

		public void internalWorked(double work) {
		}

		public boolean isCanceled() {
			return false;
		}

		public void setCanceled(boolean value) {
		}

		public void setTaskName(String name) {
		}

		public void subTask(String name) {
		}

		public void worked(int work) {
		}
	}

	private void waitForJobsToComplete(IProject pro) {
		Job job = new Job("Dummy Job") {
			public IStatus run(IProgressMonitor m) {
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.DECORATE);
		job.setRule(pro);
		job.schedule();
		try {
			job.join();
		} catch (InterruptedException e) {
			// Do nothing
		}
	}
}