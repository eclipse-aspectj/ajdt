package com.springsource.petclinic.web;

privileged aspect PetTypeController_Roo_Controller_Itd {
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pettype", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetTypeController.list(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("pettypes", com.springsource.petclinic.reference.PetType.findAllPetTypes());        
        return "pettype/list";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pettype/{id}", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetTypeController.show(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("pettype", com.springsource.petclinic.reference.PetType.findPetType(id));        
        return "pettype/show";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pettype/{id}", method = org.springframework.web.bind.annotation.RequestMethod.DELETE)    
    public java.lang.String PetTypeController.delete(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        com.springsource.petclinic.reference.PetType.findPetType(id).remove();        
        return "redirect:/pettype";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pettype/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetTypeController.form(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("pettype", new com.springsource.petclinic.reference.PetType());        
        return "pettype/create";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pettype", method = org.springframework.web.bind.annotation.RequestMethod.POST)    
    public java.lang.String PetTypeController.create(@org.springframework.web.bind.annotation.ModelAttribute("pettype") com.springsource.petclinic.reference.PetType pettype) {    
        org.springframework.util.Assert.notNull(pettype, "PetType must be provided.");        
        pettype.persist();        
        return "redirect:/pettype/"+pettype.getId();        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pettype/{id}/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetTypeController.updateForm(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("pettype", com.springsource.petclinic.reference.PetType.findPetType(id));        
        return "pettype/update";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(method = org.springframework.web.bind.annotation.RequestMethod.PUT)    
    public java.lang.String PetTypeController.update(@org.springframework.web.bind.annotation.ModelAttribute("pettype") com.springsource.petclinic.reference.PetType pettype) {    
        org.springframework.util.Assert.notNull(pettype, "PetType must be provided.");        
        pettype.merge();        
        return "redirect:/pettype/" + pettype.getId();        
    }    
    
}
