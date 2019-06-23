package com.broughty.ffold.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@NoArgsConstructor
@Data
@Slf4j
@Entity
public class Week {

    @Id
    @GeneratedValue
    private Long id;

    @NonNull
    private String firstName;

    @NonNull
    private String lastName;

    public Week(String firstName, String lastName) {
		this.firstName = firstName;
		this.lastName = lastName;
    }


    void doSomeStuff() {
        log.debug("hello {} {}", getFirstName(), getId());
    }

}