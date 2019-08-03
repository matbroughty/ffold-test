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

    PlayerGroupRepository playerGroupRepository;

    PlayerRepository playerRepository;

    @Bean
    public CommandLineRunner loadData(WeekRepository weekRepository, PlayerGroupRepository playerGroupRepository,
                                      PlayerRepository playerRepository, SeasonRepository seasonRepository,
                                      PlayerResultRepository playerResultRepository) {
        this.playerGroupRepository = playerGroupRepository;
        this.playerRepository = playerRepository;
        return (args) -> {
            PlayerGroup playerGroup = playerGroupRepository.findDistinctByTitle("HPD");
            if (playerGroup == null) {
                playerGroup = new PlayerGroup();
                playerGroup.setTitle("HPD");
                addPlayersToGroup(playerGroup, "Mat", "Jase", "Gez", "Frank");


                Season season = new Season();
                season.setYear("2019-20");
                season.setIsCurrent(true);
                season.setPlayerGroup(playerGroup);
                season.addWeek(new Week(1));
                seasonRepository.save(season);

            }

            playerGroup = playerGroupRepository.findDistinctByTitle("Kent");
            if (playerGroup == null) {
                playerGroup = new PlayerGroup();
                playerGroup.setTitle("Kent");
                addPlayersToGroup(playerGroup, "Mat", "Dan", "Ian", "PaulS", "PaulV");
                Season season = new Season();
                season.setYear("2019-20");
                season.setIsCurrent(true);
                season.setPlayerGroup(playerGroup);
                season.addWeek(new Week(1));
                seasonRepository.save(season);

            }


            // fetch all weeks
            log.info("weeks find all ():");
            log.info("-------------------------------");
            for (Week week : weekRepository.findAll()) {
                log.info(week.toString());
            }
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

    private void addPlayersToGroup(PlayerGroup playerGroup, String...names) {
        for (String name : names) {
            Player player = playerRepository.findPlayerByName(name);
            if (player == null) {
                playerGroup.addPlayer(new Player(name));
            }
        }

        playerGroupRepository.save(playerGroup);

    }
}
