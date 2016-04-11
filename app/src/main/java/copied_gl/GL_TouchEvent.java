package copied_gl;

import android.view.MotionEvent;

import gl_own.Geometry.Vector2;
import gl_own.Geometry.Vector3;

/**
 * Created by Daniel on 11/04/2016.
 */
public class GL_TouchEvent
{
    public MotionEvent e;
    public boolean touchedRegion;
    public int regionIndex;
    public Vector2 worldPosition;
    public Vector2 screenPosition;

    public GL_TouchEvent(MotionEvent e, boolean touchedRegion, int regionIndex, Vector2 worldPosition, Vector2 screenPosition) {
        this.e = e;
        this.touchedRegion = touchedRegion;
        this.regionIndex = regionIndex;
        this.worldPosition = worldPosition;
        this.screenPosition = screenPosition;
    }
}
