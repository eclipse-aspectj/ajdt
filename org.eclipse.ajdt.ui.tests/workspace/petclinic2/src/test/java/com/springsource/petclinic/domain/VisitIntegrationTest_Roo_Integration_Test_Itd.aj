package com.springsource.petclinic.domain;

privileged aspect VisitIntegrationTest_Roo_Integration_Test_Itd {
    
    declare parents: VisitIntegrationTest implements com.springsource.petclinic.AbstractIntegrationTest;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.domain.VisitDataOnDemand VisitIntegrationTest.visitDataOnDemand;    
    
    @org.junit.Test    
    public void VisitIntegrationTest.count() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        long count = Visit.countVisits();        
        junit.framework.Assert.assertTrue("Counter for 'Visit' incorrectly reported there were no entries", count > 0);        
    }    
    
    @org.junit.Test    
    public void VisitIntegrationTest.find() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = visitDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to provide an identifier", id);        
        Visit obj = Visit.findVisit(id);        
        junit.framework.Assert.assertNotNull("Find method for 'Visit' illegally returned null for id '" + id + "'", obj);        
        junit.framework.Assert.assertEquals("Find method for 'Visit' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @org.junit.Test    
    public void VisitIntegrationTest.findAll() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        long count = Visit.countVisits();        
        if (count > 250) {        
            junit.framework.Assert.fail("Too expensive to perform a find all test for 'Visit', as there are " + count + " entries; use @RooIntegrationTest.findAllMaximum=" + count + " (or higher) on 'VisitIntegrationTest' to force a find all test, or specify @RooIntegrationTest.findAll=false to disable");            
        }        
        java.util.List<Visit> result = Visit.findAllVisits();        
        junit.framework.Assert.assertNotNull("Find all method for 'Visit' illegally returned null", result);        
        junit.framework.Assert.assertTrue("Find all method for 'Visit' failed to return any data", result.size() > 0);        
    }    
    
    @org.junit.Test    
    public void VisitIntegrationTest.findEntries() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        long count = Visit.countVisits();        
        if (count > 20) {        
            count = 20;            
        }        
        java.util.List<Visit> result = Visit.findVisitEntries(0, (int) count);        
        junit.framework.Assert.assertNotNull("Find entries method for 'Visit' illegally returned null", result);        
        junit.framework.Assert.assertEquals("Find entries method for 'Visit' returned an incorrect number of entries", count, result.size());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VisitIntegrationTest.flush() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        Visit obj = Visit.findVisit(visitDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = visitDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Visit' failed to increment on flush directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VisitIntegrationTest.merge() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        Visit obj = Visit.findVisit(visitDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = visitDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Visit' failed to increment on merge directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VisitIntegrationTest.persist() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        Visit obj = visitDataOnDemand.getNewTransientEntity(Integer.MAX_VALUE);        
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to return a new transient entity", obj);        
        junit.framework.Assert.assertNull("Expected 'Visit' identifier to be null", obj.getId());        
        junit.framework.Assert.assertNull("Expected 'Visit' version to be null", obj.getVersion());        
        obj.persist();        
        junit.framework.Assert.assertNotNull("Expected newly-persisted 'Visit' identifier to be null", obj.getId());        
        junit.framework.Assert.assertEquals("Expected newly-persisted 'Visit' version to be 0", new Integer(0), obj.getVersion());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VisitIntegrationTest.remove() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to initialize correctly", visitDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = visitDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Visit' failed to provide an identifier", id);        
        Visit.findVisit(id).remove();        
        junit.framework.Assert.assertNull("Failed to remove 'Visit' with identifier '" + id + "'", Visit.findVisit(id));        
    }    
    
}
