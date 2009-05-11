package com.springsource.petclinic.reference;

import org.springframework.roo.addon.entity.ref.RooEntity;
import org.springframework.roo.addon.jpa.ref.RooJpa;
import javax.persistence.Entity;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Length;

@RooEntity
@RooJpa
@Entity
public class Specialty {

    @NotNull
    @Length(min = 0, max = 30)
    private String name;
}
