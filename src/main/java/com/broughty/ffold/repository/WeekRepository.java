package com.broughty.ffold.repository;

import com.broughty.ffold.entity.Week;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface WeekRepository extends JpaRepository<Week, Long>, CustomWeekRepository {

	Week findByWeekNumberAndSeasonId(Integer weekNumber, Long seasonId);


	@Query("SELECT w FROM Week w JOIN w.season s WHERE s.isCurrent = true")
	List<Week> findCurrentWeeks();

	@Query("SELECT w FROM Week w JOIN w.season s JOIN s.playerGroup pg WHERE s.isCurrent = true and pg.title =?1")
	List<Week> findCurrentWeeksForPlayerGroup(String playerGroup);






}