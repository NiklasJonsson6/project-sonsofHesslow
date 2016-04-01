package gl_own.Geometry;

/**
 * Created by daniel on 3/31/16.
 */

public class Vector2
{
    public Vector2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }
    public String toString()
    {
        return "("+x+", "+y+")";
    }
    public float x;
    public float y;

    public float magnitude()
    {
        return (float) Math.sqrt(x*x+y*y);
    }

    public Vector2 normalized()
    {
        float magnitude = this.magnitude();
        return new Vector2(this.x/magnitude(),this.y/magnitude());
    }
    public Vector2 projectOnto(Vector2 u)
    { //probably
        return Util.Mul(u,Util.dot(u,this) / (u.x * u.x+u.y*u.y));
    }
}
