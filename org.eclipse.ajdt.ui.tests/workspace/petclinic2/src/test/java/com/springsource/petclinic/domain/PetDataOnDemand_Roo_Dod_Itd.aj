package com.springsource.petclinic.domain;

privileged aspect PetDataOnDemand_Roo_Dod_Itd {
    
    declare @type: PetDataOnDemand: @org.springframework.beans.factory.annotation.Configurable;    
    
    declare @type: PetDataOnDemand: @org.springframework.stereotype.Component;    
    
    private java.util.Random PetDataOnDemand.rnd = new java.security.SecureRandom();    
    
    private java.util.List<Pet> PetDataOnDemand.data;    
    
    public Pet PetDataOnDemand.getNewTransientEntity(int index) {    
        Pet obj = new Pet();        
        obj.setSendReminders(new Boolean(true));        
        obj.setName("name_" + index);        
        obj.setWeight(new Float(index));        
        return obj;        
    }    
    
    public Pet PetDataOnDemand.getRandomPersistentEntity() {    
        init();        
        return data.get(rnd.nextInt(data.size()));        
    }    
    
    public Boolean PetDataOnDemand.modify(com.springsource.petclinic.domain.Pet obj) {    
        obj.setName(obj.getName() + "_modified");        
        return true;        
    }    
    
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)    
    public void PetDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
        
        data = Pet.findPetEntries(0, 10);        
        org.springframework.util.Assert.notNull(data, "Find entries implementation for 'Pet' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
        
        for (int i = 0; i < 10; i ++) {        
            Pet obj = getNewTransientEntity(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
