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
        if(beiziers.isEmpty() || Vector2.AlmostEqual(beizier.points[0], (beiziers.get(beiziers.size() - 1).points[3])))
        {
            beiziers.add(beizier);
            return true;
        }
        else
        {
            throw new RuntimeException(beizier.points[0] + " " + beiziers.get(beiziers.size()-1).points[3]);
        }
    }
    public void addBeizPath(BeizierPath beizPath)
    {
        for (Beizier b:beizPath)
        {
            if(!addBeiz(b))throw new RuntimeException("fuck me");
        }
    }

    public boolean fitAndAddBeizPath(BeizierPath addition)
    {
        if(addition.isClosed()) throw new RuntimeException("self intersecting beizierpaths is not supported");
        Vector2 currentFirst = beiziers.get(0).points[0];
        Vector2 currentLast = beiziers.get(beiziers.size()-1).points[3];
        Vector2 addedFirst = addition.points[0];
        Vector2 addedLast = addition.points[addition.points.length-1];

        if(Vector2.AlmostEqual(currentLast,addedFirst))
        {
            addBeizPath(addition);
            return true;
        }

        if(Vector2.AlmostEqual(currentLast,addedLast))
        {
            addBeizPath(addition.reverse());
            return true;
        }

        if(Vector2.AlmostEqual(currentFirst,addedLast))
        {
            BeizierPath old = get(false);
            clear();
            addBeizPath(addition);
            addBeizPath(old);
            return true;
        }

        if(Vector2.AlmostEqual(currentFirst,addedFirst))
        {
            BeizierPath old = get(false);
            clear();
            addBeizPath(old.reverse());
            addBeizPath(addition);
            return true;
        }
        System.out.println("old "+Arrays.deepToString(get(false).points));
        System.out.println("new "+Arrays.deepToString(addition.points));
        System.out.println("appearently not equal: " +currentFirst + ", " + currentLast + ", " + addedFirst + ", "  + addedLast);
        return false;
    }



    public void clear()
    {
        beiziers.clear();
    }

    public BeizierPath get(boolean close)
    {
        if(close)
        {
            if(!Vector2.AlmostEqual(beiziers.get(0).points[0],(beiziers.get(beiziers.size()-1).points[3])))
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
