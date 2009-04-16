package com.springsource.petclinic.web;

import org.springframework.roo.addon.webmvc.ref.RooWebScaffold;
import com.springsource.petclinic.reference.PetType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
*
* ROO generated Spring MVC Controller for PetType
*
*/
@RooWebScaffold(automaticallyMaintainView = true, entity = PetType.class)
@Controller
@RequestMapping("/pettype/**")
public class PetTypeController {
}
