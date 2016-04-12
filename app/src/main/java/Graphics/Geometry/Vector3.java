package Graphics.Geometry;

/**
 * Created by Daniel on 09/04/2016.
 */
public class Vector3 {
    public float x,y,z;
    public Vector3(float x, float y, float z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public Vector3(Vector2 vector2)
    {
        this(vector2.x,vector2.y,0);
    }

    @Override
    public String toString() {
        return "Vector3{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }

    public static Vector3 Add(Vector3 a, Vector3 b)
    {
        return new Vector3(a.x+b.x,a.y+b.y,a.z+b.z);
    }

    public static Vector3 Sub(Vector3 a, Vector3 b)
    {
        return new Vector3(a.x-b.x,a.y-b.y,a.z-b.z);
    }

    public static Vector3 Mul(Vector3 vec, float scalar)
    {
        return new Vector3(vec.x*scalar,vec.y*scalar,vec.z*scalar);
    }
    public Vector2 ToVector2()
    {
        return new Vector2(x,y);
    }
}