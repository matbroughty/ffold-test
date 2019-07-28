package com.broughty.ffold.entity;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Slf4j
@Entity
public class Season {
    @Id
    @GeneratedValue
    private Long id;

    /**
     * This will be in the form 2019-20 or 2018-19 etc
     */
    private String year;

    /**
     * True if current season
     */
    private Boolean isCurrent = Boolean.FALSE;

    @OneToMany(
            mappedBy = "season",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @EqualsAndHashCode.Exclude
    private List<Week> weeks = new ArrayList<>();


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_group_id")
    private PlayerGroup playerGroup;


    public void addWeek(Week week) {
        weeks.add(week);
        week.setSeason(this);
    }

    public void removeWeek(Week week) {
        weeks.remove(week);
        week.setSeason(null);
    }

}
