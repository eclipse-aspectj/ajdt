package com.springsource.petclinic.reference;

privileged aspect PetType_Roo_Jpa_Itd {
    
    @javax.persistence.PersistenceContext    
    transient javax.persistence.EntityManager PetType.entityManager;    
    
    @org.springframework.transaction.annotation.Transactional    
    public void PetType.flush() {    
        this.entityManager.flush();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void PetType.merge() {    
        PetType merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void PetType.persist() {    
        this.entityManager.persist(this);        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void PetType.remove() {    
        this.entityManager.remove(this);        
    }    
    
    public static long PetType.countPetTypes() {    
        return (Long) new PetType().entityManager.createQuery("select count(o) from PetType o").getSingleResult();        
    }    
    
    public static com.springsource.petclinic.reference.PetType PetType.findPetType(java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "An identifier is required to retrieve an instance of PetType");        
        return new PetType().entityManager.find(PetType.class, id);        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.reference.PetType> PetType.findAllPetTypes() {    
        return new PetType().entityManager.createQuery("select o from PetType o").getResultList();        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.reference.PetType> PetType.findPetTypeEntries(int firstResult, int maxResults) {    
        return new PetType().entityManager.createQuery("select o from PetType o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
