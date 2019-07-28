package com.broughty.ffold.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;
import java.math.BigDecimal;

@NoArgsConstructor
@Data
@Slf4j
@Entity
public class PlayerResult {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "week_id")
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id")
    private Player player;

    private BigDecimal winnings = BigDecimal.ZERO;

    /**
     * How many matches was the win for - typically only be 4 or 5 unless games cancelled
     */
    private Integer matches = 0;

    private String notes;



}
