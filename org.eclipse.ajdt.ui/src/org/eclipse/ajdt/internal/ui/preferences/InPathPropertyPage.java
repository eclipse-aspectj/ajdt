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
package org.eclipse.ajdt.internal.ui.preferences;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.ajdt.internal.ui.ajde.BuildOptionsAdapter;
import org.eclipse.ajdt.internal.ui.wizards.InPathBlock;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.ClasspathEntry;
import org.eclipse.jdt.internal.ui.IJavaHelpContextIds;
import org.eclipse.jdt.internal.ui.dialogs.StatusUtil;
import org.eclipse.jdt.internal.ui.preferences.PreferencesMessages;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.actions.WorkspaceModifyDelegatingOperation;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.help.WorkbenchHelp;


/**
 * Property page for configuring the AspectJ inpath
 */
public class InPathPropertyPage extends PropertyPage implements
        IStatusChangeListener {

    // Relevant project for which the inpath is being set
    private IProject thisProject;
    
    protected InPathBlock fInPathBlock;
    private static final String PAGE_SETTINGS= "InPathPropertyPage"; //$NON-NLS-1$
    private static final String INDEX= "pageIndex"; //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        // ensure the page has no special buttons
        noDefaultAndApplyButton();      
        thisProject = getProject();
        
        // TODO : Only ensure inpath is initialised ??
        BuildOptionsAdapter.ensurePropertiesInitialized(thisProject);
        
        Control result;
        if (thisProject == null || !isAspectJProject(thisProject)) {
            result= createWithoutJava(parent);
        } else if (!thisProject.isOpen()) {
            result= createForClosedProject(parent);
        } else {
            result= createWithJava(parent, thisProject);
        }
        Dialog.applyDialogFont(result);
        return result;
    }

    private IProject getProject() {
        IAdaptable adaptable = getElement();
        if (adaptable != null) {
            IJavaElement elem = (IJavaElement) adaptable
                    .getAdapter(IJavaElement.class);
            if (elem instanceof IJavaProject) { return ((IJavaProject) elem)
                    .getProject(); }
        }
        return null;
    }
    
    private boolean isAspectJProject(IProject proj) {
        try {
            return proj.hasNature(AspectJUIPlugin.ID_NATURE);
        } catch (CoreException e) {
        }
        return false;
    }

    /*
     * Content for non-Java projects.
     */ 
    private Control createWithoutJava(Composite parent) {
        Label label= new Label(parent, SWT.LEFT);
        label.setText(PreferencesMessages.getString("BuildPathsPropertyPage.no_java_project.message")); //$NON-NLS-1$
        
        fInPathBlock= null;
        setValid(true);
        return label;
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener#statusChanged(org.eclipse.core.runtime.IStatus)
     */
    public void statusChanged(IStatus status) {
        setValid(!status.matches(IStatus.ERROR));
        StatusUtil.applyToStatusLine(this, status);
    }

    /*
     * Content for closed projects.
     */     
    private Control createForClosedProject(Composite parent) {
        Label label= new Label(parent, SWT.LEFT);
        label.setText(PreferencesMessages.getString("BuildPathsPropertyPage.closed_project.message")); //$NON-NLS-1$
        
        fInPathBlock= null;
        setValid(true);
        return label;
    }
    
    /*
     * Content for valid projects.
     */
    protected Control createWithJava(Composite parent, IProject project) {
        fInPathBlock = new InPathBlock(this, getSettings().getInt(INDEX));

        // Third parameter to the below init() call should be
        // an array of IClasspathEntry which, presumably, has been obtained
        // from the store of project properties.
        IClasspathEntry[] initalInpath = null;

        try {
            initalInpath = getInitialInpathValue(project);
        } catch (CoreException ce) {
        	AspectJUIPlugin
					.getDefault()
					.getErrorHandler()
					.handleError(
							AspectJUIPlugin
									.getResourceString("InPathProp.exceptionInitializingInpath.title"),
							AspectJUIPlugin
									.getResourceString("InPathProp.exceptionInitializingInpath.message"),
							ce);
        }

        fInPathBlock.init(JavaCore.create(project), null, initalInpath);
        return fInPathBlock.createControl(parent);
    }

    private IClasspathEntry[] getInitialInpathValue(IProject project)
            throws CoreException {
        List result = new ArrayList();
        String paths = project
                .getPersistentProperty(BuildOptionsAdapter.INPATH);
        String cKinds = project
                .getPersistentProperty(BuildOptionsAdapter.INPATH_CON_KINDS);
        String eKinds = project
                .getPersistentProperty(BuildOptionsAdapter.INPATH_ENT_KINDS);

        if ((paths != null && paths.length() > 0)
                && (cKinds != null && cKinds.length() > 0)
                && (eKinds != null && eKinds.length() > 0)) {
            StringTokenizer sTokPaths = new StringTokenizer(paths,
                    File.pathSeparator);
            StringTokenizer sTokCKinds = new StringTokenizer(cKinds,
                    File.pathSeparator);
            StringTokenizer sTokEKinds = new StringTokenizer(eKinds,
                    File.pathSeparator);
            if ((sTokPaths.countTokens() == sTokCKinds.countTokens())
                    && (sTokPaths.countTokens() == sTokEKinds.countTokens())) {
                while (sTokPaths.hasMoreTokens()) {
                    IClasspathEntry entry = new ClasspathEntry(Integer
                            .parseInt(sTokCKinds.nextToken()), // content kind
                            Integer.parseInt(sTokEKinds.nextToken()), // entry
                                                                      // kind
                            new Path(sTokPaths.nextToken()), // path
                            new IPath[] {}, // inclusion patterns
                            new IPath[] {}, // exclusion patterns
                            null, // src attachment path
                            null, // src attachment root path
                            null, // output location
                            false); // is exported ?
                    result.add(entry);
                }// end while
            }// end if string token counts tally
        }// end if we have something valid to work with

        if (result.size() > 0) {
            return (IClasspathEntry[]) result.toArray(new IClasspathEntry[0]);
        } else {
            return null;
        }
    }
    
    protected IDialogSettings getSettings() {
        IDialogSettings pathSettings = AspectJUIPlugin.getDefault().getDialogSettings();
        IDialogSettings pageSettings = pathSettings.getSection(PAGE_SETTINGS);
        if (pageSettings == null) {
            pageSettings = pathSettings.addNewSection(PAGE_SETTINGS);
            // Important. Give the key INDEX a value which is one less than the
            // number of tabs that will be displayed in the page. The inpath
            // page will have two tabs hence ...
            pageSettings.put(INDEX, 1); 
        }
        return pageSettings;
    }

    /*
     * @see IPreferencePage#performOk
     */
    public boolean performOk() {
        if (fInPathBlock != null) {
            Shell shell= getControl().getShell();
            IRunnableWithProgress runnable= new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)   throws InvocationTargetException, InterruptedException {
                    try {
                        fInPathBlock.configureJavaProject(monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    } 
                }
            };
            IRunnableWithProgress op= new WorkspaceModifyDelegatingOperation(runnable);
            try {
                new ProgressMonitorDialog(shell).run(true, true, op);
            } catch (InvocationTargetException e) {
                String title= PreferencesMessages.getString("BuildPathsPropertyPage.error.title");
                String message= PreferencesMessages.getString("BuildPathsPropertyPage.error.message"); //$NON-NLS-1$
                // TODO : Handle exception 
                return false;
            } catch (InterruptedException e) {
                // cancelled
                return false;
            }
            InPathBlock.setUpdatedInPath(true);
        }
        return true;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        WorkbenchHelp.setHelp(
            getControl(),
            IJavaHelpContextIds.BUILD_PATH_PROPERTY_PAGE); // GCH change this.
    }
    
	protected void doProjectBuild() { 
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
		try {
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException {
					monitor.beginTask("", 2); //$NON-NLS-1$
					try {
						monitor
								.setTaskName(AspectJUIPlugin
										.getResourceString("OptionsConfigurationBlock.buildproject.taskname")); //$NON-NLS-1$
						thisProject.build(IncrementalProjectBuilder.FULL_BUILD,
						        "org.eclipse.ajdt.ui.ajbuilder", 
						        null,
						        new SubProgressMonitor(monitor, 2));
					} catch (CoreException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InterruptedException e) {
			// cancelled by user
		} catch (InvocationTargetException e) {
			String message = AspectJUIPlugin
					.getResourceString("OptionsConfigurationBlock.builderror.message"); //$NON-NLS-1$
			AspectJUIPlugin.getDefault().getErrorHandler().handleError(message, e);
		}
	}

    /**
     * @return Returns the project for which the inpath is being set
     */
    public IProject getThisProject() {
        return thisProject;
    }
 
    /**
     * Resets the change settings to be false e.g. says
     * that the inpath setting in the preference page hasn't been
     * changed and that the preference store settings also haven't
     * been updated.
     */
    public void resetChangeSettings() {
        fInPathBlock.resetChangeSettings();
    }
}
