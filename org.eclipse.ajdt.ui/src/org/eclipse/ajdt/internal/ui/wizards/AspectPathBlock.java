/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.launching.LaunchConfigurationManagementUtils;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDisposer;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElementAttribute;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListLabelProvider;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ClasspathOrderingWorkbookPage;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbench;

/**
 * @author gharley
 */
public class AspectPathBlock {
	private IWorkspaceRoot fWorkspaceRoot;

	private ListDialogField fAspectPathList;

	private StatusInfo fAspectPathStatus;

	private StatusInfo fOutputFolderStatus;

	private StatusInfo fBuildPathStatus;

	private IJavaProject fCurrJProject;

	private IPath fOutputLocationPath;

	private IStatusChangeListener fContext;

	private int fPageIndex;

	private AspectPathLibrariesWorkbookPage fLibrariesPage;

	private BuildPathBasePage fCurrPage;

	private List existingAspectPath;

	private Control fSWTControl;

	private final int IDX_ADDJAR = 3;

	private final int IDX_ADDEXT = 4;

	private final int IDX_ADDVAR = 5;

	private final int IDX_ADDFOL = 6;

	private final int IDX_REMOVE = 8;

	public AspectPathBlock(IStatusChangeListener context, int pageToShow) {
		fWorkspaceRoot = AspectJPlugin.getWorkspace().getRoot();
		fContext = context;
		fPageIndex = pageToShow;
		fLibrariesPage = null;
		fCurrPage = null;

		LibrariesAdapter adapter = new LibrariesAdapter();
		String[] buttonLabels = new String[] {
				/* 0 */UIMessages.InPathBlock_order_up_button,
				/* 1 */UIMessages.InPathBlock_order_down_button,
				null,
				/* IDX_ADDJAR*/UIMessages.InPathLibrariesWorkbookPage_libraries_addjar_button,
				/* IDX_ADDEXT */UIMessages.InPathLibrariesWorkbookPage_libraries_addextjar_button,
				/* IDX_ADDVAR */UIMessages.InPathLibrariesWorkbookPage_libraries_addvariable_button,
				/* IDX_ADDFOL */UIMessages.InPathLibrariesWorkbookPage_libraries_addclassfolder_button,
				/* */null,
				/* IDX_REMOVE */UIMessages.InPathLibrariesWorkbookPage_libraries_remove_button };

		fAspectPathList = new ListDialogField(adapter, buttonLabels,
				new CPListLabelProvider());
		fAspectPathList.setDialogFieldListener(adapter);
		fAspectPathList
				.setLabelText(UIMessages.AspectPathBlock_aspectpath_label);
		fAspectPathList.setUpButtonIndex(0);
		fAspectPathList.setDownButtonIndex(1);
		fAspectPathList.setRemoveButtonIndex(IDX_REMOVE);
		fAspectPathList.enableButton(IDX_REMOVE, false);

		fBuildPathStatus = new StatusInfo();
		fAspectPathStatus = new StatusInfo();
		fOutputFolderStatus = new StatusInfo();

		fCurrJProject = null;
	}

	private class LibrariesAdapter implements IDialogFieldListener,
			IListAdapter {

		private final Object[] EMPTY_ARR = new Object[0];

		// -------- IListAdapter --------
		public void customButtonPressed(ListDialogField field, int index) {
			libaryPageCustomButtonPressed(field, index);
		}

		public void selectionChanged(ListDialogField field) {
			libaryPageSelectionChanged(field);
		}

		public void doubleClicked(ListDialogField field) {
		}

		public void keyPressed(ListDialogField field, KeyEvent event) {
			libaryPageKeyPressed(field, event);
		}

		// ---------- IDialogFieldListener --------

		public void dialogFieldChanged(DialogField field) {
			libaryPageDialogFieldChanged(field);
		}
	}

