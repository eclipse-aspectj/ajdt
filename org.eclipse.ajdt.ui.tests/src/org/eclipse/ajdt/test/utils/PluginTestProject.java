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

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.pde.core.build.IBuildEntry;
import org.eclipse.pde.core.build.IBuildModelFactory;
import org.eclipse.pde.core.plugin.IFragment;
import org.eclipse.pde.core.plugin.IPlugin;
import org.eclipse.pde.core.plugin.IPluginBase;
import org.eclipse.pde.core.plugin.IPluginImport;
import org.eclipse.pde.core.plugin.IPluginLibrary;
import org.eclipse.pde.core.plugin.IPluginReference;
import org.eclipse.pde.internal.PDE;
import org.eclipse.pde.internal.core.ClasspathUtilCore;
import org.eclipse.pde.internal.core.CoreUtility;
import org.eclipse.pde.internal.core.PDEPluginConverter;
import org.eclipse.pde.internal.core.build.WorkspaceBuildModel;
import org.eclipse.pde.internal.core.plugin.WorkspaceFragmentModel;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModel;
import org.eclipse.pde.internal.core.plugin.WorkspacePluginModelBase;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.wizards.IProjectProvider;
import org.eclipse.pde.internal.ui.wizards.plugin.FragmentFieldData;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginClassCodeGenerator;
import org.eclipse.pde.internal.ui.wizards.plugin.PluginFieldData;
import org.eclipse.pde.ui.IFieldData;
import org.eclipse.pde.ui.IFragmentFieldData;
import org.eclipse.pde.ui.IPluginFieldData;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;

/**
 * The base for this class was taken from the
 * org.eclipse.contribution.junit.test project (Gamma/Beck) and then edited from
 * there. The edits have been taken from the execute method in
 * NewProjectCreationOperation which creates a new plugin project.
 * 
 * This creates a plugin project exactly as it would be created by following New
 * --> Project --> plugin project
 *  
 */
public class PluginTestProject {

	private IProject project;

	private IFieldData fData;

	private PluginClassCodeGenerator fGenerator;

	private PluginFieldData pluginFieldData;

	private WorkspacePluginModelBase fModel;

	private IProjectProvider fProjectProvider;

	private String pluginID;

	public PluginTestProject() throws CoreException {
		this("PluginTestProject", "test.plugin", "TestPlugin",
				"TestPlugin.jar", "bin", true);
	}

	public PluginTestProject(String pname) throws CoreException {
		this(pname, "test.plugin", "TestPlugin", "TestPlugin.jar", "bin", true);
	}

	public PluginTestProject(String pname, String pluginID)
			throws CoreException {
		this(pname, pluginID, "TestPlugin", "TestPlugin.jar", "bin", true);
	}

	public PluginTestProject(String pname, String pluginID, String className)
			throws CoreException {
		this(pname, pluginID, className, "TestPlugin.jar", "bin", true);
	}

	public PluginTestProject(String pname, String pluginID, String className,
			String runtimeLibName) throws CoreException {
		this(pname, pluginID, className, runtimeLibName, "bin", true);
	}

	public PluginTestProject(String pname, String pluginID, String className,
			String runtimeLibName, String outputfoldername)
			throws CoreException {
		this(pname, pluginID, className, runtimeLibName, outputfoldername, true);
	}

	public PluginTestProject(String pname, String pluginID, String className,
			String runtimeLibName, String outputfoldername,
			boolean createContent) throws CoreException {

		this.pluginID = pluginID;
		setUpPluginData(pluginID, className, runtimeLibName, outputfoldername);
		fData = pluginFieldData;
		fProjectProvider = new TestProjectProvider(pname);

		project = createProject();

		Utils.waitForJobsToComplete();
		if (project.hasNature(JavaCore.NATURE_ID)) {
			setClasspath(project, fData);
		}

		if (fData instanceof IPluginFieldData
				&& ((IPluginFieldData) fData).doGenerateClass()) {
			generateTopLevelPluginClass(project);
		}

		createManifest(project);
		Utils.waitForJobsToComplete();
		createBuildPropertiesFile(project);
		Utils.waitForJobsToComplete();

		fModel.save();

		if (fData.hasBundleStructure()) {
			String filename = (fData instanceof IFragmentFieldData) ? "fragment.xml" : "plugin.xml"; //$NON-NLS-1$ //$NON-NLS-2$
			PDEPluginConverter.convertToOSGIFormat(project, filename, null);
			Utils.waitForJobsToComplete();
			trimModel(fModel.getPluginBase());
			fModel.save();
			openFile(project.getFile("META-INF/MANIFEST.MF")); //$NON-NLS-1$
		} else {
			openFile((IFile) fModel.getUnderlyingResource());
		}
	}

