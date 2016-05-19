package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Risk;

/**
 * Created by fredr on 2016-05-08.
 */
public class OverlayChangeEvent {
    Risk risk;
    public  OverlayChangeEvent(Risk risk){
        this.risk = risk;
    }
}
