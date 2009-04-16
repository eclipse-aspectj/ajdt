package com.springsource.petclinic.reference;

privileged aspect PetTypeIntegrationTest_Roo_Integration_Test_Itd {
    
    declare parents: PetTypeIntegrationTest implements com.springsource.petclinic.AbstractIntegrationTest;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.reference.PetTypeDataOnDemand PetTypeIntegrationTest.petTypeDataOnDemand;    
    
    @org.junit.Test    
    public void PetTypeIntegrationTest.count() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        long count = PetType.countPetTypes();        
        junit.framework.Assert.assertTrue("Counter for 'PetType' incorrectly reported there were no entries", count > 0);        
    }    
    
    @org.junit.Test    
    public void PetTypeIntegrationTest.find() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = petTypeDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to provide an identifier", id);        
        PetType obj = PetType.findPetType(id);        
        junit.framework.Assert.assertNotNull("Find method for 'PetType' illegally returned null for id '" + id + "'", obj);        
        junit.framework.Assert.assertEquals("Find method for 'PetType' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @org.junit.Test    
    public void PetTypeIntegrationTest.findAll() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        long count = PetType.countPetTypes();        
        if (count > 250) {        
            junit.framework.Assert.fail("Too expensive to perform a find all test for 'PetType', as there are " + count + " entries; use @RooIntegrationTest.findAllMaximum=" + count + " (or higher) on 'PetTypeIntegrationTest' to force a find all test, or specify @RooIntegrationTest.findAll=false to disable");            
        }        
        java.util.List<PetType> result = PetType.findAllPetTypes();        
        junit.framework.Assert.assertNotNull("Find all method for 'PetType' illegally returned null", result);        
        junit.framework.Assert.assertTrue("Find all method for 'PetType' failed to return any data", result.size() > 0);        
    }    
    
    @org.junit.Test    
    public void PetTypeIntegrationTest.findEntries() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        long count = PetType.countPetTypes();        
        if (count > 20) {        
            count = 20;            
        }        
        java.util.List<PetType> result = PetType.findPetTypeEntries(0, (int) count);        
        junit.framework.Assert.assertNotNull("Find entries method for 'PetType' illegally returned null", result);        
        junit.framework.Assert.assertEquals("Find entries method for 'PetType' returned an incorrect number of entries", count, result.size());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetTypeIntegrationTest.flush() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        PetType obj = PetType.findPetType(petTypeDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = petTypeDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'PetType' failed to increment on flush directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetTypeIntegrationTest.merge() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        PetType obj = PetType.findPetType(petTypeDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = petTypeDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'PetType' failed to increment on merge directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetTypeIntegrationTest.persist() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        PetType obj = petTypeDataOnDemand.getNewTransientEntity(Integer.MAX_VALUE);        
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to return a new transient entity", obj);        
        junit.framework.Assert.assertNull("Expected 'PetType' identifier to be null", obj.getId());        
        junit.framework.Assert.assertNull("Expected 'PetType' version to be null", obj.getVersion());        
        obj.persist();        
        junit.framework.Assert.assertNotNull("Expected newly-persisted 'PetType' identifier to be null", obj.getId());        
        junit.framework.Assert.assertEquals("Expected newly-persisted 'PetType' version to be 0", new Integer(0), obj.getVersion());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetTypeIntegrationTest.remove() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to initialize correctly", petTypeDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = petTypeDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'PetType' failed to provide an identifier", id);        
        PetType.findPetType(id).remove();        
        junit.framework.Assert.assertNull("Failed to remove 'PetType' with identifier '" + id + "'", PetType.findPetType(id));        
    }    
    
}
