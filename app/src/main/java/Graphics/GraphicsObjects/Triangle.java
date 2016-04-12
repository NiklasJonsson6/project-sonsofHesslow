package Graphics.GraphicsObjects;

import android.opengl.Matrix;

import Graphics.Geometry.Vector2;

/**
 * Created by Daniel on 10/04/2016.
 */
public class Triangle extends GLObject {
    public Mesh mesh;

    public Triangle(Vector2 a,Vector2 b,Vector2 c, float[] color)
    {
        Vector2[] verts = new Vector2[]{a,b,c};
        short[] tris = new short[]{0,1,2};
        mesh = new Mesh(tris,verts,color);
    }

    public void draw(float[] projectionMatrix){
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        mesh.draw(mvpMatrix);
    }

    @Override
    public Mesh getMesh() {
        return mesh;
    }
}
