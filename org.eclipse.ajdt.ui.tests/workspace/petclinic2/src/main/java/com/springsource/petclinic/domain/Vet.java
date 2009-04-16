package com.springsource.petclinic.domain;

import org.springframework.roo.addon.entity.ref.RooEntity;
import org.springframework.roo.addon.jpa.ref.RooJpa;
import javax.persistence.Entity;
import java.util.Date;
import org.hibernate.validator.NotNull;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.validator.Past;
import java.util.Set;
import java.util.HashSet;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;

@RooEntity
@RooJpa
@Entity
public class Vet extends AbstractPerson {

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Past
    private Date employedSince;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "vet")
    private Set<VetSpecialty> specialties = new HashSet<VetSpecialty>();
}
