/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Luzius Meisser  - initial version
 *******************************************************************************/

package org.eclipse.ajdt.ui.tests.testutils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.AspectJTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

/**
 * Provides useful utils when writing test cases.
 * 
 * @author Luzius Meisser
 */
public class Utils{
	
//	public static void blockPreferencesConfigWizard() {
//		Bundle bundle = AspectJUIPlugin.getDefault().getBundle();
//		String version = (String)bundle.getHeaders().get(Constants.BUNDLE_VERSION);
//	}
//	
//	public static void restoreBlockedSettings() {
//		Bundle bundle = AspectJUIPlugin.getDefault().getBundle();
//		String version = (String)bundle.getHeaders().get(Constants.BUNDLE_VERSION);
//	}
	
	public static final String TEST_PROJECTS_FOLDER = "/workspace";

	/**
	 * Imports a specified project from the "test projects" folder.
	 * 
	 * @param projectName, The name of the project to import from the "test projects" directory
	 * @param overwrite, If a project with the name already exists, should its files be overwritten?
	 * @return The requested project if successfully imported, null otherwise
	 * @throws CoreException
	 */
	public static IProject createPredefinedProject(String projectName) throws CoreException{
		Utils.waitForJobsToComplete();
		
		File sourceDir;
		sourceDir = new File(AspectJTestPlugin.getPluginDir() + TEST_PROJECTS_FOLDER + "/" + projectName);
		if ((sourceDir == null) || (!sourceDir.exists()) || (sourceDir.isFile()))
			return null;
		
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject destFolder = root.getProject(projectName);
		
		IOverwriteQuery oq = new IOverwriteQuery(){
			public String queryOverwrite(String input){
				return YES;
				//return overwrite? YES: NO;
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
			//BlockingProgressMonitor monitor = new BlockingProgressMonitor();
			//monitor.reset();
			//project.open(monitor);
			project.open(null);
			//monitor.waitForCompletion();
		}
		
		Utils.waitForJobsToComplete();
		return project;
	}

	public static void deleteProject(IProject project) {
		// make sure nothing is still using the project
		Utils.waitForJobsToComplete();
		
		try {
			// perform the delete
			project.delete(true, true, null);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			System.out.println("***delete failed***");
			e.printStackTrace();
		}
		
		// make sure delete has finished
		Utils.waitForJobsToComplete();
	}
	
	/**
	 * Opens a file in its associated editor.
	 */
	public static IEditorPart openFileInDefaultEditor(IFile file, boolean activate){
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
	 * Opens a file in its associated editor.
	 */
	public static IEditorPart openFileInAspectJEditor(IFile file, boolean activate){
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
	
	private static void initializeHighlightRange(IEditorPart editorPart) {
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
	
	public static void waitForJobsToComplete(){
		try {
			SynchronizationUtils.joinBackgroudActivities();
//		Job job = new Job("Dummy Job"){
//			public IStatus run(IProgressMonitor m){
//				return Status.OK_STATUS;
//			}
//		};
//		job.setPriority(Job.DECORATE);
//		job.setRule(pro);
//	    job.schedule();
//	    try {
//			job.join();
//		} catch (InterruptedException e) {
//			// Do nothing
//		}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public static void setUpPluginEnvironment() throws CoreException {
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

	public static void resetPluginEnvironment() {
		IPreferenceStore ps = AspectJUIPlugin.getDefault().getPreferenceStore();
		ps.setToDefault(AspectJPreferences.PDE_AUTO_IMPORT_CONFIG_DONE);
		ps.setToDefault(AspectJPreferences.ASK_PDE_AUTO_IMPORT);
		ps.setToDefault(AspectJPreferences.DO_PDE_AUTO_IMPORT);
		ps.setToDefault(AspectJPreferences.PDE_AUTO_REMOVE_IMPORT_CONFIG_DONE);
		ps.setToDefault(AspectJPreferences.ASK_PDE_AUTO_REMOVE_IMPORT);
		ps.setToDefault(AspectJPreferences.DO_PDE_AUTO_REMOVE_IMPORT);
	}

}
