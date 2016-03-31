package gl_own;

/**
 * Created by daniel on 3/31/16.
 */
public class sq {

    Mesh m;

    //simple testing of the Mesh
    public sq(float verts[])
    {
        short[] tris = {0,1,2,0,2,3};
        float[] color = {0.2f,0.7f,0.9f,1f};
        m = new Mesh(tris, verts, color);
    }

    public void draw(float[] mvpMatrix)
    {
        m.draw(mvpMatrix);
    }
}
