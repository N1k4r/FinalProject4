package org.javarash.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;

@Entity
@Table(name = "country_language")
@ToString
@Getter @Setter
public class CountryLanguage {

    @Id
    @Column
    private int id;

    @Column
    private String language;

    @Column(name = "is_official", columnDefinition = "BIT")
    @Type(type = "org.hibernate.type.NumericBooleanType")
    private Boolean isOfficial;

    @Column
    private BigDecimal percentage;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;
}