	private void libaryPageCustomButtonPressed(DialogField field, int index) {
		CPListElement[] libentries = null;
		switch (index) {
		case IDX_ADDJAR: /* add jar */
			libentries = openJarFileDialog(null);
			break;
		case IDX_ADDEXT: /* add external jar */
			libentries = openExtJarFileDialog(null);
			break;
		case IDX_ADDVAR: /* add variable */
			libentries = openVariableSelectionDialog(null);
			break;
		case IDX_ADDFOL: /* add folder */
			libentries = openClassFolderDialog(null);
			break;
		case IDX_REMOVE: /* remove */
			removeEntry();
			return;
		}
		if (libentries != null) {
			int nElementsChosen = libentries.length;
			// remove duplicates
			List cplist = fAspectPathList.getElements();
			List elementsToAdd = new ArrayList(nElementsChosen);

			for (int i = 0; i < nElementsChosen; i++) {
				CPListElement curr = libentries[i];
				if (!cplist.contains(curr) && !elementsToAdd.contains(curr)) {
					elementsToAdd.add(curr);
					curr.setAttribute(CPListElement.SOURCEATTACHMENT,
							BuildPathSupport.guessSourceAttachment(curr));
					curr.setAttribute(CPListElement.JAVADOC, JavaUI
							.getLibraryJavadocLocation(curr.getPath()));
				}
			}
			if (!elementsToAdd.isEmpty() && (index == IDX_ADDFOL)) {
				askForAddingExclusionPatternsDialog(elementsToAdd);
			}

			fAspectPathList.addElements(elementsToAdd);
			fAspectPathList
					.postSetSelection(new StructuredSelection(libentries));
		}
	}

	private void libaryPageSelectionChanged(DialogField field) {
		List selElements = fAspectPathList.getSelectedElements();
		fAspectPathList.enableButton(IDX_REMOVE, canRemove(selElements));
	}

	private void askForAddingExclusionPatternsDialog(List newEntries) {
		HashSet modified = new HashSet();
		if (!modified.isEmpty()) {
			String title = UIMessages.InPathLibrariesWorkbookPage_exclusion_added_title;
			String message = UIMessages.InPathLibrariesWorkbookPage_exclusion_added_message;
			MessageDialog.openInformation(getShell(), title, message);
		}
	}

	protected void libaryPageKeyPressed(ListDialogField field, KeyEvent event) {
		if (field == fAspectPathList) {
			if (event.character == SWT.DEL && event.stateMask == 0) {
				List selection = field.getSelectedElements();
				if (canRemove(selection)) {
					removeEntry();
				}
			}
		}
	}

	private boolean canRemove(List selElements) {
		if (selElements.size() == 0) {
			return false;
		}
		for (int i = 0; i < selElements.size(); i++) {
			Object elem = selElements.get(i);
			if (elem instanceof CPListElementAttribute) {
				if (((CPListElementAttribute) elem).getValue() == null) {
					return false;
				}
			} else if (elem instanceof CPListElement) {
				CPListElement curr = (CPListElement) elem;
				if (curr.getParentContainer() != null) {
					return false;
				}
			}
		}
		return true;
	}

	private void libaryPageDialogFieldChanged(DialogField field) {
		if (fCurrJProject != null) {
			// already initialized
			updateAspectpathList();
		}
	}

	private void removeEntry() {
		List selElements = fAspectPathList.getSelectedElements();
		for (int i = selElements.size() - 1; i >= 0; i--) {
			Object elem = selElements.get(i);
			if (elem instanceof CPListElementAttribute) {
				CPListElementAttribute attrib = (CPListElementAttribute) elem;
				attrib.getParent().setAttribute(attrib.getKey(), null);
				selElements.remove(i);
			}
		}
		if (selElements.isEmpty()) {
			fAspectPathList.refresh();
			fAspectPathList.dialogFieldChanged(); // validate
		} else {
			fAspectPathList.removeElements(selElements);
		}
	}

