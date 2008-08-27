/*******************************************************************************
 * Copyright (c) 2008 SpringSource Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     SpringSource - initial API and implementation
 *     Andrew Eisenberg
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.viewsupport.JavaUILabelProvider;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.NewWizardMessages;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathSupport;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElementAttribute;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListLabelProvider;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ClasspathOrderingWorkbookPage;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ITreeListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.TreeListDialogField;
import org.eclipse.jdt.ui.JavaElementComparator;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.dialogs.ListSelectionDialog;

/**
 * 
 * @author andrew
 * @created Jun 26, 2008
 *
 * Abstract class that defines a block in a build paths dialog
 * 
 */
public abstract class PathBlock {

    /* constants representing button locations */
    protected static final int IDX_ADDJAR = 3;
    protected static final int IDX_ADDEXT = 4;
    protected static final int IDX_ADDVAR = 5;
    protected static final int IDX_ADDFOL = 6;
    protected static final int IDX_ADDCON = 7;
    protected static final int IDX_ADDPRJ = 8;
    protected static final int IDX_REMOVE = 9;
    

    protected class LibrariesAdapter implements IDialogFieldListener, ITreeListAdapter {
    	
	    // ---------- IDialogFieldListener --------
	    public void dialogFieldChanged(DialogField field) {
	        libaryPageDialogFieldChanged(field);
	    }

	    // -------- ITreeListAdapter --------
		public void customButtonPressed(TreeListDialogField field, int index) {
	        libaryPageCustomButtonPressed(field, index);
		}

		public void doubleClicked(TreeListDialogField field) {
			// do nothing
		}

		public Object[] getChildren(TreeListDialogField field, Object element) {
			if (element instanceof CPListElement) {
				IClasspathEntry entry = ((CPListElement) element).getClasspathEntry();
				IClasspathContainer container = getClasspathContainer(entry); 
				if (container != null) {
					return new Object[] { "From: " + container.getDescription() };
				}
			} 
			return null;
		}

		public Object getParent(TreeListDialogField field, Object element) {
			if (element instanceof CPListElementAttribute) {
				return ((CPListElementAttribute) element).getParent();
			} else {
				return null;
			}
		}

		public boolean hasChildren(TreeListDialogField field, Object element) {
			Object[] children = getChildren(field, element);
			return children != null && children.length > 0;
		}

		public void keyPressed(TreeListDialogField field, KeyEvent event) {
		}

		public void selectionChanged(TreeListDialogField field) {
	        libaryPageSelectionChanged(field);
		}
	}

	private int fPageIndex;
    private IJavaProject fCurrJProject;
    private String fUserSettingsTimeStamp;

    protected final IWorkspaceRoot fWorkspaceRoot;
    protected TreeListDialogField fPathList;
    protected IStatusChangeListener fContext;
    protected StatusInfo fPathStatus;  /* status for path list being self-consistent */
    protected StatusInfo fJavaBuildPathStatus; /* status for path list being consistent with Java build path */
    
    protected PathBlock(IStatusChangeListener context, int pageToShow) {
        fWorkspaceRoot = AspectJPlugin.getWorkspace().getRoot();
        fContext= context;
        fPageIndex= pageToShow;
        
        LibrariesAdapter adapter= new LibrariesAdapter();
        String[] buttonLabels= new String[] {
                null,
                null,
                null,
                /* IDX_ADDJAR */ UIMessages.PathLibrariesWorkbookPage_libraries_addjar_button,
                /* IDX_ADDEXT */ UIMessages.PathLibrariesWorkbookPage_libraries_addextjar_button,
                /* IDX_ADDVAR */ UIMessages.PathLibrariesWorkbookPage_libraries_addvariable_button,
                /* IDX_ADDFOL */ UIMessages.PathLibrariesWorkbookPage_libraries_addclassfolder_button,
                /* IDX_ADDCON */ UIMessages.PathLibrariesWorkbookPage_libraries_addlibrary_button,  
                /* IDX_ADDPRJ */ UIMessages.PathLibrariesWorkbookPage_libraries_addproject_button,       
                /* IDX_REMOVE */ UIMessages.PathLibrariesWorkbookPage_libraries_remove_button               
        };
        fPathList= new TreeListDialogField(adapter, buttonLabels, new CPListLabelProvider());
        fPathList.setDialogFieldListener(adapter);
        fPathList.setRemoveButtonIndex(IDX_REMOVE);
        fPathList.enableButton(IDX_REMOVE, false);

        fCurrJProject = null;
        
        fJavaBuildPathStatus= new StatusInfo();
        fPathStatus= new StatusInfo();

    }
    
