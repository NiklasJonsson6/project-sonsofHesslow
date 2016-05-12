package Graphics.Geometry;

import android.util.Pair;

import java.util.List;

import Graphics.utils.MathsUtil;

/**
 * Created by Daniel on 06/04/2016.
 */
public class Beizier
{
    public Beizier(Vector2 start,Vector2 control_1,Vector2 control_2, Vector2 end)
    {
        this(new Vector2[]{start,control_1,control_2,end});
    }
    public Beizier(Vector2[] points)
    {
        this.points = points;
    }
    public Vector2[] points;


    public Beizier[] split(float t)
    {
        if(points.length!=4)throw new IllegalArgumentException("expected quadratic beizier curve!");
        // explicit beiz split using de castillio for quadratic curves
        // could easily be extended for the generic case

        Vector2[] step_0 = points;
        Vector2[] step_1 = de_castillio_step(step_0,t);
        Vector2[] step_2 = de_castillio_step(step_1,t);
        Vector2[] step_3 = de_castillio_step(step_2,t);

        Beizier[] ret = new Beizier[2];
        ret[0] = new Beizier(step_0[0],step_1[0],step_2[0],step_3[0]);
        ret[1] = new Beizier(step_3[0],step_2[1],step_1[2],step_0[3]);
        return ret;
    }

    public Beizier[] split(float[] ts)
    {
        Beizier[] ret = new Beizier[ts.length];

        float t_left = 1;
        Beizier current = this;
        float prev = 0;
        for(int i = 0; i< ts.length;i++)
        {
            Beizier[] split = current.split((ts[i]-prev)/t_left);
            ret[i] = split[0];
            current = split[1];
            t_left -= ts[i]-prev;
            prev = ts[i];
        }
        return ret;
    }



    public static Vector2[] de_castillio_step(Vector2[] vectors, float t)
    {
        Vector2[] next_vectors = new Vector2[vectors.length-1];
        for(int i = 0; i<vectors.length-1;i++)
        {
            next_vectors[i] = MathsUtil.lerp(vectors[i], vectors[i + 1], t);
        }
        return next_vectors;
    }
    public Beizier de_catillio_reduce(float t)
    {
        return new Beizier(de_castillio_step(this.points,t));
    }

    public Vector2 getValue(float t)
    {
        Beizier nextBeiz= de_catillio_reduce(t);
        if(nextBeiz.points.length == 1) return nextBeiz.points[0];
        else return nextBeiz.getValue(t);
    }

    public static boolean Intersect(Beizier a, Beizier b, float tolerance, List<Pair<Float,Float>> _out_tab)
    {
        return Intersect(a, b, tolerance, _out_tab, 0f, 1f, 0f, 1f);
    }

    public static boolean Intersect(Beizier a, Beizier b, float tolerance)
    {
        return Intersect(a, b, tolerance, 0f, 1f, 0f, 1f);
    }

    public static boolean IsPartOf(Beizier a, Beizier b, int resolution)
    {
        float[] ts = new float[resolution];
        for(int i = 0; i<ts.length;i++)
        {
            ts[i] = (i+1)/((float) ts.length+1);
        }

        Beizier[] splits_a = a.split(ts);
        Beizier[] splits_b = b.split(ts);

        for(int i = 0; i<resolution;i++)
        {
            for(int j = 0; j<resolution;j++)
            {
                if(Beizier.Intersect(splits_a[i],splits_b[j], 0.01f))
                {
                    return true;
                }
            }
        }
        return false;

    }