	private void updateAspectpathList() {
		List projelements = fAspectPathList.getElements();

		List cpelements = fAspectPathList.getElements();
		int nEntries = cpelements.size();
		// backwards, as entries will be deleted
		int lastRemovePos = nEntries;
		for (int i = nEntries - 1; i >= 0; i--) {
			CPListElement cpe = (CPListElement) cpelements.get(i);
			int kind = cpe.getEntryKind();
			if (isEntryKind(kind)) {
				if (!projelements.remove(cpe)) {
					cpelements.remove(i);
					lastRemovePos = i;
				}
			}
		}

		cpelements.addAll(lastRemovePos, projelements);

		if (lastRemovePos != nEntries || !projelements.isEmpty()) {
			fAspectPathList.setElements(cpelements);
		}
	}

	private CPListElement[] openClassFolderDialog(CPListElement existing) {
		if (existing == null) {
			IPath[] selected = BuildPathDialogAccess.chooseClassFolderEntries(
					getShell(), fCurrJProject.getPath(),
					getUsedContainers(existing));
			if (selected != null) {
				ArrayList res = new ArrayList();
				for (int i = 0; i < selected.length; i++) {
					IPath curr = selected[i];
					IResource resource = fWorkspaceRoot.findMember(curr);
					if (resource instanceof IContainer) {
						res.add(newCPLibraryElement(resource));
					}
				}
				return (CPListElement[]) res.toArray(new CPListElement[res
						.size()]);
			}
		} else {
			// disabled
		}
		return null;
	}

	private CPListElement[] openJarFileDialog(CPListElement existing) {
		if (existing == null) {
			IPath[] selected = BuildPathDialogAccess.chooseJAREntries(
					getShell(), fCurrJProject.getPath(),
					getUsedContainers(existing));
			if (selected != null) {
				ArrayList res = new ArrayList();

				for (int i = 0; i < selected.length; i++) {
					IPath curr = selected[i];
					IResource resource = fWorkspaceRoot.findMember(curr);
					
					String outJar = AspectJCorePreferences.getProjectOutJar(fCurrJProject.getProject());
					StringBuffer projectOutJar = new StringBuffer();
					projectOutJar.append(fCurrJProject.getPath().toString());
					projectOutJar.append("/"+outJar); //$NON-NLS-1$
					
				    if (resource.getFullPath().toString().equals(projectOutJar.toString())){
				    	MessageDialog.openInformation(getShell(), UIMessages.buildpathwarning_title, UIMessages.addtoaspectpathwarning);
				    } else if (resource instanceof IFile) {
						res.add(newCPLibraryElement(resource));
					}
				}
				return (CPListElement[]) res.toArray(new CPListElement[res
						.size()]);
			}
		} else {
			IPath configured = BuildPathDialogAccess.configureJAREntry(
					getShell(), existing.getPath(), getUsedJARFiles(existing));
			if (configured != null) {
				IResource resource = fWorkspaceRoot.findMember(configured);
				if (resource instanceof IFile) {
					return new CPListElement[] { newCPLibraryElement(resource) };
				}
			}
		}
		return null;
	}

	private IPath[] getUsedContainers(CPListElement existing) {
		ArrayList res = new ArrayList();
		if (fCurrJProject.exists()) {
			try {
				IPath outputLocation = fCurrJProject.getOutputLocation();
				if (outputLocation != null && outputLocation.segmentCount() > 1) { // != Project
					res.add(outputLocation);
				}
			} catch (JavaModelException e) {
			}
		}

		List cplist = fAspectPathList.getElements();
		for (int i = 0; i < cplist.size(); i++) {
			CPListElement elem = (CPListElement) cplist.get(i);
			if (elem.getEntryKind() == IClasspathEntry.CPE_LIBRARY
					&& (elem != existing)) {
				IResource resource = elem.getResource();
				if (resource instanceof IContainer
						&& !resource.equals(existing)) {
					res.add(resource.getFullPath());
				}
			}
		}
		return (IPath[]) res.toArray(new IPath[res.size()]);
	}

