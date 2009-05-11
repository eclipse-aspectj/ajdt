package com.springsource.petclinic.web;

import org.springframework.roo.addon.webmvc.ref.RooWebScaffold;
import com.springsource.petclinic.domain.Vet;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
*
* ROO generated Spring MVC Controller for Vet
*
*/
@RooWebScaffold(automaticallyMaintainView = true, entity = Vet.class)
@Controller
@RequestMapping("/vet/**")
public class VetController {
}
