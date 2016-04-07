package gl_own.Geometry;

import android.util.Pair;

import java.security.cert.CertPathValidatorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import gl_own.FilledBeizierPath;

/**
 * Created by daniel on 3/31/16.
 */
public class Util {



    // checks if the point p inside the triangle abc
    public static boolean isInsideTri(Vector2 p, Vector2 a, Vector2 b, Vector2 c)
    {
        float s = a.y * c.x - a.x * c.y + (c.y - a.y) * p.x + (a.x - c.x) * p.y;
        float t = a.x * b.y - a.y * b.x + (a.x - b.y) * p.x + (b.x - a.x) * p.y;

        if ((s < 0) != (t < 0))
            return false;

        float A = -b.y * c.x + a.y * (c.x - b.x) + a.x * (b.y - c.y) + b.x * c.y;
        if (A < 0.0)
        {
            s = -s;
            t = -t;
            A = -A;
        }
        if((s > 0 && t > 0 && (s + t) <= A))
        {
            return true;
        }
        return false;
    }
    public static boolean isInsideTri_naive(Vector2 p, Vector2 a, Vector2 b, Vector2 c)
    {
        Vector2 p_mid = Vector2.Mul(Vector2.Add(Vector2.Add(a, b), c), 1f / 3f);

        return  (Math.signum(crossProduct(p_mid,a,b)) == Math.signum(crossProduct(p,a,b)) &&
                (Math.signum(crossProduct(p_mid,a,c)) == Math.signum(crossProduct(p,a,c))) &&
                (Math.signum(crossProduct(p_mid,b,c)) == Math.signum(crossProduct(p,b,c))));
    }

    //the cross product (With the z-component obviously set to 0) between the vectors rel-b and rel-c
    public static float crossProduct(Vector2 rel, Vector2 b, Vector2 c)
    {
        return ((b.x - rel.x)*(c.y - rel.y) - (b.y - rel.y)*(c.x - rel.x));
    }


    public static Vector2 interpolate(Vector2 start, Vector2 end, float t)
    {
        return Vector2.Add(Vector2.Mul(start, 1 - t), Vector2.Mul(end, t));
    }
}
