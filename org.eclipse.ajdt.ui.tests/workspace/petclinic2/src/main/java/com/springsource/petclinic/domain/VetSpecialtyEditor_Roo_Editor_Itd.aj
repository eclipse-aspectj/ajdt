package com.springsource.petclinic.domain;

privileged aspect VetSpecialtyEditor_Roo_Editor_Itd {
    
    declare parents: VetSpecialtyEditor implements java.beans.PropertyEditorSupport;    
    
    org.springframework.beans.SimpleTypeConverter VetSpecialtyEditor.typeConverter = new org.springframework.beans.SimpleTypeConverter();    
    
    public String VetSpecialtyEditor.getAsText() {    
        Object obj = getValue();        
        if (obj == null) {        
            return null;            
        }        
        return (String) typeConverter.convertIfNecessary(((com.springsource.petclinic.domain.VetSpecialty) obj).getId() , String.class);        
    }    
    
    public void VetSpecialtyEditor.setAsText(String text) {    
        if (text == null || "".equals(text)) {        
            setValue(null);            
            return;            
        }        
        
        Long identifier = (Long) typeConverter.convertIfNecessary(text, Long.class);        
        if (identifier == null) {        
            setValue(null);            
            return;            
        }        
        
        setValue(com.springsource.petclinic.domain.VetSpecialty.findVetSpecialty(identifier));        
    }    
    
}
