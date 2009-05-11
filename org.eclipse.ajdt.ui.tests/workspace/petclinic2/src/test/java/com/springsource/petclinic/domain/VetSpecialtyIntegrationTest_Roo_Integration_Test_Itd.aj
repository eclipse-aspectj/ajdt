package com.springsource.petclinic.domain;

privileged aspect VetSpecialtyIntegrationTest_Roo_Integration_Test_Itd {
    
    declare parents: VetSpecialtyIntegrationTest implements com.springsource.petclinic.AbstractIntegrationTest;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.domain.VetSpecialtyDataOnDemand VetSpecialtyIntegrationTest.vetSpecialtyDataOnDemand;    
    
    @org.junit.Test    
    public void VetSpecialtyIntegrationTest.count() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        long count = VetSpecialty.countVetSpecialtys();        
        junit.framework.Assert.assertTrue("Counter for 'VetSpecialty' incorrectly reported there were no entries", count > 0);        
    }    
    
    @org.junit.Test    
    public void VetSpecialtyIntegrationTest.find() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = vetSpecialtyDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to provide an identifier", id);        
        VetSpecialty obj = VetSpecialty.findVetSpecialty(id);        
        junit.framework.Assert.assertNotNull("Find method for 'VetSpecialty' illegally returned null for id '" + id + "'", obj);        
        junit.framework.Assert.assertEquals("Find method for 'VetSpecialty' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @org.junit.Test    
    public void VetSpecialtyIntegrationTest.findAll() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        long count = VetSpecialty.countVetSpecialtys();        
        if (count > 250) {        
            junit.framework.Assert.fail("Too expensive to perform a find all test for 'VetSpecialty', as there are " + count + " entries; use @RooIntegrationTest.findAllMaximum=" + count + " (or higher) on 'VetSpecialtyIntegrationTest' to force a find all test, or specify @RooIntegrationTest.findAll=false to disable");            
        }        
        java.util.List<VetSpecialty> result = VetSpecialty.findAllVetSpecialtys();        
        junit.framework.Assert.assertNotNull("Find all method for 'VetSpecialty' illegally returned null", result);        
        junit.framework.Assert.assertTrue("Find all method for 'VetSpecialty' failed to return any data", result.size() > 0);        
    }    
    
    @org.junit.Test    
    public void VetSpecialtyIntegrationTest.findEntries() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        long count = VetSpecialty.countVetSpecialtys();        
        if (count > 20) {        
            count = 20;            
        }        
        java.util.List<VetSpecialty> result = VetSpecialty.findVetSpecialtyEntries(0, (int) count);        
        junit.framework.Assert.assertNotNull("Find entries method for 'VetSpecialty' illegally returned null", result);        
        junit.framework.Assert.assertEquals("Find entries method for 'VetSpecialty' returned an incorrect number of entries", count, result.size());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialtyIntegrationTest.flush() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        VetSpecialty obj = VetSpecialty.findVetSpecialty(vetSpecialtyDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = vetSpecialtyDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'VetSpecialty' failed to increment on flush directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialtyIntegrationTest.merge() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        VetSpecialty obj = VetSpecialty.findVetSpecialty(vetSpecialtyDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = vetSpecialtyDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'VetSpecialty' failed to increment on merge directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialtyIntegrationTest.persist() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        VetSpecialty obj = vetSpecialtyDataOnDemand.getNewTransientEntity(Integer.MAX_VALUE);        
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to return a new transient entity", obj);        
        junit.framework.Assert.assertNull("Expected 'VetSpecialty' identifier to be null", obj.getId());        
        junit.framework.Assert.assertNull("Expected 'VetSpecialty' version to be null", obj.getVersion());        
        obj.persist();        
        junit.framework.Assert.assertNotNull("Expected newly-persisted 'VetSpecialty' identifier to be null", obj.getId());        
        junit.framework.Assert.assertEquals("Expected newly-persisted 'VetSpecialty' version to be 0", new Integer(0), obj.getVersion());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void VetSpecialtyIntegrationTest.remove() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to initialize correctly", vetSpecialtyDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = vetSpecialtyDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'VetSpecialty' failed to provide an identifier", id);        
        VetSpecialty.findVetSpecialty(id).remove();        
        junit.framework.Assert.assertNull("Failed to remove 'VetSpecialty' with identifier '" + id + "'", VetSpecialty.findVetSpecialty(id));        
    }    
    
}
