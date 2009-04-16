package com.springsource.petclinic.domain;

privileged aspect VetSpecialty_Roo_Entity_Itd {
    
    declare @type: VetSpecialty: @org.springframework.beans.factory.annotation.Configurable;    
    
    @javax.persistence.Id    
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.AUTO)    
    @javax.persistence.Column(name="id")    
    private java.lang.Long VetSpecialty.id;    
    
    @javax.persistence.Version    
    @javax.persistence.Column(name="version")    
    private java.lang.Integer VetSpecialty.version;    
    
    public java.lang.Long VetSpecialty.getId() {    
        return id;        
    }    
    
    public void VetSpecialty.setId(java.lang.Long id) {    
        this.id = id;        
    }    
    
    public java.lang.Integer VetSpecialty.getVersion() {    
        return version;        
    }    
    
    public void VetSpecialty.setVersion(java.lang.Integer version) {    
        this.version = version;        
    }    
    
    public java.util.Date VetSpecialty.getRegistered() {    
        return registered;        
    }    
    
    public void VetSpecialty.setRegistered(java.util.Date registered) {    
        this.registered = registered;        
    }    
    
    public com.springsource.petclinic.reference.Specialty VetSpecialty.getSpecialty() {    
        return specialty;        
    }    
    
    public void VetSpecialty.setSpecialty(com.springsource.petclinic.reference.Specialty specialty) {    
        this.specialty = specialty;        
    }    
    
    public com.springsource.petclinic.domain.Vet VetSpecialty.getVet() {    
        return vet;        
    }    
    
    public void VetSpecialty.setVet(com.springsource.petclinic.domain.Vet vet) {    
        this.vet = vet;        
    }    
    
    public java.lang.String VetSpecialty.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("Registered: " + getRegistered());        
        tsc.append("Specialty: " + getSpecialty());        
        tsc.append("Vet: " + getVet());        
        return tsc.toString();        
    }    
    
}
