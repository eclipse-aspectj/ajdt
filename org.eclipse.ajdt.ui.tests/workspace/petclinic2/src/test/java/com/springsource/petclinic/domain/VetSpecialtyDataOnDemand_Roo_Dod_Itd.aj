package com.springsource.petclinic.domain;

privileged aspect VetSpecialtyDataOnDemand_Roo_Dod_Itd {
    
    declare @type: VetSpecialtyDataOnDemand: @org.springframework.beans.factory.annotation.Configurable;    
    
    declare @type: VetSpecialtyDataOnDemand: @org.springframework.stereotype.Component;    
    
    private java.util.Random VetSpecialtyDataOnDemand.rnd = new java.security.SecureRandom();    
    
    private java.util.List<VetSpecialty> VetSpecialtyDataOnDemand.data;    
    
    public VetSpecialty VetSpecialtyDataOnDemand.getNewTransientEntity(int index) {    
        VetSpecialty obj = new VetSpecialty();        
        obj.setRegistered(new java.util.Date(new java.util.Date().getTime() - 10000000L));        
        return obj;        
    }    
    
    public VetSpecialty VetSpecialtyDataOnDemand.getRandomPersistentEntity() {    
        init();        
        return data.get(rnd.nextInt(data.size()));        
    }    
    
    public Boolean VetSpecialtyDataOnDemand.modify(com.springsource.petclinic.domain.VetSpecialty obj) {    
        return false;        
    }    
    
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)    
    public void VetSpecialtyDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
        
        data = VetSpecialty.findVetSpecialtyEntries(0, 10);        
        org.springframework.util.Assert.notNull(data, "Find entries implementation for 'VetSpecialty' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
        
        for (int i = 0; i < 10; i ++) {        
            VetSpecialty obj = getNewTransientEntity(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
