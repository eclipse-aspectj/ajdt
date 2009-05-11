package com.springsource.petclinic.domain;

privileged aspect Pet_Roo_Finder_Itd {
    
    public static javax.persistence.Query Pet.findPetsByNameAndWeight(java.lang.String name, java.lang.Float weight) {    
        org.springframework.util.Assert.notNull(name, "A Name is required.");        
        org.springframework.util.Assert.notNull(weight, "A Weight is required.");        
        javax.persistence.Query q = new Pet().entityManager.createQuery("FROM Pet AS pet WHERE pet.name = :name AND pet.weight = :weight");        
        q.setParameter("name", name);        
        q.setParameter("weight", weight);        
        return q;        
    }    
    
    public static javax.persistence.Query Pet.findPetsByOwner(com.springsource.petclinic.domain.Owner owner) {    
        org.springframework.util.Assert.notNull(owner, "A Owner is required.");        
        javax.persistence.Query q = new Pet().entityManager.createQuery("FROM Pet AS pet WHERE pet.owner = :owner");        
        q.setParameter("owner", owner);        
        return q;        
    }    
    
}
