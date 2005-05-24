/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.build;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//
//import org.eclipse.core.internal.resources.Workspace;
//import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.ajdt.internal.build.builder.BuildClasspathResolver;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPlatformRunnable;
//import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
//import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
import org.eclipse.jdt.launching.JavaRuntime;

/**
 * 
 * @author mchapman
 */
public class ClasspathExtractor implements IPlatformRunnable {

	public static final String OUT_FILE = "ajdtworkspace.properties";
	
	private String outDir = ".";
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.boot.IPlatformRunnable#run(java.lang.Object)
	 */
	public Object run(Object args) throws Exception {
		// first arg is the directory to write properties file to
		if (args instanceof String[]) {
			String[] arg = (String[])args;
			if (arg.length>0) {
				outDir = arg[0]; 
			}
		}
		
		outDir=".";
		System.out.println("output dir="+outDir);
		IWorkspace workspace = BuildPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		String workspaceLoc = root.getLocation().toOSString();
		System.out.println("workspaceLoc=" + workspaceLoc);
		ensureProjectsExist(root);
		
		// first make sure all projects are open and in sync
		IProject[] projects = root.getProjects();
		for (int i = 0; i < projects.length; i++) {
			System.out.println("project["+i+"]="+projects[i]);
			if (!projects[i].isOpen()) {
				System.out.println("not open, opening");
				projects[i].open(null);
			}
			if (projects[i].isAccessible()
				&& projects[i].hasNature(JavaCore.NATURE_ID)) {
				projects[i].refreshLocal(IResource.DEPTH_INFINITE, null);
			}
		}
		
		// now resolve the classpath of those projects we're interested in
		// and write it out to a file
		FileWriter props = new FileWriter(outDir + File.separator + OUT_FILE);
		BufferedWriter writer = new BufferedWriter(props);

		for (int i = 0; i < projects.length; i++) {
			String name = projects[i].getName();
			if (projects[i].isAccessible()
					&& projects[i].hasNature(JavaCore.NATURE_ID)) {
				if ((name.indexOf("aspectj") > 0) || (name.indexOf("ajdt") > 0)
						|| (name.indexOf("visualiser") > 0)
						|| (name.indexOf("xref") > 0)) {
					System.out.println("project[" + i + "]=" + name);
					IJavaProject jp = JavaCore.create(projects[i]);
					String cp = new BuildClasspathResolver().getClasspath(root, jp);
					if (cp != null && cp.length() > 0) {
					// paths are in platform-specific format, but to use in properties
					// files on Windows the separator needs to be / instead of \
						cp = cp.replace('\\', '/');
						System.out.println("cp=" + cp);
						writer.write("classpath."+name+"="+cp);
						writer.newLine();
					}
				} else {
					System.out.println("skipping project: "+name);
				}
			}
		}
		
		writer.flush();
		props.close();
		return null;
	}
	
//	public Object run(Object args) throws Exception {
//		IWorkspace workspace = BuildPlugin.getWorkspace();
//
//		ensureProjectsExist(workspace);
//
//		IProject[] projects = workspace.getRoot().getProjects();
//		for (int i = 0; i < projects.length; i++) {
//			if (projects[i].isAccessible()
//				&& projects[i].hasNature(JavaCore.NATURE_ID)) {
//				projects[i].refreshLocal(IResource.DEPTH_INFINITE, null);
//			}
//		}
//
//		FileWriter props = new FileWriter("ajdtworkspace.properties");
//		BufferedWriter writer = new BufferedWriter(props);
//
//		// Warning: need to use internal API to query build order
//		if (workspace instanceof Workspace) {
//			Workspace ws = (Workspace) workspace;
//			writer.write("build.order=");
//			IProject[] order = ws.getBuildOrder();
//			for (int i = 0; i < order.length; i++) {
//				writer.write(order[i].getName());
//				if (i < order.length - 1) {
//					writer.write(",");
//				}
//			}
//			writer.newLine();
//		}
//
//		String workspaceLoc = workspace.getRoot().getLocation().toOSString();
//		for (int i = 0; i < projects.length; i++) {
//			if (projects[i].isAccessible()
//				&& projects[i].hasNature(JavaCore.NATURE_ID)) {
//				projects[i].refreshLocal(IResource.DEPTH_INFINITE, null);
//				String cp = getProjectClasspath(projects[i]);
//				if (cp != null && cp.length() > 0) {
//					// paths are in platform-specific format, but to use in properties
//					// files on Windows the separator needs to be / instead of \
//					cp = cp.replace('\\', '/');
//					//if (cp.startsWith("/")) { // workaround for dynamic plugin classpaths
//					//	cp = workspaceLoc + cp;
//					//}
//					writer.write(
//						"newclasspath." + projects[i].getName() + "=" + cp);
//					writer.newLine();
//				}
//
//				cp = oldGetProjectClasspath(projects[i]);
//				if (cp != null && cp.length() > 0) {
//					// paths are in platform-specific format, but to use in properties
//					// files on Windows the separator needs to be / instead of \
//					cp = cp.replace('\\', '/');
//					//if (cp.startsWith("/")) { // workaround for dynamic plugin classpaths
//					//	cp = workspaceLoc + cp;
//					//}
//					writer.write(
//						"classpath." + projects[i].getName() + "=" + cp);
//					writer.newLine();
//				}
//
//				String outdir = getAbsoluteOutputLocation(projects[i]);
//				if (outdir != null && outdir.length() > 0) {
//					// paths are in platform-specific format, but to use in properties
//					// files on Windows the separator needs to be / instead of \
//					outdir = outdir.replace('\\', '/');
//					writer.write(
//						"outdir." + projects[i].getName() + "=" + outdir);
//					writer.newLine();
//				}
//			}
//		}
//		writer.flush();
//		props.close();
//		return null;
//	}
//
//	public static String __oldGetProjectClasspath(IProject project) {
//		StringBuffer classpath = new StringBuffer();
//		try {
//			IJavaProject jp = JavaCore.create(project);
//			IRuntimeClasspathEntry[] rtcp =
//				JavaRuntime.computeUnresolvedRuntimeClasspath(jp);
//			List cp = new ArrayList();
//			for (int i = 0; i < rtcp.length; i++) {
//				IClasspathEntry entry = rtcp[i].getClasspathEntry();
//				if ((entry != null)
//					&& (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER)) {
//						System.out.println("container entry="+entry);
//					IRuntimeClasspathEntry[] resolved =
//						JavaRuntime.resolveRuntimeClasspathEntry(rtcp[i], jp);
//					for (int j = 0; j < resolved.length; j++) {
//						System.out.println("res="+resolved[j].getLocation()+" path="+resolved[j].getPath().toOSString());
//						cp.add(resolved[j].getLocation());
//					}
//				}
//			}
//			for (Iterator it = cp.iterator(); it.hasNext();) {
//				classpath.append(it.next());
//				classpath.append(File.pathSeparator);
//			}
//		} catch (CoreException e) {
//			System.err.println(e);
//		}
//		return classpath.toString();
//	}
//
		
