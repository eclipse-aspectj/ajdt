package com.springsource.petclinic.web;

privileged aspect OwnerController_Roo_Controller_Itd {
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "owner", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String OwnerController.list(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("owners", com.springsource.petclinic.domain.Owner.findAllOwners());        
        return "owner/list";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "owner/{id}", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String OwnerController.show(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("owner", com.springsource.petclinic.domain.Owner.findOwner(id));        
        return "owner/show";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "owner/{id}", method = org.springframework.web.bind.annotation.RequestMethod.DELETE)    
    public java.lang.String OwnerController.delete(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        com.springsource.petclinic.domain.Owner.findOwner(id).remove();        
        return "redirect:/owner";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "owner/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String OwnerController.form(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("pets", com.springsource.petclinic.domain.Pet.findAllPets());        
        modelMap.addAttribute("owner", new com.springsource.petclinic.domain.Owner());        
        return "owner/create";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "owner", method = org.springframework.web.bind.annotation.RequestMethod.POST)    
    public java.lang.String OwnerController.create(@org.springframework.web.bind.annotation.ModelAttribute("owner") com.springsource.petclinic.domain.Owner owner) {    
        org.springframework.util.Assert.notNull(owner, "Owner must be provided.");        
        owner.persist();        
        return "redirect:/owner/"+owner.getId();        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "owner/{id}/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String OwnerController.updateForm(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("pets", com.springsource.petclinic.domain.Pet.findAllPets());        
        modelMap.addAttribute("owner", com.springsource.petclinic.domain.Owner.findOwner(id));        
        return "owner/update";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(method = org.springframework.web.bind.annotation.RequestMethod.PUT)    
    public java.lang.String OwnerController.update(@org.springframework.web.bind.annotation.ModelAttribute("owner") com.springsource.petclinic.domain.Owner owner) {    
        org.springframework.util.Assert.notNull(owner, "Owner must be provided.");        
        owner.merge();        
        return "redirect:/owner/" + owner.getId();        
    }    
    
    @org.springframework.web.bind.annotation.InitBinder    
    void OwnerController.initBinder(org.springframework.web.bind.WebDataBinder binder) {    
        binder.registerCustomEditor(java.util.Date.class, new org.springframework.beans.propertyeditors.CustomDateEditor(new java.text.SimpleDateFormat("MM/dd/yyyy"), false));        
    }    
    
}
