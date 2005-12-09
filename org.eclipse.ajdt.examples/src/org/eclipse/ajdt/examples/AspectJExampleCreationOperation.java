/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ajdt.examples;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.buildconfig.DefaultBuildConfigurator;
import org.eclipse.ajdt.ui.buildconfig.IBuildConfiguration;
import org.eclipse.ajdt.ui.buildconfig.IProjectBuildConfigurator;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

public class AspectJExampleCreationOperation implements IRunnableWithProgress {

	private IResource fElementToOpen;

	private AspectJExampleCreationWizardPage[] fPages;

	private IOverwriteQuery fOverwriteQuery;

	/**
	 * Constructor for ExampleProjectCreationOperation
	 */
	public AspectJExampleCreationOperation(
			AspectJExampleCreationWizardPage[] pages,
			IOverwriteQuery overwriteQuery) {
		fElementToOpen = null;
		fPages = pages;
		fOverwriteQuery = overwriteQuery;
	}

	/*
	 * @see IRunnableWithProgress#run(IProgressMonitor)
	 */
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			monitor
					.beginTask(
							AspectJExampleMessages
									.getString("ExampleProjectCreationOperation.op_desc"), fPages.length); //$NON-NLS-1$
			IWorkspaceRoot root = AspectJExamplePlugin.getWorkspace().getRoot();

