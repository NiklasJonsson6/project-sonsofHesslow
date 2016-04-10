package gl_own;

import gl_own.Geometry.Vector2;
import gl_own.Geometry.Vector3;


public abstract class GLObject {
    public float[] modelMatrix = new float[16];
    public GLObject()
    {
        modelMatrix = new float[]
        {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
        };
    }

    public void setPos(Vector3 vec)
    {
        modelMatrix[3] = vec.x;
        modelMatrix[7] = vec.y;
        modelMatrix[11] = 0;
    }

    public void setPos(Vector2 vec)
    {
        setPos(new Vector3(vec));
    }
    public abstract void draw(float[] projectionMatrix);
}
