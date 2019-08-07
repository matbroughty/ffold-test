package com.broughty.ffold;

import com.broughty.ffold.entity.*;
import com.broughty.ffold.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;


@SpringBootApplication
public class FfoldApplication {

    private static final Logger log = LoggerFactory.getLogger(FfoldApplication.class);
    PlayerGroupRepository playerGroupRepository;
    PlayerRepository playerRepository;

    public static void main(String[] args) {
        SpringApplication.run(FfoldApplication.class);
    }

    @Bean
    @Transactional
    public CommandLineRunner loadData(WeekRepository weekRepository, PlayerGroupRepository playerGroupRepository,
                                      PlayerRepository playerRepository, SeasonRepository seasonRepository,
                                      PlayerResultRepository playerResultRepository) {
        this.playerGroupRepository = playerGroupRepository;
        this.playerRepository = playerRepository;
        return (args) -> {
            PlayerGroup playerGroup = playerGroupRepository.findDistinctByTitle("HPD").orElse(null);
            if (playerGroup == null) {
                playerGroup = new PlayerGroup();
                playerGroup.setTitle("HPD");
                addPlayersToGroup(playerGroup, "Mat", "Jase", "Gez", "Frank");


                Season season = new Season();
                season.setYear("2019-20");
                season.setIsCurrent(true);
                season.setPlayerGroup(playerGroup);
                Week week = new Week(1);
                season.addWeek(week);
                seasonRepository.save(season);

                week.setNotes("Week 1 - generated.  Championship games");

                playerGroup.getPlayers().forEach(player -> {
                    PlayerResult playerResult = new PlayerResult();
                    playerResult.setPlayer(player);
                    playerResult.setWeek(week);
                    playerResult.setWinnings(player.getName().equalsIgnoreCase("Jase") ? new BigDecimal(12.81) : new BigDecimal(120.90));
                    playerResult.setMatches(0);
                    playerResultRepository.save(playerResult);
                });

                Week week2 = new Week(2);
                season.addWeek(week2);
                weekRepository.save(week2);
                seasonRepository.save(season);

                playerGroup.getPlayers().forEach(player -> {
                    PlayerResult playerResult = new PlayerResult();
                    playerResult.setPlayer(player);
                    playerResult.setWeek(week2);
                    playerResult.setWinnings(player.getName().equalsIgnoreCase("Jase") ? new BigDecimal(33.99) : new BigDecimal(901.76));
                    playerResult.setMatches(0);
                    playerResultRepository.save(playerResult);
                });


            }

            playerGroup = playerGroupRepository.findDistinctByTitle("Kent").orElse(null);
            if (playerGroup == null) {
                playerGroup = new PlayerGroup();
                playerGroup.setTitle("Kent");
                addPlayersToGroup(playerGroup, "Matty", "Dan", "Ian", "PaulS", "PaulV");
                Season season = new Season();
                season.setYear("2019-20");
                season.setIsCurrent(true);
                season.setPlayerGroup(playerGroup);
                Week week = new Week(1);
                week.setNotes("Week 1 - generated.  Championship games");
                season.addWeek(week);
                seasonRepository.save(season);


                playerGroup.getPlayers().forEach(player -> {
                    PlayerResult playerResult = new PlayerResult();
                    playerResult.setPlayer(player);
                    playerResult.setWeek(week);
                    playerResult.setWinnings(BigDecimal.ZERO);
                    playerResult.setMatches(0);
                    playerResultRepository.save(playerResult);
                });


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


            weekRepository.findCurrentSeasonsWeeksForPlayerGroupMap("HPD", null).forEach(map -> {
                        log.info("On another week");
                        map.forEach((k, v) -> log.info("Key = {} and value = {}", k, v));
                    }

            );


        };
    }

    private void addPlayersToGroup(PlayerGroup playerGroup, String... names) {
        for (String name : names) {
            Player player = playerRepository.findPlayerByName(name);
            //if (player == null) {
            playerGroup.addPlayer(new Player(name));
//            }else{
//                playerGroup.addPlayer(playerRepository.);
//            }
        }

        playerGroupRepository.save(playerGroup);

    }
}
