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
import org.eclipse.jdt.core.IClassFile;
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
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.ImportContainer;

/**
 * 
 * @author andrew
 * Tests  Bug 265553
 * Ensure that binary handles can be traversed
 * Also check that we can get the correct start and end location for binary handles
 */
public class AJModelTest5 extends AJDTCoreTestCase {

    // maybe remove
    public void testBug265553AJHandleIdentifiers2() throws Exception {
        IProject onAspectPath = createPredefinedProject("Bug265553AspectPath"); //$NON-NLS-1$
        final AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(onAspectPath);
        
        final List/*String*/ accumulatedErrors = new ArrayList();
        
        AsmManager asm = AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(onAspectPath).getModel();
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
    public void testBug265553AJHandleIdentifiers() throws Exception {
        createPredefinedProject("Bug265553AspectPath"); //$NON-NLS-1$
        IProject base = createPredefinedProject("Bug265553Base"); //$NON-NLS-1$
        final AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(base);

        final List/*String*/ accumulatedErrors = new ArrayList();

        AsmManager asm = AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(base).getModel();
        IHierarchy hierarchy = asm.getHierarchy();
        hierarchy.getRoot().walk(new HierarchyWalker() {
            protected void preProcess(IProgramElement node) {
                if (node.getName().equals("binaries") || node.getParent().getName().equals("binaries") ||
                        (node.getParent().getName().endsWith(".class") && (
                                node.getKind() == IProgramElement.Kind.IMPORT_REFERENCE ||
                                node.getKind() == IProgramElement.Kind.PACKAGE_DECLARATION))) {
                    // binary java elements do not have these and neither should IPEs
                } else {
                    checkAJHandle(node.getHandleIdentifier(), model);
                }
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
        createPredefinedProject("Bug265553AspectPath"); //$NON-NLS-1$
        IProject project = createPredefinedProject("Bug265553Base"); //$NON-NLS-1$
        final AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(project);
        final List/*String*/ accumulatedErrors = new ArrayList();
        IJavaProject jProject = JavaCore.create(project);
        IPackageFragment[] frags = jProject.getPackageFragments();
        for (int i = 0; i < frags.length; i++) {
            ICompilationUnit[] units = frags[i].getCompilationUnits();
            for (int j = 0; j < units.length; j++) {
                accumulatedErrors.addAll(walk(units[j], model));
            }
            if (frags[i].getElementName().equals("p") || 
                    frags[i].getElementName().equals("q")) {
                IClassFile[] classes = frags[i].getClassFiles();
                for (int j = 0; j < classes.length; j++) {
                    accumulatedErrors.addAll(walk(classes[j], model));
                }
            }
        }
    }
    
    
    
    private Collection<String> walk(IJavaElement elt, AJProjectModelFacade model) throws Exception {
        final List<String> accumulatedErrors = new ArrayList<String>();
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

    public static void checkAJHandle(String origAjHandle, AJProjectModelFacade model) {
    	try {
			HandleTestUtils.checkAJHandle(origAjHandle, model);
		} catch (JavaModelException e) {
			throw new Error(e);
		}
    }

 }
