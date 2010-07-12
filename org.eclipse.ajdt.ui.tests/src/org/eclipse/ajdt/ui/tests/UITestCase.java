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
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.editor.AspectJEditor;
import org.eclipse.ajdt.internal.ui.preferences.AspectJPreferences;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.ajdt.ui.tests.testutils.SynchronizationUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.browser.IBrowserViewerContainer;
import org.eclipse.ui.internal.console.ConsoleView;
import org.eclipse.ui.internal.console.IOConsolePage;
import org.eclipse.ui.internal.views.markers.ExtendedMarkersView;
import org.eclipse.ui.internal.views.markers.ProblemsView;
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
    protected Display display = Display.getCurrent();

	public UITestCase(String name) {
		super(name);
	}
	
	public UITestCase() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
		AllUITests.setupAJDTPlugin();
        System.out.println("------------------------\nStarting " + this.getName());
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

	/**
	 * Delete a project.
	 */	
	protected void deleteProject(final IProject project) {
		// make sure nothing is still using the project
		waitForJobsToComplete();
		
		WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
			protected void execute(IProgressMonitor monitor)
					throws CoreException {
				try {
					// force opening so that project can be deleted without logging (see bug 23629)
					if (project.exists() && !project.isOpen()) {
						project.open(null);
					}
				} catch (CoreException e) {
				}

				int retryCount = 0; // wait 1 minute at most
				while (project.isAccessible() && ++retryCount <= 60) {
					System.out.println("Running GC and waiting 1s..."); //$NON-NLS-1$
					try {
						System.gc();
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
					try {
						// perform the delete
						project.delete(true, false, null);
						project.delete(true, true, null);
					} catch (CoreException e) {
						// just print for info
						System.out
								.println("Retry " + retryCount + ": " + e.getMessage() + " [" + project.getFullPath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					} catch (IllegalArgumentException iae) {
						// just print for info
						System.out
								.println("Retry " + retryCount + ": " + iae.getMessage() + " [" + project.getFullPath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					}
				}
			}
		};
		try {
			op.run(null);
		} catch (InvocationTargetException ex) {
		} catch (InterruptedException e) {
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

	/**
	 * Returns the IWorkspace this test suite is running on.
	 */
	public IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}
	
	public IWorkspaceRoot getWorkspaceRoot() {
		return getWorkspace().getRoot();
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
	
	protected IMarker[] getAllProblemViewMarkers() {
		try {
			ProblemsView problemsView = (ProblemsView) AspectJUIPlugin.getDefault().getActiveWorkbenchWindow().getActivePage().showView("org.eclipse.ui.views.ProblemView");        //$NON-NLS-1$
			Method getAllMarkersMethod = ExtendedMarkersView.class.getDeclaredMethod("getAllMarkers", new Class[]{}); //$NON-NLS-1$
			getAllMarkersMethod.setAccessible(true);
			return (IMarker[]) getAllMarkersMethod.invoke(problemsView, new Object[]{});
		} catch(Exception e) {
			fail("Could not get problem view markers: " + e.getMessage()); //$NON-NLS-1$
			return null;
		}
		
	}

    protected static String getConsoleViewContents() {
        ConsoleView cview = null;
        IViewReference[] views = AspectJUIPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getViewReferences();
        for (int i = 0; i < views.length; i++) {
            if (views[i].getView(false) instanceof ConsoleView) {
                cview = (ConsoleView) views[i].getView(false);
            }
        }
        assertNotNull("Console view should be open", cview); //$NON-NLS-1$
        IOConsolePage page = (IOConsolePage) cview.getCurrentPage();
        TextViewer viewer = page.getViewer();
        return viewer.getDocument().get();
    }

    /**
     * Post a key event (equivalent to posting a key down event then a key up
     * event)
     * 
     * @param c -
     *            the character to post
     */
    protected void postKey(char c) {
        postKeyDown(c);
        postKeyUp(c);
    }

    /**
     * Post a key down event
     * 
     * @param c -
     *            the character to post
     */
    protected void postKeyDown(char c) {
        Event event = new Event();
        event.type = SWT.KeyDown;
        event.character = c;
        display.post(event);
        sleep(10);
    }

    /**
     * Post a key up event
     * 
     * @param c -
     *            the character to post
     */
    protected void postKeyUp(char c) {
        Event event = new Event();
        event.type = SWT.KeyUp;
        event.character = c;
        display.post(event);
        sleep(10);
    }

    /**
     * Post a key event (equivalent to posting a key down event then a key up
     * event)
     * 
     * @param keyCode -
     *            one of the key codes defined int he SWT class
     */
    protected void postKey(int keyCode) {
        postKeyDown(keyCode);
        postKeyUp(keyCode);
    }

    /**
     * Post a key down event
     * 
     * @param keyCode -
     *            one of the key codes defined int he SWT class
     */
    protected void postKeyDown(int keyCode) {
        Event event = new Event();
        event.type = SWT.KeyDown;
        event.keyCode = keyCode;
        display.post(event);
        sleep(10);
    }

    /**
     * Post a key up event
     * 
     * @param keyCode -
     *            one of the key codes defined int he SWT class
     */
    protected void postKeyUp(int keyCode) {
        Event event = new Event();
        event.type = SWT.KeyUp;
        event.keyCode = keyCode;
        display.post(event);
        sleep(10);
    }

    
    protected void sleep() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    protected void sleep(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
        }
    }
    
    public void setAutobuilding(boolean autobuild) throws CoreException {
        IWorkspaceDescription workspaceDesc = AspectJPlugin.getWorkspace().getDescription();
        workspaceDesc.setAutoBuilding(autobuild);
        AspectJPlugin.getWorkspace().setDescription(workspaceDesc);

    }
    
    public boolean isAutobuilding() {
        return AspectJPlugin.getWorkspace().getDescription().isAutoBuilding();
    }

}