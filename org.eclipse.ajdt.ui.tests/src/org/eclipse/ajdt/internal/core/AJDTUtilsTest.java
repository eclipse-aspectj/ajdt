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
package org.eclipse.ajdt.internal.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.utils.AJDTUtils;
import org.eclipse.ajdt.test.utils.Utils;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobChangeListener;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginModel;
import org.eclipse.pde.internal.ui.editor.plugin.ManifestEditor;

/**
 * @author hawkinsh
 * 
 */
public class AJDTUtilsTest extends TestCase {

	/*
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
	}

	/*
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testAddAndRemoveAspectJNatureWithPluginProject()
			throws Exception {
		Utils.setUpPluginEnvironment();
		IProject testPluginProject = Utils.createPredefinedProject("Hello World Java Plugin");
		Utils.waitForJobsToComplete();
		assertFalse("Plugin project shouldn't have AspectJ nature",
				AspectJPlugin.isAJProject(testPluginProject.getProject()));
		assertFalse("Plugin should not import AJDE plugin",
				hasDependencyOnAJDE(testPluginProject));
		AJDTUtils.addAspectJNature(testPluginProject.getProject());
		Utils.waitForJobsToComplete();
		assertTrue("Plugin project should now have AspectJ nature",
				AspectJPlugin.isAJProject(testPluginProject.getProject()));
		assertTrue("Plugin should now import AJDE plugin",
				hasDependencyOnAJDE(testPluginProject));
		AJDTUtils.removeAspectJNature(testPluginProject.getProject());
		Utils.waitForJobsToComplete();
		assertFalse("Plugin should not import AJDE plugin",
				hasDependencyOnAJDE(testPluginProject));
		assertFalse("Plugin project shouldn't have AspectJ nature",
				AspectJPlugin.isAJProject(testPluginProject.getProject()));
		Utils.deleteProject(testPluginProject);
		Utils.resetPluginEnvironment();
	}

	public void testAddAndRemoveAspectJNature() throws CoreException {
		IProject testProject = Utils.createPredefinedProject("project.java.Y");
		IJavaProject jY = JavaCore.create(testProject);
		Utils.waitForJobsToComplete();
		
		assertFalse("Java project should not have AspectJ Nature",
				AspectJPlugin.isAJProject(testProject.getProject()));
		assertFalse("Build path shouldn't contain aspectjrt.jar",
				hasAjrtOnBuildPath(jY));
		AJDTUtils.addAspectJNature(testProject.getProject());
		assertTrue("Java project should now have AspectJ Nature", AspectJPlugin
				.isAJProject(testProject.getProject()));
		assertTrue("Build path should now contain aspectjrt.jar",
				hasAjrtOnBuildPath(jY));
		AJDTUtils.removeAspectJNature(testProject.getProject());
		assertFalse("Java project should not have AspectJ Nature",
				AspectJPlugin.isAJProject(testProject.getProject()));
		assertFalse("Build path shouldn't contain aspectjrt.jar",hasAjrtOnBuildPath(jY));
		Utils.deleteProject(testProject);
		Utils.waitForJobsToComplete();
	}
	
	/**
	 * Test for bug 93532 - NPE when add aspectj nature to a plugin project
	 * which doesn't have a plugin.xml file.
	 * 
	 * @throws Exception
	 */
	public void testBug93532() throws Exception {
		IProject testProject = Utils.createPredefinedProject("bug93532");
		IJavaProject jY = JavaCore.create(testProject);
		Utils.waitForJobsToComplete();
		
		assertFalse("Java project should not have AspectJ Nature",
				AspectJPlugin.isAJProject(testProject.getProject()));
		assertFalse("Build path shouldn't contain aspectjrt.jar",
				hasAjrtOnBuildPath(jY));
		AJDTUtils.addAspectJNature(testProject.getProject());
		assertTrue("Java project should now have AspectJ Nature", AspectJPlugin
				.isAJProject(testProject.getProject()));
		assertTrue("Build path should now contain aspectjrt.jar",
				hasAjrtOnBuildPath(jY));
		AJDTUtils.removeAspectJNature(testProject.getProject());
		assertFalse("Java project should not have AspectJ Nature",
				AspectJPlugin.isAJProject(testProject.getProject()));
		assertFalse("Build path shouldn't contain aspectjrt.jar",hasAjrtOnBuildPath(jY));
		Utils.deleteProject(testProject);
		Utils.waitForJobsToComplete();	    
	}

	/**
	 * This tests whether you get back the manifest editor for the project you
	 * require.
	 * 
	 */
	public void testGetPDEManifestEditor() throws Exception {
		Utils.setUpPluginEnvironment();
		// know that the plugin id of this is HelloWorld
		IProject projectA1 = Utils.createPredefinedProject("Hello World Java Plugin");
		Utils.waitForJobsToComplete();
		
		// know that the plugin id for this is PluginWithView
		IProject projectA2 = Utils.createPredefinedProject("PluginWithView");
		Utils.waitForJobsToComplete();

		assertTrue("projectA1 should have manifest editor for project A1",
				AJDTUtils.getAndPrepareToChangePDEModel(projectA1.getProject())
						.getPartName().equals("HelloWorld"));
		assertTrue("projectA2 should have manifest editor for project A2",
				AJDTUtils.getAndPrepareToChangePDEModel(projectA2.getProject())
						.getPartName().equals("PluginWithView"));
		Utils.deleteProject(projectA1);
		Utils.deleteProject(projectA2);
		Utils.resetPluginEnvironment();
	}

