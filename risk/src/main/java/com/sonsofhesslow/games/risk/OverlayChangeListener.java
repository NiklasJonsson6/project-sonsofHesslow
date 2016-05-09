package com.sonsofhesslow.games.risk;

/**
 * Created by fredr on 2016-05-08.
 */
public interface OverlayChangeListener {
    void phaseEvent(OverlayChangeEvent overlayChangeEvent);
    void placeEvent(OverlayChangeEvent overlayChangeEvent);
    void playerChangeEvent(OverlayChangeEvent overlayChangeEvent);
}
