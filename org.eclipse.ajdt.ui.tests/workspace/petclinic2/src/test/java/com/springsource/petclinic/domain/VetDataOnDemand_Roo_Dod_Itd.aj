package com.springsource.petclinic.domain;

privileged aspect VetDataOnDemand_Roo_Dod_Itd {
    
    declare @type: VetDataOnDemand: @org.springframework.beans.factory.annotation.Configurable;    
    
    declare @type: VetDataOnDemand: @org.springframework.stereotype.Component;    
    
    private java.util.Random VetDataOnDemand.rnd = new java.security.SecureRandom();    
    
    private java.util.List<Vet> VetDataOnDemand.data;    
    
    public Vet VetDataOnDemand.getNewTransientEntity(int index) {    
        Vet obj = new Vet();        
        obj.setEmployedSince(new java.util.Date(new java.util.Date().getTime() - 10000000L));        
        obj.setLastName("lastName_" + index);        
        obj.setAddress("address_" + index);        
        obj.setCity("city_" + index);        
        obj.setTelephone(new Integer(index));        
        obj.setBirthDay(new java.util.Date());        
        return obj;        
    }    
    
    public Vet VetDataOnDemand.getRandomPersistentEntity() {    
        init();        
        return data.get(rnd.nextInt(data.size()));        
    }    
    
    public Boolean VetDataOnDemand.modify(com.springsource.petclinic.domain.Vet obj) {    
        obj.setLastName(obj.getLastName() + "_modified");        
        return true;        
    }    
    
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)    
    public void VetDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
        
        data = Vet.findVetEntries(0, 10);        
        org.springframework.util.Assert.notNull(data, "Find entries implementation for 'Vet' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
        
        for (int i = 0; i < 10; i ++) {        
            Vet obj = getNewTransientEntity(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
