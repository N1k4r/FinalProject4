package org.javarash.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "country")
@ToString
@Getter @Setter
public class Country {

    @Id
    @Column
    private int id;

    @Column
    private String code;

    @Column(name = "code_2")
    private String secondCode;

    @Column
    private String name;

    @Column
    @Enumerated(EnumType.ORDINAL)
    private Continent continent;

    @Column
    private String region;

    @Column(name = "surface_area")
    private BigDecimal surfaceArea;

    @Column(name = "indep_year")
    private Short indepYear;

    @Column
    private int population;

    @Column(name = "life_expectancy")
    private BigDecimal lifeExpectancy;

    @Column
    private BigDecimal gnp;

    @Column(name = "gnpo_id")
    private BigDecimal gnpoId;

    @Column(name = "local_name")
    private String localName;

    @Column(name = "government_form")
    private String governmentForm;

    @Column(name = "head_of_state")
    private String headOfState;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "capital")
    private City city;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id")
    private Set<CountryLanguage> countryLanguages;

}
