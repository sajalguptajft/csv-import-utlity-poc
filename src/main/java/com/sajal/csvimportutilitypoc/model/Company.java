package com.sajal.csvimportutilitypoc.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import static javax.persistence.GenerationType.IDENTITY;
import static org.springframework.beans.BeanUtils.copyProperties;

@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Getter @Setter private int id;

    @Column(unique = true, nullable = false)
    @Getter @Setter private String name;

    @Getter @Setter private String street1;

    @Getter @Setter private String street2;

    @Getter @Setter private String city;

    @Getter @Setter private String state;

    @Getter @Setter private String zip;

    public Company(CsvInputFile csvRow) {
        copyProperties(csvRow, this);
        this.name = csvRow.getCompanyName();
    }

}
