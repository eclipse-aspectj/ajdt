package com.springsource.petclinic.domain;

privileged aspect Owner_Roo_Entity_Itd {
    
    declare @type: Owner: @org.springframework.beans.factory.annotation.Configurable;    
    
    public java.util.Set<com.springsource.petclinic.domain.Pet> Owner.getPets() {    
        return pets;        
    }    
    
    public void Owner.setPets(java.util.Set<com.springsource.petclinic.domain.Pet> pets) {    
        this.pets = pets;        
    }    
    
    public java.lang.String Owner.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("FirstName: " + getFirstName());        
        tsc.append("LastName: " + getLastName());        
        tsc.append("Address: " + getAddress());        
        tsc.append("City: " + getCity());        
        tsc.append("Telephone: " + getTelephone());        
        tsc.append("HomePage: " + getHomePage());        
        tsc.append("Email: " + getEmail());        
        tsc.append("BirthDay: " + getBirthDay());        
        tsc.append("Pets: " + pets.size());        
        return tsc.toString();        
    }    
    
}
