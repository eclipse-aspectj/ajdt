package com.springsource.petclinic.domain;

privileged aspect OwnerDataOnDemand_Roo_Dod_Itd {
    
    declare @type: OwnerDataOnDemand: @org.springframework.beans.factory.annotation.Configurable;    
    
    declare @type: OwnerDataOnDemand: @org.springframework.stereotype.Component;    
    
    private java.util.Random OwnerDataOnDemand.rnd = new java.security.SecureRandom();    
    
    private java.util.List<Owner> OwnerDataOnDemand.data;    
    
    public Owner OwnerDataOnDemand.getNewTransientEntity(int index) {    
        Owner obj = new Owner();        
        obj.setLastName("lastName_" + index);        
        obj.setAddress("address_" + index);        
        obj.setCity("city_" + index);        
        obj.setTelephone(new Integer(index));        
        obj.setBirthDay(new java.util.Date());        
        return obj;        
    }    
    
    public Owner OwnerDataOnDemand.getRandomPersistentEntity() {    
        init();        
        return data.get(rnd.nextInt(data.size()));        
    }    
    
    public Boolean OwnerDataOnDemand.modify(com.springsource.petclinic.domain.Owner obj) {    
        obj.setLastName(obj.getLastName() + "_modified");        
        return true;        
    }    
    
    @org.springframework.transaction.annotation.Transactional(propagation=org.springframework.transaction.annotation.Propagation.REQUIRES_NEW)    
    public void OwnerDataOnDemand.init() {    
        if (data != null) {        
            return;            
        }        
        
        data = Owner.findOwnerEntries(0, 10);        
        org.springframework.util.Assert.notNull(data, "Find entries implementation for 'Owner' illegally returned null");        
        if (data.size() > 0) {        
            return;            
        }        
        
        for (int i = 0; i < 10; i ++) {        
            Owner obj = getNewTransientEntity(i);            
            obj.persist();            
            data.add(obj);            
        }        
    }    
    
}
