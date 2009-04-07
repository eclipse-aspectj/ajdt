package org.eclipse.ajdt.core.model;


aspect EnsureInitialized {
    pointcut accessPrivateState() : (execution(public * AJProjectModelFacade.*(..)) || 
            execution(* AJProjectModelFacade.get*(..)));
    
    before(AJProjectModelFacade model) : 
        accessPrivateState() /*&& !cflowbelow(accessPrivateState())*/ && 
            this(model) {
        if (!model.isInitialized) {
            model.init();
        }
    }
}
