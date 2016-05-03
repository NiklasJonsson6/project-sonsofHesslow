package Graphics.Geometry;

import android.util.Pair;

import com.example.niklas.projectsonsofhesslow.ArrayUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Daniel on 06/04/2016.
 */
public class BeizierPath implements Iterable<Beizier> {

    public Vector2[] points;
    public BeizierPath(Vector2[] points) // on the form start c1 c2 p c1 c2 (explicit end)(or implicit start)
    {
        for(int i = 0; i<points.length;i++)
        {
            if(points[i] == null) throw new IllegalArgumentException("beizier path cannot have null elements...");
        }
        this.points = points;
    }

    public boolean isClosed()
    {
        return points.length%3 == 0;
    }

    @Override
    public Iterator<Beizier> iterator() {
        return new BeizIterator(this);
    }

    public class BeizIterator implements Iterator<Beizier>
    {
        BeizierPath path;
        public int index = 0;
        Beizier b;
        public BeizIterator(BeizierPath path)
        {
            b = new Beizier(new Vector2[4]);
            this.path = path;
        }

        @Override
        public boolean hasNext() {
            return index < path.points.length / 3;
        }

        @Override
        public Beizier next() {
            // initializing all these beziers takes a whole bunch of time..
            // come up with a way not to? passing around arrays isn't exactly oop.
            // but it would speed things a bunch...

            Beizier ret = new Beizier(path.points[index*3],path.points[index*3+1],
                    path.points[index*3+2],path.points[(index*3+3)%path.points.length]);
            ++index;
            return ret;
        }

        @Override
        public void remove() {
            throw new RuntimeException("remove is not implemented");
        }
    }


    /*
    //somehow we're fucking up the ordering here.. I think... or we just have floating point errors above. Idk.
    //anyway sticking with the naive method for now.
    private static Vector2[] approximateBeizierPath(Vector2[] path, float precision)
    {
        List<Vector2> ret = new ArrayList<>();

        for(int i = 0; i<path.length-2;i+=3)
        {
            Vector2[] beiz = new Vector2[]{path[i],path[i+1],path[i+2],path[(i+3)%path.length]};
            ret.addAll(approximateBeizier(beiz, precision));
        }
        return ret.toArray(new Vector2[ret.size()]);
    }

    //slow af with all these list ideally we should pass around the same list.
    private static List<Vector2> approximateBeizier(Vector2[] beiz, float precision)
    {
        Vector2[][] split = Util.beizSplit(beiz, 0.5f);
        float a = Util.crossProduct(split[0][0], split[1][0], split[1][3]);
        List<Vector2> ret = new ArrayList<>();
        if(Math.abs(a)<precision)
        {
            for(int i = 0; i< beiz.length-1;i++)
            {
                ret.add(beiz[i]);
            }
            return ret;
        }
        else
        {
            ret.addAll(approximateBeizier(split[0],precision));
            ret.addAll(approximateBeizier(split[1],precision));
            System.out.println(ret.size());
            return ret;
        }
    }
    */
    public Vector2[] approximateBeizierPath_naive(int precision)
    {
        Vector2[] verts = new Vector2[precision*((points.length)/3)];
        int counter = 0;
        for(Beizier beiz : this)
        {
            for(int i = 0;i<precision;i++)
            {
                verts[counter*precision+i] = beiz.getValue(i/(float)precision);
            }
            ++counter;
        }
        return verts;
    }

    private Vector2[] subdivide(Vector2[] path)
    {
        Vector2[] ret = new Vector2[path.length*2];
        int i = 0;
        for(Beizier beizier: this)
        {
            Beizier[] divided = beizier.split(0.5f);
            ret[i*2]   = divided[0].points[0];
            ret[i*2+1] = divided[0].points[1];
            ret[i*2+2] = divided[0].points[2];

            ret[i*2+3] = divided[1].points[0];
            ret[i*2+4] = divided[1].points[1];
            ret[i*2+5] = divided[1].points[2];
            ++i;
        }
        return ret;
    }

