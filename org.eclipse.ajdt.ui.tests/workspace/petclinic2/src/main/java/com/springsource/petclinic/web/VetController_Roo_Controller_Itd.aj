package com.springsource.petclinic.web;

privileged aspect VetController_Roo_Controller_Itd {
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "vet", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String VetController.list(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("vets", com.springsource.petclinic.domain.Vet.findAllVets());        
        return "vet/list";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "vet/{id}", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String VetController.show(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("vet", com.springsource.petclinic.domain.Vet.findVet(id));        
        return "vet/show";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "vet/{id}", method = org.springframework.web.bind.annotation.RequestMethod.DELETE)    
    public java.lang.String VetController.delete(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        com.springsource.petclinic.domain.Vet.findVet(id).remove();        
        return "redirect:/vet";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "vet/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String VetController.form(org.springframework.ui.ModelMap modelMap) {    
        modelMap.addAttribute("vetspecialtys", com.springsource.petclinic.domain.VetSpecialty.findAllVetSpecialtys());        
        modelMap.addAttribute("vet", new com.springsource.petclinic.domain.Vet());        
        return "vet/create";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "vet", method = org.springframework.web.bind.annotation.RequestMethod.POST)    
    public java.lang.String VetController.create(@org.springframework.web.bind.annotation.ModelAttribute("vet") com.springsource.petclinic.domain.Vet vet) {    
        org.springframework.util.Assert.notNull(vet, "Vet must be provided.");        
        vet.persist();        
        return "redirect:/vet/"+vet.getId();        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(value = "vet/{id}/form", method = org.springframework.web.bind.annotation.RequestMethod.GET)    
    public java.lang.String VetController.updateForm(@org.springframework.web.bind.annotation.PathVariable("id") java.lang.Long id, org.springframework.ui.ModelMap modelMap) {    
        org.springframework.util.Assert.notNull(id, "Identifier must be provided.");        
        modelMap.addAttribute("vetspecialtys", com.springsource.petclinic.domain.VetSpecialty.findAllVetSpecialtys());        
        modelMap.addAttribute("vet", com.springsource.petclinic.domain.Vet.findVet(id));        
        return "vet/update";        
    }    
    
    @org.springframework.web.bind.annotation.RequestMapping(method = org.springframework.web.bind.annotation.RequestMethod.PUT)    
    public java.lang.String VetController.update(@org.springframework.web.bind.annotation.ModelAttribute("vet") com.springsource.petclinic.domain.Vet vet) {    
        org.springframework.util.Assert.notNull(vet, "Vet must be provided.");        
        vet.merge();        
        return "redirect:/vet/" + vet.getId();        
    }    
    
    @org.springframework.web.bind.annotation.InitBinder    
    void VetController.initBinder(org.springframework.web.bind.WebDataBinder binder) {    
        binder.registerCustomEditor(java.util.Date.class, new org.springframework.beans.propertyeditors.CustomDateEditor(new java.text.SimpleDateFormat("MM/dd/yyyy"), false));        
        binder.registerCustomEditor(java.util.Date.class, new org.springframework.beans.propertyeditors.CustomDateEditor(new java.text.SimpleDateFormat("MM/dd/yyyy"), false));        
    }    
    
}