	private IPath[] getUsedJARFiles(CPListElement existing) {
		List res = new ArrayList();
		List cplist = fAspectPathList.getElements();
		for (int i = 0; i < cplist.size(); i++) {
			CPListElement elem = (CPListElement) cplist.get(i);
			if (elem.getEntryKind() == IClasspathEntry.CPE_LIBRARY
					&& (elem != existing)) {
				IResource resource = elem.getResource();
				if (resource instanceof IFile) {
					res.add(resource.getFullPath());
				}
			}
		}
		return (IPath[]) res.toArray(new IPath[res.size()]);
	}

	private CPListElement newCPLibraryElement(IResource res) {
		return new CPListElement(fCurrJProject, IClasspathEntry.CPE_LIBRARY,
				res.getFullPath(), res);
	}

	private CPListElement[] openExtJarFileDialog(CPListElement existing) {
		if (existing == null) {
			IPath[] selected = BuildPathDialogAccess
					.chooseExternalJAREntries(getShell());
			if (selected != null) {
				ArrayList res = new ArrayList();
				for (int i = 0; i < selected.length; i++) {
					res.add(new CPListElement(fCurrJProject,
							IClasspathEntry.CPE_LIBRARY, selected[i], null));
				}
				return (CPListElement[]) res.toArray(new CPListElement[res
						.size()]);
			}
		} else {
			IPath configured = BuildPathDialogAccess.configureExternalJAREntry(
					getShell(), existing.getPath());
			if (configured != null) {
				return new CPListElement[] { new CPListElement(fCurrJProject,
						IClasspathEntry.CPE_LIBRARY, configured, null) };
			}
		}
		return null;
	}

	private CPListElement[] openVariableSelectionDialog(CPListElement existing) {
		List existingElements = fAspectPathList.getElements();
		ArrayList existingPaths = new ArrayList(existingElements.size());
		for (int i = 0; i < existingElements.size(); i++) {
			CPListElement elem = (CPListElement) existingElements.get(i);
			if (elem.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
				existingPaths.add(elem.getPath());
			}
		}
		IPath[] existingPathsArray = (IPath[]) existingPaths
				.toArray(new IPath[existingPaths.size()]);

		if (existing == null) {
			IPath[] paths = BuildPathDialogAccess.chooseVariableEntries(
					getShell(), existingPathsArray);
			if (paths != null) {
				ArrayList result = new ArrayList();
				for (int i = 0; i < paths.length; i++) {
					CPListElement elem = new CPListElement(fCurrJProject,
							IClasspathEntry.CPE_VARIABLE, paths[i], null);
					IPath resolvedPath = JavaCore
							.getResolvedVariablePath(paths[i]);
					elem.setIsMissing((resolvedPath == null)
							|| !resolvedPath.toFile().exists());
					if (!existingElements.contains(elem)) {
						result.add(elem);
					}
				}
				return (CPListElement[]) result
						.toArray(new CPListElement[result.size()]);
			}
		} else {
			IPath path = BuildPathDialogAccess.configureVariableEntry(
					getShell(), existing.getPath(), existingPathsArray);
			if (path != null) {
				CPListElement elem = new CPListElement(fCurrJProject,
						IClasspathEntry.CPE_VARIABLE, path, null);
				return new CPListElement[] { elem };
			}
		}
		return null;
	}

