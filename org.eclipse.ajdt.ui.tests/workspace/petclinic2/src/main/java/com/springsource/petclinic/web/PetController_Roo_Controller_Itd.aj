package com.springsource.petclinic.web;

privileged aspect PetController_Roo_Controller_Itd {
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pet", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetController.list(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("pets", com.springsource.petclinic.domain.Pet.findAllPets());        
        return "pet/list";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pet/{id}", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetController.show(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("pet", com.springsource.petclinic.domain.Pet.findPet(id));        
        return "pet/show";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pet/{id}", method = org.springframework.web.bind.annotation.RequestMethod.DELETE)    
    public java.lang.String PetController.delete(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        com.springsource.petclinic.domain.Pet.findPet(id).remove();        
        return "redirect:/pet";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pet/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetController.form(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("owners", com.springsource.petclinic.domain.Owner.findAllOwners());        
        modelMap.addAttribute("pettypes", com.springsource.petclinic.reference.PetType.findAllPetTypes());        
        modelMap.addAttribute("pet", new com.springsource.petclinic.domain.Pet());        
        return "pet/create";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pet", method = org.springframework.web.bind.annotation.RequestMethod.POST)    
    public java.lang.String PetController.create(@org.springframework.web.bind.annotation.ModelAttribute("pet") com.springsource.petclinic.domain.Pet pet) {    
        org.springframework.util.Assert.notNull(pet, "Pet must be provided.");        
        pet.persist();        
        return "redirect:/pet/"+pet.getId();        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "pet/{id}/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String PetController.updateForm(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("owners", com.springsource.petclinic.domain.Owner.findAllOwners());        
        modelMap.addAttribute("pettypes", com.springsource.petclinic.reference.PetType.findAllPetTypes());        
        modelMap.addAttribute("pet", com.springsource.petclinic.domain.Pet.findPet(id));        
        return "pet/update";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(method = org.springframework.web.bind.annotation.RequestMethod.PUT)    
    public java.lang.String PetController.update(@org.springframework.web.bind.annotation.ModelAttribute("pet") com.springsource.petclinic.domain.Pet pet) {    
        org.springframework.util.Assert.notNull(pet, "Pet must be provided.");        
        pet.merge();        
        return "redirect:/pet/" + pet.getId();        
    }    
    
}
