package gl_own;

import com.example.niklas.projectsonsofhesslow.Updatable;

import java.util.Arrays;

/**
 * Created by daniel on 3/31/16.
 */
public class Camera implements Updatable{

    private static Camera instance;
    public static Camera getInstance()
    {
        if(instance == null) instance = new Camera();
        return instance;
    }

    public float[] pos = {0,0,-3};
    private float[] targetPos = pos;
    private float[] prevPosition = pos;
    float t;
    final float interpolationTime = 0.5f; // in seconds

    public float X()
    {
        return pos[0];
    }

    public float Y()
    {
        return pos[1];
    }
    public float Z()
    {
        return pos[2];
    }

    public void Update(float dt)
    {
        if(!Arrays.equals(targetPos, prevPosition))
        {
            t+= dt;
            float sin = (float)Math.sin(t);
            float sin_i = 1-(float)Math.sin(t);

            pos[0] = sin*prevPosition[0] + sin_i*targetPos[0];
            pos[1] = sin*prevPosition[1] + sin_i*targetPos[1];
            pos[2] = sin*prevPosition[2] + sin_i*targetPos[2];
        }
    }

    public void setPos(float[] pos)
    {
        prevPosition = this.pos;
        targetPos = pos;
        t = 0;
    }

}