	private CPListElement[] openContainerSelectionDialog(CPListElement existing) {
		if (existing == null) {
			IClasspathEntry[] created = BuildPathDialogAccess
					.chooseContainerEntries(getShell(), fCurrJProject,
							getRawClasspath());
			if (created != null) {
				CPListElement[] res = new CPListElement[created.length];
				for (int i = 0; i < res.length; i++) {
					res[i] = new CPListElement(fCurrJProject,
							IClasspathEntry.CPE_CONTAINER,
							created[i].getPath(), null);
				}
				return res;
			}
		} else {
			IClasspathEntry created = BuildPathDialogAccess
					.configureContainerEntry(getShell(), existing
							.getClasspathEntry(), fCurrJProject,
							getRawClasspath());
			if (created != null) {
				CPListElement elem = new CPListElement(fCurrJProject,
						IClasspathEntry.CPE_CONTAINER, created.getPath(), null);
				return new CPListElement[] { elem };
			}
		}
		return null;
	}

	private IClasspathEntry[] getRawClasspath() {
		IClasspathEntry[] currEntries = new IClasspathEntry[fAspectPathList
				.getSize()];
		for (int i = 0; i < currEntries.length; i++) {
			CPListElement curr = (CPListElement) fAspectPathList.getElement(i);
			currEntries[i] = curr.getClasspathEntry();
		}
		return currEntries;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage#isEntryKind(int)
	 */
	public boolean isEntryKind(int kind) {
		return kind == IClasspathEntry.CPE_LIBRARY
				|| kind == IClasspathEntry.CPE_VARIABLE
				|| kind == IClasspathEntry.CPE_CONTAINER;
	}

	/*
	 * @see BuildPathBasePage#getSelection
	 */
	public List getSelection() {
		return fAspectPathList.getSelectedElements();
	}

	/*
	 * @see BuildPathBasePage#setSelection
	 */
	public void setSelection(List selElements) {
		fAspectPathList.selectElements(new StructuredSelection(selElements));
	}

	public void setSelection(List selection, boolean expand) {
		fAspectPathList.selectElements(new StructuredSelection(selection));
	}

	private void buildPathDialogFieldChanged(DialogField field) {
		if (field == fAspectPathList) {
			updateAspectPathStatus();
		}
		doStatusLineUpdate();
	}

	// ---------- util method ------------

	public void init(IJavaProject jproject, IPath outputLocation,
			IClasspathEntry[] aspectpathEntries) {
		fCurrJProject = jproject;
		existingAspectPath = null;
		if (outputLocation == null) {
			outputLocation = getDefaultBuildPath(jproject);
		}

		if (aspectpathEntries != null) {
			existingAspectPath = getExistingEntries(aspectpathEntries);
		}

		if (existingAspectPath == null) {
			existingAspectPath = new ArrayList();
		}

		fOutputLocationPath = new Path(outputLocation.makeRelative().toString())
				.makeAbsolute();

		fAspectPathList.setElements(existingAspectPath);

		if (fLibrariesPage != null) {
			fLibrariesPage.init(fCurrJProject);
		}

		doStatusLineUpdate();
	}

	public void updateAspectPathStatus() {
		fAspectPathStatus.setOK();

		List elements = fAspectPathList.getElements();

		CPListElement entryMissing = null;
		int nEntriesMissing = 0;
		IClasspathEntry[] entries = new IClasspathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPListElement currElement = (CPListElement) elements.get(i);
			entries[i] = currElement.getClasspathEntry();
			if (currElement.isMissing()) {
				nEntriesMissing++;
				if (entryMissing == null) {
					entryMissing = currElement;
				}
			}
		}

		if (nEntriesMissing > 0) {
			if (nEntriesMissing == 1) {
				fAspectPathStatus
						.setWarning(UIMessages.AspectPathBlock_warning_EntryMissing);
			} else {
				fAspectPathStatus
						.setWarning(UIMessages.AspectBlock_warning_EntriesMissing);
			}
		}

		updateBuildPathStatus();
	}

	private void doStatusLineUpdate() {
		IStatus res = findMostSevereStatus();
		fContext.statusChanged(res);
	}

	private Shell getShell() {
		if (fSWTControl != null) {
			return fSWTControl.getShell();
		}
		return AspectJUIPlugin.getDefault().getActiveWorkbenchWindow()
				.getShell();
	}

