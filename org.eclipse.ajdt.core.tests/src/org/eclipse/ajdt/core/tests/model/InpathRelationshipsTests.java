/*******************************************************************************
 * Copyright (c) 2009 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *  Andrew Eisenberg - initial implementation
 *******************************************************************************/
package org.eclipse.ajdt.core.tests.model;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.AJCompilationUnit;
import org.eclipse.ajdt.core.model.AJProjectModelFacade;
import org.eclipse.ajdt.core.model.AJProjectModelFactory;
import org.eclipse.ajdt.core.tests.AJDTCoreTestCase;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 * Tests bug 271269.  Ensures that elements on the in path are
 * properly represented in the model.
 * 
 * No IProgramElement exists, but the relationship map should hold 
 * the relationships
 * 
 * @author Andrew Eisenberg
 */
public class InpathRelationshipsTests extends AJDTCoreTestCase {
	
    IProject target;
    IProject depends;
    AJProjectModelFacade model;
    protected void setUp() throws Exception {
        super.setUp();
        depends = createPredefinedProject("Bug271269Depends");
        target = createPredefinedProject("Bug271269");
        model = AJProjectModelFactory.getInstance().getModelForProject(target);
    }

    private Set gatherTargetTypesNonDefault() throws Exception{
        ICompilationUnit source = (ICompilationUnit) JavaCore.create(depends.getFile("src/g/Source.java"));
        IType[] types = source.getAllTypes();
        Set targetTypes = new HashSet();
        for (int i = 0; i < types.length; i++) {
            targetTypes.add(types[i]);
        }
        
        IPackageFragment frag = (IPackageFragment) JavaCore.create(target.getFolder("binaryFolder/g"));
        IClassFile[] cFiles = frag.getClassFiles();
        for (int i = 0; i < cFiles.length; i++) {
            targetTypes.add(cFiles[i].getType());
        }
        return targetTypes;
    }

    private Set gatherTargetTypesDefault() throws Exception{
        ICompilationUnit source = (ICompilationUnit) JavaCore.create(depends.getFile("src/InDefault.java"));
        IType[] types = source.getAllTypes();
        Set targetTypes = new HashSet();
        for (int i = 0; i < types.length; i++) {
            targetTypes.add(types[i]);
        }
        
        IPackageFragment frag = ((IPackageFragmentRoot) JavaCore.create(target.getFolder("binaryFolder"))).getPackageFragment("");
        IClassFile[] cFiles = frag.getClassFiles();
        for (int i = 0; i < cFiles.length; i++) {
            targetTypes.add(cFiles[i].getType());
        }
        return targetTypes;
    }

    public void testInPathRelationshipsNonDefault() throws Exception {
        IFile file = target.getFile("src/snippet/AdvisesLinked.aj");
        AJCompilationUnit unit = (AJCompilationUnit) AspectJCore.create(file);
        Set /*IType*/ targetTypes = gatherTargetTypesNonDefault();
        Map relationships = model.getRelationshipsForFile(unit);
        assertEquals("Should have found 15 relationships in the compilation unit", 15, relationships.size());
        for (Iterator allRelsIter = relationships.values().iterator(); allRelsIter.hasNext();) {
            List rels = (List) allRelsIter.next();
            for (Iterator relsIter = rels.iterator(); relsIter.hasNext();) {
                IRelationship rel = (IRelationship) relsIter.next();
                for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext();) {
                    String targetHandle = (String) targetIter.next();
                    IJavaElement elt = model.programElementToJavaElement(targetHandle);
                    assertTrue("Java element should exist: " + elt.getHandleIdentifier(), elt.exists());
                    targetTypes.remove(elt);
                }
            }
        }
        
        assertTrue("The following types should have been a target of an ITD:\n" + printTargetTypes(targetTypes), targetTypes.size() == 0);
    }

    public void testInPathRelationshipsDefault() throws Exception {
        IFile file = target.getFile("src/AdvisesLinkedDefault.aj");
        AJCompilationUnit unit = (AJCompilationUnit) AspectJCore.create(file);
        Set /*IType*/ targetTypes = gatherTargetTypesDefault();
        Map relationships = model.getRelationshipsForFile(unit);
        assertEquals("Should have found 15 relationships in the compilation unit", 15, relationships.size());
        for (Iterator allRelsIter = relationships.values().iterator(); allRelsIter.hasNext();) {
            List rels = (List) allRelsIter.next();
            for (Iterator relsIter = rels.iterator(); relsIter.hasNext();) {
                IRelationship rel = (IRelationship) relsIter.next();
                for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext();) {
                    String targetHandle = (String) targetIter.next();
                    IJavaElement elt = model.programElementToJavaElement(targetHandle);
                    assertTrue("Java element should exist: " + elt.getHandleIdentifier(), elt.exists());
                    targetTypes.remove(elt);
                }
            }
        }
        
        assertTrue("The following types should have been a target of an ITD:\n" + printTargetTypes(targetTypes), targetTypes.size() == 0);
    }
    
    /**
     * relationships should not be removed after an incremental build
     */
    public void testRelationshipsAfterIncrementalBuild() throws Exception {
        IFile file = target.getFile("src/snippet/AdvisesLinked.aj");;
        file.touch(null);
        waitForAutoBuild();
        testInPathRelationshipsNonDefault();
        
        file = target.getFile("src/AdvisesLinkedDefault.aj");
        file.touch(null);
        waitForAutoBuild();
        testInPathRelationshipsDefault();
    }

    private String printTargetTypes(Set targetTypes) {
        StringBuffer sb = new StringBuffer();
        for (Iterator targetIter = targetTypes.iterator(); targetIter.hasNext();) {
            IType type = (IType) targetIter.next();
            sb.append(type.getHandleIdentifier() + "\n");
        }
        return sb.toString();
    }
    
}