    protected abstract void internalSetProjectPath(List pathEntries,
            StringBuffer pathBuffer, StringBuffer contentKindBuffer,
            StringBuffer entryKindBuffer);
    
    
    protected abstract String getBlockNote();

    protected abstract String getBlockTitle();


    
    public void init() {
        initializeTimeStamp();
        updatePathStatus();
    }
    
    
    private void libaryPageSelectionChanged(DialogField field) {
        List selElements = fPathList.getSelectedElements();
        fPathList.enableButton(IDX_REMOVE, canRemove(selElements));
    }
    private void libaryPageDialogFieldChanged(DialogField field) {
        if (fCurrJProject != null) {
            // already initialized
//            updatePathList();
            updatePathStatus();
            doStatusLineUpdate();
        }
    }
  
    // don't think thisis needed any more because we accept 
    // all kinds of classpath entries now (ie- projects and containers)
//  private void updatePathList() {
//        List projelements = fPathList.getElements();
//        List cpelements = fPathList.getElements();
//        
//        int nEntries = cpelements.size();
//        // backwards, as entries will be deleted
//        int lastRemovePos = nEntries;
//        for (int i = nEntries - 1; i >= 0; i--) {
//            CPListElement cpe = (CPListElement) cpelements.get(i);
//            if (!projelements.remove(cpe)) {
//                cpelements.remove(i);
//                lastRemovePos = i;
//            }
//        }
//
//        cpelements.addAll(lastRemovePos, projelements);
//
//        if (lastRemovePos != nEntries || !projelements.isEmpty()) {
//            fPathList.setElements(cpelements);
//        }
//    }




  
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
            case IDX_ADDFOL: /* add class folder */
                libentries = openClassFolderDialog(null);
                break;
            case IDX_ADDCON: /* add container */
                libentries = openContainerSelectionDialog(null);
                break;
            case IDX_ADDPRJ: /* add project reference */
                libentries = openProjectSelectionDialog(null);
                break;
            case IDX_REMOVE: /* remove */
                removeEntry();
                return;
        }
        if (libentries != null) {
            int nElementsChosen = libentries.length;
            // remove duplicates, but ignore 
            // elements with classpath containers
            // since there is no direct control over
            // them.
            List cplist = fPathList.getElements();
            List elementsToAdd = new ArrayList(nElementsChosen);

            for (int i = 0; i < nElementsChosen; i++) {
                CPListElement curr = libentries[i];
                if (!cplist.contains(curr) && !elementsToAdd.contains(curr)) {
                    elementsToAdd.add(curr);
                    curr.setAttribute(CPListElement.SOURCEATTACHMENT,
                            BuildPathSupport.guessSourceAttachment(curr));
                    
                    // Not working and I don't think we need this.
//                    try {
//						curr.setAttribute(CPListElement.JAVADOC, 
//								JavaUI.getJavadocBaseLocation(
//										fCurrJProject.getJavaModel().findElement(curr.getPath())));
//					} catch (JavaModelException e) {
//					}
                }
            }
            if (!elementsToAdd.isEmpty() && (index == IDX_ADDFOL)) {
                askForAddingExclusionPatternsDialog(elementsToAdd);
            }

            fPathList.addElements(elementsToAdd);

            fPathList.postSetSelection(new StructuredSelection(libentries));

            updatePathStatus();
            doStatusLineUpdate();
        }
    }

  protected void doStatusLineUpdate() {
        IStatus res = findMostSevereStatus();
        fContext.statusChanged(res);
    }
  
    /**
     * checks for missing entries in the path
     */
    private void updatePathStatus() {
        fPathStatus.setOK();

        List elements = fPathList.getElements();

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
                fPathStatus.setWarning(UIMessages.InPathBlock_warning_EntryMissing);
            } else {
                fPathStatus.setWarning(UIMessages.InPathBlock_warning_EntriesMissing);
            }
        }
        updateJavaBuildPathStatus();
    }


    /**
     * checks to see if the path is well-formed,
     * duplicates, etc.
     */
    protected void updateJavaBuildPathStatus() {
        List elements = fPathList.getElements();
        List /* IClasspathEntry */ entries = new ArrayList();

        for (int i = elements.size() - 1; i >= 0; i--) {
            CPListElement currElement = (CPListElement) elements.get(i);
            // ignore elements that are part of a container 
            // since user does not have direct control over removing them
            if (!inClasspathContainer(currElement)) {
            	entries.add(currElement.getClasspathEntry());
            }
        }

        IPath outPath;
        try {
            outPath = fCurrJProject.getOutputLocation();
        } catch (JavaModelException e) {
            outPath = fCurrJProject.getPath();
        }
        
        IClasspathEntry[] entriesArr = (IClasspathEntry[]) entries.toArray(new IClasspathEntry[0]);
        IJavaModelStatus status = JavaConventions.validateClasspath(
                fCurrJProject, entriesArr, outPath);

        if (!status.isOK()) {
            fJavaBuildPathStatus.setError(status.getMessage());
            return;
        }

        IJavaModelStatus dupStatus = checkForDuplicates(fCurrJProject, entriesArr);
        if (!dupStatus.isOK()) {
            fJavaBuildPathStatus.setError(dupStatus.getMessage());
            return;
        }

        fJavaBuildPathStatus.setOK();
    }

  
    /**
     * Checks for duplicate entries on the inpath compared to the Java build
     * path
     * 
     * This checks to make sure that duplicate entries are not being referred
     * to. For example, it is possible for the JUnit jar to be referred to
     * through a classpath variable as well as a classpath container. This
     * method checks for such duplicates
     * 
     * 1. if an inpath entry is on the build path, then remove it from checking
     * 2. resolve the remaining inpath entries 3. resolve the build path 4.
     * there should be no overlap
     */
    private IJavaModelStatus checkForDuplicates(IJavaProject currJProject,
            IClasspathEntry[] entries) {
        try {
            Map allEntries = new HashMap(entries.length, 1.0f);
            for (int i = 0; i < entries.length; i++) {
            	// ignore entries that are inside of a container
            	if (getClasspathContainer(entries[i]) == null) {
	                allEntries.put(entries[i].getPath().toPortableString(),
	                        entries[i]);
            	}
            }

            IClasspathEntry[] rawProjectClasspath = currJProject
                    .getRawClasspath();
            for (int i = 0; i < rawProjectClasspath.length; i++) {
                allEntries.remove(rawProjectClasspath[i].getPath()
                        .toPortableString());
            }

            IClasspathEntry[] resolvedProjectClasspath = currJProject
                    .getResolvedClasspath(true);
            Map resolvedEntries = new HashMap();
            Iterator allEntriesIter = allEntries.values().iterator();
            while (allEntriesIter.hasNext()) {
                ClasspathEntry rawEntry = (ClasspathEntry) allEntriesIter
                        .next();
                switch (rawEntry.entryKind) {
                    case IClasspathEntry.CPE_SOURCE:
                    case IClasspathEntry.CPE_LIBRARY:
                    case IClasspathEntry.CPE_VARIABLE:
                        IClasspathEntry resolvedEntry = JavaCore
                                .getResolvedClasspathEntry(rawEntry);
                        resolvedEntries.put(resolvedEntry.getPath()
                                .toPortableString(), resolvedEntry);
                        break;
                    case IClasspathEntry.CPE_CONTAINER:
                        IClasspathContainer container = JavaCore
                        .getClasspathContainer(rawEntry.getPath(),
                                currJProject);
                        List containerEntries = AspectJCorePreferences.resolveClasspathContainer(
                                container, currJProject.getProject());
                        
                        for (Iterator containerIter = containerEntries.iterator(); containerIter.hasNext(); ) {
                            IClasspathEntry containerEntry = (IClasspathEntry) containerIter.next();
                            resolvedEntries.put(containerEntry.getPath()
                                    .toPortableString(), containerEntry);
                        }
                        break;
                        
                    case IClasspathEntry.CPE_PROJECT:
                        IProject thisProject = currJProject.getProject();
                        IProject requiredProj = thisProject.getWorkspace().getRoot().getProject(
                                rawEntry.getPath().makeRelative().toPortableString());
                        if (! requiredProj.getName().equals(thisProject.getName())   
                                && requiredProj.exists()) {
                            List containerEntries2 = AspectJCorePreferences.resolveDependentProjectClasspath(requiredProj, rawEntry);
                            for (Iterator containerIter = containerEntries2.iterator(); containerIter.hasNext(); ) {
                                IClasspathEntry containerEntry = (IClasspathEntry) containerIter.next();
                                resolvedEntries.put(containerEntry.getPath()
                                        .toPortableString(), containerEntry);
                            }
                            
                        }
                        break;
                }
            }

            for (int i = 0; i < resolvedProjectClasspath.length; i++) {
                if (resolvedEntries.containsKey(resolvedProjectClasspath[i]
                        .getPath().toPortableString())) {
                    // duplicate found.
                    return new JavaModelStatus(IStatus.WARNING,
                            IStatus.WARNING, currJProject, currJProject
                                    .getPath(),
                            UIMessages.InPathBlock_DuplicateBuildEntry
                                    + resolvedProjectClasspath[i].getPath());
                }
            }

            return JavaModelStatus.VERIFIED_OK;
        } catch (JavaModelException e) {
            return new JavaModelStatus(e);
        }

    }



    private void removeEntry() {
        List selElements = fPathList.getSelectedElements();
        for (int i = selElements.size() - 1; i >= 0; i--) {
            Object elem = selElements.get(i);
            if (elem instanceof CPListElementAttribute) {
                CPListElementAttribute attrib = (CPListElementAttribute) elem;
                attrib.getParent().setAttribute(attrib.getKey(), null);
                selElements.remove(i);
            }
        }
        if (selElements.isEmpty()) {
            fPathList.refresh();
            fPathList.dialogFieldChanged(); // validate
        } else {
            fPathList.removeElements(selElements);
        }
    }
  
    /**
     * Determines whether or not the remove button is active.
     * 
     * @param selElements Selected elements
     * @return true if all elements are CPListElements that are not attributes
     * and are not contained in classpath containers
     */
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
            	// Bug 243356
            	// can't remove elements that are contained in a container
                CPListElement curr = (CPListElement) elem;
                if (inClasspathContainer(curr)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean inClasspathContainer(CPListElement element) {
    	IClasspathAttribute[] attributes = element.getClasspathEntry().getExtraAttributes();
    	for (int i = 0; i < attributes.length; i++) {
			if (AspectJCorePreferences.isAspectPathAttribute(attributes[i]) || 
					AspectJCorePreferences.isInPathAttribute(attributes[i])) {
				if (! (attributes[i].getValue().equals(attributes[i].getName()))) {
					return true;
				}
			}
    	}
    	return false;
	}
  
    private IClasspathContainer getClasspathContainer(IClasspathEntry classpathEntry) {
    	IClasspathAttribute[] attributes = classpathEntry.getExtraAttributes();
    	for (int i = 0; i < attributes.length; i++) {
			if (AspectJCorePreferences.isAspectPathAttribute(attributes[i]) || 
					AspectJCorePreferences.isInPathAttribute(attributes[i])) {
				if (attributes[i].getValue() != null) {
					try {
						return JavaCore.getClasspathContainer(new Path(attributes[i].getValue()), fCurrJProject);
					} catch (JavaModelException e) {
						return null;
					}
				}
			}
		}
		return null;
	}

	// Don't think this is used.
    private void askForAddingExclusionPatternsDialog(List newEntries) {
        HashSet modified = new HashSet();
        if (!modified.isEmpty()) {
            String title = UIMessages.InPathLibrariesWorkbookPage_exclusion_added_title;
            String message = UIMessages.InPathLibrariesWorkbookPage_exclusion_added_message;
            MessageDialog.openInformation(AspectJUIPlugin.getDefault()
                    .getActiveWorkbenchWindow().getShell(), title, message);
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
                return (CPListElement[]) 
                        res.toArray(new CPListElement[res.size()]);
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

                    String outJar = AspectJCorePreferences
                            .getProjectOutJar(fCurrJProject.getProject());
                    StringBuffer projectOutJar = new StringBuffer();
                    projectOutJar.append(fCurrJProject.getPath().toString());
                    projectOutJar.append("/" + outJar); //$NON-NLS-1$

                    if (resource.getFullPath().toString().equals(
                            projectOutJar.toString())) {
                        MessageDialog.openInformation(getShell(),
                                UIMessages.buildpathwarning_title,
                                UIMessages.addtoinpathwarning);
                    } else if (resource instanceof IFile) {
                        res.add(newCPLibraryElement(resource));
                    }
                }
                return (CPListElement[]) 
                        res.toArray(new CPListElement[res.size()]);
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
                return (CPListElement[]) 
                        res.toArray(new CPListElement[res.size()]);
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
        List existingElements = fPathList.getElements();
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
                return (CPListElement[]) 
                        result.toArray(new CPListElement[result.size()]);
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

    private CPListElement[] openProjectSelectionDialog(CPListElement elem) {
        
        try {
            ArrayList selectable= new ArrayList();
            selectable.addAll(Arrays.asList(fCurrJProject.getJavaModel().getJavaProjects()));
            selectable.remove(fCurrJProject);
            
            List elements= fPathList.getElements();
            for (int i= 0; i < elements.size(); i++) {
                CPListElement curr= (CPListElement) elements.get(0);
                if (curr.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
                    IJavaProject proj= (IJavaProject) JavaCore.create(curr.getResource());
                    selectable.remove(proj);
                }
            }
            Object[] selectArr= selectable.toArray();
            new JavaElementComparator().sort(null, selectArr);
                    
            ListSelectionDialog dialog= new ListSelectionDialog(getShell(), Arrays.asList(selectArr), new ArrayContentProvider(), new JavaUILabelProvider(), NewWizardMessages.ProjectsWorkbookPage_chooseProjects_message); 
            dialog.setTitle(NewWizardMessages.ProjectsWorkbookPage_chooseProjects_title);  // TODO make our own text
            dialog.setHelpAvailable(false);
            if (dialog.open() == Window.OK) {
                Object[] result= dialog.getResult();
                CPListElement[] cpElements= new CPListElement[result.length];
                for (int i= 0; i < result.length; i++) {
                    IJavaProject curr= (IJavaProject) result[i];
                    cpElements[i]= new CPListElement(fCurrJProject, IClasspathEntry.CPE_PROJECT, curr.getPath(), curr.getResource());
                }
                return cpElements;
            }
        } catch (JavaModelException e) {
            return null;
        }
        return null;
    }

    private IClasspathEntry[] getRawClasspath() {
        IClasspathEntry[] currEntries = new IClasspathEntry[fPathList.getSize()];
        for (int i = 0; i < currEntries.length; i++) {
            CPListElement curr = (CPListElement) fPathList.getElement(i);
            currEntries[i] = curr.getClasspathEntry();
        }
        return currEntries;
    }


  
    private CPListElement newCPLibraryElement(IResource res) {
        return new CPListElement(fCurrJProject, IClasspathEntry.CPE_LIBRARY,
                res.getFullPath(), res);
    }

  
  private IPath[] getUsedContainers(CPListElement existing) {
        ArrayList res = new ArrayList();
        if (fCurrJProject.exists()) {
            try {
                IPath outputLocation = fCurrJProject.getOutputLocation();
                if (outputLocation != null && outputLocation.segmentCount() > 1) { // !=
                                                                                    // Project
                    res.add(outputLocation);
                }
            } catch (JavaModelException e) {}
        }

        List cplist = fPathList.getElements();
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
        List cplist = fPathList.getElements();
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

  public TabItem tabContent(TabFolder folder) {

        TabItem item;

        // TODO : Move over to the AJDT image registry
        ImageRegistry imageRegistry = JavaPlugin.getDefault()
                .getImageRegistry();

        PathBlockWorkbookPage ordpage = new PathBlockWorkbookPage(
                fPathList);
        item = new TabItem(folder, SWT.NONE);
        item.setText(getBlockTitle());
        item.setImage(imageRegistry.get(JavaPluginImages.IMG_OBJS_LIBRARY));
        item.setData(ordpage);
        item.setControl(ordpage.getControl(folder));

        Control control = item.getControl();
        if (control instanceof Composite) {
            Label label = new Label((Composite) control, SWT.LEFT | SWT.WRAP);
            label.setText(getBlockNote());
        }
        return item;
    }
  
    public void configureJavaProject(IProgressMonitor monitor)
            throws CoreException, InterruptedException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.setTaskName(NLS.bind(UIMessages.PathBlock_operationdesc_java,
                getBlockTitle()));
        monitor.beginTask("", 10); //$NON-NLS-1$

        try {
            internalConfigureJavaProject(fPathList.getElements(), monitor);
            initializeTimeStamp();
        } finally {
            monitor.done();
        }
    }
  
    /**
     * @param buffer
     * @return
     */
    protected StringBuffer removeFinalPathSeparatorChar(StringBuffer buffer) {
        // Chop off extra path separator from end of the string.
        if ((buffer.length() > 0)
                && (buffer.charAt(buffer.length() - 1) == File.pathSeparatorChar)) {
            buffer = buffer.deleteCharAt(buffer.length() - 1);
        }
        return buffer;
    }
    
    protected void internalConfigureJavaProject(List pathElements,
            IProgressMonitor monitor) throws CoreException, InterruptedException {
        int nEntries = pathElements.size();
        List /* IClasspathEntry */ pathEntries = new ArrayList();

        for (int i = 0; i < nEntries; i++) {
        	// Bug 243356
        	// remove from the list if this is an entry contained in a container
        	// because entries in containers are computed separately
            CPListElement element = ((CPListElement) pathElements.get(i));
			if (!inClasspathContainer(element)) {
				pathEntries.add(element.getClasspathEntry());
            }
        }

        monitor.worked(2);

        StringBuffer pathBuffer = new StringBuffer();
        StringBuffer contentKindBuffer = new StringBuffer();
        StringBuffer entryKindBuffer = new StringBuffer();
        for (Iterator pathIter = pathEntries.iterator(); pathIter.hasNext();) {
			IClasspathEntry pathEntry = (IClasspathEntry) pathIter.next();
            pathBuffer.append(pathEntry.getPath());
            pathBuffer.append(File.pathSeparator);
            contentKindBuffer.append(pathEntry.getContentKind());
            contentKindBuffer.append(File.pathSeparator);
            entryKindBuffer.append(pathEntry.getEntryKind());
            entryKindBuffer.append(File.pathSeparator);
        }

        pathBuffer = removeFinalPathSeparatorChar(pathBuffer);
        contentKindBuffer = removeFinalPathSeparatorChar(contentKindBuffer);
        entryKindBuffer = removeFinalPathSeparatorChar(entryKindBuffer);

        internalSetProjectPath(pathElements, pathBuffer,
                contentKindBuffer, entryKindBuffer);
    }

    protected ArrayList /*CPListElement*/ getExistingEntries(IClasspathEntry[] pathEntries) {
        ArrayList /*CPListElement*/ newPath = new ArrayList();
        for (int i = 0; i < pathEntries.length; i++) {
            IClasspathEntry curr = pathEntries[i];
            newPath.add(CPListElement.createFromExisting(curr,
                    fCurrJProject));
        }
        return newPath;
    }

    protected Shell getShell() {
        return AspectJUIPlugin.getDefault().getActiveWorkbenchWindow()
                .getShell();
    }

    protected IJavaProject getJavaProject() {
        return fCurrJProject;
    }
    
    protected void setJavaProject(IJavaProject project) {
        this.fCurrJProject = project;
    }
    

    protected IStatus findMostSevereStatus() {
        return StatusUtil.getMostSevere(new IStatus[] { fPathStatus,
                fJavaBuildPathStatus });
    }

    public int getPageIndex() {
        return fPageIndex;
    }
    
    protected String getEncodedSettings() {
        StringBuffer buf= new StringBuffer();   
        
        int nElements= fPathList.getSize();
        buf.append('[').append(nElements).append(']');
        for (int i= 0; i < nElements; i++) {
            CPListElement elem= (CPListElement) fPathList.getElement(i);
            elem.appendEncodedSettings(buf);
        }
        return buf.toString();
    }
    
    public boolean hasChangesInDialog() {
        String currSettings= getEncodedSettings();
        return !currSettings.equals(fUserSettingsTimeStamp);
    }
    
    public void initializeTimeStamp() {
        fUserSettingsTimeStamp= getEncodedSettings();
    }
}