	private IPath getDefaultBuildPath(IJavaProject jproj) {
		IPreferenceStore store = PreferenceConstants.getPreferenceStore();
		if (store.getBoolean(PreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ)) {
			String outputLocationName = store
					.getString(PreferenceConstants.SRCBIN_BINNAME);
			return jproj.getProject().getFullPath().append(outputLocationName);
		} else {
			return jproj.getProject().getFullPath();
		}
	}

	private ArrayList getExistingEntries(IClasspathEntry[] aspectpathEntries) {
		ArrayList newAspectPath = new ArrayList();
		for (int i = 0; i < aspectpathEntries.length; i++) {
			IClasspathEntry curr = aspectpathEntries[i];
			newAspectPath.add(CPListElement.createFromExisting(curr,
					fCurrJProject));
		}
		return newAspectPath;
	}

	private void updateBuildPathStatus() {
		List elements = fAspectPathList.getElements();
		IClasspathEntry[] entries = new IClasspathEntry[elements.size()];

		for (int i = elements.size() - 1; i >= 0; i--) {
			CPListElement currElement = (CPListElement) elements.get(i);
			entries[i] = currElement.getClasspathEntry();
		}

		IJavaModelStatus status = JavaConventions.validateClasspath(
				fCurrJProject, entries, fOutputLocationPath);
		if (!status.isOK()) {
			fBuildPathStatus.setError(status.getMessage());
			return;
		}
		fBuildPathStatus.setOK();
	}

	private IStatus findMostSevereStatus() {
		return StatusUtil.getMostSevere(new IStatus[] { fAspectPathStatus,
				fOutputFolderStatus, fBuildPathStatus });
	}

	public void configureJavaProject(IProgressMonitor monitor)
			throws CoreException, InterruptedException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.setTaskName(UIMessages.AspectPathBlock_operationdesc_java);
		monitor.beginTask("", 10); //$NON-NLS-1$

