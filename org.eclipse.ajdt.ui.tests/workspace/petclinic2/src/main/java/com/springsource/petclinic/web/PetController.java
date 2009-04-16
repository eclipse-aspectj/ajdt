package com.springsource.petclinic.web;

import org.springframework.roo.addon.webmvc.ref.RooWebScaffold;
import com.springsource.petclinic.domain.Pet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
*
* ROO generated Spring MVC Controller for Pet
*
*/
@RooWebScaffold(automaticallyMaintainView = true, entity = Pet.class)
@Controller
@RequestMapping("/pet/**")
public class PetController {
}
