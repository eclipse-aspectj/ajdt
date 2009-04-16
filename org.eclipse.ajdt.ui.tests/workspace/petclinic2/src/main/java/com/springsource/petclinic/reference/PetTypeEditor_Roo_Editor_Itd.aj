package com.springsource.petclinic.reference;

privileged aspect PetTypeEditor_Roo_Editor_Itd {
    
    declare parents: PetTypeEditor implements java.beans.PropertyEditorSupport;    
    
    org.springframework.beans.SimpleTypeConverter PetTypeEditor.typeConverter = new org.springframework.beans.SimpleTypeConverter();    
    
    public String PetTypeEditor.getAsText() {    
        Object obj = getValue();        
        if (obj == null) {        
            return null;            
        }        
        return (String) typeConverter.convertIfNecessary(((com.springsource.petclinic.reference.PetType) obj).getId() , String.class);        
    }    
    
    public void PetTypeEditor.setAsText(String text) {    
        if (text == null || "".equals(text)) {        
            setValue(null);            
            return;            
        }        
        
        Long identifier = (Long) typeConverter.convertIfNecessary(text, Long.class);        
        if (identifier == null) {        
            setValue(null);            
            return;            
        }        
        
        setValue(com.springsource.petclinic.reference.PetType.findPetType(identifier));        
    }    
    
}
