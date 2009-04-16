package com.springsource.petclinic.domain;

privileged aspect OwnerIntegrationTest_Roo_Integration_Test_Itd {
    
    declare parents: OwnerIntegrationTest implements com.springsource.petclinic.AbstractIntegrationTest;    
    
    @org.springframework.beans.factory.annotation.Autowired    
    private com.springsource.petclinic.domain.OwnerDataOnDemand OwnerIntegrationTest.ownerDataOnDemand;    
    
    @org.junit.Test    
    public void OwnerIntegrationTest.count() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        long count = Owner.countOwners();        
        junit.framework.Assert.assertTrue("Counter for 'Owner' incorrectly reported there were no entries", count > 0);        
    }    
    
    @org.junit.Test    
    public void OwnerIntegrationTest.find() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = ownerDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to provide an identifier", id);        
        Owner obj = Owner.findOwner(id);        
        junit.framework.Assert.assertNotNull("Find method for 'Owner' illegally returned null for id '" + id + "'", obj);        
        junit.framework.Assert.assertEquals("Find method for 'Owner' returned the incorrect identifier", id, obj.getId());        
    }    
    
    @org.junit.Test    
    public void OwnerIntegrationTest.findAll() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        long count = Owner.countOwners();        
        if (count > 250) {        
            junit.framework.Assert.fail("Too expensive to perform a find all test for 'Owner', as there are " + count + " entries; use @RooIntegrationTest.findAllMaximum=" + count + " (or higher) on 'OwnerIntegrationTest' to force a find all test, or specify @RooIntegrationTest.findAll=false to disable");            
        }        
        java.util.List<Owner> result = Owner.findAllOwners();        
        junit.framework.Assert.assertNotNull("Find all method for 'Owner' illegally returned null", result);        
        junit.framework.Assert.assertTrue("Find all method for 'Owner' failed to return any data", result.size() > 0);        
    }    
    
    @org.junit.Test    
    public void OwnerIntegrationTest.findEntries() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        long count = Owner.countOwners();        
        if (count > 20) {        
            count = 20;            
        }        
        java.util.List<Owner> result = Owner.findOwnerEntries(0, (int) count);        
        junit.framework.Assert.assertNotNull("Find entries method for 'Owner' illegally returned null", result);        
        junit.framework.Assert.assertEquals("Find entries method for 'Owner' returned an incorrect number of entries", count, result.size());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void OwnerIntegrationTest.flush() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        Owner obj = Owner.findOwner(ownerDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = ownerDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Owner' failed to increment on flush directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void OwnerIntegrationTest.merge() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        Owner obj = Owner.findOwner(ownerDataOnDemand.getRandomPersistentEntity().getId());        
        boolean modified = ownerDataOnDemand.modify(obj);        
        int currentVersion = obj.getVersion();        
        obj.merge();        
        obj.flush();        
        if (modified) {        
            junit.framework.Assert.assertTrue("Version for 'Owner' failed to increment on merge directive", obj.getVersion() > currentVersion);            
        }        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void OwnerIntegrationTest.persist() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        Owner obj = ownerDataOnDemand.getNewTransientEntity(Integer.MAX_VALUE);        
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to return a new transient entity", obj);        
        junit.framework.Assert.assertNull("Expected 'Owner' identifier to be null", obj.getId());        
        junit.framework.Assert.assertNull("Expected 'Owner' version to be null", obj.getVersion());        
        obj.persist();        
        junit.framework.Assert.assertNotNull("Expected newly-persisted 'Owner' identifier to be null", obj.getId());        
        junit.framework.Assert.assertEquals("Expected newly-persisted 'Owner' version to be 0", new Integer(0), obj.getVersion());        
    }    
    
    @org.junit.Test    
    @org.springframework.transaction.annotation.Transactional    
    public void OwnerIntegrationTest.remove() {    
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to initialize correctly", ownerDataOnDemand.getRandomPersistentEntity());        
        java.lang.Long id = ownerDataOnDemand.getRandomPersistentEntity().getId();        
        junit.framework.Assert.assertNotNull("Data on demand for 'Owner' failed to provide an identifier", id);        
        Owner.findOwner(id).remove();        
        junit.framework.Assert.assertNull("Failed to remove 'Owner' with identifier '" + id + "'", Owner.findOwner(id));        
    }    
    
}
