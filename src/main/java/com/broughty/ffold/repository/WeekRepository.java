package com.broughty.ffold.repository;

import com.broughty.ffold.entity.Week;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WeekRepository extends JpaRepository<Week, Long> {

	Week findByWeekNumberAndSeasonId(Integer weekNumber, Long seasonId);
}