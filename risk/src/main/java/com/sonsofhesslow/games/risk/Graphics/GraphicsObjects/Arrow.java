package com.sonsofhesslow.games.risk.graphics.GraphicsObjects;

import android.opengl.Matrix;

import java.nio.FloatBuffer;

import com.sonsofhesslow.games.risk.graphics.Geometry.Vector2;

/**
 * Created by Daniel on 12/05/2016.
 */
public class Arrow extends GLObject{
    DefaultShader shader;
    Mesh mesh;
    Mesh outlineMesh;
    float color[];
    FloatBuffer sideBuffer;

    public Arrow(Vector2 from, Vector2 to, float[] color, Renderer renderer)
    {
        super(renderer);
        this.color = color;

        // verts
        //   0
        //1 23 4
        //
        //  56

        // tris
        // /_\
        //  |
        //  |
        float width     = 0.02f;
        float headRatio = 4f;
        float outline= 0.01f;
        Vector2 dir = Vector2.Sub(from,to).normalized();
        Vector2 orth = Vector2.Mul(new Vector2(-dir.y, dir.x), width);
        Vector2 orthHead = Vector2.Mul(orth, headRatio);
        Vector2 bottomHead = Vector2.Sub(to, Vector2.Mul(dir, -(width * headRatio)));

        short[] tris = new short[]{0,1,4,3,2,5,5,6,3};

        Vector2[] verts = new Vector2[]
                {
                        Vector2.Add(to,Vector2.Mul(dir,outline)),
                        Vector2.Sub(bottomHead,orthHead),   // left left
                        Vector2.Sub(bottomHead, orth),      // left center
                        Vector2.Add(bottomHead, orth),      // right center
                        Vector2.Add(bottomHead,orthHead),   // right right
                        Vector2.Sub(from, orth),            // left bottom
                        Vector2.Add(from, orth),             // right bottom
                };
        mesh = new Mesh(tris, verts);

        orth = Vector2.Mul(new Vector2(-dir.y, dir.x), width+outline);
        orthHead = Vector2.Mul(new Vector2(-dir.y, dir.x), headRatio*width+outline*2f);
        bottomHead = Vector2.Sub(to, Vector2.Mul(dir, -(width * headRatio+outline)));

        tris = new short[]{0,1,4,3,2,5,5,6,3};
        verts = new Vector2[]
                {
                        to,
                        Vector2.Sub(bottomHead,orthHead),   // left left
                        Vector2.Sub(bottomHead, orth),      // left center
                        Vector2.Add(bottomHead, orth),      // right center
                        Vector2.Add(bottomHead,orthHead),   // right right
                        Vector2.Sub(from, orth),            // left bottom
                        Vector2.Add(from, orth),             // right bottom
                };
        outlineMesh = new Mesh(tris,verts);
    }

    @Override
    public void draw(float[] projectionMatrix) {
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        shader.use(outlineMesh, projectionMatrix, new float[]{0, 0, 0, 1});
        shader.use(mesh, projectionMatrix, color);
    }

    @Override
    public void gl_init() {
        mesh.init();
        outlineMesh.init();
        shader = new DefaultShader();
    }
}
