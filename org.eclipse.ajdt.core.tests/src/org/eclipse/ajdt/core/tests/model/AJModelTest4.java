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
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.internal.core.ImportContainer;

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
                accumulatedErrors.addAll(checkAJHandle(node.getHandleIdentifier(), model));
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
        accumulatedErrors.addAll(checkJavaHandle(elt.getHandleIdentifier(), model));
        if (elt instanceof IParent) {
            IParent parent = (IParent) elt;
            IJavaElement[] children = parent.getChildren();
            for (int i = 0; i < children.length; i++) {
                accumulatedErrors.addAll(walk(children[i], model));
            }
        }
        return accumulatedErrors;
    }

    public static List checkAJHandle(String origAjHandle, AJProjectModelFacade model) {
        List/*String*/ accumulatedErrors = new ArrayList();
        
        try {
            
            IJavaElement origJavaElement = model.programElementToJavaElement(origAjHandle);
            String origJavaHandle = origJavaElement.getHandleIdentifier();
            
            // AspectJ adds the import container always even when there are no imports
            if (!origJavaElement.exists() && !(origJavaElement instanceof ImportContainer)
                    && !(origJavaElement instanceof IInitializer) ) { // Bug 263310
                accumulatedErrors.add("Java element " + origJavaElement.getHandleIdentifier() + " does not exist");
            }
            
            if (origJavaElement.getJavaProject().getProject().equals(model.getProject())) {
            
                IProgramElement recreatedAjElement = model.javaElementToProgramElement(origJavaElement);
                String recreatedAjHandle = recreatedAjElement.getHandleIdentifier();
                
                IJavaElement recreatedJavaElement = model.programElementToJavaElement(recreatedAjHandle);
                String recreatedJavaHandle = recreatedJavaElement.getHandleIdentifier();
                
                
                if (!origJavaHandle.equals(recreatedJavaHandle)) {
                    accumulatedErrors.add("Handle identifier of JavaElements should be equal:\n\t" + origJavaHandle + "\n\t" + recreatedJavaHandle);
                }
                
                if (!origAjHandle.equals(recreatedAjHandle)) {
                    accumulatedErrors.add("Handle identifier of ProgramElements should be equal:\n\t" + origAjHandle + "\n\t" + recreatedAjHandle);
                }
                
                if (!origJavaElement.equals(recreatedJavaElement)) {
                    accumulatedErrors.add("JavaElements should be equal:\n\t" + origJavaElement + "\n\t" + recreatedJavaElement);
                }
                
                if (!origJavaElement.getElementName().equals(recreatedJavaElement.getElementName())) {
                    accumulatedErrors.add("JavaElement names should be equal:\n\t" + origJavaElement.getElementName() + "\n\t" + recreatedJavaElement.getElementName());
                }
                
                if (origJavaElement.getElementType()!= recreatedJavaElement.getElementType()) {
                    accumulatedErrors.add("JavaElement types should be equal:\n\t" + origJavaElement.getElementType() + "\n\t" + recreatedJavaElement.getElementType());
                }
                
                if (!origJavaElement.getParent().equals(recreatedJavaElement.getParent())) {
                    accumulatedErrors.add("JavaElement parents should be equal:\n\t" + origJavaElement.getParent() + "\n\t" + recreatedJavaElement.getParent());
                }
                
                if (!origJavaElement.getJavaProject().equals(recreatedJavaElement.getJavaProject())) {
                    accumulatedErrors.add("JavaElement projects should be equal:\n\t" + origJavaElement.getJavaProject() + "\n\t" + recreatedJavaElement.getJavaProject());
                }
            } else {
                // reference to another project
                if (!origJavaElement.exists()) {
                    accumulatedErrors.add("Program Element in other project should exist, but doesn't:\n\t" + origJavaHandle );
                }
    
                
                // check to make sure that this element is in the other model
                AJProjectModelFacade otherModel = AJProjectModelFactory.getInstance().getModelForProject(origJavaElement.getJavaProject().getProject());
                IProgramElement ipe = otherModel.javaElementToProgramElement(origJavaElement);
                checkAJHandle(ipe.getHandleIdentifier(), otherModel);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            accumulatedErrors.add("Error thrown on: " + origAjHandle);
            accumulatedErrors.add(e.getMessage());
            for (int i = 0; i < e.getStackTrace().length; i++) {
                accumulatedErrors.add("\t" + e.getStackTrace()[i].toString());
            }
        }
        return accumulatedErrors;
    }
    
    
    public static List checkJavaHandle(String origJavaHandle, AJProjectModelFacade model) {
        List/*String*/ accumulatedErrors = new ArrayList();
        
        try {
            
            IJavaElement origJavaElement = JavaCore.create(origJavaHandle);
            IProgramElement origAjElement = model.javaElementToProgramElement(origJavaElement);
            String origAjHandle = origAjElement.getHandleIdentifier();
            
            // AspectJ adds the import container always even when there are no imports
            if (!origJavaElement.exists() && !(origJavaElement instanceof ImportContainer)
            && !(origJavaElement instanceof IInitializer) ) { // Bug 263310
                accumulatedErrors.add("Java element " + origJavaElement.getHandleIdentifier() + " does not exist");
            }
            
            if (origJavaElement.getJavaProject().getProject().equals(model.getProject())) {
            
                IProgramElement recreatedAjElement = model.javaElementToProgramElement(origJavaElement);
                String recreatedAjHandle = recreatedAjElement.getHandleIdentifier();
                
                IJavaElement recreatedJavaElement = model.programElementToJavaElement(recreatedAjHandle);
                String recreatedJavaHandle = recreatedJavaElement.getHandleIdentifier();
                
                
                if (!origJavaHandle.equals(recreatedJavaHandle)) {
                    accumulatedErrors.add("Handle identifier of JavaElements should be equal:\n\t" + origJavaHandle + "\n\t" + recreatedJavaHandle);
                }
                
                if (!origAjHandle.equals(recreatedAjHandle)) {
                    accumulatedErrors.add("Handle identifier of ProgramElements should be equal:\n\t" + origAjHandle + "\n\t" + recreatedAjHandle);
                }
                
                if (!origJavaElement.equals(recreatedJavaElement)) {
                    accumulatedErrors.add("JavaElements should be equal:\n\t" + origJavaElement + "\n\t" + recreatedJavaElement);
                }
                
                if (!origJavaElement.getElementName().equals(recreatedJavaElement.getElementName())) {
                    accumulatedErrors.add("JavaElement names should be equal:\n\t" + origJavaElement.getElementName() + "\n\t" + recreatedJavaElement.getElementName());
                }
                
                if (origJavaElement.getElementType()!= recreatedJavaElement.getElementType()) {
                    accumulatedErrors.add("JavaElement types should be equal:\n\t" + origJavaElement.getElementType() + "\n\t" + recreatedJavaElement.getElementType());
                }
                
                if (!origJavaElement.getParent().equals(recreatedJavaElement.getParent())) {
                    accumulatedErrors.add("JavaElement parents should be equal:\n\t" + origJavaElement.getParent() + "\n\t" + recreatedJavaElement.getParent());
                }
                
                if (!origJavaElement.getJavaProject().equals(recreatedJavaElement.getJavaProject())) {
                    accumulatedErrors.add("JavaElement projects should be equal:\n\t" + origJavaElement.getJavaProject() + "\n\t" + recreatedJavaElement.getJavaProject());
                }
            } else {
                // reference to another project
                if (!origJavaElement.exists()) {
                    accumulatedErrors.add("Program Element in other project should exist, but doesn't:\n\t" + origJavaHandle );
                }
    
                
                // check to make sure that this element is in the other model
                AJProjectModelFacade otherModel = AJProjectModelFactory.getInstance().getModelForProject(origJavaElement.getJavaProject().getProject());
                IProgramElement ipe = otherModel.javaElementToProgramElement(origJavaElement);
                checkAJHandle(ipe.getHandleIdentifier(), otherModel);
            }
        } catch (Exception e) {
            e.fillInStackTrace();
            accumulatedErrors.add("Error thrown on: " + origJavaHandle);
            accumulatedErrors.add(e.getMessage());
            for (int i = 0; i < e.getStackTrace().length; i++) {
                accumulatedErrors.add("\t" + e.getStackTrace()[i].toString());
            }
        }
        return accumulatedErrors;
    }

 }
