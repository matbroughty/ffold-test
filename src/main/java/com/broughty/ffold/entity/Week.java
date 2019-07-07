package com.broughty.ffold.entity;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
@Slf4j
@Entity
public class Week {

    @Id
    @GeneratedValue
    private Long id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id")
    private Season season;


    @ToString.Exclude
    @OneToMany(
            mappedBy = "week",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PlayerResult> playerResults = new ArrayList<>();


    @NonNull
    private Integer weekNumber;


    public Week(Integer weekNumber) {
        this.weekNumber = weekNumber;
    }



    public void addPlayerResult(PlayerResult result) {
        playerResults.add(result);
        result.setWeek(this);
    }

    public void removePlayerResult(PlayerResult result) {
        playerResults.remove(result);
        result.setWeek(null);
    }




}