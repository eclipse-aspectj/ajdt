/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation Sebastian
 * Davids <sdavids@gmx.de>bug 38692
 * Luzius Meisser - adjusted for ajdoc 
 ******************************************************************************/
package org.eclipse.ajdt.internal.ajdocexport;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.CoreUtils;
import org.eclipse.ajdt.internal.ui.resources.AspectJImages;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.Launch;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.actions.OpenBrowserUtil;
import org.eclipse.jdt.internal.ui.jarpackager.ConfirmSaveModifiedResourcesDialog;
import org.eclipse.jdt.internal.ui.javadocexport.JavadocExportMessages;
import org.eclipse.jdt.internal.ui.javaeditor.EditorUtility;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Copied from org.eclipse.jdt.internal.ui.javadocexport.JavadocWizard
 * Changes marked with // AspectJ Extension
 */
public class AJdocWizard extends Wizard implements IExportWizard {

//	 AspectJ Extension
	private AJdocTreeWizardPage fJTWPage;
	private AJdocSpecificsWizardPage fJSWPage;
	private AJdocStandardWizardPage fJSpWPage;
	
	private IPath fDestination;

	private boolean fWriteCustom;
	private boolean fOpenInBrowser;

	private final String TREE_PAGE_DESC = "JavadocTreePage"; //$NON-NLS-1$
	private final String SPECIFICS_PAGE_DESC = "JavadocSpecificsPage"; //$NON-NLS-1$
	private final String STANDARD_PAGE_DESC = "JavadocStandardPage"; //$NON-NLS-1$

	private final int YES = 0;
	private final int YES_TO_ALL = 1;
	private final int NO = 2;
	private final int NO_TO_ALL = 3;
	//private final String JAVADOC_ANT_INFORMATION_DIALOG = "javadocAntInformationDialog";//$NON-NLS-1$

	private AJdocOptionsManager fStore;
	private IWorkspaceRoot fRoot;
	private IFile fXmlJavadocFile;

	private static final String ID_JAVADOC_PROCESS_TYPE = "org.eclipse.jdt.ui.javadocProcess"; //$NON-NLS-1$

