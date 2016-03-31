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
    public float x;
    public float y;

    float magnitude()
    {
        return (float) Math.sqrt(x*x+y*y);
    }

    Vector2 normalized()
    {
        float magnitude = this.magnitude();
        return new Vector2(this.x/magnitude(),this.y/magnitude());
    }
}
