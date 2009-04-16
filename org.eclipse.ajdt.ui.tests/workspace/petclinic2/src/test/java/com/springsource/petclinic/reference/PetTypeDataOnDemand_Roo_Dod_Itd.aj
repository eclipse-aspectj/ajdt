package com.springsource.petclinic.reference;

privileged aspect PetTypeDataOnDemand_Roo_Dod_Itd {
    
    declare @type: PetTypeDataOnDemand: @org.springframework.beans.factory.annotation.Configurable;    
    
    declare @type: PetTypeDataOnDemand: @org.springframework.stereotype.Component;    
    
    private java.util.Random PetTypeDataOnDemand.rnd = new java.security.SecureRandom();    
    
    private java.util.List<PetType> PetTypeDataOnDemand.data;    
    
    public PetType PetTypeDataOnDemand.getNewTransientEntity(int index) {    
        PetType obj = new PetType();        
        obj.setType("type_" + index);        
        return obj;        
    }    
    
    public PetType PetTypeDataOnDemand.getRandomPersistentEntity() {    
        init();        
        return data.get(rnd.nextInt(data.size()));        
    }    
    
    public Boolean PetTypeDataOnDemand.modify(com.springsource.petclinic.reference.PetType obj) {    
        obj.setType(obj.getType() + "_modified");        
        return true;        
    }    
    
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)    
    public void PetTypeDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
        
        data = PetType.findPetTypeEntries(0, 10);        
        org.springframework.util.Assert.notNull(data, "Find entries implementation for 'PetType' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
        
        for (int i = 0; i < 10; i ++) {        
            PetType obj = getNewTransientEntity(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
