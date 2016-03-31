package gl_own;

import gl_own.Geometry.Util;
import gl_own.Geometry.Vector2;

/**
 * Created by daniel on 3/31/16.
 */
public class FilledBeizierPath {

    Mesh m;

    final float precision = 20;


    //todo real precision

    public FilledBeizierPath(Vector2[] points) //alternating. start ctl ctl point ctl ctl point ctl ctl (start)
    {
        if(points.length%3 != 0) throw new IllegalArgumentException("not a quadratic beizier curve");

        Vector2[] segmented = new Vector2[20*(points.length)/3];
        for(int j = 0;j<points.length/3;j++)
        {
            Vector2 start =     points[j*3];
            Vector2 control_1 = points[j*3+1];
            Vector2 control_2 = points[j*3+2];
            Vector2 end =       points[(j*3+3) % points.length];

            for(int i = 0;i<precision;i++)
            {
                segmented[j*20+i] = beiz_4(start,control_1,control_2,end,i/(precision));
            }
        }

        float[] color = {0.2f,0.7f,0.9f,1f};

        short[] tris = new short[(segmented.length-2)*3];
        for(int i = 0; i< segmented.length-2;i++)
        {
            if(i%2==0)
            {
                tris[i*3+0] = ((short)i);
                tris[i*3+1] = ((short)(i+1));
                tris[i*3+2] = (short)(segmented.length-1-i);
            }
            else
            {
                tris[i*3+0] = ((short)i);
                tris[i*3+2] = (short)(segmented.length-2-i);
                tris[i*3+1] = (short)(segmented.length-1-i);
            }
        }
        m = new Mesh(tris, segmented, color);
    }
    public Vector2 beiz_4(Vector2 start, Vector2 control_1, Vector2 control_2, Vector2 end, float t)
    {
        System.out.println(t);
        return beiz_3(interpolate(start, control_1, t), interpolate(control_1, control_2, t), interpolate(control_2, end, t), t);
    }

    public Vector2 beiz_3(Vector2 start, Vector2 control_1, Vector2 end, float t)
    {
        return interpolate(interpolate(start, control_1, t), interpolate(control_1, end, t), t);
    }

    public Vector2 interpolate(Vector2 start, Vector2 end, float t)
    {
        return Util.Add(Util.Mul(start, t), Util.Mul(end, 1-t));
    }

    public void draw(float[] mvpMatrix)
    {
        m.draw(mvpMatrix);
    }
}
