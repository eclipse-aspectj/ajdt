/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sian January  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import junit.framework.TestCase;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.browser.IBrowserViewerContainer;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;


/**
 * Superclass for all the UI tests, with several utility methods
 */
public abstract class UITestCase extends TestCase {

	public static final String TEST_PROJECTS_FOLDER = "/workspace"; //$NON-NLS-1$

	public UITestCase(String name) {
		super(name);
	}
	
	public UITestCase() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		AllUITests.setupAJDTPlugin();
	}
	
	protected void tearDown() throws Exception {
		super.tearDown();
		waitForJobsToComplete();
		closeAllEditors();
		ILaunchConfiguration[] launchConfigurations = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations();
		for (int i = 0; i < launchConfigurations.length; i++) {
			ILaunchConfiguration configuration = launchConfigurations[i];
			configuration.delete();
		}
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < allProjects.length; i++) {
			IProject project = allProjects[i];
			deleteProject(project);
		}
	}
	
	/**
	 * Imports a specified project from the "test projects" folder.
	 * 
	 * @param projectName, The name of the project to import from the "test projects" directory
	 * @param overwrite, If a project with the name already exists, should its files be overwritten?
	 * @return The requested project if successfully imported, null otherwise
	 * @throws CoreException
	 */
	protected IProject createPredefinedProject(String projectName) throws CoreException{
		waitForJobsToComplete();
		
		File sourceDir;
		sourceDir = new File(AspectJTestPlugin.getPluginDir() + TEST_PROJECTS_FOLDER + "/" + projectName); //$NON-NLS-1$
		if ((sourceDir == null) || (!sourceDir.exists()) || (sourceDir.isFile()))
			return null;
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject destFolder = root.getProject(projectName);
		
		IOverwriteQuery oq = new IOverwriteQuery(){
			public String queryOverwrite(String input){
				return YES;
			}
		};
		ImportOperation impop = new ImportOperation(destFolder.getFullPath(), sourceDir, FileSystemStructureProvider.INSTANCE, oq);
		impop.setCreateContainerStructure(false);
		try {
			impop.run(null);
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
			return null;
		} catch (InterruptedException e1) {
			e1.printStackTrace();
			return null;
		}
		IProject project = root.getProject(destFolder.getName());
		// if project isn't open, then open it
		if (project != null && !(project.isOpen())) {
			project.open(null);
		}
		
		waitForJobsToComplete();
		return project;
	}

	protected void deleteProject(IProject project) {
		// make sure nothing is still using the project
		waitForJobsToComplete();
		String projectName = project.getName();
		try {
			// perform the delete
			project.delete(true, true, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			System.out.println("***delete of project " + projectName + " failed***"); //$NON-NLS-1$
			e.printStackTrace();
		}
		
		// make sure delete has finished
		waitForJobsToComplete();
	}

	/**
	 * Opens a file in its associated editor.
	 */
	protected IEditorPart openFileInDefaultEditor(IFile file, boolean activate){
		if (file != null) {
			IWorkbenchPage p= JavaPlugin.getActivePage();
			if (p != null) {
				try {
					IEditorPart editorPart= IDE.openEditor(p, file, activate);
					initializeHighlightRange(editorPart);
					return editorPart;
				} catch (PartInitException e) {
				}
			}
		}
		return null;
	}

	/**
	 * Opens a file in the AspectJ editor
	 */
	protected IEditorPart openFileInAspectJEditor(IFile file, boolean activate){
		if (file != null) {
			IWorkbenchPage p= JavaPlugin.getActivePage();
			if (p != null) {
				try {
					IEditorPart editorPart= IDE.openEditor(p, file, AspectJEditor.ASPECTJ_EDITOR_ID, activate);
					initializeHighlightRange(editorPart);
					return editorPart;
				} catch (PartInitException e) {
				}
			}
		}
		return null;
	}

	private void initializeHighlightRange(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			IAction toggleAction= editorPart.getEditorSite().getActionBars().getGlobalActionHandler(ITextEditorActionDefinitionIds.TOGGLE_SHOW_SELECTED_ELEMENT_ONLY);
			if (toggleAction != null && toggleAction.isEnabled() && toggleAction.isChecked()) {
				if (toggleAction instanceof TextEditorAction) {
					// Reset the action 
					((TextEditorAction)toggleAction).setEditor(null);
					// Restore the action 
					((TextEditorAction)toggleAction).setEditor((ITextEditor)editorPart);
				} else {
					// Uncheck 
					toggleAction.run();
					// Check
					toggleAction.run();
				}
			}
		}
	}

	protected void waitForJobsToComplete(){
		SynchronizationUtils.joinBackgroudActivities();
	}

	protected void setUpPluginEnvironment() {
		// set the project up so that when asked, the pde plugin
		// is added automatically and the preference configurations
		// have all been set up (therefore don't need user
		// interaction.
		AspectJPreferences.setAskPDEAutoImport(false);
		AspectJPreferences.setDoPDEAutoImport(true);
		AspectJPreferences.setPDEAutoImportConfigDone(true);
		AspectJPreferences.setAskPDEAutoRemoveImport(false);
		AspectJPreferences.setDoPDEAutoRemoveImport(true);
		AspectJPreferences.setPDEAutoRemoveImportConfigDone(true);
	}

	protected void resetPluginEnvironment() {
		IPreferenceStore ps = AspectJUIPlugin.getDefault().getPreferenceStore();
		ps.setToDefault(AspectJPreferences.PDE_AUTO_IMPORT_CONFIG_DONE);
		ps.setToDefault(AspectJPreferences.ASK_PDE_AUTO_IMPORT);
		ps.setToDefault(AspectJPreferences.DO_PDE_AUTO_IMPORT);
		ps.setToDefault(AspectJPreferences.PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE);
		ps.setToDefault(AspectJPreferences.ASK_PDE_AUTO_REMOVE_IMPORT);
		ps.setToDefault(AspectJPreferences.DO_PDE_AUTO_REMOVE_IMPORT);
	}

	/**
	 * Closes all open editors without saving
	 */
	protected void closeAllEditors() {
		IEditorReference[] editors = AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i = 0; i < editors.length; i++) {
			IEditorPart editor = editors[i].getEditor(false);
			if(editor instanceof ITextEditor) {
				((ITextEditor)editor).close(false);
			} else if (editor instanceof FormEditor) {
				((FormEditor)editor).close(false);
			} else if (editor instanceof IBrowserViewerContainer) {
				((IBrowserViewerContainer)editor).close();
			}
		}
	}

	/**
	 * Recursively delete the given directory and all its children
	 * @param f
	 */
	protected void deleteDir(File f) {
        try {
			if (!f.getCanonicalFile().equals(f.getAbsoluteFile())) {
			    // dont follow symbolic links
			    return;
			}
		} catch (IOException e) {
			return;
		}
		File[] files = f.listFiles();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				deleteDir(files[i]);
			} else {
				files[i].delete();
			}
		}
		f.delete();
	}


	protected String readFile(IFile file) throws Exception {
		StringBuffer contents = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(file
				.getContents()));
		String line = br.readLine();
		while (line != null) {
			contents.append(line);
			line = br.readLine();
		}
		br.close();
		return contents.toString();
	}

}