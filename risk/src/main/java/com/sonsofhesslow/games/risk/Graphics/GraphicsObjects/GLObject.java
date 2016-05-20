package com.sonsofhesslow.games.risk.graphics.GraphicsObjects;

import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.Geometry.Vector3;


public abstract class GLObject {
    float[] modelMatrix = new float[16];
    public boolean isActive = true;

    private final Renderer renderer;
    GLObject(Renderer renderer)
    {
        this.renderer = renderer;
        modelMatrix = new float[]
        {
            1,0,0,0,
            0,1,0,0,
            0,0,1,0,
            0,0,0,1,
        };
        renderer.delayedInit(this);
    }

    public float[] getMatrix()
    {
        return modelMatrix;
    }

    public float drawOrder=0;
    private Vector3 pos = Vector3.Zero();
    public void Remove()
    {
        renderer.remove(this);
    }

    public Vector3 getPos()
    {
        return new Vector3(pos); // the return value cannot modify our state.
    }
    public void setPos(Vector3 vec)
    {
        pos = vec;
        modelMatrix[12] = vec.x;
        modelMatrix[13] = vec.y;
        modelMatrix[14] = vec.z;
        drawOrder = -vec.z;
    }

    public void setPos(Vector2 vec)
    {
        setPos(new Vector3(vec,0));
    }
    public abstract void draw(float[] projectionMatrix);
    public abstract void gl_init();
}
