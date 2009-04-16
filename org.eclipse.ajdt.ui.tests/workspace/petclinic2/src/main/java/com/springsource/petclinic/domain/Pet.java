package com.springsource.petclinic.domain;

import org.springframework.roo.addon.entity.ref.RooEntity;
import org.springframework.roo.addon.jpa.ref.RooJpa;
import javax.persistence.Entity;
import org.hibernate.validator.Length;
import org.hibernate.validator.NotNull;
import org.hibernate.validator.Min;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import com.springsource.petclinic.reference.PetType;

@RooEntity
@Entity
@RooJpa(finders = { "findPetsByNameAndWeight", "findPetsByOwner" })
public class Pet {

    @Length(min = 0, max = 50)
    private String contactEmails;

    @NotNull
    private Boolean sendReminders;

    @NotNull
    private String name;

    @NotNull
    @Min(0)
    private Float weight;

    @ManyToOne
    @JoinColumn
    private Owner owner;

    @ManyToOne
    @JoinColumn
    private PetType type;
}
