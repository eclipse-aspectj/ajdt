package com.springsource.petclinic.domain;

privileged aspect Visit_Roo_Finder_Itd {
    
    public static javax.persistence.Query Visit.findVisitsByDescriptionAndVisitDate(java.lang.String description, java.util.Date visitDate) {    
        org.springframework.util.Assert.notNull(description, "A Description is required.");        
        org.springframework.util.Assert.notNull(visitDate, "A VisitDate is required.");        
        javax.persistence.Query q = new Visit().entityManager.createQuery("FROM Visit AS visit WHERE visit.description = :description AND visit.visitDate = :visitDate");        
        q.setParameter("description", description);        
        q.setParameter("visitDate", visitDate);        
        return q;        
    }    
    
    public static javax.persistence.Query Visit.findVisitsByVisitDateBetween(java.util.Date minVisitDate, java.util.Date maxVisitDate) {    
        org.springframework.util.Assert.notNull(minVisitDate, "A minimum VisitDate is required.");        
        org.springframework.util.Assert.notNull(maxVisitDate, "A maximum VisitDate is required.");        
        javax.persistence.Query q = new Visit().entityManager.createQuery("FROM Visit AS visit WHERE visit.visitDate BETWEEN :minVisitDate AND :maxVisitDate ");        
        q.setParameter("minVisitDate", minVisitDate);        
        q.setParameter("maxVisitDate", maxVisitDate);        
        return q;        
    }    
    
    public static javax.persistence.Query Visit.findVisitsByDescriptionLike(java.lang.String description) {    
        org.springframework.util.Assert.notNull(description, "A Description is required.");        
        javax.persistence.Query q = new Visit().entityManager.createQuery("FROM Visit AS visit WHERE visit.description LIKE :description ");        
        q.setParameter("description", "%"+description+"%");        
        return q;        
    }    
    
}
