/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Chapman - initial version
 *     Helen Hawkins - updated for new ajde interface (bug 148190)
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.IRelationship;
import org.eclipse.ajdt.core.AspectJCore;
import org.eclipse.ajdt.core.javaelements.IntertypeElement;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;


/**
 * See bug 253245
 * This class is a facade and only used for backwards compatibility for the SpringIDE
 * 
 * @deprecated
 */
public class AJModel {
    
    // This is the code we are trying to satisfy
//public static Set<IMethod> getDeclaredMethods(IType type) throws JavaModelException {
//  Set<IMethod> methods = new HashSet<IMethod>();
//  AJRelationshipType[] types = new AJRelationshipType[] { AJRelationshipManager.DECLARED_ON };
//  List<AJRelationship> rels = AJModel.getInstance().getAllRelationships(
//          type.getResource().getProject(), types);
//  for (AJRelationship rel : rels) {
//      if (rel.getTarget().equals(type)) {
//          IntertypeElement iType = (IntertypeElement) rel.getSource();
//          methods.add(iType);
//      }
//  }
//  return methods;
//}

    
    
	private static AJModel instance = new AJModel();
	
	/**
	 * @deprecated see bug 253245
	 * @return the AJModel singleton
	 */
	public static AJModel getInstance() {
        return instance;
    }
	
	/**
	 * @param proj the project whose relationships we are looking for
	 * @param relTypes the relationship types we are looking for
	 * @return all DECLARED_ON relationships for the given java element
	 * 
	 * @deprecated see bug 253245
	 */
	public List/*AJRelationship*/ getAllRelationships(IProject proj, AJRelationshipType[] relTypes) {
	    AJProjectModelFacade model = AJProjectModelFactory.getInstance().getModelForProject(proj);
	    if (model.hasModel()) {  // returns false if project is not built
	        List/*IRelationship*/ allRels = model.getRelationshipsForProject(relTypes);
	        List/*AJRelationship*/ ajRels = new ArrayList(allRels.size());
	        for (Iterator relIter = allRels.iterator(); relIter.hasNext();) {
	            IRelationship rel = (IRelationship) relIter.next();
	            IJavaElement source = model.programElementToJavaElement(rel.getSourceHandle());
	            if (source instanceof IntertypeElement) {
    	            for (Iterator targetIter = rel.getTargets().iterator(); targetIter.hasNext();) {
                        String target = (String) targetIter.next();
                        // ensure a Java handle is used here.
                        // because the things being compared to are 
                        // Java handles.
                        // This is avoiding problems when Type is in a .aj file
                        IJavaElement elt = model.programElementToJavaElement(target);
                        elt = JavaCore.create(AspectJCore.convertToJavaCUHandle(elt.getHandleIdentifier(), elt));
                        if (elt != null) {
                            // will be null if type is an aspect type or contained in an aspect type
                            AJRelationship ajRel = new AJRelationship(
                                    source, 
                                    AJRelationshipManager.toRelationshipType(rel.getName()), 
                                    elt, 
                                    rel.hasRuntimeTest());
                            ajRels.add(ajRel);
                        }
    	            }
                }
            }
	        return ajRels;
	    } else {
	        return Collections.EMPTY_LIST;
	    }
	}
}

