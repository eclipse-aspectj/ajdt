package com.springsource.petclinic.domain;

privileged aspect Pet_Roo_Entity_Itd {
    
    declare @type: Pet: @org.springframework.beans.factory.annotation.Configurable;    
    
    @javax.persistence.Id    
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.AUTO)    
    @javax.persistence.Column(name="id")    
    private java.lang.Long Pet.id;    
    
    @javax.persistence.Version    
    @javax.persistence.Column(name="version")    
    private java.lang.Integer Pet.version;    
    
    public java.lang.Long Pet.getId() {    
        return id;        
    }    
    
    public void Pet.setId(java.lang.Long id) {    
        this.id = id;        
    }    
    
    public java.lang.Integer Pet.getVersion() {    
        return version;        
    }    
    
    public void Pet.setVersion(java.lang.Integer version) {    
        this.version = version;        
    }    
    
    public java.lang.String Pet.getContactEmails() {    
        return contactEmails;        
    }    
    
    public void Pet.setContactEmails(java.lang.String contactEmails) {    
        this.contactEmails = contactEmails;        
    }    
    
    public java.lang.Boolean Pet.getSendReminders() {    
        return sendReminders;        
    }    
    
    public void Pet.setSendReminders(java.lang.Boolean sendReminders) {    
        this.sendReminders = sendReminders;        
    }    
    
    public java.lang.String Pet.getName() {    
        return name;        
    }    
    
    public void Pet.setName(java.lang.String name) {    
        this.name = name;        
    }    
    
    public java.lang.Float Pet.getWeight() {    
        return weight;        
    }    
    
    public void Pet.setWeight(java.lang.Float weight) {    
        this.weight = weight;        
    }    
    
    public com.springsource.petclinic.domain.Owner Pet.getOwner() {    
        return owner;        
    }    
    
    public void Pet.setOwner(com.springsource.petclinic.domain.Owner owner) {    
        this.owner = owner;        
    }    
    
    public com.springsource.petclinic.reference.PetType Pet.getType() {    
        return type;        
    }    
    
    public void Pet.setType(com.springsource.petclinic.reference.PetType type) {    
        this.type = type;        
    }    
    
    public java.lang.String Pet.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("ContactEmails: " + getContactEmails());        
        tsc.append("SendReminders: " + getSendReminders());        
        tsc.append("Name: " + getName());        
        tsc.append("Weight: " + getWeight());        
        tsc.append("Owner: " + getOwner());        
        tsc.append("Type: " + getType());        
        return tsc.toString();        
    }    
    
}
