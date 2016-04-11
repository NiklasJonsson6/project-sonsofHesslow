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



    public static boolean isInsideTri(Vector2 pt, Vector2 v1, Vector2 v2, Vector2 v3)
    {
        boolean b1, b2, b3;

        b1 = crossProduct(pt, v1, v2) < 0.0f;
        b2 = crossProduct(pt, v2, v3) < 0.0f;
        b3 = crossProduct(pt, v3, v1) < 0.0f;

        return ((b1 == b2) && (b2 == b3) && crossProduct(v1,v2,v3)!=0);
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
    public static void printMatrix(float[] matrix)
    {
        for(int y = 0; y<4;y++){
            for(int x = 0; x<4;x++){
                System.out.print(matrix[x+y*4]+" ");
            }
            System.out.print("\n");
        }
    }
}