	public static String oldGetProjectClasspath(IProject project) {
		StringBuffer classpath = new StringBuffer();
		try {
			IJavaProject jp = JavaCore.create(project);
			IRuntimeClasspathEntry[] rtcp =
				JavaRuntime.computeUnresolvedRuntimeClasspath(jp);
			List cp = new ArrayList();
			for (int i = 0; i < rtcp.length; i++) {
				IRuntimeClasspathEntry[] resolved =
					JavaRuntime.resolveRuntimeClasspathEntry(rtcp[i], jp);
				for (int j = 0; j < resolved.length; j++) {
					cp.add(resolved[j].getLocation());
				}
			}
			for (Iterator it = cp.iterator(); it.hasNext();) {
				classpath.append(it.next());
				classpath.append(File.pathSeparator);
			}
		} catch (CoreException e) {
			System.err.println(e);
		}
		return classpath.toString();
	}
//
//	public static String getProjectClasspath(IProject project) {
//		System.out.println("project="+project.getName());
//		StringBuffer classpath = new StringBuffer();
//		try {
//			IJavaProject jp = JavaCore.create(project);
//			IClasspathEntry[] ipe = jp.getResolvedClasspath(false);
//			for (int i = 0; i < ipe.length; i++) {
//				String p = ipe[i].getPath().toOSString();
//				System.out.println("p="+p);
//				classpath.append(p);
//				classpath.append(File.pathSeparator);
//			}
//			/*String[] rcp = JavaRuntime.computeDefaultRuntimeClassPath(jp);
//			for (int j = 0; j < rcp.length; j++) {
//				classpath.append(rcp[j]);
//				classpath.append(File.pathSeparator);
//			}*/
//		} catch (CoreException e) {
//			System.err.println(e);
//		}
//		return classpath.toString();
//	}
//
//	/**
//	 * If the project has any source folders returns the disk location 
//	 * of the project's output directory, else returns null.
//	 */
//	public static String getAbsoluteOutputLocation(IProject project) {
//		try {
//			String returning = null;
//			String location = project.getLocation().toOSString();
//			String path = project.getFullPath().toOSString();
//			IJavaProject jp = JavaCore.create(project);
//			String output = jp.getOutputLocation().toOSString();
//			int index = location.indexOf(path);
//			boolean srcInThisProject = output.indexOf(path) != -1;
//			if (index != -1 && srcInThisProject) {
//				returning = location.substring(0, index).concat(output);
//			}
//			if (returning != null) {
//				IClasspathEntry[] entries = jp.getRawClasspath();
//				for (int i = 0; i < entries.length; i++) {
//					IClasspathEntry entry = entries[i];
//					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
//						return returning;
//					}
//				}
//			}
//		} catch (JavaModelException e) {
//			System.err.println(e);
//		}
//		return null;
//	}
//
	/*
	 * New projects get copied into the workspace directory, but they aren't visible
	 * from inside Eclipse until createProject() has been called for them
	 */
	private void ensureProjectsExist(IWorkspaceRoot root) {
		String workspaceLoc = root.getLocation().toOSString();

		File ws = new File(workspaceLoc);
		File[] contents = ws.listFiles();
		for (int i = 0; i < contents.length; i++) {
			if (contents[i].isDirectory()
					&& !contents[i].getName().startsWith(".")) {
				IProject project = root.getProject(contents[i].getName());
				if (!project.exists()) {
					System.out.println("project " + contents[i].getName()
							+ " doesn't exist, creating");
					try {
						project.create(null);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
}
