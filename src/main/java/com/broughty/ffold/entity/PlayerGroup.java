package com.broughty.ffold.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@NoArgsConstructor
@Data
@Slf4j
@Entity
public class PlayerGroup {
    @Id
    @GeneratedValue
    private Long id;

    @NaturalId
    private String title;

    @ManyToMany(cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    })

    @JoinTable(name = "player_group_player",
            joinColumns = @JoinColumn(name = "player_group_id"),
            inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    @ToString.Exclude
    private Set<Player> players = new HashSet<>();

    public void addPlayer(Player player) {
        players.add(player);
        player.getPlayerGroups().add(this);
    }

    public void removePlayer(Player player) {
        players.remove(player);
        player.getPlayerGroups().remove(this);
    }

//    @OneToMany(
//            mappedBy = "player_group",
//            cascade = CascadeType.ALL,
//            orphanRemoval = true
//    )
//    private List<Season> seasons = new ArrayList<>();
//
//    public void addSeason(Season season) {
//        seasons.add(season);
//        season.setPlayerGroup(this);
//    }
//
//    public void removeSeason(Season season) {
//        seasons.remove(season);
//        season.setPlayerGroup(null);
//    }

}
