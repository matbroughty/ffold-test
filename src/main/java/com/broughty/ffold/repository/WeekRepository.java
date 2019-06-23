package com.broughty.ffold.repository;

import com.broughty.ffold.entity.Week;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeekRepository extends JpaRepository<Week, Long> {

	List<Week> findByLastNameStartsWithIgnoreCase(String lastName);
}