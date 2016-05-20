package com.sonsofhesslow.games.risk.graphics.Geometry;

/**
 * Created by daniel on 3/31/16.
 */

public class Vector2
{
    public static Vector2 lerp(Vector2 start, Vector2 end, float t)
    {
        return Add(Mul(start, 1 - t), Mul(end, t));
    }

    public static boolean isInsideTri(Vector2 pt, Vector2 v1, Vector2 v2, Vector2 v3)
    {
        boolean b1, b2, b3;

        b1 = crossProduct(pt, v1, v2) < 0.0f;
        b2 = crossProduct(pt, v2, v3) < 0.0f;
        b3 = crossProduct(pt, v3, v1) < 0.0f;
        return ((b1 == b2) && (b2 == b3) && crossProduct(v1, v2, v3)!=0);
    }

    //the cross product (With the z-component obviously set to 0) between the vectors rel-b and rel-c
    public static float crossProduct(Vector2 rel, Vector2 b, Vector2 c)
    {
        return ((b.x - rel.x)*(c.y - rel.y) - (b.y - rel.y)*(c.x - rel.x));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector2 vector2 = (Vector2) o;

        return Float.compare(vector2.x, x) == 0 && Float.compare(vector2.y, y) == 0;

    }

    @Override
    public int hashCode() {
        int result = (x != +0.0f ? Float.floatToIntBits(x) : 0);
        result = 31 * result + (y != +0.0f ? Float.floatToIntBits(y) : 0);
        return result;
    }

    public Vector2(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

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
    public static boolean AlmostEqual(Vector2 a, Vector2 b)
    {
        return Vector2.Sub(a,b).magnitude()<0.01f;
    }

    public String toString()
    {
        return "("+x+", "+y+")";
    }
    public final float x;
    public final float y;

    public float magnitude()
    {
        return (float) Math.sqrt(x*x+y*y);
    }

    public Vector2 normalized() {
        return new Vector2(this.x/magnitude(),this.y/magnitude());
    }
    public float projectFactor(Vector2 u)
    {
        return dot(u, this) / (dot(u,u));
    }
    public Vector2 projectOnto(Vector2 u)
    {
        return Mul(u, dot(u, this) / (dot(u,u)));
    }
    public static Vector2 Zero()
    {
        return new Vector2(0,0);
    }

}
