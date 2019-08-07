package com.broughty.ffold.repository;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

public interface CustomWeekRepository {
    List<Map<String, Object>> findCurrentSeasonsWeeksForPlayerGroupMap(String playerGroup, String weekNumber);

    Map<String, Object> findWeeksForPlayerGroupAndSeasonYearMap(String playerGroup, String year);

    Map<String, PlayerTotals> buildPlayerTotalMap(String playerGroup);

    Map<String, Object> createNextWeekForPlayerGroupMap(String playerGroup);
}