			for (int i = 0; i < fPages.length; i++) {
				createProject(root, fPages[i], new SubProgressMonitor(monitor,
						1));
			}
		} finally {
			monitor.done();
		}
	}

	public IResource getElementToOpen() {
		return fElementToOpen;
	}

	private void createProject(IWorkspaceRoot root,
			AspectJExampleCreationWizardPage page, IProgressMonitor monitor)
			throws InvocationTargetException, InterruptedException {
		IConfigurationElement desc = page.getConfigurationElement();

		IConfigurationElement[] imports = desc.getChildren("import"); //$NON-NLS-1$
		IConfigurationElement[] natures = desc.getChildren("nature"); //$NON-NLS-1$
		IConfigurationElement[] references = desc.getChildren("references"); //$NON-NLS-1$
		int nImports = (imports == null) ? 0 : imports.length;
		int nNatures = (natures == null) ? 0 : natures.length;
		int nReferences = (references == null) ? 0 : references.length;

		monitor
				.beginTask(
						AspectJExampleMessages
								.getString("ExampleProjectCreationOperation.op_desc_proj"), nImports + 1); //$NON-NLS-1$

		String name = page.getName();

		String[] natureIds = new String[nNatures];
		for (int i = 0; i < nNatures; i++) {
			natureIds[i] = natures[i].getAttribute("id"); //$NON-NLS-1$
		}
		IProject[] referencedProjects = new IProject[nReferences];
		for (int i = 0; i < nReferences; i++) {
			referencedProjects[i] = root.getProject(references[i]
					.getAttribute("id")); //$NON-NLS-1$
		}

		IProject proj = configNewProject(root, name, natureIds,
				referencedProjects, monitor);

		for (int i = 0; i < nImports; i++) {
			doImports(proj, imports[i], new SubProgressMonitor(monitor, 1));
		}

		setSrcFolder(proj);

		String open = desc.getAttribute("open"); //$NON-NLS-1$
		if (open != null && open.length() > 0) {
			IResource fileToOpen = proj.findMember(new Path(open));
			if (fileToOpen != null) {
				fElementToOpen = fileToOpen;
			}
		}

		// set active build configuration
		String value = desc.getAttribute("build"); //$NON-NLS-1$
		if (value != null){
			AspectJPreferences.setActiveBuildConfigurationName(proj,value);
		}
		//In case Build Configurator got initialized before we created the
		// build configuration files,
		//let's reinitailize it
		IProjectBuildConfigurator pbc = DefaultBuildConfigurator.getBuildConfigurator()
				.getProjectBuildConfigurator(proj);
		pbc.reInit();
		

		
		//if it already created a Standard Buildconfiguration despite of there
		// are others, remove it:
		try {
			IResource[] files = proj.members(IResource.FILE);
			int buildfiles = 0;
			for (int i = 0; i < files.length; i++) {
				if ((files[i].getType() != IResource.FOLDER)
						&& IBuildConfiguration.EXTENSION.equals(files[i]
								.getFileExtension())
						&& files[i].exists()) {
					buildfiles++;
				}
			}
			if (buildfiles > 1) {
				IFile f = (IFile)proj.findMember(IBuildConfiguration.STANDARD_BUILD_CONFIGURATION_NAME + "." + IBuildConfiguration.EXTENSION); //$NON-NLS-1$
				IBuildConfiguration bc = pbc.getBuildConfiguration(f);
				if (bc != null){
					pbc.removeBuildConfiguration(bc);
				}
			}
		} catch (CoreException e) {
		}
	}

	private IProject configNewProject(IWorkspaceRoot root, String name,
			String[] natureIds, IProject[] referencedProjects,
			IProgressMonitor monitor) throws InvocationTargetException {
		try {
			IProject project = root.getProject(name);
			if (!project.exists()) {
				project.create(null);
			}
			if (!project.isOpen()) {
				project.open(null);
			}
			IProjectDescription desc = project.getDescription();
			desc.setLocation(null);

			desc.setNatureIds(natureIds);

			desc.setReferencedProjects(referencedProjects);

			project.setDescription(desc, new SubProgressMonitor(monitor, 1));

			// ensure Java builder is removed
			desc = project.getDescription();
			ICommand[] buildCommands = desc.getBuildSpec();
			if (contains(buildCommands, JavaCore.BUILDER_ID)) {
				desc.setBuildSpec(remove(buildCommands, JavaCore.BUILDER_ID));
				project.setDescription(desc, null);
			}

			// add aspectjrt.jar to project classpath
			AspectJUIPlugin.addAjrtToBuildPath(project);

			// add JRE entry to project classpath
			IJavaProject javaProject = JavaCore.create(project);
			try {
				IClasspathEntry[] originalCP = javaProject.getRawClasspath();
				IClasspathEntry jreCP = JavaCore.newContainerEntry(new Path(
						"org.eclipse.jdt.launching.JRE_CONTAINER")); //$NON-NLS-1$

				// Update the raw classpath with the new jreCP entry.
				int originalCPLength = originalCP.length;
				IClasspathEntry[] newCP = new IClasspathEntry[originalCPLength + 1];
				System.arraycopy(originalCP, 0, newCP, 0, originalCPLength);
				newCP[originalCPLength] = jreCP;
				javaProject.setRawClasspath(newCP, new NullProgressMonitor());
			} catch (JavaModelException e) {
				e.printStackTrace();
			}

			return project;
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * Check if the given biuld command list contains a given command
	 */
	private boolean contains(ICommand[] commands, String builderId) {
		boolean found = false;
		for (int i = 0; i < commands.length; i++) {
			if (commands[i].getBuilderName().equals(builderId)) {
				found = true;
				break;
			}
		}
		return found;
	}

	/**
	 * Remove a build command from a list
	 */
	private ICommand[] remove(ICommand[] sourceCommands, String builderId) {
		ICommand[] newCommands = new ICommand[sourceCommands.length - 1];
		int newCommandIndex = 0;
		for (int i = 0; i < sourceCommands.length; i++) {
			if (!sourceCommands[i].getBuilderName().equals(builderId)) {
				newCommands[newCommandIndex++] = sourceCommands[i];
			}
		}
		return newCommands;
	}

	private void doImports(IProject project, IConfigurationElement curr,
			IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		try {
			IPath destPath;
			String name = curr.getAttribute("dest"); //$NON-NLS-1$
			if (name == null || name.length() == 0) {
				destPath = project.getFullPath();
			} else {
				IFolder folder = project.getFolder(name);
				if (!folder.exists()) {
					folder.create(true, true, null);
				}
				destPath = folder.getFullPath();
			}
			String importPath = curr.getAttribute("src"); //$NON-NLS-1$
			if (importPath == null) {
				importPath = ""; //$NON-NLS-1$
				AspectJExamplePlugin
						.log("projectsetup descriptor: import missing"); //$NON-NLS-1$
				return;
			}

			ZipFile zipFile = getZipFileFromPluginDir(importPath);
			importFilesFromZip(zipFile, destPath, new SubProgressMonitor(
					monitor, 1));
		} catch (CoreException e) {
			throw new InvocationTargetException(e);
		}
	}

	/**
	 * If there is a "src" folder in the project, replace the existing classpath entry
	 * with an new entry for this source folder.
	 * @param project
	 */
	private void setSrcFolder(IProject project) {
		try {
			IResource srcFolder = project.findMember("src"); //$NON-NLS-1$
			if ((srcFolder != null)
					&& (srcFolder.getType() == IResource.FOLDER)) {
				IJavaProject javaProject = JavaCore.create(project);
				IClasspathEntry[] javaCP = javaProject.getRawClasspath();
				boolean done = false;
				for (int i = 0; !done && (i < javaCP.length); i++) {
					if (javaCP[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						// replace this entry with our new one
						javaCP[i] = JavaCore.newSourceEntry(srcFolder.getFullPath());
						javaProject.setRawClasspath(javaCP, new NullProgressMonitor());
						done = true;
					}
				}
			}
		} catch (JavaModelException e) {
			e.printStackTrace();
		}
	}

	private ZipFile getZipFileFromPluginDir(String pluginRelativePath)
			throws CoreException {
		try {
			URL starterURL = new URL(AspectJExamplePlugin.getDefault()
					.getBundle().getEntry("/"), pluginRelativePath); //$NON-NLS-1$
			return new ZipFile(Platform.asLocalURL(starterURL).getFile());
		} catch (IOException e) {
			String message = pluginRelativePath + ": " + e.getMessage(); //$NON-NLS-1$
			Status status = new Status(IStatus.ERROR, AspectJExamplePlugin
					.getPluginId(), IStatus.ERROR, message, e);
			throw new CoreException(status);
		}
	}

	private void importFilesFromZip(ZipFile srcZipFile, IPath destPath,
			IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(
				srcZipFile);
		ImportOperation op = new ImportOperation(destPath, structureProvider
				.getRoot(), structureProvider, fOverwriteQuery);
		op.run(monitor);
	}
}