		try {
			internalConfigureJavaProject(fAspectPathList.getElements(), monitor);
		} finally {
			monitor.done();
		}
	}

	protected void internalConfigureJavaProject(List aspectPathEntries,
			IProgressMonitor monitor) throws CoreException,
			InterruptedException {
		int nEntries = aspectPathEntries.size();
		IClasspathEntry[] aspectpath = new IClasspathEntry[nEntries];

		for (int i = 0; i < nEntries; i++) {
			CPListElement entry = ((CPListElement) aspectPathEntries.get(i));
			aspectpath[i] = JavaCore.getResolvedClasspathEntry(entry
					.getClasspathEntry());
		}

		monitor.worked(2);

		StringBuffer aspectpathBuffer = new StringBuffer();
		StringBuffer contentKindBuffer = new StringBuffer();
		StringBuffer entryKindBuffer = new StringBuffer();
		for (int i = 0; i < aspectpath.length; i++) {
			aspectpathBuffer.append(aspectpath[i].getPath());
			aspectpathBuffer.append(File.pathSeparator);
			contentKindBuffer.append(aspectpath[i].getContentKind());
			contentKindBuffer.append(File.pathSeparator);
			entryKindBuffer.append(aspectpath[i].getEntryKind());
			entryKindBuffer.append(File.pathSeparator);
		}// end for

		aspectpathBuffer = removeFinalPathSeparatorChar(aspectpathBuffer);
		contentKindBuffer = removeFinalPathSeparatorChar(contentKindBuffer);
		entryKindBuffer = removeFinalPathSeparatorChar(entryKindBuffer);

		AspectJCorePreferences.setProjectAspectPath(fCurrJProject.getProject(),
				aspectpathBuffer.toString(), contentKindBuffer.toString(),
				entryKindBuffer.toString());

		LaunchConfigurationManagementUtils.updateAspectPaths(fCurrJProject,
				existingAspectPath, aspectPathEntries);
	}

	/**
	 * @param buffer
	 * @return
	 */
	private StringBuffer removeFinalPathSeparatorChar(StringBuffer buffer) {
		// Chop off extra path separator from end of the string.
		if ((buffer.length() > 0)
				&& (buffer.charAt(buffer.length() - 1) == File.pathSeparatorChar)) {
			buffer = buffer.deleteCharAt(buffer.length() - 1);
		}
		return buffer;
	}

	public int getPageIndex() {
		return fPageIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ajdt.internal.ui.wizards.buildpaths.BuildPathsBlock#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);

		TabFolder folder = new TabFolder(composite, SWT.NONE);
		folder.setLayout(new TabFolderLayout());
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));

		// TODO : Move over to the AJDT image registry
		ImageRegistry imageRegistry = JavaPlugin.getDefault()
				.getImageRegistry();

		TabItem item;
		IWorkbench workbench = AspectJUIPlugin.getDefault().getWorkbench();

		fLibrariesPage = new AspectPathLibrariesWorkbookPage(fWorkspaceRoot,
				fAspectPathList);
		item = new TabItem(folder, SWT.NONE);
		item.setText(UIMessages.AspectPathBlock_tab_libraries);
		item.setImage(imageRegistry.get(JavaPluginImages.IMG_OBJS_LIBRARY));
		item.setData(fLibrariesPage);
		item.setControl(fLibrariesPage.getControl(folder));

		// a non shared image
		Image cpoImage = JavaPluginImages.DESC_TOOL_CLASSPATH_ORDER
				.createImage();
		composite.addDisposeListener(new ImageDisposer(cpoImage));

		ClasspathOrderingWorkbookPage ordpage = new ClasspathOrderingWorkbookPage(
				fAspectPathList);
		item = new TabItem(folder, SWT.NONE);
		item.setText(UIMessages.InPathBlock_tab_inpath_order);
		item.setImage(cpoImage);
		item.setData(ordpage);
		item.setControl(ordpage.getControl(folder));

		if (fCurrJProject != null) {
			fLibrariesPage.init(fCurrJProject);
		}

		folder.setSelection(fPageIndex);
		fCurrPage = (BuildPathBasePage) folder.getItem(fPageIndex).getData();
		folder.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				tabChanged(e.item);
			}
		});

		workbench.getHelpSystem().setHelp(composite,
				IJavaHelpContextIds.BUILD_PATH_BLOCK); // GCH change this.
		Dialog.applyDialogFont(composite);
		return composite;
	}

	public TabItem tabContent(TabFolder folder) {

		TabItem item;

		//    	 TODO : Move over to the AJDT image registry
		ImageRegistry imageRegistry = JavaPlugin.getDefault()
				.getImageRegistry();

		ClasspathOrderingWorkbookPage ordpage = new ClasspathOrderingWorkbookPage(
				fAspectPathList);
		item = new TabItem(folder, SWT.NONE);
		item.setText(UIMessages.AspectPathBlock_tab_libraries);
		item.setImage(imageRegistry.get(JavaPluginImages.IMG_OBJS_LIBRARY));
		item.setData(ordpage);
		item.setControl(ordpage.getControl(folder));

		Control control = item.getControl();
		if (control instanceof Composite) {
			Label label = new Label((Composite)control,SWT.LEFT | SWT.WRAP);
			label.setText(UIMessages.AspectPathBlock_note);
		}
		return item;
	}

	protected void tabChanged(Widget widget) {
		if (widget instanceof TabItem) {
			TabItem tabItem = (TabItem) widget;
			BuildPathBasePage newPage = (BuildPathBasePage) tabItem.getData();
			if (fCurrPage != null) {
				List selection = fCurrPage.getSelection();
				if (!selection.isEmpty()) {
					newPage.setSelection(selection, false);
				}
			}
			fCurrPage = newPage;
			fPageIndex = tabItem.getParent().getSelectionIndex();
		}
	}

}
