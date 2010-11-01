/*******************************************************************************
 * Copyright (c) 2010 SpringSource Corporation and others.
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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaModelException;

/**
 * 
 * @author Andrew Eisenberg
 * @created Oct 29, 2010
 */
public abstract class AbstractModelTest extends AJDTCoreTestCase {

    public AbstractModelTest(String name) {
        super(name);
    }

    public AbstractModelTest() {
        super();
    }

    protected void checkHandles(IJavaProject jProject) throws Exception {
        final AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForJavaElement(jProject);
        final List<String> accumulatedErrors = new ArrayList<String>();
        
        // check all the java handles
        IPackageFragment[] frags = jProject.getPackageFragments();
        for (int i = 0; i < frags.length; i++) {
            ICompilationUnit[] units = frags[i].getCompilationUnits();
            for (int j = 0; j < units.length; j++) {
                accumulatedErrors.addAll(walk(units[j], model));
            }
        }
        
        
        // now check all the aj handles
        AsmManager asm = AspectJPlugin.getDefault().getCompilerFactory().getCompilerForProject(jProject.getProject()).getModel();
        IHierarchy hierarchy = asm.getHierarchy();
        hierarchy.getRoot().walk(new HierarchyWalker() {
            protected void preProcess(IProgramElement node) {
                try {
                    HandleTestUtils.checkAJHandle(node.getHandleIdentifier(), model);
                } catch (JavaModelException e) {
                    throw new RuntimeException(e);
                }
            } 
        });

        
        if (accumulatedErrors.size() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("Found errors in comparing elements:\n");
            for (String msg : accumulatedErrors) {
                sb.append(msg + "\n");
            }
            fail(sb.toString());
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

}