	public static void openJavadocWizard(AJdocWizard wizard, Shell shell,
			IStructuredSelection selection) {
		wizard.init(PlatformUI.getWorkbench(), selection);

		PixelConverter converter = new PixelConverter(shell);

		WizardDialog dialog = new WizardDialog(shell, wizard);
		dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(100),
				converter.convertHeightInCharsToPixels(20));
		dialog.open();
	}

	public AJdocWizard() {
		this(null);
	}

	public AJdocWizard(IFile xmlJavadocFile) {
		super();
		setDefaultPageImageDescriptor(AspectJImages.W_EXPORT_AJDOC.getImageDescriptor());
//		 AspectJ Extension - message
		setWindowTitle(UIMessages.ajdocWizard_javadocwizard_title);
		setDialogSettings(JavaPlugin.getDefault().getDialogSettings());

		fRoot = ResourcesPlugin.getWorkspace().getRoot();
		fXmlJavadocFile = xmlJavadocFile;

		fWriteCustom = false;
	}

	/*
	 * @see IWizard#performFinish()
	 */
	public boolean performFinish() {

		IJavaProject[] checkedProjects = fJTWPage.getCheckedProjects();
		updateStore(checkedProjects);

		//If the wizard was not launched from an ant file store the setttings
		if (fXmlJavadocFile == null) {
			fStore.updateDialogSettings(getDialogSettings(), checkedProjects);
		}

		// Wizard will not run with unsaved files.
		if (!checkPreconditions(fStore.getSourceElements())) {
			return false;
		}

		fDestination = new Path(fStore.getDestination());
		fDestination.toFile().mkdirs();

		fOpenInBrowser = fStore.doOpenInBrowser();

		//Ask if you wish to set the javadoc location for the projects (all) to
		//the location of the newly generated javadoc
		if (fStore.isFromStandard()) {
			try {

				URL newURL = fDestination.toFile().toURL();
				List projs = new ArrayList();
				//get javadoc locations for all projects
				for (int i = 0; i < checkedProjects.length; i++) {
					IJavaProject curr = checkedProjects[i];
					URL currURL = JavaUI.getProjectJavadocLocation(curr);
					if (!newURL.equals(currURL)) { // currURL can be null
						//if not all projects have the same javadoc location
						// ask if you want to change
						//them to have the same javadoc location
						projs.add(curr);
					}
				}
				if (!projs.isEmpty()) {
					setAllJavadocLocations((IJavaProject[]) projs
							.toArray(new IJavaProject[projs.size()]), newURL);
				}
			} catch (MalformedURLException e) {
				JavaPlugin.log(e);
			}
		}

//		 AspectJ Extension - commenting out unused code
//		if (fJSWPage.generateAnt()) {
//			//@Improve: make a better message
//			OptionalMessageDialog
//					.open(
//							JAVADOC_ANT_INFORMATION_DIALOG,
//							getShell(),
//							JavadocExportMessages
//									.getString("AJdocWizard.antInformationDialog.title"), null, JavadocExportMessages.getString("AJdocWizard.antInformationDialog.message"), MessageDialog.INFORMATION, new String[] { IDialogConstants.OK_LABEL }, 0); //$NON-NLS-1$ //$NON-NLS-2$
//			try {
//				fStore.createXML(checkedProjects);
//			} catch (CoreException e) {
//				ExceptionHandler
//						.handle(
//								e,
//								getShell(),
//								JavadocExportMessages
//										.getString("AJdocWizard.error.writeANT.title"), JavadocExportMessages.getString("AJdocWizard.error.writeANT.message")); //$NON-NLS-1$ //$NON-NLS-2$
//			}
//		}

		if (!executeJavadocGeneration())
			return false;

		return true;
	}

	private void updateStore(IJavaProject[] checkedProjects) {
		//writes the new settings to store
		fJTWPage.updateStore(checkedProjects);
		fJSpWPage.updateStore();
		fJSWPage.updateStore();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizard#performCancel()
	 */
	public boolean performCancel() {

		IJavaProject[] checkedProjects = fJTWPage.getCheckedProjects();
		updateStore(checkedProjects);

		//If the wizard was not launched from an ant file store the setttings
		if (fXmlJavadocFile == null) {
			fStore.updateDialogSettings(getDialogSettings(), checkedProjects);
		}
		return super.performCancel();
	}

	private void setAllJavadocLocations(IJavaProject[] projects, URL newURL) {
		Shell shell = getShell();
		Image image = shell == null ? null : shell.getDisplay().getSystemImage(
				SWT.ICON_QUESTION);
		String[] buttonlabels = new String[] { IDialogConstants.YES_LABEL,
				IDialogConstants.YES_TO_ALL_LABEL, IDialogConstants.NO_LABEL,
				IDialogConstants.NO_TO_ALL_LABEL };

		for (int j = 0; j < projects.length; j++) {
			IJavaProject iJavaProject = projects[j];
//			 AspectJ Extension - message
			String message= Messages.format(JavadocExportMessages.JavadocWizard_updatejavadoclocation_message, new String[] { iJavaProject.getElementName(), fDestination.toOSString()}); 
			MessageDialog dialog= new MessageDialog(shell, JavadocExportMessages.JavadocWizard_updatejavadocdialog_label, image, message, 4, buttonlabels, 1);

			switch (dialog.open()) {
			case YES:
				JavaUI.setProjectJavadocLocation(iJavaProject, newURL);
				break;
			case YES_TO_ALL:
				for (int i = j; i < projects.length; i++) {
					iJavaProject = projects[i];
					JavaUI.setProjectJavadocLocation(iJavaProject, newURL);
					j++;
				}
				break;
			case NO_TO_ALL:
				j = projects.length;
				break;
			case NO:
			default:
				break;
			}
		}
	}

	// AspectJ Extension - this method has been changed to get the arguments
	// required for generating ajdoc i.e. location of tools.jar, ajde.jar etc.
	private boolean executeJavadocGeneration() {

		Process process = null;
		try {
			List userVmArgs = new ArrayList();
			List progArgs = new ArrayList();

			fStore.getArgumentArray(userVmArgs, progArgs);

			File file = File.createTempFile("javadoc-arguments", ".tmp"); //$NON-NLS-1$//$NON-NLS-2$
			
			String jreDir = JavaRuntime.getDefaultVMInstall().getInstallLocation().getAbsolutePath();
			String aspectjrtDir = CoreUtils.getAspectjrtClasspath();
			String aspectjtoolsDir = ""; //$NON-NLS-1$
			URL ajdeURL = Platform.getBundle(AspectJPlugin.TOOLS_PLUGIN_ID).getEntry("ajde.jar"); //$NON-NLS-1$
			URL coreURL = Platform.getBundle("org.eclipse.core.runtime").getEntry("runtime.jar"); //$NON-NLS-1$ //$NON-NLS-2$
			if (coreURL==null) {
				// From Eclipse 3.1M6 onwards, the runtime plugin is itself a JAR file
				coreURL = Platform.getBundle("org.eclipse.core.runtime").getEntry("/"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
			try {
				File ajdeFile = new File(Platform.asLocalURL(ajdeURL).getFile());
				if (ajdeFile.exists()) {
					aspectjtoolsDir += ajdeFile.getAbsolutePath();
				}
				File coreFile = new File(Platform.asLocalURL(coreURL).getFile());
				if (coreFile.exists()) {
					aspectjtoolsDir += File.pathSeparator + coreFile.getAbsolutePath();
				}
			} catch (IOException e) {
			}
			
			List vmArgs = new ArrayList();
			String[] contentsOfJREDir = JavaRuntime.getDefaultVMInstall().getInstallLocation().list();
			boolean foundJavaCmd = false;
			for (int i = 0; i < contentsOfJREDir.length; i++) {
				if (contentsOfJREDir[i].equals("bin")) { //$NON-NLS-1$
					vmArgs.add(jreDir + File.separator + "bin" + File.separator + "java"); //$NON-NLS-1$ //$NON-NLS-2$
					foundJavaCmd = true;
					break;
				}
			}
			if (!foundJavaCmd) {
				vmArgs.add(jreDir + File.separator + "jre" + File.separator + "bin" + File.separator + "java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$				
			}
			String toolsDir = jreDir + File.separator + "lib" + File.separator + "tools.jar"; //$NON-NLS-1$ //$NON-NLS-2$
			boolean noXmx = true;
			for (Iterator iter = userVmArgs.iterator(); iter.hasNext();) {
				String element = (String) iter.next();
				if (element.startsWith("-Xmx")) { //$NON-NLS-1$
					noXmx = false;
				} 
				if (element.indexOf("tools.jar") != -1) { //$NON-NLS-1$
					toolsDir = element;
				} else {
					vmArgs.add(element);
				}
			}
			if (noXmx) {
				// user didn't specify max heap size, so provide our own setting (from ajdoc.bat)
				vmArgs.add("-Xmx64M"); //$NON-NLS-1$
			}
			vmArgs.add("-classpath"); //$NON-NLS-1$
			
			vmArgs.add(aspectjtoolsDir + File.pathSeparator + toolsDir + File.pathSeparator + aspectjrtDir);
			vmArgs.add("org.aspectj.tools.ajdoc.Main"); //$NON-NLS-1$
			
			for (int i = 0; i < progArgs.size(); i++) {
				String curr = (String) progArgs.get(i);	
				vmArgs.add(checkForSpaces(curr));
			}
			
			String[] args = (String[]) vmArgs
					.toArray(new String[vmArgs.size()]);
			
			process = Runtime.getRuntime().exec(args);
			if (process != null) {
				// construct a formatted command line for the process properties
				StringBuffer buf = new StringBuffer();
				for (int i = 0; i < args.length; i++) {
					buf.append(args[i]);
					buf.append(' ');
				}
				
				IDebugEventSetListener listener= new
				  JavadocDebugEventListener(getShell().getDisplay(), file);
				DebugPlugin.getDefault().addDebugEventListener(listener);

				ILaunchConfigurationWorkingCopy wc = null;
				try {
					ILaunchConfigurationType lcType = DebugPlugin
							.getDefault()
							.getLaunchManager()
							.getLaunchConfigurationType(
									IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
					String name = JavadocExportMessages.JavadocWizard_launchconfig_name;
					wc = lcType.newInstance(null, name);
					wc.setAttribute(IDebugUIConstants.ATTR_PRIVATE, true);

					ILaunch newLaunch = new Launch(wc, ILaunchManager.RUN_MODE,
							null);
//					 AspectJ Extension - message
					IProcess iprocess = DebugPlugin
							.newProcess(
									newLaunch,
									process,
									UIMessages.ajdocWizard_ajdocprocess_label);
					iprocess
							.setAttribute(IProcess.ATTR_CMDLINE, buf.toString());
					iprocess.setAttribute(IProcess.ATTR_PROCESS_TYPE,
							ID_JAVADOC_PROCESS_TYPE);

					DebugPlugin.getDefault().getLaunchManager().addLaunch(
							newLaunch);

				} catch (CoreException e) {
//					AspectJ Extension - message
					String title = UIMessages.ajdocWizard_error_title;
					String message = UIMessages.ajdocWizard_launch_error_message;
					ExceptionHandler.handle(e, getShell(), title, message);
				}

				return true;

			}
		} catch (IOException e) {
//			 AspectJ Extension - message
			String title = UIMessages.ajdocWizard_error_title;
			String message = UIMessages.ajdocWizard_exec_error_message;

			IStatus status = new Status(IStatus.ERROR, JavaUI.ID_PLUGIN,
					IStatus.ERROR, e.getMessage(), e);
			ExceptionHandler.handle(new CoreException(status), getShell(),
					title, message);
			return false;
		}
		return false;

	}

	private String checkForSpaces(String curr) {
		if (curr.indexOf(' ') == -1) {
			return curr;
		}
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < curr.length(); i++) {
			char ch = curr.charAt(i);
			if (ch == '\\' || ch == '\'') {
				buf.append('\\');
			}
			buf.append(ch);
		}
		return buf.toString();
	}

	/**
	 * Creates a list of all CompilationUnits and extracts from that list a list
	 * of dirty files. The user is then asked to confirm if those resources
	 * should be saved or not.
	 * 
	 * @param elements
	 * @return <code>true</code> if all preconditions are satisfied otherwise
	 *         false
	 */
	private boolean checkPreconditions(IJavaElement[] elements) {

		ArrayList resources = new ArrayList();
		for (int i = 0; i < elements.length; i++) {
			if (elements[i] instanceof ICompilationUnit) {
				resources.add(elements[i].getResource());
			}
		}

		//message could be null
		IFile[] unSavedFiles = getUnsavedFiles(resources);
		return saveModifiedResourcesIfUserConfirms(unSavedFiles);
	}

	/**
	 * Returns the files which are not saved and which are part of the files
	 * being exported.
	 * 
	 * @param resources
	 * @return an array of unsaved files
	 */
	private IFile[] getUnsavedFiles(List resources) {
		IEditorPart[] dirtyEditors = JavaPlugin.getDirtyEditors();
		Set unsavedFiles = new HashSet(dirtyEditors.length);
		if (dirtyEditors.length > 0) {
			for (int i = 0; i < dirtyEditors.length; i++) {
				if (dirtyEditors[i].getEditorInput() instanceof IFileEditorInput) {
					IFile dirtyFile = ((IFileEditorInput) dirtyEditors[i]
							.getEditorInput()).getFile();
					if (resources.contains(dirtyFile)) {
						unsavedFiles.add(dirtyFile);
					}
				}
			}
		}
		return (IFile[]) unsavedFiles.toArray(new IFile[unsavedFiles.size()]);
	}

	/**
	 * Asks to confirm to save the modified resources and save them if OK is
	 * pressed. Must be run in the display thread.
	 * 
	 * @param dirtyFiles
	 * @return true if user pressed OK and save was successful.
	 */
	private boolean saveModifiedResourcesIfUserConfirms(IFile[] dirtyFiles) {
		if (confirmSaveModifiedResources(dirtyFiles)) {
			try {
				if (saveModifiedResources(dirtyFiles))
					return true;
			} catch (CoreException e) {
//				 AspectJ Extension - message
//				ExceptionHandler
//						.handle(
//								e,
//								getShell(),
//								JavadocExportMessages
//										.getString("AJdocWizard.saveresourcedialogCE.title"), JavadocExportMessages.getString("AJdocWizard.saveresourcedialogCE.message")); //$NON-NLS-1$ //$NON-NLS-2$
				ExceptionHandler.handle(e, getShell(), JavadocExportMessages.JavadocWizard_saveresourcedialogCE_title, JavadocExportMessages.JavadocWizard_saveresourcedialogCE_message); 

			} catch (InvocationTargetException e) {
//				 AspectJ Extension - message
//				ExceptionHandler
//						.handle(
//								e,
//								getShell(),
//								JavadocExportMessages
//										.getString("AJdocWizard.saveresourcedialogITE.title"), JavadocExportMessages.getString("AJdocWizard.saveresourcedialogITE.message")); //$NON-NLS-1$ //$NON-NLS-2$
				ExceptionHandler.handle(e, getShell(), JavadocExportMessages.JavadocWizard_saveresourcedialogITE_title, JavadocExportMessages.JavadocWizard_saveresourcedialogITE_message); 

			}
		}
		return false;
	}

	/**
	 * Asks the user to confirm to save the modified resources.
	 * 
	 * @param dirtyFiles
	 * @return true if user pressed OK.
	 */
	private boolean confirmSaveModifiedResources(IFile[] dirtyFiles) {
		if (dirtyFiles == null || dirtyFiles.length == 0)
			return true;

		// Get display for further UI operations
		Display display = getShell().getDisplay();
		if (display == null || display.isDisposed())
			return false;

		// Ask user to confirm saving of all files
		final ConfirmSaveModifiedResourcesDialog dlg = new ConfirmSaveModifiedResourcesDialog(
				getShell(), dirtyFiles);
		final int[] intResult = new int[1];
		Runnable runnable = new Runnable() {
			public void run() {
				intResult[0] = dlg.open();
			}
		};
		display.syncExec(runnable);

		return intResult[0] == IDialogConstants.OK_ID;
	}

	/**
	 * Save all of the editors in the workbench. Must be run in the display
	 * thread.
	 * 
	 * @param dirtyFiles
	 * @return true if successful.
	 * @throws CoreException
	 * @throws InvocationTargetException
	 */
	private boolean saveModifiedResources(final IFile[] dirtyFiles)
			throws CoreException, InvocationTargetException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		boolean autoBuild = description.isAutoBuilding();
		description.setAutoBuilding(false);
		try {
			workspace.setDescription(description);
			// This save operation can not be cancelled.
			try {
				PlatformUI.getWorkbench().getProgressService().runInUI(
						PlatformUI.getWorkbench().getProgressService(),
						createSaveModifiedResourcesRunnable(dirtyFiles),
						workspace.getRoot());
			} finally {
				description.setAutoBuilding(autoBuild);
				workspace.setDescription(description);
			}
		} catch (InterruptedException ex) {
			return false;
		}
		return true;
	}

	private IRunnableWithProgress createSaveModifiedResourcesRunnable(
			final IFile[] dirtyFiles) {
		return new IRunnableWithProgress() {
			public void run(IProgressMonitor pm) {
				if (pm == null) {
					pm = new NullProgressMonitor();
				}
				IEditorPart[] editorsToSave = JavaPlugin.getDirtyEditors();
//				String name = JavadocExportMessages
//						.getString("AJdocWizard.savetask.name"); //$NON-NLS-1$
				String name= JavadocExportMessages.JavadocWizard_savetask_name; 
				pm.beginTask(name, editorsToSave.length);
				try {
					List dirtyFilesList = Arrays.asList(dirtyFiles);
					for (int i = 0; i < editorsToSave.length; i++) {
						if (editorsToSave[i].getEditorInput() instanceof IFileEditorInput) {
							IFile dirtyFile = ((IFileEditorInput) editorsToSave[i]
									.getEditorInput()).getFile();
							if (dirtyFilesList.contains((dirtyFile)))
								editorsToSave[i].doSave(new SubProgressMonitor(
										pm, 1));
						}
						pm.worked(1);
					}
				} finally {
					pm.done();
				}
			}
		};
	}

	/*
	 * @see IWizard#addPages()
	 */
	public void addPages() {

		fJTWPage = new AJdocTreeWizardPage(TREE_PAGE_DESC, fStore);
		fJSWPage = new AJdocSpecificsWizardPage(SPECIFICS_PAGE_DESC,fJTWPage, fStore);
		fJSpWPage= new AJdocStandardWizardPage(STANDARD_PAGE_DESC, fJTWPage, fStore);

		super.addPage(fJTWPage);
		super.addPage(fJSWPage);
		super.addPage(fJSpWPage);
		
		fJTWPage.init();
		fJSWPage.init();
		fJSpWPage.init();

	}

	public void init(IWorkbench workbench,
			IStructuredSelection structuredSelection) {
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		List selected = Collections.EMPTY_LIST;
		if (window != null) {
			ISelection selection = window.getSelectionService().getSelection();
			if (selection instanceof IStructuredSelection) {
				selected = ((IStructuredSelection) selection).toList();
			} else {
				IJavaElement element = EditorUtility.getActiveEditorJavaInput();
				if (element != null) {
					selected = new ArrayList();
					selected.add(element);
				}
			}
		}
		fStore = new AJdocOptionsManager(fXmlJavadocFile,
				getDialogSettings(), selected);
	}

	private void refresh(IPath path) {
		if (fRoot.findContainersForLocation(path).length > 0) {
			try {
				fRoot.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				JavaPlugin.log(e);
			}
		}
	}

	private void spawnInBrowser(Display display) {
		if (fOpenInBrowser) {
			try {
				IPath indexFile = fDestination.append("index.html"); //$NON-NLS-1$
				URL url = indexFile.toFile().toURL();
				OpenBrowserUtil.open(url, display, getWindowTitle());
			} catch (MalformedURLException e) {
				JavaPlugin.log(e);
			}
		}
	}

	private class JavadocDebugEventListener implements IDebugEventSetListener {
		private Display fDisplay;

		private File fFile;

		public JavadocDebugEventListener(Display display, File file) {
			fDisplay = display;
			fFile = file;
		}

		public void handleDebugEvents(DebugEvent[] events) {
			for (int i = 0; i < events.length; i++) {
				if (events[i].getKind() == DebugEvent.TERMINATE) {
					try {
						if (!fWriteCustom) {
							fFile.delete();
							refresh(fDestination); //If destination of javadoc
												   // is in workspace then
												   // refresh workspace
							spawnInBrowser(fDisplay);
						}
					} finally {
						DebugPlugin.getDefault().removeDebugEventListener(this);
					}
					return;
				}
			}
		}
	}

	public IWizardPage getNextPage(IWizardPage page) {
		if (page instanceof AJdocTreeWizardPage) {
			return fJSWPage;
		} else if (page instanceof AJdocSpecificsWizardPage) {
			return null;
		} else
			return null;
	}

	public IWizardPage getPreviousPage(IWizardPage page) {
		if (page instanceof AJdocSpecificsWizardPage) {
			return fJSWPage;
		} else if (page instanceof AJdocTreeWizardPage) {
			return null;
		} else
			return null;
	}

}