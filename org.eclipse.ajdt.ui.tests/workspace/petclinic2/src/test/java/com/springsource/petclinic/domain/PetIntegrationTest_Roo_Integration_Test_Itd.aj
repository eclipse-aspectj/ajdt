package com.springsource.petclinic.domain;

privileged aspect PetIntegrationTest_Roo_Integration_Test_Itd {
    
    declare parents: PetIntegrationTest implements com.springsource.petclinic.AbstractIntegrationTest;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.domain.PetDataOnDemand PetIntegrationTest.petDataOnDemand;    
    
    @org.junit.Test    
    public void PetIntegrationTest.count() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        long count = Pet.countPets();        
        junit.framework.Assert.assertTrue("Counter for 'Pet' incorrectly reported there were no entries", count > 0);        
    }    
    
    @org.junit.Test    
    public void PetIntegrationTest.find() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = petDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to provide an identifier", id);        
        Pet obj = Pet.findPet(id);        
        junit.framework.Assert.assertNotNull("Find method for 'Pet' illegally returned null for id '" + id + "'", obj);        
        junit.framework.Assert.assertEquals("Find method for 'Pet' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @org.junit.Test    
    public void PetIntegrationTest.findAll() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        long count = Pet.countPets();        
        if (count > 250) {        
            junit.framework.Assert.fail("Too expensive to perform a find all test for 'Pet', as there are " + count + " entries; use @RooIntegrationTest.findAllMaximum=" + count + " (or higher) on 'PetIntegrationTest' to force a find all test, or specify @RooIntegrationTest.findAll=false to disable");            
        }        
        java.util.List<Pet> result = Pet.findAllPets();        
        junit.framework.Assert.assertNotNull("Find all method for 'Pet' illegally returned null", result);        
        junit.framework.Assert.assertTrue("Find all method for 'Pet' failed to return any data", result.size() > 0);        
    }    
    
    @org.junit.Test    
    public void PetIntegrationTest.findEntries() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        long count = Pet.countPets();        
        if (count > 20) {        
            count = 20;            
        }        
        java.util.List<Pet> result = Pet.findPetEntries(0, (int) count);        
        junit.framework.Assert.assertNotNull("Find entries method for 'Pet' illegally returned null", result);        
        junit.framework.Assert.assertEquals("Find entries method for 'Pet' returned an incorrect number of entries", count, result.size());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetIntegrationTest.flush() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        Pet obj = Pet.findPet(petDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = petDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Pet' failed to increment on flush directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetIntegrationTest.merge() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        Pet obj = Pet.findPet(petDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = petDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Pet' failed to increment on merge directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetIntegrationTest.persist() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        Pet obj = petDataOnDemand.getNewTransientEntity(Integer.MAX_VALUE);        
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to return a new transient entity", obj);        
        junit.framework.Assert.assertNull("Expected 'Pet' identifier to be null", obj.getId());        
        junit.framework.Assert.assertNull("Expected 'Pet' version to be null", obj.getVersion());        
        obj.persist();        
        junit.framework.Assert.assertNotNull("Expected newly-persisted 'Pet' identifier to be null", obj.getId());        
        junit.framework.Assert.assertEquals("Expected newly-persisted 'Pet' version to be 0", new Integer(0), obj.getVersion());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void PetIntegrationTest.remove() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to initialize correctly", petDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = petDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Pet' failed to provide an identifier", id);        
        Pet.findPet(id).remove();        
        junit.framework.Assert.assertNull("Failed to remove 'Pet' with identifier '" + id + "'", Pet.findPet(id));        
    }    
    
}
