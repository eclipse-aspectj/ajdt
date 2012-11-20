/*******************************************************************************
 *  Copyright (c) 2012 VMware Inc and others
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     Andrew Eisenberg - initial API and implementation
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.ajde;

import java.io.File;
import java.net.URI;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

/**
 * A cache that maps between file system URIs and one or more instances of the file in the
 * workspace.  There is one of these per project
 * @author Andrew Eisenberg
 * @created Nov 20, 2012
 */
public class FileURICache {
    private final static IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    private final WeakHashMap<URI, IFile[]> uriFileMap;
    private final WeakHashMap<URI, IContainer[]> uriContainerMap;
    private final IProject project;
    public FileURICache(IProject project) {
        this.project = project;
        this.uriFileMap = new WeakHashMap<URI, IFile[]>();
        this.uriContainerMap = new WeakHashMap<URI, IContainer[]>();
    }
    
    public IFile[] findFilesForURI(URI uri) {
        IFile[] files = uriFileMap.get(uri);
        if (files == null) {
            files = root.findFilesForLocationURI(uri);
            // don't cache if all references to this file are in other projects
            // i.e., cache if there are no files or if there is at least one file in current project
            boolean shouldCache = files.length == 0;
            for (IFile file : files) {
                if (file.getProject().equals(project)) {
                    shouldCache = true;
                }
            }
            if (shouldCache) {
                uriFileMap.put(uri, files);
            }
        }
        return files;
    }
    public IContainer[] findContainersForURI(URI uri) {
        IContainer[] containers = uriContainerMap.get(uri);
        if (containers == null) {
            containers = root.findContainersForLocationURI(uri);
            // don't cache if all references to this file are in other projects
            // i.e., cache if there are no files or if there is at least one file in current project
            boolean shouldCache = containers.length == 0;
            for (IContainer container : containers) {
                if (container.getProject().equals(project)) {
                    shouldCache = true;
                }
            }
            if (shouldCache) {
                uriContainerMap.put(uri, containers);
            }
        }
        return containers;
    }
    
    /**
     * Return the IResource within the project that maps to the given File
     * If project is not specified, arbitrarily choose the first resource
     */
    public IResource findResource(String fullPath, IProject p) {
        URI uri = new File(fullPath).toURI();
        IFile[] files = findFilesForURI(uri);
        if (files != null) {
            for (IFile file : files) {
                if (p == null || file.getProject().equals(p)) {
                    return file;
                }
            }
        } else {
            // maybe a folder
            IContainer[] containers = findContainersForURI(uri);
            for (IContainer container : containers) {
                if (p == null || container.getProject().equals(p)) {
                    return container;
                }
            }
        }
        return null;
    }
    
    /**
     * Find the first resoruce in the workspace that corresponds to this file path
     * @param fullPath
     * @return
     */
    public IResource findResource(String fullPath) {
        return findResource(fullPath, null);
    }

}
