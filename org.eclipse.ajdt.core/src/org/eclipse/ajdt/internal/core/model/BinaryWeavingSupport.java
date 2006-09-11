/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *******************************************************************************/
package org.eclipse.ajdt.internal.core.model;

import java.io.File;

import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnitManager;
import org.eclipse.ajdt.internal.core.AJWorkingCopyOwner;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class BinaryWeavingSupport {

	public static boolean isActive = true;
	
	public static IJavaElement locateBinaryElementsInWorkspace(IProgramElement link) {
		ISourceLocation isl = link.getSourceLocation();
		String filename = isl.getSourceFileName();
		File sourceFile = isl.getSourceFile();
		String path = sourceFile.getPath();
		int ind = path.indexOf('!');
		if (ind <= 0) {
			return null;
		}
		String dir = path.substring(0, ind);
		String cls = path.substring(ind + 1).replace('\\', '/');
		IProject project = findProjectForPath(new Path(dir));
		if (project == null) {
			return null;
		}
		
		String pack = "";
		int ind2 = cls.lastIndexOf('/');
		if (ind2 != -1) { // not default package
			pack = cls.substring(0,ind2).replace('/', '.');
		}
		IResource res = findSourceFolderResource(project,pack,filename);
			
		if (res == null) {
			return null;
		}
		AJCompilationUnit ajcu = AJCompilationUnitManager.INSTANCE.getAJCompilationUnit((IFile)res);
		if (ajcu == null) {
			return null;
		}
		IJavaElement je = findElementAtLine(ajcu,isl.getLine());
		return je;
	}
	
	public static IResource findSourceFolderResource(IProject project, String packageName, String typeName) {
		IJavaProject jp = JavaCore.create(project);
		IType type;
		try {
			type = jp.findType(packageName, typeName, AJWorkingCopyOwner.INSTANCE, new NullProgressMonitor());
			if (type != null) {
				return type.getResource();
			}
		} catch (JavaModelException e) {
		}
		return null;
	}
	
	public static IProject findProjectForPath(IPath path) {
		IWorkspaceRoot root = AspectJPlugin.getWorkspace().getRoot();
		IContainer[] containers = root.findContainersForLocation(path);
		if (containers.length == 0) {
			return null;
		}
		IProject project = null;
		for (int i = 0; (project == null) && (i < containers.length); i++) {
			project = containers[i].getProject();
			if (!project.exists()) {
				project = null;
			}
		}
		return project;
	}
	
	public static IJavaElement findElementAtLine(AJCompilationUnit ajcu, int targetLine) {
		int line = 0;
		ajcu.requestOriginalContentMode();
		String src = "";
		try {
			src = ((ISourceReference) ajcu).getSource();
		} catch (JavaModelException e1) {
		}
		ajcu.discardOriginalContentMode();

		for (int i = 0; i < src.length() - 1; i++) {
			if (src.charAt(i) == '\n') {
				line++;
				if (line == targetLine) {
					try {
						return ((ICompilationUnit)ajcu).getElementAt(i + 1);
					} catch (JavaModelException e) {
					}
				}
			}
		}
		return null;
	}

	
}
