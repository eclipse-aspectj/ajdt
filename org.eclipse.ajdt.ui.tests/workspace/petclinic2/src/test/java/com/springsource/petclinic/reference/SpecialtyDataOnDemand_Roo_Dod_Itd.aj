package com.springsource.petclinic.reference;

privileged aspect SpecialtyDataOnDemand_Roo_Dod_Itd {
    
    declare @type: SpecialtyDataOnDemand: @org.springframework.beans.factory.annotation.Configurable;    
    
    declare @type: SpecialtyDataOnDemand: @org.springframework.stereotype.Component;    
    
    private java.util.Random SpecialtyDataOnDemand.rnd = new java.security.SecureRandom();    
    
    private java.util.List<Specialty> SpecialtyDataOnDemand.data;    
    
    public Specialty SpecialtyDataOnDemand.getNewTransientEntity(int index) {    
        Specialty obj = new Specialty();        
        obj.setName("name_" + index);        
        return obj;        
    }    
    
    public Specialty SpecialtyDataOnDemand.getRandomPersistentEntity() {    
        init();        
        return data.get(rnd.nextInt(data.size()));        
    }    
    
    public Boolean SpecialtyDataOnDemand.modify(com.springsource.petclinic.reference.Specialty obj) {    
        obj.setName(obj.getName() + "_modified");        
        return true;        
    }    
    
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)    
    public void SpecialtyDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
        
        data = Specialty.findSpecialtyEntries(0, 10);        
        org.springframework.util.Assert.notNull(data, "Find entries implementation for 'Specialty' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
        
        for (int i = 0; i < 10; i ++) {        
            Specialty obj = getNewTransientEntity(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
