package com.sajal.csvimportutilitypoc.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;

import java.util.Objects;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = SEQUENCE)
    @Getter @Setter private int id;

    @Column(unique = true, nullable = false)
    @Getter @Setter private String email;

    @ManyToOne
    @JoinColumn(name = "company_id", referencedColumnName = "id")
    @Getter @Setter private Company company;

    public User(String email, Company company) {
        this.email = email;
        this.company = company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

}
