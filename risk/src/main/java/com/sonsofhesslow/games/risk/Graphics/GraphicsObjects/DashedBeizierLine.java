package com.sonsofhesslow.games.risk.Graphics.GraphicsObjects;
import android.opengl.Matrix;
import com.sonsofhesslow.games.risk.Graphics.Geometry.BeizierPath;
import com.sonsofhesslow.games.risk.Graphics.Geometry.Vector2;

public class DashedBeizierLine extends GLObject{
    public Mesh mesh;
    DefaultShader shader;
    @Override
    public void gl_init() {
        mesh.init();
        shader = new DefaultShader();
    }

    final int naive_precision = 30; //higher is more detailed

    public DashedBeizierLine(BeizierPath path, Renderer renderer) // start ctl ctl point ctl ctl point ctl ctl (start)
    {
        super(renderer);
        Vector2[] verts = path.approximateBeizierPath_naive(naive_precision);
        Vector2[] outline_verts = new Vector2[verts.length*2];

        //while the triangles are not guaranteed to be non-overlapping constant winding.
        // the way we render the triangles we just dont care.
        short[] outline_tris = new short[6*outline_verts.length];
        for(int i = 0;i<verts.length;i++)
        {
            Vector2 prev = verts[i];
            Vector2 current = verts[(i+1)%verts.length];
            Vector2 next = verts[Math.min((i + 2), verts.length - 1)];
            Vector2 diff = Vector2.Sub(next,prev);
            Vector2 orth = Vector2.Mul(new Vector2(-diff.y,diff.x).normalized(),0.01f); //think about this.

            outline_verts[i*2+0] = Vector2.Add(current, orth);
            outline_verts[i*2+1] = Vector2.Sub(current, orth);

            if(i%2==0)
            {
                outline_tris[i*6+0] =(short)(i*2+0);
                outline_tris[i*6+1] =(short)(i*2+1);
                outline_tris[i*6+2] =(short)((i*2+2)%outline_verts.length);

                outline_tris[i*6+3] =(short)(i*2+1);
                outline_tris[i*6+4] =(short)((i*2+2)%outline_verts.length);
                outline_tris[i*6+5] =(short)((i*2+3)%outline_verts.length);
            }
        }

        mesh = new Mesh(outline_tris,outline_verts);
    }

    public void draw(float[] projectionMatrix){
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        shader.use(mesh,mvpMatrix,new float[]{0,0,0,1});
    }
}
