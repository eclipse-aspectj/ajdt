/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 ******************************************************************************/
package org.eclipse.ajdt.internal.ui.preferences;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.ajdt.internal.ui.AspectJProjectPropertiesPage;
import org.eclipse.ajdt.internal.ui.CompilerPropertyPage;
import org.eclipse.ajdt.internal.ui.wizards.AspectPathBlock;
import org.eclipse.ajdt.internal.ui.wizards.InPathBlock;
import org.eclipse.ajdt.ui.AspectJUIPlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;

/**
 * This aspect handles the building of projects whenever OK is pressed on 
 * any of the AspectJ project preference pages.
 * 
 * @author hawkinsh  
 */
public aspect PreferencePageBuilder {

    private PreferencePage prefPage;
    private IProject project;   
    
    pointcut okPressed() : 
        execution(boolean AspectPathPropertyPage.performOk())
        || execution(boolean InPathPropertyPage.performOk())
        || execution(boolean CompilerPropertyPage.performOk())
        || execution(boolean AspectJProjectPropertiesPage.performOk());

    /**
     * If the settings on any of the properties pages have changed and
     * all the pages have performed the required updates to the 
     * preference settings, then do a build. Otherwise don't build.
     */
    after() returning : okPressed() {
        if (somethingHasChanged()) {
            getSettings(thisJoinPoint.getThis());
            if (wantToBuild() 
                    && aspectJPageReady()
                    && aspectPathPageReady()
                    && compilerPageReady()
                    && inpathPageReady()) {
                doProjectBuild();
            }
            // after going through the settings for this page, reset the change
            // settings back to false - this means that if another page has
            // been activated but nothing has changed, then will do nothing.
            // Also deals with the case when you click "apply".
            resetPreferencePageChangeSettings();
        }
    }

    /**
     * Returning whether Autobuilding is set or whether the user chose
     * to build on the compiler page 
     */
    private boolean wantToBuild() {
        if (AspectJUIPlugin.getWorkspace().getDescription().isAutoBuilding()
                || (CompilerPropertyPage.compilerSettingsHaveChanged() 
                        && CompilerPropertyPage.chosenToDoBuild())) {
            return true;
        }
        return false;        
    }
    
    /**
     * Find out which property page the performOk() method was executed
     * on and the project for which these property pages are currently
     * open. 
     */
    private void getSettings(Object o) {
        if (o instanceof PreferencePage) {
            prefPage = (PreferencePage)o;
            if (prefPage instanceof AspectPathPropertyPage) {
                project = ((AspectPathPropertyPage)prefPage).getThisProject();
            } else if (prefPage instanceof InPathPropertyPage) {
                project = ((InPathPropertyPage)prefPage).getThisProject();
            } else if (prefPage instanceof CompilerPropertyPage) {
                project = ((CompilerPropertyPage)prefPage).getThisProject();
            } else if (prefPage instanceof AspectJProjectPropertiesPage) {
                project = ((AspectJProjectPropertiesPage)prefPage).getThisProject();
            }
        }        
    }
    
    /**
     * Reset the change settings on the relevant page
     *
     */
    private void resetPreferencePageChangeSettings() {
        if (prefPage instanceof AspectPathPropertyPage) {
            ((AspectPathPropertyPage)prefPage).resetChangeSettings();
        } else if (prefPage instanceof InPathPropertyPage) {
            ((InPathPropertyPage)prefPage).resetChangeSettings();
        } else if (prefPage instanceof CompilerPropertyPage) {
            ((CompilerPropertyPage)prefPage).resetChangeSettings();
        } else if (prefPage instanceof AspectJProjectPropertiesPage) {
            ((AspectJProjectPropertiesPage)prefPage).resetChangeSettings();
        }        
    }
    
    /**
     * Check whether the settings on any of the aspectj pages 
     * have changed and return true if they have.
     */
    private boolean somethingHasChanged() {
        if (AspectJProjectPropertiesPage.aspectjSettingHasChanged()
                || CompilerPropertyPage.compilerSettingsHaveChanged()
                || AspectPathBlock.aspectPathHasChanged()
                || InPathBlock.inPathHasChanged()) {
            return true;
        }
        return false;
    }
    
    /**
     * If the settings on the AspectJ page have been changed and updated, 
     * (this only happens if the "performOK" method has been executed
     * on the AspectJ page) or if the settings haven't changed, then return true. 
     * Otherwise, return false;
     */
    private boolean aspectJPageReady() {
        if ((AspectJProjectPropertiesPage.aspectjSettingHasChanged() 
                        && AspectJProjectPropertiesPage.aspectjSettingHasBeenUpdated())
                        || !AspectJProjectPropertiesPage.aspectjSettingHasChanged()) {
            return true;
        }
        return false;
    }
    
    /**
     * Check if the settings on the Compiler page have been changed and updated, 
     * (this only happens if the "performOK" method has been executed
     * on the Compiler page) or if the settings haven't changed. If this is the
     * case and the user has chosen to build now, then return true, otherwise
     * return false.
     */
    private boolean compilerPageReady() {
        if (((CompilerPropertyPage.compilerSettingsHaveChanged()
                        && CompilerPropertyPage.compilerSettingsHaveBeenUpdated()) 
                        || !CompilerPropertyPage.compilerSettingsHaveChanged())
                        && CompilerPropertyPage.chosenToDoBuild()) {
            return true;
        }
        return false;
    }
    
    /**
     * If the settings on the AspectPath page have been changed and updated, 
     * (this only happens if the "performOK" method has been executed
     * on the AspectPath page) or if the settings haven't changed, then return true. 
     * Otherwise, return false;
     */
    private boolean aspectPathPageReady() {
        if ((AspectPathBlock.aspectPathHasChanged() && AspectPathBlock.aspectPathHasBeenUpdated()) 
                || !AspectPathBlock.aspectPathHasChanged()) {
            return true;
        }        
        return false;
    }
    
    /**
     * If the settings on the inpath page have been changed and updated, 
     * (this only happens if the "performOK" method has been executed
     * on the inpath page) or if the settings haven't changed, then return true. 
     * Otherwise, return false;
     */
    private boolean inpathPageReady() {
        if ((InPathBlock.inPathHasChanged() && InPathBlock.inPathHasBeenUpdated()) 
                || !InPathBlock.inPathHasChanged()) {
            return true;
        }
        return false;
    }
    
    /**
     * Build the project
     */
    private void doProjectBuild() {
        if (prefPage == null || project == null) {
            return;
        }
        ProgressMonitorDialog dialog = new ProgressMonitorDialog(prefPage.getShell());
        try {
            dialog.run(true, true, new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor)
                        throws InvocationTargetException {
                    monitor.beginTask("", 2); //$NON-NLS-1$
                    try {
                        monitor
                                .setTaskName(AspectJUIPlugin
                                        .getResourceString("OptionsConfigurationBlock.buildproject.taskname")); //$NON-NLS-1$
                        project.build(IncrementalProjectBuilder.FULL_BUILD,
                                "org.eclipse.ajdt.ui.ajbuilder", null,
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
            AspectJUIPlugin.getDefault().getErrorHandler().handleError(message,
                    e);
        }
    }

}
