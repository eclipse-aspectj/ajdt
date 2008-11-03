package org.eclipse.ajdt.core.model;


aspect EnsureInitialized {
    before(AJProjectModelFacade model) : 
            execution(public * AJProjectModelFacade.*(..)) && 
            this(model) {
        if (!model.isInitialized) {
            model.init();
        }
    }
}