	// Stuff for plugin projects that I've added..................

	// ----------- FROM HERE ----------------

	private void setUpPluginData(String pluginID, String className,
			String libName, String outputfoldername) {
		pluginFieldData = new PluginFieldData();
		pluginFieldData.setSourceFolderName("src");
		pluginFieldData.setOutputFolderName(outputfoldername);
		pluginFieldData.setId(pluginID);
		pluginFieldData.setClassname(className);
		pluginFieldData.setLibraryName(libName);
	}

	private IProject createProject() throws CoreException {
		IProject project = fProjectProvider.getProject();
		if (!project.exists()) {
			CoreUtility.createProject(project, fProjectProvider
					.getLocationPath(), null);
			project.open(null);
			Utils.waitForJobsToComplete();
		}
		//if (!project.hasNature(PDE.PLUGIN_NATURE))
			//CoreUtility.addNatureToProject(project, PDE.PLUGIN_NATURE, null);
		CoreUtility.addNatureToProject(project, PDE.PLUGIN_NATURE, null);
		Utils.waitForJobsToComplete();
		if (!fData.isSimple() && !project.hasNature(JavaCore.NATURE_ID)) {
			CoreUtility.addNatureToProject(project, JavaCore.NATURE_ID, null);
			Utils.waitForJobsToComplete();
		}
		if (!fData.isSimple()
				&& fData.getSourceFolderName().trim().length() > 0) {
			IFolder folder = project.getFolder(fData.getSourceFolderName());
			if (!folder.exists()) {
				CoreUtility.createFolder(folder, true, true, null);
				Utils.waitForJobsToComplete();
			}
		}
		return project;
	}

	private void setClasspath(IProject project, IFieldData data)
			throws JavaModelException, CoreException {
		// Set output folder
		IJavaProject javaProject = JavaCore.create(project);
		IPath path = project.getFullPath().append(data.getOutputFolderName());
		javaProject.setOutputLocation(path, null);
		Utils.waitForJobsToComplete();
		
		// Set classpath
		IClasspathEntry[] entries = new IClasspathEntry[3];
		path = project.getFullPath().append(data.getSourceFolderName());
		entries[0] = JavaCore.newSourceEntry(path);
		entries[1] = ClasspathUtilCore.createContainerEntry();
		entries[2] = ClasspathUtilCore.createJREEntry();
		javaProject.setRawClasspath(entries, null);
		Utils.waitForJobsToComplete();
	}

	private void generateTopLevelPluginClass(IProject project)
			throws CoreException {
		IPluginFieldData data = (IPluginFieldData) fData;
		fGenerator = new PluginClassCodeGenerator(project, data.getClassname(),
				data);
		fGenerator.generate(null);
		Utils.waitForJobsToComplete();
	}

	private void createManifest(IProject project) throws CoreException {
		if (fData instanceof IFragmentFieldData) {
			fModel = new WorkspaceFragmentModel(project.getFile("fragment.xml")); //$NON-NLS-1$
		} else {
			fModel = new WorkspacePluginModel(project.getFile("plugin.xml")); //$NON-NLS-1$
		}

		IPluginBase pluginBase = fModel.getPluginBase();
		if (!fData.isLegacy())
			pluginBase.setSchemaVersion("3.0"); //$NON-NLS-1$
		pluginBase.setId(fData.getId());
		pluginBase.setVersion(fData.getVersion());
		pluginBase.setName(fData.getName());
		pluginBase.setProviderName(fData.getProvider());
		if (pluginBase instanceof IFragment) {
			IFragment fragment = (IFragment) pluginBase;
			FragmentFieldData data = (FragmentFieldData) fData;
			fragment.setPluginId(data.getPluginId());
			fragment.setPluginVersion(data.getPluginVersion());
			fragment.setRule(data.getMatch());
		} else {
			if (((IPluginFieldData) fData).doGenerateClass())
				((IPlugin) pluginBase).setClassName(((IPluginFieldData) fData)
						.getClassname());
		}
		if (!fData.isSimple()) {
			IPluginLibrary library = fModel.getPluginFactory().createLibrary();
			library.setName(fData.getLibraryName());
			library.setExported(true);
			pluginBase.add(library);
		}

		IPluginReference[] dependencies = getDependencies();
		for (int i = 0; i < dependencies.length; i++) {
			IPluginReference ref = dependencies[i];
			IPluginImport iimport = fModel.getPluginFactory().createImport();
			iimport.setId(ref.getId());
			iimport.setVersion(ref.getVersion());
			iimport.setMatch(ref.getMatch());
			pluginBase.add(iimport);
		}
	}

