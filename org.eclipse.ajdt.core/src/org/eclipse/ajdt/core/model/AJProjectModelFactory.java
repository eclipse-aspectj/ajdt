/*******************************************************************************
 * Copyright (c) 2008 SpringSource and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      SpringSource
 *      Andrew Eisenberg (initial implementation)
 *******************************************************************************/
package org.eclipse.ajdt.core.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaElement;

/**
 * A factory for generating {@link AJProjectModdelFacade} objects.
 * 
 * For now, we are not caching project models.  Seems like 
 * they are created and destroyed more erraticlly than I 
 * thought.  But, doing so might be a good area for future 
 * efficiency improvements.
 */
public class AJProjectModelFactory {

    // no caching
//    private Map/*IJavaProject,AJProjectModelFacade*/
//        projectModelMap = new HashMap();
    
    private final static AJProjectModelFactory INSTANCE = new AJProjectModelFactory();
    

    public static AJProjectModelFactory getInstance() {
        return INSTANCE;
    }
    
    private AJProjectModelFactory() { }
    
    /**
     * creates a new project model
     * 
     * may introduce caching in the future
     */
    public AJProjectModelFacade getModelForProject(IProject project) {
//        AJProjectModelFacade model = (AJProjectModelFacade) projectModelMap.get(project);
//        if (model == null) {
//            model = new AJProjectModelFacade(project);
//            projectModelMap.put(project, model);
//        }
//        return model;
        return new AJProjectModelFacade(project);
    }
    
    /**
     * A convenience method to get the model when the handle is a java element
     */
    public AJProjectModelFacade getModelForJavaElement(IJavaElement elt) {
        return getModelForProject(elt.getJavaProject().getProject());
    }
    
    /**
     * does nothing since there is no caching
     * @param project
     */
    public void removeModelForProject(IProject project) {
//        AJProjectModelFacade model = (AJProjectModelFacade) projectModelMap.remove(project);
//        if (model != null) {
//            model.dispose();
//        }
    }
}
