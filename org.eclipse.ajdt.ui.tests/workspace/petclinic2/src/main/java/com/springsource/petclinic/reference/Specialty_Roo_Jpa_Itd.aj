package com.springsource.petclinic.reference;

privileged aspect Specialty_Roo_Jpa_Itd {
    
    @javax.persistence.PersistenceContext    
    transient javax.persistence.EntityManager Specialty.entityManager;    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Specialty.flush() {    
        this.entityManager.flush();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Specialty.merge() {    
        Specialty merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Specialty.persist() {    
        this.entityManager.persist(this);        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Specialty.remove() {    
        this.entityManager.remove(this);        
    }    
    
    public static long Specialty.countSpecialtys() {    
        return (Long) new Specialty().entityManager.createQuery("select count(o) from Specialty o").getSingleResult();        
    }    
    
    public static com.springsource.petclinic.reference.Specialty Specialty.findSpecialty(java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "An identifier is required to retrieve an instance of Specialty");        
        return new Specialty().entityManager.find(Specialty.class, id);        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.reference.Specialty> Specialty.findAllSpecialtys() {    
        return new Specialty().entityManager.createQuery("select o from Specialty o").getResultList();        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.reference.Specialty> Specialty.findSpecialtyEntries(int firstResult, int maxResults) {    
        return new Specialty().entityManager.createQuery("select o from Specialty o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
