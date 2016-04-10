package gl_own;

import android.opengl.Matrix;

import java.sql.SQLOutput;
import java.util.Arrays;

import gl_own.Geometry.Util;
import gl_own.Geometry.Vector2;

/**
 * Created by Daniel on 10/04/2016.
 */
public class Square extends GLObject {
    public Mesh mesh;

    public Square(Vector2 center, float side,float[] color)
    {
        float hside = side/2;
        Vector2 top_rigth =     Vector2.Add(center, new Vector2( hside,  hside));
        Vector2 top_left =      Vector2.Add(center, new Vector2(-hside,  hside));
        Vector2 bottom_left =   Vector2.Add(center, new Vector2(-hside, -hside));
        Vector2 bottom_rigth =  Vector2.Add(center, new Vector2( hside, -hside));
        Vector2[] verts = new Vector2[]{top_rigth,top_left,bottom_left,bottom_rigth};
        short[] tris = new short[]{0,1,2,0,2,3};
        mesh = new Mesh(tris,verts,color);
    }

    public void draw(float[] projectionMatrix){
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        mesh.draw(mvpMatrix);
    }
}
