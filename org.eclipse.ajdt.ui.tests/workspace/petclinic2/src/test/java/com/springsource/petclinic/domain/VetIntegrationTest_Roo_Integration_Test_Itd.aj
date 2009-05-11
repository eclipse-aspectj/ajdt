package com.springsource.petclinic.domain;

privileged aspect VetIntegrationTest_Roo_Integration_Test_Itd {
    
    declare parents: VetIntegrationTest implements com.springsource.petclinic.AbstractIntegrationTest;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.domain.VetDataOnDemand VetIntegrationTest.vetDataOnDemand;    
    
    @org.junit.Test    
    public void VetIntegrationTest.count() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        long count = Vet.countVets();        
        junit.framework.Assert.assertTrue("Counter for 'Vet' incorrectly reported there were no entries", count > 0);        
    }    
    
    @org.junit.Test    
    public void VetIntegrationTest.find() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = vetDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to provide an identifier", id);        
        Vet obj = Vet.findVet(id);        
        junit.framework.Assert.assertNotNull("Find method for 'Vet' illegally returned null for id '" + id + "'", obj);        
        junit.framework.Assert.assertEquals("Find method for 'Vet' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @org.junit.Test    
    public void VetIntegrationTest.findAll() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        long count = Vet.countVets();        
        if (count > 250) {        
            junit.framework.Assert.fail("Too expensive to perform a find all test for 'Vet', as there are " + count + " entries; use @RooIntegrationTest.findAllMaximum=" + count + " (or higher) on 'VetIntegrationTest' to force a find all test, or specify @RooIntegrationTest.findAll=false to disable");            
        }        
        java.util.List<Vet> result = Vet.findAllVets();        
        junit.framework.Assert.assertNotNull("Find all method for 'Vet' illegally returned null", result);        
        junit.framework.Assert.assertTrue("Find all method for 'Vet' failed to return any data", result.size() > 0);        
    }    
    
    @org.junit.Test    
    public void VetIntegrationTest.findEntries() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        long count = Vet.countVets();        
        if (count > 20) {        
            count = 20;            
        }        
        java.util.List<Vet> result = Vet.findVetEntries(0, (int) count);        
        junit.framework.Assert.assertNotNull("Find entries method for 'Vet' illegally returned null", result);        
        junit.framework.Assert.assertEquals("Find entries method for 'Vet' returned an incorrect number of entries", count, result.size());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetIntegrationTest.flush() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        Vet obj = Vet.findVet(vetDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = vetDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Vet' failed to increment on flush directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetIntegrationTest.merge() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        Vet obj = Vet.findVet(vetDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = vetDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Vet' failed to increment on merge directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetIntegrationTest.persist() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        Vet obj = vetDataOnDemand.getNewTransientEntity(Integer.MAX_VALUE);        
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to return a new transient entity", obj);        
        junit.framework.Assert.assertNull("Expected 'Vet' identifier to be null", obj.getId());        
        junit.framework.Assert.assertNull("Expected 'Vet' version to be null", obj.getVersion());        
        obj.persist();        
        junit.framework.Assert.assertNotNull("Expected newly-persisted 'Vet' identifier to be null", obj.getId());        
        junit.framework.Assert.assertEquals("Expected newly-persisted 'Vet' version to be 0", new Integer(0), obj.getVersion());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetIntegrationTest.remove() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to initialize correctly", vetDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = vetDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Vet' failed to provide an identifier", id);        
        Vet.findVet(id).remove();        
        junit.framework.Assert.assertNull("Failed to remove 'Vet' with identifier '" + id + "'", Vet.findVet(id));        
    }    
    
}
