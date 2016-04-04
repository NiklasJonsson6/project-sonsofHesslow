package gl_own.Geometry;

/**
 * Created by daniel on 3/31/16.
 */
public class Util {

    public static float Angle(Vector2 a, Vector2 b)
    {
        return dot(a, b)/(a.magnitude()*b.magnitude());
    }

    public static float dot(Vector2 a, Vector2 b)
    {
        return a.x *b.x + a.y * b.y;
    }
    public static Vector2 Add(Vector2 a, Vector2 b)
    {
        return new Vector2(a.x+b.x,a.y+b.y);
    }
    public static Vector2 Sub(Vector2 a, Vector2 b)
    {
        return new Vector2(a.x-b.x,a.y-b.y);
    }
    public static Vector2 Mul(Vector2 vec, float scalar)
    {
        return new Vector2(vec.x*scalar,vec.y*scalar);
    }

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
}
