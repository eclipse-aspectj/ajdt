package com.springsource.petclinic.domain;

privileged aspect Vet_Roo_Entity_Itd {
    
    declare @type: Vet: @org.springframework.beans.factory.annotation.Configurable;    
    
    public java.util.Date Vet.getEmployedSince() {    
        return employedSince;        
    }    
    
    public void Vet.setEmployedSince(java.util.Date employedSince) {    
        this.employedSince = employedSince;        
    }    
    
    public java.util.Set<com.springsource.petclinic.domain.VetSpecialty> Vet.getSpecialties() {    
        return specialties;        
    }    
    
    public void Vet.setSpecialties(java.util.Set<com.springsource.petclinic.domain.VetSpecialty> specialties) {    
        this.specialties = specialties;        
    }    
    
    public java.lang.String Vet.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("FirstName: " + getFirstName());        
        tsc.append("LastName: " + getLastName());        
        tsc.append("Address: " + getAddress());        
        tsc.append("City: " + getCity());        
        tsc.append("Telephone: " + getTelephone());        
        tsc.append("HomePage: " + getHomePage());        
        tsc.append("Email: " + getEmail());        
        tsc.append("BirthDay: " + getBirthDay());        
        tsc.append("EmployedSince: " + getEmployedSince());        
        tsc.append("Specialties: " + specialties.size());        
        return tsc.toString();        
    }    
    
}
