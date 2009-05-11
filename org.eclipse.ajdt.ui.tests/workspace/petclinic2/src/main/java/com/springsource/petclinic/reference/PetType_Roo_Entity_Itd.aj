package com.springsource.petclinic.reference;

privileged aspect PetType_Roo_Entity_Itd {
    
    declare @type: PetType: @org.springframework.beans.factory.annotation.Configurable;    
    
    @javax.persistence.Id    
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.AUTO)    
    @javax.persistence.Column(name="id")    
    private java.lang.Long PetType.id;    
    
    @javax.persistence.Version    
    @javax.persistence.Column(name="version")    
    private java.lang.Integer PetType.version;    
    
    public java.lang.Long PetType.getId() {    
        return id;        
    }    
    
    public void PetType.setId(java.lang.Long id) {    
        this.id = id;        
    }    
    
    public java.lang.Integer PetType.getVersion() {    
        return version;        
    }    
    
    public void PetType.setVersion(java.lang.Integer version) {    
        this.version = version;        
    }    
    
    public java.lang.String PetType.getType() {    
        return type;        
    }    
    
    public void PetType.setType(java.lang.String type) {    
        this.type = type;        
    }    
    
    public java.lang.String PetType.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("Type: " + getType());        
        return tsc.toString();        
    }    
    
}
