package com.springsource.petclinic.reference;

privileged aspect SpecialtyIntegrationTest_Roo_Integration_Test_Itd {
    
    declare parents: SpecialtyIntegrationTest implements com.springsource.petclinic.AbstractIntegrationTest;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.reference.SpecialtyDataOnDemand SpecialtyIntegrationTest.specialtyDataOnDemand;    
    
    @org.junit.Test    
    public void SpecialtyIntegrationTest.count() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        long count = Specialty.countSpecialtys();        
        junit.framework.Assert.assertTrue("Counter for 'Specialty' incorrectly reported there were no entries", count > 0);        
    }    
    
    @org.junit.Test    
    public void SpecialtyIntegrationTest.find() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = specialtyDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to provide an identifier", id);        
        Specialty obj = Specialty.findSpecialty(id);        
        junit.framework.Assert.assertNotNull("Find method for 'Specialty' illegally returned null for id '" + id + "'", obj);        
        junit.framework.Assert.assertEquals("Find method for 'Specialty' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @org.junit.Test    
    public void SpecialtyIntegrationTest.findAll() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        long count = Specialty.countSpecialtys();        
        if (count > 250) {        
            junit.framework.Assert.fail("Too expensive to perform a find all test for 'Specialty', as there are " + count + " entries; use @RooIntegrationTest.findAllMaximum=" + count + " (or higher) on 'SpecialtyIntegrationTest' to force a find all test, or specify @RooIntegrationTest.findAll=false to disable");            
        }        
        java.util.List<Specialty> result = Specialty.findAllSpecialtys();        
        junit.framework.Assert.assertNotNull("Find all method for 'Specialty' illegally returned null", result);        
        junit.framework.Assert.assertTrue("Find all method for 'Specialty' failed to return any data", result.size() > 0);        
    }    
    
    @org.junit.Test    
    public void SpecialtyIntegrationTest.findEntries() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        long count = Specialty.countSpecialtys();        
        if (count > 20) {        
            count = 20;            
        }        
        java.util.List<Specialty> result = Specialty.findSpecialtyEntries(0, (int) count);        
        junit.framework.Assert.assertNotNull("Find entries method for 'Specialty' illegally returned null", result);        
        junit.framework.Assert.assertEquals("Find entries method for 'Specialty' returned an incorrect number of entries", count, result.size());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void SpecialtyIntegrationTest.flush() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        Specialty obj = Specialty.findSpecialty(specialtyDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = specialtyDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Specialty' failed to increment on flush directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void SpecialtyIntegrationTest.merge() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        Specialty obj = Specialty.findSpecialty(specialtyDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = specialtyDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Specialty' failed to increment on merge directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void SpecialtyIntegrationTest.persist() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        Specialty obj = specialtyDataOnDemand.getNewTransientEntity(Integer.MAX_VALUE);        
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to return a new transient entity", obj);        
        junit.framework.Assert.assertNull("Expected 'Specialty' identifier to be null", obj.getId());        
        junit.framework.Assert.assertNull("Expected 'Specialty' version to be null", obj.getVersion());        
        obj.persist();        
        junit.framework.Assert.assertNotNull("Expected newly-persisted 'Specialty' identifier to be null", obj.getId());        
        junit.framework.Assert.assertEquals("Expected newly-persisted 'Specialty' version to be 0", new Integer(0), obj.getVersion());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void SpecialtyIntegrationTest.remove() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to initialize correctly", specialtyDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = specialtyDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Specialty' failed to provide an identifier", id);        
        Specialty.findSpecialty(id).remove();        
        junit.framework.Assert.assertNull("Failed to remove 'Specialty' with identifier '" + id + "'", Specialty.findSpecialty(id));        
    }    
    
}
