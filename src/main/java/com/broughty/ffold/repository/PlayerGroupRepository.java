package com.broughty.ffold.repository;

import com.broughty.ffold.entity.PlayerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerGroupRepository extends JpaRepository<PlayerGroup, Long> {
    PlayerGroup findDistinctByTitle(String title);
}
