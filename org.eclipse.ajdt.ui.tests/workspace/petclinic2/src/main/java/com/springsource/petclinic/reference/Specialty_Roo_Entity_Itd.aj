package com.springsource.petclinic.reference;

privileged aspect Specialty_Roo_Entity_Itd {
    
    declare @type: Specialty: @org.springframework.beans.factory.annotation.Configurable;    
    
    @javax.persistence.Id    
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.AUTO)    
    @javax.persistence.Column(name="id")    
    private java.lang.Long Specialty.id;    
    
    @javax.persistence.Version    
    @javax.persistence.Column(name="version")    
    private java.lang.Integer Specialty.version;    
    
    public java.lang.Long Specialty.getId() {    
        return id;        
    }    
    
    public void Specialty.setId(java.lang.Long id) {    
        this.id = id;        
    }    
    
    public java.lang.Integer Specialty.getVersion() {    
        return version;        
    }    
    
    public void Specialty.setVersion(java.lang.Integer version) {    
        this.version = version;        
    }    
    
    public java.lang.String Specialty.getName() {    
        return name;        
    }    
    
    public void Specialty.setName(java.lang.String name) {    
        this.name = name;        
    }    
    
    public java.lang.String Specialty.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("Name: " + getName());        
        return tsc.toString();        
    }    
    
}
