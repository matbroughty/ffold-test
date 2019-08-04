package com.broughty.ffold.repository;

import com.broughty.ffold.entity.PlayerResult;
import com.broughty.ffold.entity.Week;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CustomWeekRepositoryImpl implements CustomWeekRepository {

    private static final Logger log = LoggerFactory.getLogger(CustomWeekRepositoryImpl.class);

    private static final String CURRENT_SEASON_ALL_WEEKS = "SELECT w FROM Week w JOIN w.season s JOIN s.playerGroup pg WHERE s.isCurrent = true and pg.title =?1";

    private static final String CURRENT_SEASON_SPECIFIC_WEEK = "SELECT w FROM Week w JOIN w.season s JOIN s.playerGroup pg WHERE s.isCurrent = true and pg.title =?1 and w.weekNumber=?2";


    @Autowired
    private EntityManager entityManager;

    @Transactional
    @Override
    public List<Map<String, Object>> findCurrentSeasonsWeeksForPlayerGroupMap(String playerGroup, String weekNumber) {
        log.info("findCurrentSeasonsWeeksForPlayerGroupMap with player group {} and week {}", playerGroup, weekNumber);
        List<Map<String, Object>> weeksListMap = new ArrayList<>();
        List<Week> weeks;
        if (StringUtils.isNumeric(weekNumber)) {
            weeks = entityManager.createQuery(CURRENT_SEASON_SPECIFIC_WEEK)
                    .setParameter(1, playerGroup).setParameter(2, Integer.valueOf(weekNumber)).getResultList();
        } else {
            weeks = entityManager.createQuery(CURRENT_SEASON_ALL_WEEKS)
                    .setParameter(1, playerGroup).getResultList();
        }

        log.info("Weeks size = {}", weeks.size());
        weeks.forEach(week -> {
            Map<String, Object> weekMap = new LinkedHashMap<>();
            log.info("Processing week {}", week);
            weekMap.put("Week Number", week.getWeekNumber());
            weekMap.putAll(week.getPlayerResults()
                    .stream()
                    .filter(pr -> pr.getPlayer() != null)
                    .collect(Collectors.toMap(pr -> pr.getPlayer().getName(), PlayerResult::getWinnings)));
            weekMap.put("Season", week.getSeason().getYear());
            weekMap.put("Week Notes", week.getNotes());
            //weekMap.put("Season Id", week.getSeason().getId());
            //weekMap.put("Week Id", week.getId().toString());
            weeksListMap.add(weekMap);
        });

        return weeksListMap;
    }

    @Override
    public Map<String, Object> findWeeksForPlayerGroupAndSeasonYearMap(String playerGroup, String year) {
        return null;
    }

    @Override
    public Map<String, PlayerTotals> buildPlayerTotalMap(String playerGroup) {
        List<Week> weeks = entityManager.createQuery(CURRENT_SEASON_ALL_WEEKS)
                .setParameter(1, playerGroup).getResultList();

        Map<String, PlayerTotals> playerTotalsMap = new HashMap<>();
        weeks.stream().forEach(week -> {
            week.getPlayerResults()
                    .stream()
                    .filter(pr -> pr.getWinnings().compareTo(BigDecimal.ZERO) > 0)
                    .forEach(playerResult -> {
                        PlayerTotals playerTotals = playerTotalsMap.get(playerResult.getPlayer().getName());
                        if (playerTotals == null) {
                            playerTotals = new PlayerTotals(playerResult.getPlayer().getName());
                            playerTotalsMap.put(playerResult.getPlayer().getName(), playerTotals);
                        }
                        playerTotals.setTotalWon(playerTotals.getTotalWon().add(playerResult.getWinnings()));
                        playerTotals.getWinningWeeks().add(playerResult.getWeek());
                    });

        });
        playerTotalsMap.forEach((k,v) -> log.info("buildPlayerTotalMap with player name {} and value {}", k, v));
        return playerTotalsMap;
    }
}
