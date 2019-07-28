package com.broughty.ffold.repository;

import java.util.List;
import java.util.Map;

public interface CustomWeekRepository{
    List<Map<String,Object>> findCurrentSeasonsWeeksForPlayerGroupMap(String playerGroup);

    Map<String,Object> findWeeksForPlayerGroupAndSeasonYearMap(String playerGroup, String year);
}
