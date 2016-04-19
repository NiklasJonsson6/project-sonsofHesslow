package Graphics.GraphicsObjects;

import Graphics.MyGLRenderer;
import Graphics.Geometry.Vector2;
import Graphics.Geometry.Vector3;


public abstract class GLObject {
    public float[] modelMatrix = new float[16];
    public boolean isActive = true;

    public GLObject()
    {

        modelMatrix = new float[]
        {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
        };
        MyGLRenderer.delayed_init(this);
    }

    public void setPos(Vector3 vec)
    {
        modelMatrix[12] = vec.x;
        modelMatrix[13] = vec.y;
        modelMatrix[14] = vec.z;
    }

    public void setPos(Vector2 vec)
    {
        setPos(new Vector3(vec,0));
    }
    public abstract void draw(float[] projectionMatrix);
    public abstract Mesh[] getMeshes();
}
