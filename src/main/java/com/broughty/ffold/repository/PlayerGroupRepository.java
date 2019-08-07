package com.broughty.ffold.repository;

import com.broughty.ffold.entity.PlayerGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerGroupRepository extends JpaRepository<PlayerGroup, Long> {
    Optional<PlayerGroup> findDistinctByTitle(String title);
}
