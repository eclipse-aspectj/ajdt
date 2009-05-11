package com.springsource.petclinic.domain;

privileged aspect OwnerEditor_Roo_Editor_Itd {
    
    declare parents: OwnerEditor implements java.beans.PropertyEditorSupport;    
    
    org.springframework.beans.SimpleTypeConverter OwnerEditor.typeConverter = new org.springframework.beans.SimpleTypeConverter();    
    
    public String OwnerEditor.getAsText() {    
        Object obj = getValue();        
        if (obj == null) {        
            return null;            
        }        
        return (String) typeConverter.convertIfNecessary(((com.springsource.petclinic.domain.Owner) obj).getId() , String.class);        
    }    
    
    public void OwnerEditor.setAsText(String text) {    
        if (text == null || "".equals(text)) {        
            setValue(null);            
            return;            
        }        
        
        Long identifier = (Long) typeConverter.convertIfNecessary(text, Long.class);        
        if (identifier == null) {        
            setValue(null);            
            return;            
        }        
        
        setValue(com.springsource.petclinic.domain.Owner.findOwner(identifier));        
    }    
    
}
