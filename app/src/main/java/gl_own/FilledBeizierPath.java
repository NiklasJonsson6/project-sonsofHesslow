package gl_own;

import java.util.LinkedList;
import java.util.List;

import gl_own.Geometry.Util;
import gl_own.Geometry.Vector2;

/**
 * Created by daniel on 3/31/16.
 */
public class FilledBeizierPath {

    public Mesh m;

    final float precision = 15;


    //todo real precision
    public FilledBeizierPath(Vector2[] points,float[] matrix) //alternating. start ctl ctl point ctl ctl point ctl ctl (start)
    {
        if(points.length%3 != 0) throw new IllegalArgumentException("not a quadratic beizier curve");

        Vector2[] verts = new Vector2[(int)precision*(points.length)/3];
        for(int j = 0;j<points.length/3;j++)
        {
            Vector2 start =     points[j*3];
            Vector2 control_1 = points[j*3+1];
            Vector2 control_2 = points[j*3+2];
            Vector2 end =       points[(j*3+3) % points.length];

            for(int i = 0;i<precision;i++)
            {
                Vector2[] beizPoints = new Vector2[]{start,control_1,control_2,end};
                verts[j*(int)precision+i] = beiz(beizPoints, i / (precision));
            }
        }

        float[] color = {0.2f,0.7f,0.9f,1f};
        short[] tris = new short[(verts.length-1)*3];
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
            System.out.println("top while");
            for(int i = 0;i<remainingIndices.size();i++)
            {
                System.out.println("top for");

                int index_a = remainingIndices.get(i);
                int index_b = remainingIndices.get((i+1) % remainingIndices.size());
                int index_c = remainingIndices.get((i+2) % remainingIndices.size());

                Vector2 a = verts[index_a];
                Vector2 b = verts[index_b];
                Vector2 c = verts[index_c];

                Vector2 ab = Util.Sub(b,a);
                Vector2 bc = Util.Sub(c, b);
                double a_ab = Math.atan2(ab.y, ab.x);
                a_ab = a_ab > 0 ? a_ab : a_ab +Math.PI*2;

                double a_bc = Math.atan2(bc.y, bc.x);
                a_bc = a_bc > 0 ? a_bc : a_bc +Math.PI*2;


                if(a_ab - a_bc > Math.PI)
                {
                    System.out.println("is convex");
                    boolean noneInside = true;
                    for(int j = 0; j<verts.length;j++)
                    {
                        if (Util.isInsideTri(verts[j], a, b, c))
                        {
                            noneInside = false;
                            break;
                        }
                    }

                    if(noneInside)
                    {
                        System.out.println("none inside");
                        tris[current_index++] = (short) index_a;
                        tris[current_index++] = (short) index_b;
                        tris[current_index++] = (short) index_c;

                        remainingIndices.remove((i+1) % remainingIndices.size());
                        removed = true;
                        if(remainingIndices.size() == 2) break;
                    }
                    else
                    {
                        System.out.println("point inside");
                    }
                }
            }
            if (!removed)
                break;
        }

        /*

        for(int i = 0; i< verts.length-1;i++)
        {
            if(i%2==0)
            {
                tris[i*3+0] = ((short)i);
                tris[i*3+1] = ((short)(i+1));
                tris[i*3+2] = (short)(verts.length-1-i);
            }
            else
            {
                tris[i*3+0] = ((short)i);
                tris[i*3+1] = (short)(verts.length-1-i);
                tris[i*3+2] = (short)(verts.length-2-i);
            }
        }
         */
        m = new Mesh(tris, verts, color, matrix);
    }

    public Vector2 beiz(Vector2[] vectors, float t)
    {
        Vector2[] next_vectors = new Vector2[vectors.length-1];
        for(int i = 0; i<vectors.length-1;i++)
        {
            next_vectors[i] = interpolate(vectors[i],vectors[i+1],t);
        }
        if(next_vectors.length == 1) return next_vectors[0];
        else return beiz(next_vectors,t);
    }

    public Vector2 interpolate(Vector2 start, Vector2 end, float t)
    {
        return Util.Add(Util.Mul(start, t), Util.Mul(end, 1-t));
    }

    public void draw()
    {
        m.draw();
    }
}
