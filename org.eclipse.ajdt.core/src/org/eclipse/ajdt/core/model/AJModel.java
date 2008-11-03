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
import org.eclipse.core.resources.IProject;


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
	            IRelationship relElt = (IRelationship) relIter.next();
	            for (Iterator targetIter = relElt.getTargets().iterator(); targetIter.hasNext();) {
                    String target = (String) targetIter.next();
                    AJRelationship ajRel = new AJRelationship(
                            model.programElementToJavaElement(relElt.getSourceHandle()), 
                            AJRelationshipManager.toRelationshipType(relElt.getName()), 
                            model.programElementToJavaElement(target), 
                            relElt.hasRuntimeTest());
                   ajRels.add(ajRel);
                }
            }
	        return ajRels;
	    } else {
	        return Collections.EMPTY_LIST;
	    }
	}
}

