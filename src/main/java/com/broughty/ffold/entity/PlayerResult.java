package com.broughty.ffold.entity;


import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.persistence.*;

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


}
