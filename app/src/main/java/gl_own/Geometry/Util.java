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
}
