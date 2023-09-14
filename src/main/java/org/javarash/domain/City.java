package org.javarash.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "city")
@ToString
@Getter @Setter
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column
    private String name;

    @Column
    private String district;

    @Column
    private int population;

    @ManyToOne
    @JoinColumn(name = "country_id")
    private Country country;
}
