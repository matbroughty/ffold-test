package com.broughty.ffold.repository;

import com.broughty.ffold.entity.PlayerResult;
import com.broughty.ffold.entity.Season;
import com.broughty.ffold.entity.Week;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class CustomWeekRepositoryImpl implements CustomWeekRepository {

    public static final String WEEK_NUMBER = "Week Number";

    public static final String SEASON = "Season";

    public static final String SEASON_ID = "season_id";

    public static final String WEEK_NOTES = "Week Notes";

    public static final String WEEK_ID = "week_id";

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
            weekMap.put(WEEK_NUMBER, week.getWeekNumber());
            weekMap.putAll(week.getPlayerResults()
                    .stream()
                    .filter(pr -> pr.getPlayer() != null)
                    .collect(Collectors.toMap(pr -> pr.getPlayer().getName(), PlayerResult::getWinnings)));
            //weekMap.put(SEASON, week.getSeason().getYear());
            weekMap.put(WEEK_NOTES, week.getNotes());
            weekMap.put(SEASON_ID, week.getSeason().getId());
            weekMap.put(WEEK_ID, week.getId().toString());
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
        playerTotalsMap.forEach((k, v) -> log.info("buildPlayerTotalMap with player name {} and value {}", k, v));
        return playerTotalsMap;
    }

    @Override
    @Transactional
    public Map<String, Object> createNextWeekForPlayerGroupMap(String playerGroup) {
        List<Week> weeks = entityManager.createQuery(CURRENT_SEASON_ALL_WEEKS)
                .setParameter(1, playerGroup).getResultList();
        Week week = weeks.stream().max(Comparator.comparing(Week::getWeekNumber)).orElse(new Week());
        Map<String, Object> weekMap = new LinkedHashMap<>();
        week.getSeason().getPlayerGroup().getPlayers().forEach(p-> weekMap.put(p.getName(), BigDecimal.ZERO));
        weekMap.put(WEEK_NUMBER, week.getWeekNumber() + 1);
        weekMap.put(SEASON_ID, week.getSeason().getId());
        weekMap.put(WEEK_NOTES, "");
        return weekMap;
    }


    @Transactional
    @Override
    public void delete(@NonNull Map<String, Object> weekDetails) {
        log.info("in delete with {} ", weekDetails.keySet().stream().map(k-> "key = " + k + " value = " + weekDetails.get(k)).collect(Collectors.toList()));
        String weekId = weekDetails.get(WEEK_ID) != null ? weekDetails.get(WEEK_ID).toString() : null;
        if(StringUtils.isNotBlank(weekId)){
            Week week = entityManager.find(Week.class, Long.valueOf(weekId));
            log.warn("deleting week with id {} - week {}", weekId, week);
            entityManager.remove(week);
        }
    }

    @Transactional
    @Override
    public void save(Map<String, Object> weekDetails) {
        log.info("in save with {} ", weekDetails.keySet().stream().map(k-> "key = " + k + " value = " + weekDetails.get(k)).collect(Collectors.toList()));
        String weekId = weekDetails.get(WEEK_ID) != null ? weekDetails.get(WEEK_ID).toString() : null;
        Week week;
        if(StringUtils.isNotBlank(weekId)){
            week = entityManager.find(Week.class, Long.valueOf(weekId));
            log.info("Updating week {}", week);
            week.getPlayerResults().stream().forEach(pr-> {
                log.info("Updating player result for {} from {} to {} ", pr.getPlayer(), pr.getWinnings(), weekDetails.get(pr.getPlayer().getName()));
                pr.setWinnings(new BigDecimal(weekDetails.get(pr.getPlayer().getName()).toString()));
            });
        }else{
            week = new Week();
            Season season = entityManager.find(Season.class, Long.valueOf(weekDetails.get(SEASON_ID).toString()));
            week.setSeason(season);
            season.getPlayerGroup().getPlayers().forEach(player -> {
                PlayerResult playerResult = new PlayerResult();
                playerResult.setWeek(week);
                playerResult.setPlayer(player);
                playerResult.setWinnings(new BigDecimal(weekDetails.get(player.getName()).toString()));
                log.info("adding new player result {} to week {}", playerResult, week);
                week.addPlayerResult(playerResult);
            });

        }
        week.setNotes(weekDetails.get(WEEK_NOTES).toString());
        week.setWeekNumber(Integer.valueOf(weekDetails.get(WEEK_NUMBER).toString()));
        log.info("Persisting week {}", week);
        entityManager.persist(week);
    }
}
