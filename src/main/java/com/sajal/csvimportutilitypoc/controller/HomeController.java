package com.sajal.csvimportutilitypoc.controller;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.sajal.csvimportutilitypoc.dao.CompanyDAO;
import com.sajal.csvimportutilitypoc.dao.UserDAO;
import com.sajal.csvimportutilitypoc.model.Company;
import com.sajal.csvimportutilitypoc.model.CsvInputFile;
import com.sajal.csvimportutilitypoc.model.User;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;
import java.util.regex.Pattern;

import static java.time.Instant.now;
import static java.util.Collections.singletonList;
import static java.util.Collections.synchronizedList;
import static org.apache.commons.collections.SetUtils.synchronizedSet;

@RestController
@RequestMapping("/")
public class HomeController {

    private static final String EMAIL_REGEX = "^(.+)@(.+)$";

    @Autowired private CompanyDAO companyDAO;
    @Autowired private UserDAO userDAO;

    @GetMapping public String home() { return "Please call PUT api."; }

    @PutMapping
    public boolean uploadFile(@NonNull MultipartFile csvMultipartFile) throws Exception {
        System.out.println("Starting import utility : " + now());

        // This file must be a .csv file.
        if (!"text/csv".equals(csvMultipartFile.getContentType())) {
            throw new Exception("err.excel.incorrectFormat " + csvMultipartFile.getContentType());
        }

        // String -> Name of the company
        // List<String>> -> List of errors
        Map<String, List<String>> errorMap = new HashMap<>();

        File csvFile = multipartToFile(csvMultipartFile);
        CsvToBean<CsvInputFile> csvFileData = readCsvFile(csvFile);
        Set<Company> companyList = synchronizedSet(new HashSet<>());
        csvFileData.stream().parallel().forEach(row -> validateCompanyAndAddToCompanySetIfNotExists(companyList, row, errorMap));
        companyDAO.saveAll(companyList);
        System.out.println("> Finished Company : " + now());

        csvFileData = readCsvFile(csvFile);
        Set<User> userSet = synchronizedSet(new HashSet<>());
        csvFileData.stream().parallel().forEach(row -> createUserIfNotExists(userSet, row, errorMap));
        userDAO.saveAll(userSet);
        System.out.println("> Finished User : " + now());

//        System.out.println("Errors :");
//        System.out.println();
//        errorMap.forEach((k, v) -> {
//            System.out.println("Error in company - " + k);
//            v.forEach(err -> System.out.println("Error - " + err));
//            System.out.println("---------------");
//        });
//        System.out.println();

        System.out.println("Finished import utility : " + now());
        return true;
    }

    // ---------------- company stuff
    private void validateCompanyAndAddToCompanySetIfNotExists(Set<Company> companySet, CsvInputFile row, Map<String, List<String>> errorMap) {
        final String companyName = row.getCompanyName();
        if (companyName == null || "".equals(companyName)) {
            return;
        }

        Optional<Company> company = companyDAO.findByName(companyName);
        if (company.isEmpty()) {
            addCompanyToSet(companySet, row);
        } else {
            errorMap.put(companyName, singletonList("Company already exists"));
        }
    }

    private void addCompanyToSet(Set<Company> companySet, CsvInputFile row) {
        companySet.add(new Company(row));
    }

    // ---------------- user stuff
    private void createUserIfNotExists(Set<User> userSet, CsvInputFile row, Map<String, List<String>> errorMap) {
        ArrayList<String> emailList = new ArrayList<>();
        emailList.add(row.getEmail1());
        emailList.add(row.getEmail2());
        emailList.add(row.getEmail3());

        synchronizedList(emailList).stream().parallel()
                                   .forEach(email -> {
                                       try {
                                           validateEmailAndAddToUserSetIfNotExists(userSet, email, row.getCompanyName(), errorMap);
                                       } catch (Exception exception) {
                                           exception.printStackTrace();
                                       }
                                   });
    }

    private void validateEmailAndAddToUserSetIfNotExists(Set<User> userSet, String email, String companyName, Map<String, List<String>> errorMap) throws Exception {
        if (email == null || !validateEmail(email)) {
            return;
        }

        if (userDAO.findByEmail(email).isEmpty()) {
            addUserToSet(userSet, email, companyName);
        } else {
            if (errorMap.containsKey(companyName)) {
                errorMap.get(companyName).add("Email already exists" + email);
            } else {
                errorMap.put(companyName, singletonList("Email already exists" + email));
            }
        }
    }

    private void addUserToSet(Set<User> userSet, String email, String companyName) throws Exception {
        userSet.add(new User(email, companyDAO.findByName(companyName)
                                              .orElseThrow(() -> new Exception("Company not found" + companyName))));
    }

    private boolean validateEmail(String email) {
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        return pattern.matcher(email).matches();
    }

    // ---------------- file stuff
    private static File multipartToFile(MultipartFile csvFile) throws Exception {
        try {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + csvFile.getOriginalFilename());
            csvFile.transferTo(convFile);
            return convFile;
        } catch (Exception ignored) {
            throw new Exception("File convert error");
        }
    }

    private CsvToBean<CsvInputFile> readCsvFile(File csvFile) throws FileNotFoundException {
        return new CsvToBeanBuilder(new FileReader(csvFile))
                        .withType(CsvInputFile.class)
                        .withIgnoreLeadingWhiteSpace(true)
                        .build();
    }

}
