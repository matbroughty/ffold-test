package com.broughty.ffold.repository;

import com.broughty.ffold.entity.PlayerResult;
import com.broughty.ffold.entity.Week;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Data
@ToString
@Slf4j
public
class PlayerTotals {

    @NonNull
    private String playerName;

    @NonNull
    private BigDecimal totalWon = BigDecimal.ZERO;

    private List<Week> winningWeeks = new ArrayList<>();

    private List<PlayerResult> winningResults = new ArrayList<>();


}