	// We now longer change project dependencies in this way, so test removed
//	public void testChangeProjectToClassDependencies() throws Exception {
//		JavaTestProject jtp1 = new JavaTestProject("JavaTestProject1");
//		Utils.waitForJobsToComplete();
//		JavaTestProject jtp2 = new JavaTestProject("JavaTestProject2");
//		Utils.waitForJobsToComplete();
//		// this ensures a src folder is created.
//		jtp2.getSourceFolder();
//		Utils.waitForJobsToComplete();
//		ProjectDependenciesUtils.addProjectDependency(jtp1.getJavaProject(),
//				jtp2.getProject());
//		Utils.waitForJobsToComplete();
//		assertTrue("test project 1 has a project dependency on test project 2",
//				checkDependencyType(jtp1.getJavaProject(), jtp2.getProject())
//						.equals("project"));
//		AJDTUtils.changeProjectDependencies(jtp2.getProject());
//		Utils.waitForJobsToComplete();
//		assertTrue(
//				"test project 1 has a class folder dependency on test project 2",
//				checkDependencyType(jtp1.getJavaProject(), jtp2.getProject())
//						.equals("classfolder"));
//		jtp1.dispose();
//		jtp2.dispose();
//	}

	public void testAddAndRemoveAjrtToBuildPath() throws Exception {
		IProject projectY = Utils.createPredefinedProject("project.java.Y");
		IJavaProject jY = JavaCore.create(projectY);
		Utils.waitForJobsToComplete();

		assertFalse("project.java.Y should not have ajrt on build path",
				hasAjrtOnBuildPath(jY));
		AspectJUIPlugin.addAjrtToBuildPath(projectY);
		Utils.waitForJobsToComplete();

		assertTrue("project.java.Y should have ajrt on build path",
				hasAjrtOnBuildPath(jY));

		AspectJUIPlugin.removeAjrtFromBuildPath(projectY);
		Utils.waitForJobsToComplete();
		assertFalse("project.java.Y should not have ajrt on build path",
				hasAjrtOnBuildPath(jY));

		Utils.deleteProject(projectY);
	}

	private String checkDependencyType(IJavaProject projectToHaveDependency,
			IProject projectDependedOn) {
		try {
			IClasspathEntry[] cpEntry = projectToHaveDependency
					.getRawClasspath();
			for (int i = 0; i < cpEntry.length; i++) {
				IClasspathEntry entry = cpEntry[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
						&& entry.getPath().equals(
								projectDependedOn.getFullPath())) {
					return "project";
				} else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					List outputLocationPaths = CoreUtils
							.getOutputLocationPaths(projectDependedOn);
					for (Iterator iterator = outputLocationPaths.iterator(); iterator
							.hasNext();) {
						IPath path = (IPath) iterator.next();
						if (entry.getPath().equals(path)) {
							return "classfolder";
						}
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return "none";
	}

	private boolean hasAjrtOnBuildPath(IJavaProject javaProject) {
		try {
			IClasspathEntry[] originalCP = javaProject.getRawClasspath();
			for (int i = 0; i < originalCP.length; i++) {
				IPath path = originalCP[i].getPath();
				if (path.toOSString().endsWith("ASPECTJRT_LIB")
						|| path.toOSString().endsWith("aspectjrt.jar")) {
					return true;
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
		return false;
	}

	// private void addImportToPDEModel(IPluginModel model, String importId)
	// throws CoreException {
	//
	// IPluginImport importNode = model.getPluginFactory().createImport();
	// importNode.setId(importId);
	// model.getPluginBase().getImports();
	// model.getPluginBase().add(importNode);
	//
	// IFile manifestFile = (IFile) model.getUnderlyingResource();
	// manifestFile.refreshLocal(IResource.DEPTH_INFINITE, null);
	// Utils.waitForJobsToComplete();
	// }

	private boolean hasDependencyOnAJDE(IProject project) {
		ManifestEditor manEd = AJDTUtils
				.getAndPrepareToChangePDEModel(project);
		if (manEd == null) {
			return false;
		}
		IPluginModel model = (IPluginModel) manEd.getAggregateModel();
		IPluginImport[] imports = model.getPluginBase().getImports();

		for (int i = 0; i < imports.length; i++) {
			IPluginImport importObj = imports[i];
			if (importObj.getId().equals(AspectJPlugin.RUNTIME_PLUGIN_ID)) {
				return true;
			}
		}
		return false;
	}

	public static class MyJobChangeListener implements IJobChangeListener {

		private List scheduledBuilds = new ArrayList();

		public void aboutToRun(IJobChangeEvent event) {
		}

		public void awake(IJobChangeEvent event) {
		}

		public void done(IJobChangeEvent event) {
			if (event.getJob().getPriority() == Job.BUILD) {
				System.out.println(">> finished a build");
				scheduledBuilds.remove(event.getJob());
			}

		}

		public void running(IJobChangeEvent event) {
		}

		public void scheduled(IJobChangeEvent event) {
			if (event.getJob().getPriority() == Job.BUILD) {
				System.out.println(">> scheduled a build");
				scheduledBuilds.add(event.getJob());
			}
		}

		public void sleeping(IJobChangeEvent event) {
		}

		public boolean buildsAreScheduled() {
			return !(scheduledBuilds.isEmpty());
		}

	}

}