    public static boolean isNeighbour(BeizierPath a, BeizierPath b)
    {
        for(Beizier beizier_a : a)
        {
            for(Beizier beizier_b : b)
            {
                if(Beizier.IsPartOf(beizier_a, beizier_b, 10))
                    return true;
            }
        }
        return false;
    }

    public static class splitReturn
    {
        public BeizierPath first;
        public BeizierPath second;
        public Vector2 firstSplitPoint;
        public Vector2 secondSplitPoint;
    }
    public static splitReturn splitBeizPath(BeizierPath path, BeizierPath line)
    {
        List<Pair<Integer,Float>> path_splits = new ArrayList<>();
        List<Pair<Integer,Float>> line_splits= new ArrayList<>();

        List<Pair<Float,Float>> intersectionPoints = new ArrayList<>();

        int i = 0;
        for(Beizier lineBeiz:line)
        {
            int j = 0;
            for(Beizier pathBeiz:path)
            {
                if(Beizier.Intersect(lineBeiz,pathBeiz,0.001f,intersectionPoints))
                {
                    // currently only one intersection per beizier is supported.
                    // however intersection points may return multiple values that are all within the
                    // tolerance of the one intersection point. Thats why we're not currently throwing any exceptions.
                    //and instead just gets the first and ignores the rest.
                    line_splits.add(new Pair<Integer, Float>(i,intersectionPoints.get(0).first));
                    path_splits.add(new Pair<Integer, Float>(j, intersectionPoints.get(0).second));
                }
                intersectionPoints.clear();
                ++j;
            }
            ++i;
        }

        if(line_splits.size() != 2 || path_splits.size() != 2)
        {
            return null;
        }

        BeizierPath[] split_path = splitBeizPath(path,path_splits);
        BeizierPath[] split_line = splitBeizPath(line,line_splits);

        BeizierPathBuilder b = new BeizierPathBuilder();
        b.addBeizPath(split_path[2]);
        if(!b.fitAndAddBeizPath(split_path[0])) {
            throw new RuntimeException("1: Bug in  split Beizier Path...");
        }
        if(!b.fitAndAddBeizPath(split_line[1]))        {
            throw new RuntimeException("2: Bug in  split Beizier Path...");
        }

        splitReturn ret = new splitReturn();
        ret.first = b.get(true);
        b.clear();
        b.addBeizPath(split_line[1]);
        b.fitAndAddBeizPath(split_path[1]);
        ret.second = b.get(true);
        ret.firstSplitPoint = split_line[1].points[0];
        ret.secondSplitPoint= split_line[1].points[split_line[1].points.length-1];
        return ret;
    }

    public BeizierPath reverse()
    {
        points = ArrayUtils.reverse(points);
        return this;
    }

    public static BeizierPath[] splitBeizPath(BeizierPath beizPath, List<Pair<Integer,Float>> poses)
    {

        Collections.sort(poses, new Comparator<Pair<Integer, Float>>() {
            @Override
            public int compare(Pair<Integer, Float> lhs, Pair<Integer, Float> rhs) {
                return (int)Math.signum(lhs.first-rhs.first);
            }
        });

        //does not yet support one beiz beeing split multiple times...
        List<BeizierPath> tmp = new ArrayList<>();
        int current_index = 0;
        int i = 0;
        BeizierPathBuilder builder = new BeizierPathBuilder();

        for(Beizier currentBeiz : beizPath)
        {

            if(current_index<poses.size()&&poses.get(current_index).first <= i)
            {
                Beizier[] split = currentBeiz.split(poses.get(current_index).second);
                builder.addBeiz(split[0]);
                tmp.add(builder.get(false));
                builder.clear();
                builder.addBeiz(split[1]);
                ++current_index;
            }
            else
            {
                builder.addBeiz(currentBeiz);
            }
            ++i;
        }

        tmp.add(builder.get(false));
        return tmp.toArray(new BeizierPath[tmp.size()]);
    }

}
