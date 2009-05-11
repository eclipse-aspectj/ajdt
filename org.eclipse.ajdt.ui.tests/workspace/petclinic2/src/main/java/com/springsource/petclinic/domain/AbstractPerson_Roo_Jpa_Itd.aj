package com.springsource.petclinic.domain;

privileged aspect AbstractPerson_Roo_Jpa_Itd {
    
    @javax.persistence.PersistenceContext    
    transient javax.persistence.EntityManager AbstractPerson.entityManager;    
    
    @org.springframework.transaction.annotation.Transactional    
    public void AbstractPerson.flush() {    
        this.entityManager.flush();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void AbstractPerson.merge() {    
        AbstractPerson merged = this.entityManager.merge(this);        
        this.entityManager.flush();        
        this.id = merged.getId();        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void AbstractPerson.persist() {    
        this.entityManager.persist(this);        
    }    
    
    @org.springframework.transaction.annotation.Transactional    
    public void AbstractPerson.remove() {    
        this.entityManager.remove(this);        
    }    
    
}
 