package com.broughty.ffold;

import com.broughty.ffold.entity.*;
import com.broughty.ffold.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;


@SpringBootApplication
public class FfoldApplication {

    private static final Logger log = LoggerFactory.getLogger(FfoldApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(FfoldApplication.class);
    }

    @Bean
    public CommandLineRunner loadData(WeekRepository weekRepository, PlayerGroupRepository playerGroupRepository,
                                      PlayerRepository playerRepository, SeasonRepository seasonRepository,
                                      PlayerResultRepository playerResultRepository) {
        return (args) -> {
            Player player = null;
            PlayerGroup playerGroup = playerGroupRepository.findDistinctByTitle("HPD");

            if (playerGroup == null) {
                playerGroup = new PlayerGroup();
                playerGroup.setTitle("HPD");

                player = playerRepository.findPlayerByName("Mat");
                if (player == null) {
                    playerGroup.addPlayer(new Player("Mat"));
                }


                playerGroupRepository.save(playerGroup);
            }


            Season season = new Season();
            season.setYear("2019-20");
            season.setIsCurrent(true);
            season.setPlayerGroup(playerGroup);
            season.addWeek(new Week(1));
            season.addWeek(new Week(2));
            season.addWeek(new Week(3));
            season.addWeek(new Week(4));

            seasonRepository.save(season);


            // fetch all weeks
            log.info("weeks find all ():");
            log.info("-------------------------------");
            for (Week week : weekRepository.findAll()) {
                log.info(week.toString());
            }
            log.info("");

            // fetch an individual week by number
            Week week = weekRepository.findByWeekNumberAndSeasonId(1, season.getId());

            PlayerResult playerResult = new PlayerResult();
            playerResult.setWeek(week);
            playerResult.setPlayer(playerRepository.findPlayerByName("Mat"));
            playerResult.setMatches(5);
            playerResult.setWinnings(new BigDecimal("123.44"));
            playerResult.setNotes("TOT,MCY,LIV,MUN,ARS");
            playerResultRepository.save(playerResult);


            log.info("Week found with with week number 1 and season id 1 ");
            log.info("--------------------------------");
            log.info(week.toString());
            log.info("");

            // fetch customers by last name
            log.info("All Players in group:");
            log.info("--------------------------------------------");
            playerGroupRepository.findAll().forEach(pg -> log.info("Player Group = {}", pg));
            log.info("");


            weekRepository.findCurrentSeasonsWeeksForPlayerGroupMap("HPD").forEach(map -> {
                        log.info("On another week");
                        map.forEach((k,v) -> log.info("Key = {} and value = {}", k, v));
                    }

            );


        };
    }
}
