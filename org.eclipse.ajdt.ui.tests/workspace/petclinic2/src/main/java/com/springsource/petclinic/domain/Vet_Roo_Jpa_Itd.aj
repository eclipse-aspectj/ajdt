package com.springsource.petclinic.domain;

privileged aspect Vet_Roo_Jpa_Itd {
    
    public static long Vet.countVets() {    
        return (Long) new Vet().entityManager.createQuery("select count(o) from Vet o").getSingleResult();        
    }    
    
    public static com.springsource.petclinic.domain.Vet Vet.findVet(java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "An identifier is required to retrieve an instance of Vet");        
        return new Vet().entityManager.find(Vet.class, id);        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Vet> Vet.findAllVets() {    
        return new Vet().entityManager.createQuery("select o from Vet o").getResultList();        
    }    
    
    @SuppressWarnings("unchecked")    
    public static java.util.List<com.springsource.petclinic.domain.Vet> Vet.findVetEntries(int firstResult, int maxResults) {    
        return new Vet().entityManager.createQuery("select o from Vet o").setFirstResult(firstResult).setMaxResults(maxResults).getResultList();        
    }    
    
}
