package com.springsource.petclinic.domain;

privileged aspect Visit_Roo_Entity_Itd {
    
    declare @type: Visit: @org.springframework.beans.factory.annotation.Configurable;    
    
    @javax.persistence.Id    
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.AUTO)    
    @javax.persistence.Column(name="id")    
    private java.lang.Long Visit.id;    
    
    @javax.persistence.Version    
    @javax.persistence.Column(name="version")    
    private java.lang.Integer Visit.version;    
    
    public java.lang.Long Visit.getId() {    
        return id;        
    }    
    
    public void Visit.setId(java.lang.Long id) {    
        this.id = id;        
    }    
    
    public java.lang.Integer Visit.getVersion() {    
        return version;        
    }    
    
    public void Visit.setVersion(java.lang.Integer version) {    
        this.version = version;        
    }    
    
    public java.lang.String Visit.getDescription() {    
        return description;        
    }    
    
    public void Visit.setDescription(java.lang.String description) {    
        this.description = description;        
    }    
    
    public java.util.Date Visit.getVisitDate() {    
        return visitDate;        
    }    
    
    public void Visit.setVisitDate(java.util.Date visitDate) {    
        this.visitDate = visitDate;        
    }    
    
    public com.springsource.petclinic.domain.Pet Visit.getPet() {    
        return pet;        
    }    
    
    public void Visit.setPet(com.springsource.petclinic.domain.Pet pet) {    
        this.pet = pet;        
    }    
    
    public com.springsource.petclinic.domain.Vet Visit.getVet() {    
        return vet;        
    }    
    
    public void Visit.setVet(com.springsource.petclinic.domain.Vet vet) {    
        this.vet = vet;        
    }    
    
    public java.lang.String Visit.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("Description: " + getDescription());        
        tsc.append("VisitDate: " + getVisitDate());        
        tsc.append("Pet: " + getPet());        
        tsc.append("Vet: " + getVet());        
        return tsc.toString();        
    }    
    
}
