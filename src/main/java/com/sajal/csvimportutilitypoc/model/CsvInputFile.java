package com.sajal.csvimportutilitypoc.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter
@ToString
public class CsvInputFile {

    @CsvBindByName(column = "Company", required = true)
    private String companyName;

    @CsvBindByName(column = "Street1")
    private String street1;

    @CsvBindByName(column = "Street2")
    private String street2;

    @CsvBindByName(column = "City")
    private String city;

    @CsvBindByName(column = "State")
    private String state;

    @CsvBindByName(column = "Zip")
    private String zip;

    @CsvBindByName(column = "Email1", required = true)
    private String email1;

    @CsvBindByName(column = "Email2")
    private String email2;

    @CsvBindByName(column = "Email3")
    private String email3;

}
