package com.springsource.petclinic.domain;

import org.springframework.roo.addon.entity.ref.RooEntity;
import org.springframework.roo.addon.jpa.ref.RooJpa;
import javax.persistence.Entity;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

@RooEntity
@RooJpa
@Entity
public class Owner extends AbstractPerson {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private Set<Pet> pets = new HashSet<Pet>();
    
    public void test() throws Exception {
        this.getAddress();
	}
}
