package gl_own;

import copied_gl.MyGLRenderer;
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
        setPos(new Vector3(vec));
    }
    public abstract void draw(float[] projectionMatrix);
    public abstract Mesh getMesh();
}