	private IPluginReference[] getDependencies() {
		ArrayList result = new ArrayList();
		if (fGenerator != null) {
			IPluginReference[] refs = fGenerator.getDependencies();
			for (int i = 0; i < refs.length; i++) {
				result.add(refs[i]);
			}
		}
		return (IPluginReference[]) result.toArray(new IPluginReference[result
				.size()]);
	}

	private void createBuildPropertiesFile(IProject project)
			throws CoreException {
		IFile file = project.getFile("build.properties"); //$NON-NLS-1$
		if (!file.exists()) {
			WorkspaceBuildModel model = new WorkspaceBuildModel(file);
			IBuildModelFactory factory = model.getFactory();
			IBuildEntry binEntry = factory
					.createEntry(IBuildEntry.BIN_INCLUDES);
			binEntry
					.addToken(fData instanceof IFragmentFieldData ? "fragment.xml" //$NON-NLS-1$
							: "plugin.xml"); //$NON-NLS-1$
			if (fData.hasBundleStructure())
				binEntry.addToken("META-INF/"); //$NON-NLS-1$
			if (!fData.isSimple()) {
				binEntry.addToken(fData.getLibraryName());

				IBuildEntry entry = factory.createEntry(IBuildEntry.JAR_PREFIX
						+ fData.getLibraryName());
				String srcFolder = fData.getSourceFolderName().trim();
				if (srcFolder.length() > 0)
					entry.addToken(new Path(srcFolder).addTrailingSeparator()
							.toString());
				else
					entry.addToken("."); //$NON-NLS-1$
				model.getBuild().add(entry);

				entry = factory.createEntry(IBuildEntry.OUTPUT_PREFIX
						+ fData.getLibraryName());
				String outputFolder = fData.getOutputFolderName().trim();
				if (outputFolder.length() > 0)
					entry.addToken(new Path(outputFolder)
							.addTrailingSeparator().toString());
				else
					entry.addToken("."); //$NON-NLS-1$
				model.getBuild().add(entry);
			}
			model.getBuild().add(binEntry);
			model.save();
		}
	}

	private void trimModel(IPluginBase base) throws CoreException {
		base.setId(null);
		base.setVersion(null);
		base.setName(null);
		base.setProviderName(null);

		if (base instanceof IFragment) {
			((IFragment) base).setPluginId(null);
			((IFragment) base).setPluginVersion(null);
			((IFragment) base).setRule(0);
		} else {
			((IPlugin) base).setClassName(null);
		}

		IPluginImport[] imports = base.getImports();
		for (int i = 0; i < imports.length; i++) {
			base.remove(imports[i]);
		}

		IPluginLibrary[] libraries = base.getLibraries();
		for (int i = 0; i < libraries.length; i++) {
			base.remove(libraries[i]);
		}
	}

	private void openFile(final IFile file) {
		final IWorkbenchWindow ww = PDEPlugin.getActiveWorkbenchWindow();
		final IWorkbenchPage page = ww.getActivePage();
		if (page == null)
			return;

		final IWorkbenchPart focusPart = page.getActivePart();
		ww.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				if (focusPart instanceof ISetSelectionTarget) {
					ISelection selection = new StructuredSelection(file);
					((ISetSelectionTarget) focusPart).selectReveal(selection);
				}
				try {
					page.openEditor(new FileEditorInput(file),
							PDEPlugin.MANIFEST_EDITOR_ID);
				} catch (PartInitException e) {
				}
			}
		});
	}

	public class TestProjectProvider implements IProjectProvider {

		private String projName;

		public TestProjectProvider(String projectName) {
			projName = projectName;
		}

		public IProject getProject() {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(
					getProjectName());
		}

		public String getProjectName() {
			return projName;
		}

		public IPath getLocationPath() {
			return getProject().getLocation();
		}
	}

	// ----------- TO HERE ----------------

	public IProject getProject() {
		return project;
	}

	public synchronized void dispose() throws CoreException {
		Utils.deleteProject(project);
	}

	/**
	 * @return Returns the pluginID.
	 */
	public String getPluginID() {
		return pluginID;
	}

	/**
	 * @return Returns the fModel.
	 */
	public WorkspacePluginModelBase getWorkspacePluginModelBase() {
		return fModel;
	}

}

