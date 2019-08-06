package com.broughty.ffold.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.NaturalId;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Data
@Slf4j
@Entity
public class Player {

    @Id
    @GeneratedValue
    private Long id;

    @NaturalId
    private String name;

    @EqualsAndHashCode.Exclude
    @ManyToMany(mappedBy = "players")
    private Set<PlayerGroup> playerGroups = new HashSet<>();

    public Player(String name) {
        this.name = name;
    }
}
