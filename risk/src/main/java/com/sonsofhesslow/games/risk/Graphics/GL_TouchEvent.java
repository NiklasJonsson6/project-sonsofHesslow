package com.sonsofhesslow.games.risk.graphics;

import android.view.MotionEvent;

import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;

/**
 * Created by Daniel on 11/04/2016.
 */
public class GL_TouchEvent
{
    public final MotionEvent e;
    public final boolean touchedRegion;
    public final int regionId;
    public final Vector2 worldPosition;
    public final Vector2 screenPosition;
    public final float scale;
    public final boolean isZooming;

    public GL_TouchEvent(MotionEvent e, boolean touchedRegion, boolean isZooming, int regionId, Vector2 worldPosition, Vector2 screenPosition, float scale) {
        this.e = e;
        this.touchedRegion = touchedRegion;
        this.regionId = regionId;
        this.worldPosition = worldPosition;
        this.screenPosition = screenPosition;
        this.scale = scale;
        this.isZooming = isZooming;
    }
}