    //todo do real bounding in the intersections. Also probably tolerance by area if we want to be somewhat fancy.
    private static boolean Intersect(Beizier beizier_a, Beizier beizier_b, float tolerance,
                                         List<Pair<Float,Float>> _out_tab, float ta_low, float ta_high,float tb_low, float tb_high)
    {
        Vector2[] a = beizier_a.points;
        Vector2[] b = beizier_b.points;

        //using the beizier subdivision algorithm plus keeping track of the t intersection.

        // currently implementation assumes quadratic beizier curves.
        // the algorithm as such could easily be extended to higher level curves

        float ta_mid = (ta_low + ta_high)/2;
        float tb_mid = (tb_low + tb_high)/2;


        //lazy bounding box.
        float min_ax = a[0].x;
        float max_ax = a[0].x;
        float min_ay = a[0].y;
        float max_ay = a[0].y;

        for(int i = 1; i<a.length;i++)
        {
            if(a[i].x<min_ax)min_ax=a[i].x;
            if(a[i].y<min_ay)min_ay=a[i].y;
            if(a[i].x>max_ax)max_ax=a[i].x;
            if(a[i].y>max_ay)max_ay=a[i].y;
        }

        float min_bx = b[0].x;
        float max_bx = b[0].x;
        float min_by = b[0].y;
        float max_by = b[0].y;

        for(int i = 1; i<a.length;i++)
        {
            if(b[i].x<min_bx)min_bx=b[i].x;
            if(b[i].y<min_by)min_by=b[i].y;
            if(b[i].x>max_bx)max_bx=b[i].x;
            if(b[i].y>max_by)max_by=b[i].y;
        }

        boolean boundIntersectX = min_by < max_ay && max_by > min_ay;
        boolean boundIntersectY = min_bx < max_ax && max_bx > min_ax;

        if(!(boundIntersectX && boundIntersectY))
        {
            return false;
        }

        if(ta_high-ta_low < tolerance){
            _out_tab.add(new Pair<>(ta_mid, tb_mid));
            return true;
        }

        Beizier[] a_split = beizier_a.split(0.5f);
        Beizier a_1 = a_split[0];
        Beizier a_2 = a_split[1];

        Beizier[] b_split = beizier_b.split(0.5f);
        Beizier b_1 = b_split[0];
        Beizier b_2 = b_split[1];

        boolean intersect_11 = Intersect(a_1, b_1, tolerance, _out_tab, ta_low, ta_mid, tb_low, tb_mid);
        boolean intersect_12 = Intersect(a_1, b_2, tolerance, _out_tab, ta_low, ta_mid, tb_mid, tb_high);
        boolean intersect_21 = Intersect(a_2, b_1, tolerance, _out_tab, ta_mid, ta_high, tb_low, tb_mid);
        boolean intersect_22 = Intersect(a_2, b_2, tolerance, _out_tab, ta_mid, ta_high, tb_mid, tb_high);

        return intersect_11 || intersect_12 || intersect_21 || intersect_22;
    }

    public boolean isOnCurve(Vector2 p, float precision)
    {

        float dist = Math.min(Vector2.Sub(p, points[0]).magnitude(),Vector2.Sub(p, points[3]).magnitude());
        if(dist < precision)return true;
        //the control points are a bounding box of the curve.
        if(!MathsUtil.isInsideTri(p, points[0], points[1], points[2])&&
                !MathsUtil.isInsideTri(p, points[3], points[1], points[2]))
        {
            return false;
        }

        Beizier[] r = split(0.5f);
        return r[0].isOnCurve(p,precision) || r[1].isOnCurve(p, precision);
    }

    private static boolean Intersect(Beizier beizier_a, Beizier beizier_b, float tolerance,
                                     float ta_low, float ta_high,float tb_low, float tb_high)
    {
        Vector2[] a = beizier_a.points;
        Vector2[] b = beizier_b.points;

        //using the beizier subdivision algorithm plus keeping track of the t intersection.

        // currently implementation assumes quadratic beizier curves.
        // the algorithm as such could easily be extended to higher level curves

        float ta_mid = (ta_low + ta_high)/2;
        float tb_mid = (tb_low + tb_high)/2;


        //lazy bounding box.
        float min_ax = a[0].x;
        float max_ax = a[0].x;
        float min_ay = a[0].y;
        float max_ay = a[0].y;

        for(int i = 1; i<a.length;i++)
        {
            if(a[i].x<min_ax)min_ax=a[i].x;
            if(a[i].y<min_ay)min_ay=a[i].y;
            if(a[i].x>max_ax)max_ax=a[i].x;
            if(a[i].y>max_ay)max_ay=a[i].y;
        }

        float min_bx = b[0].x;
        float max_bx = b[0].x;
        float min_by = b[0].y;
        float max_by = b[0].y;

        for(int i = 1; i<a.length;i++)
        {
            if(b[i].x<min_bx)min_bx=b[i].x;
            if(b[i].y<min_by)min_by=b[i].y;
            if(b[i].x>max_bx)max_bx=b[i].x;
            if(b[i].y>max_by)max_by=b[i].y;
        }

        boolean boundIntersectX = min_by < max_ay && max_by > min_ay;
        boolean boundIntersectY = min_bx < max_ax && max_bx > min_ax;

        if(!(boundIntersectX && boundIntersectY))
        {
            return false;
        }

        //ignoring the div by two since tolerance is arbitrary anyway.
        float a_area = Math.abs((max_ax-min_ax)* (max_ay-min_ay));
        float b_area = Math.abs((max_bx - min_bx) * (max_by - min_by));

        if(ta_high-ta_low < tolerance){
            return true;
        }

        Beizier[] a_split = beizier_a.split(0.5f);
        Beizier a_1 = a_split[0];
        Beizier a_2 = a_split[1];

        Beizier[] b_split = beizier_b.split(0.5f);
        Beizier b_1 = b_split[0];
        Beizier b_2 = b_split[1];

        return  Intersect(a_1, b_1, tolerance, ta_low, ta_mid, tb_low, tb_mid)||
                Intersect(a_1, b_2, tolerance, ta_low, ta_mid, tb_mid, tb_high) ||
                Intersect(a_2, b_1, tolerance, ta_mid, ta_high, tb_low, tb_mid)||
                Intersect(a_2, b_2, tolerance, ta_mid, ta_high, tb_mid, tb_high);
    }
}
