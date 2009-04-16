package com.springsource.petclinic.domain;

privileged aspect Visit_Roo_Jpa_Itd {
    
    @javax.persistence.PersistenceContext    
    transient javax.persistence.EntityManager Visit.entityManager;    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Visit.flush() {    
        this.entityManager.flush();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Visit.merge() {    
        Visit merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Visit.persist() {    
        this.entityManager.persist(this);        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void Visit.remove() {    
        this.entityManager.remove(this);        
    }    
    
    public static long Visit.countVisits() {    
        return (Long) new Visit().entityManager.createQuery("select count(o) from Visit o").getSingleResult();        
    }    
    
    public static com.springsource.petclinic.domain.Visit Visit.findVisit(java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "An identifier is required to retrieve an instance of Visit");        
        return new Visit().entityManager.find(Visit.class, id);        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Visit> Visit.findAllVisits() {    
        return new Visit().entityManager.createQuery("select o from Visit o").getResultList();        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Visit> Visit.findVisitEntries(int firstResult, int maxResults) {    
        return new Visit().entityManager.createQuery("select o from Visit o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
