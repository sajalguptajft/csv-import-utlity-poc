package com.sajal.csvimportutilitypoc.dao;

import com.sajal.csvimportutilitypoc.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyDAO extends JpaRepository<Company, Integer> {

    Optional<Company> findByName(String name);

}
