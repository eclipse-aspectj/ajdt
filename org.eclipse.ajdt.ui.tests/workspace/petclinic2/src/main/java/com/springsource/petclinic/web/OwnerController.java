package com.springsource.petclinic.web;

import org.springframework.roo.addon.webmvc.ref.RooWebScaffold;
import com.springsource.petclinic.domain.Owner;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
*
* ROO generated Spring MVC Controller for Owner
*
*/
@RooWebScaffold(automaticallyMaintainView = true, entity = Owner.class)
@Controller
@RequestMapping("/owner/**")
public class OwnerController {
    
    void test() {
        new Owner().toString();
    }
}
