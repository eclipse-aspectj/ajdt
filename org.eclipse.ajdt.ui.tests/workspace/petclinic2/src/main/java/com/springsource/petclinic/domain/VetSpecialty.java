package com.springsource.petclinic.domain;

import org.springframework.roo.addon.entity.ref.RooEntity;
import org.springframework.roo.addon.jpa.ref.RooJpa;
import javax.persistence.Entity;
import java.util.Date;
import org.hibernate.validator.NotNull;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.validator.Past;
import com.springsource.petclinic.reference.Specialty;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@RooEntity
@RooJpa
@Entity
public class VetSpecialty {

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Past
    private Date registered;

    @ManyToOne
    @JoinColumn
    private Specialty specialty;

    @ManyToOne
    @JoinColumn
    private Vet vet;
}
