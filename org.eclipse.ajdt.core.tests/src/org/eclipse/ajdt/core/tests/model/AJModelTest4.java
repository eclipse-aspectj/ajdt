/*******************************************************************************
 * Copyright (c) 2008 SpringSource Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      Andrew Eisenberg = Initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.eclipse.ajdt.core.AspectJPlugin;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.ajdt.core.tests.HandleTestUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author andrew
 * @created Sep 18, 2008
 * Stress test the handle identifiers with a particularly nasty projectby 
 *
 */
public class AJModelTest4 extends AJDTCoreTestCase {

    public void testAJHandleIdentifiers() throws Exception {
        IProject project = createPredefinedProject("Handle Testing"); //$NON-NLS-1$
        final AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);

        final List/*String*/ accumulatedErrors = new ArrayList();

		AsmManager asm = AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(project.getProject()).getModel();
        IHierarchy hierarchy = asm.getHierarchy();
        hierarchy.getRoot().walk(new HierarchyWalker() {
            protected void preProcess(IProgramElement node) {
            	checkAJHandle(node.getHandleIdentifier(), model);
            } 
        });
        if (accumulatedErrors.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Found errors in comparing elements:\n");
            for (Iterator iterator = accumulatedErrors.iterator(); iterator
                    .hasNext();) {
                String msg = (String) iterator.next();
                sb.append(msg + "\n");
            }
            fail(sb.toString());
        }
    }
    
    public void testJavaHandleIdentifiers() throws Exception {
        IProject project = createPredefinedProject("Handle Testing"); //$NON-NLS-1$
        final AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);
        final List/*String*/ accumulatedErrors = new ArrayList();
        IJavaProject jProject = JavaCore.create(project);
        IPackageFragment[] frags = jProject.getPackageFragments();
        for (int i = 0; i < frags.length; i++) {
            ICompilationUnit[] units = frags[i].getCompilationUnits();
            for (int j = 0; j < units.length; j++) {
                accumulatedErrors.addAll(walk(units[j], model));
            }
        }
    }
    
    
    
    private Collection walk(IJavaElement elt, AJProjectModelFacade model) throws Exception {
        final List/*String*/ accumulatedErrors = new ArrayList();
        accumulatedErrors.addAll(HandleTestUtils.checkJavaHandle(elt.getHandleIdentifier(), model));
        if (elt instanceof IParent) {
            IParent parent = (IParent) elt;
            IJavaElement[] children = parent.getChildren();
            for (int i = 0; i < children.length; i++) {
                accumulatedErrors.addAll(walk(children[i], model));
            }
        }
        return accumulatedErrors;
    }

    public static void checkAJHandle(String ajHandle, AJProjectModelFacade model) {
		try {
			HandleTestUtils.checkAJHandle(ajHandle, model);
		} catch (JavaModelException e) {
			throw new Error(e);
		}
	}

 }
