package com.springsource.petclinic.domain;

privileged aspect Owner_Roo_Jpa_Itd {
    
    public static long Owner.countOwners() {    
        return (Long) new Owner().entityManager.createQuery("select count(o) from Owner o").getSingleResult();        
    }    
    
    public static com.springsource.petclinic.domain.Owner Owner.findOwner(java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "An identifier is required to retrieve an instance of Owner");        
        return new Owner().entityManager.find(Owner.class, id);        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Owner> Owner.findAllOwners() {    
        return new Owner().entityManager.createQuery("select o from Owner o").getResultList();        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Owner> Owner.findOwnerEntries(int firstResult, int maxResults) {    
        return new Owner().entityManager.createQuery("select o from Owner o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
