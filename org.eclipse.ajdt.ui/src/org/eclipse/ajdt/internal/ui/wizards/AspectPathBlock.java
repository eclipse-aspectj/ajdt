/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.core.AspectJCorePreferences;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.internal.launching.LaunchConfigurationManagementUtils;
import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
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
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.viewsupport.ImageDisposer;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.BuildPathBasePage;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListLabelProvider;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.ClasspathOrderingWorkbookPage;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IStringButtonAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
    
    public AspectPathBlock(IStatusChangeListener context, int pageToShow) {
        fWorkspaceRoot= AspectJPlugin.getWorkspace().getRoot();
        fContext= context;
        fPageIndex= pageToShow;
        fLibrariesPage= null;
        fCurrPage= null;
        
        AspectPathAdapter adapter= new AspectPathAdapter();           
        String[] buttonLabels= new String[] {
                /* 0 */ UIMessages.InPathBlock_order_up_button,
                /* 1 */ UIMessages.InPathBlock_order_down_button};
        
        fAspectPathList= new ListDialogField(null, buttonLabels, new CPListLabelProvider());
        fAspectPathList.setDialogFieldListener(adapter);
        fAspectPathList.setLabelText(UIMessages.AspectPathBlock_aspectpath_label);
        fAspectPathList.setUpButtonIndex(0);
        fAspectPathList.setDownButtonIndex(1);
            
        fBuildPathStatus= new StatusInfo();
        fAspectPathStatus= new StatusInfo();
        fOutputFolderStatus= new StatusInfo();
        
        fCurrJProject= null;
    }
    
    private class AspectPathAdapter implements IStringButtonAdapter,
            IDialogFieldListener {

        // -------- IStringButtonAdapter --------
        public void changeControlPressed(DialogField field) {
            // buildPathChangeControlPressed(field);
        }

        // ---------- IDialogFieldListener --------
        public void dialogFieldChanged(DialogField field) {
            buildPathDialogFieldChanged(field);
        }
    }
    
    
    private void buildPathDialogFieldChanged(DialogField field) {
        if (field == fAspectPathList) {
            updateAspectPathStatus();
        }
        doStatusLineUpdate();
    }   
    
    // ---------- util method ------------
    
    public void init(
            IJavaProject jproject,
            IPath outputLocation,
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
                fAspectPathStatus.setWarning(UIMessages.AspectPathBlock_warning_EntryMissing);
            } else {
                fAspectPathStatus.setWarning(UIMessages.AspectBlock_warning_EntriesMissing);
            }
        }

        updateBuildPathStatus();
    }

    private void doStatusLineUpdate() {
        IStatus res = findMostSevereStatus();
        fContext.statusChanged(res);
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
        return StatusUtil.getMostSevere(new IStatus[] { fAspectPathStatus, fOutputFolderStatus, fBuildPathStatus });
    }

    public void configureJavaProject(IProgressMonitor monitor)
            throws CoreException, InterruptedException {
        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        monitor.setTaskName(UIMessages.AspectPathBlock_operationdesc_java);
        monitor.beginTask("", 10); //$NON-NLS-1$

        try {
            internalConfigureJavaProject(fAspectPathList.getElements(),
                     monitor);
        } finally {
            monitor.done();
        }
    }
    
    protected void internalConfigureJavaProject(List aspectPathEntries,
             IProgressMonitor monitor)
            throws CoreException, InterruptedException {
        int nEntries = aspectPathEntries.size();
        IClasspathEntry[] aspectpath = new IClasspathEntry[nEntries];

        for (int i = 0; i < nEntries; i++) {
            CPListElement entry = ((CPListElement) aspectPathEntries.get(i));
            aspectpath[i] = JavaCore.getResolvedClasspathEntry(entry.getClasspathEntry());
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
        
        AspectJCorePreferences.setProjectAspectPath(fCurrJProject.getProject(),aspectpathBuffer.toString(),
        		contentKindBuffer.toString(),entryKindBuffer.toString());
        
        LaunchConfigurationManagementUtils.updateAspectPaths(fCurrJProject, existingAspectPath, aspectPathEntries);
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
        ImageRegistry imageRegistry =
            JavaPlugin.getDefault().getImageRegistry();

        TabItem item;
        IWorkbench workbench = AspectJUIPlugin.getDefault().getWorkbench();
        
        fLibrariesPage =
            new AspectPathLibrariesWorkbookPage(fWorkspaceRoot, fAspectPathList);
        item = new TabItem(folder, SWT.NONE);
        item.setText(UIMessages.AspectPathBlock_tab_libraries);
        item.setImage(imageRegistry.get(JavaPluginImages.IMG_OBJS_LIBRARY));
        item.setData(fLibrariesPage);
        item.setControl(fLibrariesPage.getControl(folder));

        // a non shared image
        Image cpoImage =
            JavaPluginImages.DESC_TOOL_CLASSPATH_ORDER.createImage();
        composite.addDisposeListener(new ImageDisposer(cpoImage));

        ClasspathOrderingWorkbookPage ordpage =
            new ClasspathOrderingWorkbookPage(fAspectPathList);
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

        workbench.getHelpSystem().setHelp(composite, IJavaHelpContextIds.BUILD_PATH_BLOCK); // GCH change this.
        Dialog.applyDialogFont(composite);
        return composite;
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
