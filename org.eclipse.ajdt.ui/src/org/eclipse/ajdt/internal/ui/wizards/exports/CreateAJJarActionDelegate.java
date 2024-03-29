/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Spring Source (Andrew Eisenberg) - adapted for use with AJDT
 *******************************************************************************/
package org.eclipse.ajdt.internal.ui.wizards.exports;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ajdt.internal.ui.wizards.AJJarPackageActionDelegate;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.internal.corext.util.Messages;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.ExceptionHandler;
import org.eclipse.jdt.internal.ui.viewsupport.BasicElementLabels;
import org.eclipse.jdt.ui.jarpackager.IJarDescriptionReader;
import org.eclipse.jdt.ui.jarpackager.IJarExportRunnable;
import org.eclipse.jdt.ui.jarpackager.JarPackageData;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Adapted from CreateJarActionDelegate
 * AspectJ Changes marked
 */
public class CreateAJJarActionDelegate extends AJJarPackageActionDelegate {  // AspectJ Change

    /*
     * @see IActionDelegate
     */
    public void run(IAction action) {
        IFile[] descriptions= getDescriptionFiles(getSelection());
        MultiStatus mergedStatus;
        int length= descriptions.length;
        if (length < 1)
            return;

        // Create read multi status
        String message;
        if (length > 1)
            message= JarPackagerMessages.JarFileExportOperation_creationOfSomeJARsFailed;
        else
            message= JarPackagerMessages.JarFileExportOperation_jarCreationFailed;
        MultiStatus readStatus= new MultiStatus(JavaPlugin.getPluginId(), 0, message, null);
        AJJarPackageData[] jarPackages= readJarPackages(descriptions, readStatus);  // AspectJ Change
        if (jarPackages.length > 0) {
            IStatus status= export(jarPackages);
            if (status == null)
                return; // cancelled
            if (readStatus.getSeverity() == IStatus.ERROR)
                message= readStatus.getMessage();
            else
                message= status.getMessage();
            // Create new status because we want another message - no API to set message
            mergedStatus= new MultiStatus(JavaPlugin.getPluginId(), status.getCode(), readStatus.getChildren(), message, null);
            mergedStatus.merge(status);
        } else
            mergedStatus= readStatus;

        if (!mergedStatus.isOK())
            ErrorDialog.openError(getShell(), JarPackagerMessages.CreateJarActionDelegate_jarExport_title, null, mergedStatus);
    }

    private AJJarPackageData[] readJarPackages(IFile[] descriptions, MultiStatus readStatus) {  // AspectJ Change
        List<AJJarPackageData> jarPackagesList= new ArrayList<>(descriptions.length);
      for (IFile description : descriptions) {
          AJJarPackageData jarPackage = readJarPackage(description, readStatus);
        if (jarPackage != null)
          jarPackagesList.add(jarPackage);
      }
        return jarPackagesList.toArray(new AJJarPackageData[0]);  // AspectJ Change
    }

    private IStatus export(AJJarPackageData[] jarPackages) {  // AspectJ Change
        Shell shell= getShell();
        IJarExportRunnable op= jarPackages[0].createJarExportRunnable(jarPackages, shell);
        try {
            PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false, true, op);
            //PlatformUI.getWorkbench().getProgressService().run(false, true, op); // see bug 118152
        } catch (InvocationTargetException ex) {
            if (ex.getTargetException() != null) {
                ExceptionHandler.handle(ex, shell, JarPackagerMessages.CreateJarActionDelegate_jarExportError_title, JarPackagerMessages.CreateJarActionDelegate_jarExportError_message);
                return null;
            }
        } catch (InterruptedException e) {
            // do nothing on cancel
            return null;
        }
        return op.getStatus();
    }

    /**
     * Reads the JAR package spec from file.
     * @param description the description file
     * @param readStatus status
     * @return returns the read the JAR package spec
     */
    protected AJJarPackageData readJarPackage(IFile description, MultiStatus readStatus) {  // AspectJ Change
        Assert.isLegal(description.isAccessible());
        Assert.isNotNull(description.getFileExtension());
        Assert.isLegal(description.getFileExtension().equals(AJJarPackagerUtil.DESCRIPTION_EXTENSION));  // AspectJ Change
        AJJarPackageData jarPackage= new AJJarPackageData();  // AspectJ Change
        IJarDescriptionReader reader= null;
        try {
            reader= jarPackage.createJarDescriptionReader(description.getContents());
            // Do not save - only generate JAR
            reader.read(jarPackage);
            jarPackage.setSaveManifest(false);
            jarPackage.setSaveDescription(false);
        } catch (CoreException ex) {
                String message= Messages.format(JarPackagerMessages.JarFileExportOperation_errorReadingFile, new Object[] {BasicElementLabels.getPathLabel(description.getFullPath(), false), ex.getStatus().getMessage()});
                addToStatus(readStatus, message, ex);
                return null;
        } finally {
            if (reader != null)
                // AddWarnings
                readStatus.addAll(reader.getStatus());
            try {
                if (reader != null)
                    reader.close();
            }
            catch (CoreException ex) {
                String message= Messages.format(JarPackagerMessages.JarFileExportOperation_errorClosingJarPackageDescriptionReader, BasicElementLabels.getPathLabel(description.getFullPath(), false));
                addToStatus(readStatus, message, ex);
            }
        }
        return jarPackage;
    }

    private void addToStatus(MultiStatus multiStatus, String defaultMessage, CoreException ex) {
        IStatus status= ex.getStatus();
        String message= ex.getLocalizedMessage();
        if (message == null || message.length() < 1)
            status= new Status(status.getSeverity(), status.getPlugin(), status.getCode(), defaultMessage, ex);
        multiStatus.add(status);
    }
}
