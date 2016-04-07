package gl_own.Geometry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Daniel on 07/04/2016.
 */

public class BeizierPathBuilder
{
    List<Beizier> beiziers = new ArrayList<>();

    public boolean addBeiz(Beizier beizier)
    {
        if(beiziers.isEmpty() || Vector2.Sub(beizier.points[0],(beiziers.get(beiziers.size()-1).points[3])).magnitude()<0.01f)
        {
            beiziers.add(beizier);
            return true;
        }
        else
        {
            throw new RuntimeException(beizier.points[0] + " " + beiziers.get(beiziers.size()-1).points[3]);
        }
    }
    public boolean addBeizPath(BeizierPath beizPath)
    {
        for (Beizier b:beizPath)
        {
            if(!addBeiz(b))throw new RuntimeException("fuck me");
            //probably backtrack as well or something...
        }
        return true;
    }
    public void clear()
    {
        beiziers.clear();
    }

    public BeizierPath get(boolean close)
    {
        if(close)
        {
            if(Vector2.Sub(beiziers.get(0).points[0],(beiziers.get(beiziers.size()-1).points[3])).magnitude()>0.01f)
            {
                for(Beizier b: beiziers)
                {
                    System.out.println(Arrays.deepToString(b.points));
                }
                throw new RuntimeException("fucked up beiz yoo!");
            }
            else
            {
                Vector2[] points = new Vector2[beiziers.size()*3];
                int i = 0;
                for(Beizier b : beiziers)
                {
                    points[i++] =b.points[0];
                    points[i++] =b.points[1];
                    points[i++] =b.points[2];
                }
                return new BeizierPath(points);
            }
        }
        else
        {
            Vector2[] points = new Vector2[beiziers.size()*3+1];
            int i = 0;
            for(Beizier b : beiziers)
            {
                points[i++] =b.points[0];
                points[i++] =b.points[1];
                points[i++] =b.points[2];
            }
            Vector2 endPoint = beiziers.get(beiziers.size()-1).points[3];

            points[i++] = endPoint;

            return new BeizierPath(points);
        }
    }
}
