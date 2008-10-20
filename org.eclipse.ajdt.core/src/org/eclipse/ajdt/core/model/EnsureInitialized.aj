package org.eclipse.ajdt.core.model;

import org.aspectj.asm.AsmManager;

aspect EnsureInitialized {
    before(AJProjectModelFacade model) : 
            execution(public * AJProjectModelFacade.*(..)) && 
            this(model) {
        if (!model.isInitialized) {
            model.init();
        }
//        if (model.isInitialized) {
//            AsmManager.getDefault().setRelationshipMap(model.relationshipMap);
//            AsmManager.getDefault().setHierarchy(model.structureModel);
//        }
    }
}
