package com.springsource.petclinic.domain;

import org.springframework.roo.addon.entity.ref.RooEntity;
import org.springframework.roo.addon.jpa.ref.RooJpa;
import javax.persistence.Entity;
import org.hibernate.validator.Length;
import java.util.Date;
import org.hibernate.validator.NotNull;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.validator.Past;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

@RooEntity
@Entity
@RooJpa(finders = { "findVisitsByDescriptionAndVisitDate", "findVisitsByVisitDateBetween", "findVisitsByDescriptionLike" })
public class Visit {

    @Length(min = 0, max = 255)
    private String description;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Past
    private Date visitDate;

    @NotNull
    @ManyToOne
    @JoinColumn
    private Pet pet;

    @ManyToOne
    @JoinColumn
    private Vet vet;
}
