package com.springsource.petclinic.domain;

privileged aspect Pet_Roo_Jpa_Itd {
    
    @javax.persistence.PersistenceContext    
    transient javax.persistence.EntityManager Pet.entityManager;    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Pet.flush() {    
        this.entityManager.flush();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Pet.merge() {    
        Pet merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Pet.persist() {    
        this.entityManager.persist(this);        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Pet.remove() {    
        this.entityManager.remove(this);        
    }    
    
    public static long Pet.countPets() {    
        return (Long) new Pet().entityManager.createQuery("select count(o) from Pet o").getSingleResult();        
    }    
    
    public static com.springsource.petclinic.domain.Pet Pet.findPet(java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "An identifier is required to retrieve an instance of Pet");        
        return new Pet().entityManager.find(Pet.class, id);        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Pet> Pet.findAllPets() {    
        return new Pet().entityManager.createQuery("select o from Pet o").getResultList();        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Pet> Pet.findPetEntries(int firstResult, int maxResults) {    
        return new Pet().entityManager.createQuery("select o from Pet o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
