package com.broughty.ffold.repository;

import com.broughty.ffold.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Player findPlayerByName(String name);
}
