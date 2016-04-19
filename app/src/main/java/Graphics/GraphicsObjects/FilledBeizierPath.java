package Graphics.GraphicsObjects;

import android.opengl.Matrix;

import java.util.LinkedList;
import java.util.List;

import Graphics.Geometry.BeizierPath;
import Graphics.Geometry.Util;
import Graphics.Geometry.Vector2;

/**
 * Created by daniel on 3/31/16.
 */
public class FilledBeizierPath extends GLObject{

    public Mesh fill_mesh;
    public Mesh outline_mesh;

    @Override
    public Mesh[] getMeshes() {
        return new Mesh[]{fill_mesh,outline_mesh};
    }

    final int naive_precision = 30; //higher is more detailed


    public FilledBeizierPath(BeizierPath path) // start ctl ctl point ctl ctl point ctl ctl (start)
    {
        if(!path.isClosed()) throw new IllegalArgumentException("the beizier path needs to be closed!");

        Vector2[] verts = path.approximateBeizierPath_naive(naive_precision);
        Vector2[] outline_verts = new Vector2[verts.length*2];

        //while the triangles are not guaranteed to be non-overlapping constant winding.
        // the way we render the triangles we just dont care.
        short[] outline_tris = new short[6*outline_verts.length];
        for(int i = 0;i<verts.length;i++)
        {
            Vector2 prev = verts[i];
            Vector2 current = verts[(i+1)%verts.length];
            Vector2 next = verts[(i+2)%verts.length];
            Vector2 diff = Vector2.Sub(next,prev);
            Vector2 orth = Vector2.Mul(new Vector2(-diff.y,diff.x).normalized(),0.01f); //think about this.

            outline_verts[i*2+0] = Vector2.Add(current, orth);
            outline_verts[i*2+1] = Vector2.Sub(current, orth);

            outline_tris[i*6+0] =(short)(i*2+0);
            outline_tris[i*6+1] =(short)(i*2+1);
            outline_tris[i*6+2] =(short)((i*2+2)%outline_verts.length);

            outline_tris[i*6+3] =(short)(i*2+1);
            outline_tris[i*6+4] =(short)((i*2+2)%outline_verts.length);
            outline_tris[i*6+5] =(short)((i*2+3)%outline_verts.length);
        }

        outline_mesh = new Mesh(outline_tris,outline_verts,new float[]{0,0,0,1});


        //finding out the most prominent winding order
        float wind_ack = 0;
        for(int i = 0; i<verts.length;i++)
        {
            Vector2 cur = verts[i];
            Vector2 next = verts[(i+1) % verts.length];
            wind_ack +=(next.x-cur.x)*(next.y+cur.y);
        }
        float winding = Math.signum(wind_ack);

        //setup the remaining vertex indices
        short[] tris = new short[(verts.length-2)*3];
        int current_index = 0;
        List<Integer> remainingIndices = new LinkedList<>();
        for(int i = 0; i<verts.length;i++)
        {
            remainingIndices.add(i);
        }

        //triangulation by earclipping
        while(remainingIndices.size() >= 3)
        {
            boolean removed = false;
            for(int i = 0;i<remainingIndices.size();i++)
            {
                int index_a = remainingIndices.get(i);
                int index_b = remainingIndices.get((i+1) % remainingIndices.size());
                int index_c = remainingIndices.get((i+2) % remainingIndices.size());

                Vector2 a = verts[index_a];
                Vector2 b = verts[index_b];
                Vector2 c = verts[index_c];

                //only add the tri if it's inside the polygon
                if(Math.signum(Util.crossProduct(a, b, c))!=winding)
                {
                    //check if there is any other vertex inside our proposed triangle
                    boolean noneInside = true;
                    for(int j = 0; j<verts.length;j++)
                    {
                        if(j == index_a || j == index_b || j == index_c)continue;
                        if (Util.isInsideTri(verts[j], a, b, c))
                        {
                            noneInside = false;
                            break;
                        }
                    }

                    if(noneInside)
                    {
                        //add the triangle and remove the middle vertex from further consideration
                        tris[current_index++] = (short) index_a;
                        tris[current_index++] = (short) index_b;
                        tris[current_index++] = (short) index_c;

                        remainingIndices.remove((i+1) % remainingIndices.size());
                        removed = true;
                        if(remainingIndices.size() == 2) break;
                    }
                }
            }
            if (!removed)
            {
                System.out.println("not all tris was drawn.. is it self intersecting or is the precision set very high?");
                break;
            }
        }

        float[] color = {(float)Math.random(),(float)Math.random(),(float)Math.random(),1f};
        fill_mesh = new Mesh(tris, verts, color);
    }

    public void draw(float[] projectionMatrix){
        float[] mvpMatrix = new float[16];
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, modelMatrix, 0);
        fill_mesh.draw(projectionMatrix);
        outline_mesh.draw(projectionMatrix);
    }
}
