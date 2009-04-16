package com.springsource.petclinic.domain;

privileged aspect AbstractPerson_Roo_Entity_Itd {
    
    declare @type: AbstractPerson: @org.springframework.beans.factory.annotation.Configurable;    
    
    @javax.persistence.Id     
    @javax.persistence.GeneratedValue(strategy=javax.persistence.GenerationType.AUTO)    
    @javax.persistence.Column(name="id")    
    private java.lang.Long AbstractPerson.id;    
    
    @javax.persistence.Version    
    @javax.persistence.Column(name="version")    
    private java.lang.Integer AbstractPerson.version;    
    
    public java.lang.Long AbstractPerson.getId() {    
        return id;        
    }    
    
    public void AbstractPerson.setId(java.lang.Long id) {    
        this.id = id;        
    }    
    
    public java.lang.Integer AbstractPerson.getVersion() {    
        return version;        
    }    
    
    public void AbstractPerson.setVersion(java.lang.Integer version) {    
        this.version = version;        
    }    
    
    public java.lang.String AbstractPerson.getFirstName() {    
        return firstName;        
    }    
    
    public void AbstractPerson.setFirstName(java.lang.String firstName) {    
        this.firstName = firstName;        
    }    
    
    public java.lang.String AbstractPerson.getLastName() {    
        return lastName;        
    }    
    
    public void AbstractPerson.setLastName(java.lang.String lastName) {    
        this.lastName = lastName;        
    }    
    
    public java.lang.String AbstractPerson.getAddress() {    
        return address;        
    }    
    
    public void AbstractPerson.setAddress(java.lang.String address) {    
        this.address = address;        
    }    
    
    public java.lang.String AbstractPerson.getCity() {    
        return city;        
    }    
    
    public void AbstractPerson.setCity(java.lang.String city) {    
        this.city = city;        
    }    
    
    public java.lang.Integer AbstractPerson.getTelephone() {    
        return telephone;        
    }    
    
    public void AbstractPerson.setTelephone(java.lang.Integer telephone) {    
        this.telephone = telephone;        
    }    
    
    public java.lang.String AbstractPerson.getHomePage() {    
        return homePage;        
    }    
    
    public void AbstractPerson.setHomePage(java.lang.String homePage) {    
        this.homePage = homePage;        
    }    
    
    public java.lang.String AbstractPerson.getEmail() {    
        return email;        
    }    
    
    public void AbstractPerson.setEmail(java.lang.String email) {    
        this.email = email;        
    }    
    
    public java.util.Date AbstractPerson.getBirthDay() {    
        return birthDay;        
    }    
    
    public void AbstractPerson.setBirthDay(java.util.Date birthDay) {    
        this.birthDay = birthDay;        
    }    
    
    public java.lang.String AbstractPerson.toString() {    
        org.springframework.core.style.ToStringCreator tsc = new org.springframework.core.style.ToStringCreator(this);        
        tsc.append("FirstName: " + getFirstName());        
        tsc.append("LastName: " + getLastName());        
        tsc.append("Address: " + getAddress());        
        tsc.append("City: " + getCity());        
        tsc.append("Telephone: " + getTelephone());        
        tsc.append("HomePage: " + getHomePage());        
        tsc.append("Email: " + getEmail());        
        tsc.append("BirthDay: " + getBirthDay());        
        return tsc.toString();        
    }    
    
}
