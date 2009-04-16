package com.springsource.petclinic.domain;

privileged aspect VetSpecialty_Roo_Jpa_Itd {
    
    @javax.persistence.PersistenceContext    
    transient javax.persistence.EntityManager VetSpecialty.entityManager;    
    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialty.flush() {    
        this.entityManager.flush();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialty.merge() {    
        VetSpecialty merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialty.persist() {    
        this.entityManager.persist(this);        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialty.remove() {    
        this.entityManager.remove(this);        
    }    
    
    public static long VetSpecialty.countVetSpecialtys() {    
        return (Long) new VetSpecialty().entityManager.createQuery("select count(o) from VetSpecialty o").getSingleResult();        
    }    
    
    public static com.springsource.petclinic.domain.VetSpecialty VetSpecialty.findVetSpecialty(java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "An identifier is required to retrieve an instance of VetSpecialty");        
        return new VetSpecialty().entityManager.find(VetSpecialty.class, id);        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.VetSpecialty> VetSpecialty.findAllVetSpecialtys() {    
        return new VetSpecialty().entityManager.createQuery("select o from VetSpecialty o").getResultList();        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.VetSpecialty> VetSpecialty.findVetSpecialtyEntries(int firstResult, int maxResults) {    
        return new VetSpecialty().entityManager.createQuery("select o from VetSpecialty o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
