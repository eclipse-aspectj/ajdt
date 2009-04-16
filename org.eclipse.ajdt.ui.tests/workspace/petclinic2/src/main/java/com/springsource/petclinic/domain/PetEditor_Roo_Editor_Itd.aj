package com.springsource.petclinic.domain;

privileged aspect PetEditor_Roo_Editor_Itd {
    
    declare parents: PetEditor implements java.beans.PropertyEditorSupport;    
    
    org.springframework.beans.SimpleTypeConverter PetEditor.typeConverter = new org.springframework.beans.SimpleTypeConverter();    
    
    public String PetEditor.getAsText() {    
        Object obj = getValue();        
        if (obj == null) {        
            return null;            
        }        
        return (String) typeConverter.convertIfNecessary(((com.springsource.petclinic.domain.Pet) obj).getId() , String.class);        
    }    
    
    public void PetEditor.setAsText(String text) {    
        if (text == null || "".equals(text)) {        
            setValue(null);            
            return;            
        }        
        
        Long identifier = (Long) typeConverter.convertIfNecessary(text, Long.class);        
        if (identifier == null) {        
            setValue(null);            
            return;            
        }        
        
        setValue(com.springsource.petclinic.domain.Pet.findPet(identifier));        
    }    
    
}
