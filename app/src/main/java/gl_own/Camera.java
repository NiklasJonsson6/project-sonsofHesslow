package gl_own;

import java.util.Arrays;

import gl_own.Geometry.Vector2;
import gl_own.Geometry.Vector3;

/**
 * Created by daniel on 3/31/16.
 */
public class Camera {

    private static Camera instance;
    public static Camera getInstance()
    {
        if(instance == null) instance = new Camera();
        return instance;
    }

    public Vector3 pos = new Vector3(-5,-5,-3); //-5-5 is about the current center..
    public void setPos(Vector3 newPos)
    {
        pos = newPos;
    }
    public void setPosRel(Vector3 newPos)
    {
        pos = Vector3.Add(newPos,pos);
    }
}
