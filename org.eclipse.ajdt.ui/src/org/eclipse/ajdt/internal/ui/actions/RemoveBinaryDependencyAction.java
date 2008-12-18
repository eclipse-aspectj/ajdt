/**********************************************************************
Copyright (c) 2008 SpringSource Corporation
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/epl-v10.html
Contributors:
Andrew Eisenberg
**********************************************************************/
package org.eclipse.ajdt.internal.ui.actions;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.ajdt.internal.ui.text.UIMessages;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.wizards.buildpaths.CPListElement;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


/**
 * 
 * @author andrew
 * @created Aug 11, 2008
 *
 * This action removes any binary dependencies on project classpaths and replaces them
 * with the appropriate project dependencies.
 * 
 * 
 */
public class RemoveBinaryDependencyAction implements IObjectActionDelegate {

    private Vector selected = new Vector();


    public void run(IAction action) {
        
    }

    /**
     * From IActionDelegate - set the availability or otherwise of this
     * action.
     * 
     * This action is enabled only if all parts of the selection are projects. 
     * The action is applicable to the selected projects only.
     */
    public void selectionChanged(IAction action, ISelection sel) {
        selected.clear();
        boolean enable = true;
        if (sel instanceof IStructuredSelection) {
            IStructuredSelection selection = (IStructuredSelection) sel;
            for (Iterator iter = selection.iterator(); iter.hasNext();) {
                Object object = iter.next();
                if (object instanceof IAdaptable) {
                    IProject project = (IProject) ((IAdaptable)object).getAdapter(IProject.class);  
                    if(project != null) {
                        selected.add(project);
                    } else {
                        enable = false;
                        break;
                    }
        
                } else {
                    enable = false;
                    break;
                }
            }
            action.setEnabled(enable);
        }
    }

    public void setActivePart(IAction action, IWorkbenchPart targetPart) { }
    
    
    /**
     * Dependencies on binary folders of a project should be avoided
     * should use project dependencies instead.  
     * This method checks for paths that use binary dependencies
     */
    private void checkForBinaryDependencies(IJavaProject checkForProject) {
        try {
            IClasspathEntry[] checkForEntries = checkForProject.getRawClasspath();
            String dependentProjectName = null;
            
            outer:
            for (int i = 0; i < checkForEntries.length; i++) {
                IClasspathEntry checkForEntry = checkForEntries[i];
                
    
                // check to see if this is a bin folder or a jar file
                IFile file = checkForProject.getProject().getFile(checkForEntry.getPath());
                if (checkForEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY &&
                        file.getType() == IResource.FOLDER) {
                    IFolder folder = (IFolder) file;
                    IPath folderPath = folder.getFullPath();
                    IProject proj = folder.getProject();
                    IJavaProject jProj = JavaCore.create(proj);
                        
                        if (folderPath.equals(jProj.getOutputLocation())) {
                            dependentProjectName = jProj.getProject().getName(); 
                            break outer;
                        }
                        
                        IClasspathEntry[] referencedProjCP = jProj.getRawClasspath();
                        for (int j = 0; j < referencedProjCP.length; j++) {
                            IClasspathEntry classpathEntry = referencedProjCP[j];
                            if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                                if (folderPath.equals(classpathEntry.getOutputLocation())) {
                                    dependentProjectName = jProj.getProject().getName(); 
                                    break outer;
                                }
                            }
                        }
                }
            }
            
            if (dependentProjectName != null) {
                // change the dependency from binary to project
            }
        } catch (JavaModelException e) {
        }
    }
}
