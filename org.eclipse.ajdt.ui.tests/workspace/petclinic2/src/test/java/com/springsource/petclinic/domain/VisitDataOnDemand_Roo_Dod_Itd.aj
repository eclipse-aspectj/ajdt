package com.springsource.petclinic.domain;

privileged aspect VisitDataOnDemand_Roo_Dod_Itd {
    
    declare @type: VisitDataOnDemand: @org.springframework.beans.factory.annotation.Configurable;    
    
    declare @type: VisitDataOnDemand: @org.springframework.stereotype.Component;    
    
    private java.util.Random VisitDataOnDemand.rnd = new java.security.SecureRandom();    
    
    private java.util.List<Visit> VisitDataOnDemand.data;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.domain.PetDataOnDemand VisitDataOnDemand.petDataOnDemand;    
    
    public Visit VisitDataOnDemand.getNewTransientEntity(int index) {    
        Visit obj = new Visit();        
        obj.setVisitDate(new java.util.Date(new java.util.Date().getTime() - 10000000L));        
        obj.setPet(petDataOnDemand.getRandomPersistentEntity());        
        return obj;        
    }    
    
    public Visit VisitDataOnDemand.getRandomPersistentEntity() {    
        init();        
        return data.get(rnd.nextInt(data.size()));        
    }    
    
    public Boolean VisitDataOnDemand.modify(com.springsource.petclinic.domain.Visit obj) {    
        return false;        
    }    
    
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)    
    public void VisitDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
        
        data = Visit.findVisitEntries(0, 10);        
        org.springframework.util.Assert.notNull(data, "Find entries implementation for 'Visit' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
        
        for (int i = 0; i < 10; i ++) {        
            Visit obj = getNewTransientEntity(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
