package Graphics;

import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import Graphics.Geometry.BeizierPath;
import Graphics.Geometry.Vector2;
import Graphics.GraphicsObjects.DashedBeizierLine;
import Graphics.GraphicsObjects.FilledBeizierPath;
import Graphics.SvgReader;

/**
 * Created by daniel on 4/1/16.
 */
public class SvgImporter {

    // it appears as though the java scanner cannot properly handle documents with long lines.
    // parsing floats that reach past some arbitrary limit of x characters breaks them up.
    // this is not acceptable beaviour.

    public static class SVG_ReturnValue
    {

        public SVG_ReturnValue(FilledBeizierPath path, Integer continent_id, Set<Integer> neighbors) {
            this.path = path;
            this.continent_id = continent_id;
            this.neighbors = neighbors;
        }

        public FilledBeizierPath path;
        public Integer continent_id;
        public Set<Integer> neighbors;
    }

    public static List<SVG_ReturnValue> read(InputStream svgStream) throws IOException
    {
        List<BeizierPath> paths = new ArrayList<>();
        List<BeizierPath> splits = new ArrayList<>();
        List<BeizierPath> connections = new ArrayList<>();

        SvgReader sr = new SvgReader(svgStream);
        //parse all paths in the svg. add them into the appropriate category.
        int counter = 0;
        while(true) {
            ReadRet new_read = sr.readPath();
            if(new_read != null) {
                BeizierPath readBeiz = new_read.path;

                for(int i = 0; i< readBeiz.points.length;i++) {
                    readBeiz.points[i]= new Vector2(-readBeiz.points[i].x/250,-readBeiz.points[i].y/250);
                }

                if(readBeiz.isClosed()) {
                    paths.add(readBeiz);
                }
                else {
                    if(!new_read.isDashed)
                        splits.add(readBeiz);
                    else connections.add(readBeiz);
                }
            }
            else
            {
                break;
            }
        }
        System.out.println("number of connections: " + connections.size());

        //split the paths with the splits.
        //keep track of which split split what
        List<Pair<BeizierPath, Integer>> paths_with_info = new ArrayList<>(paths.size());

        int continentId = 0;
        for(BeizierPath b : paths){
            paths_with_info.add(new Pair<>(b, continentId++));
        }

        List<Vector2> splitPoints = new ArrayList<>();
        while(splits.size()>0)
        {
            boolean removed = false;
            for(int i = 0;i<splits.size();i++)
            {
                BeizierPath split = splits.get(i);
                int pathLen = paths_with_info.size();
                for(int j = 0;j<pathLen;j++)
                {
                    Pair<BeizierPath,Integer> path_with_info = paths_with_info.get(j);
                    BeizierPath.splitReturn new_paths = BeizierPath.splitBeizPath(path_with_info.first, split);
                    if(new_paths != null)
                    {
                        paths_with_info.remove(j);
                        --j;
                        --pathLen;
                        paths_with_info.add(new Pair<>(new_paths.first, path_with_info.second));
                        paths_with_info.add(new Pair<>(new_paths.second, path_with_info.second));
                        removed = true;
                        splitPoints.add(new_paths.firstSplitPoint);
                        splitPoints.add(new_paths.secondSplitPoint);
                    }
                }
                if(removed)
                {
                    splits.remove(i);
                    --i;
                    break;
                }
            }
            if(!removed) {
                break;
            }
        }

        List<SVG_ReturnValue> ret = new ArrayList<>(paths_with_info.size());
        for(Pair<BeizierPath,Integer> p : paths_with_info) {
            // using a hashset here might be slow...
            ret.add(new SVG_ReturnValue(new FilledBeizierPath(p.first),p.second, new HashSet<Integer>()));
        }

        //could still be sped up a bunch. probably should be as well. we'll see...
        for(Vector2 point : splitPoints) {
            List<Integer> neighbors = new ArrayList<>();
            int i = 0;
            for(SVG_ReturnValue val : ret) {
                for(Vector2 q : val.path.path.points) {
                    if(Vector2.AlmostEqual(point,q))
                    {
                        neighbors.add(i);
                    }
                }
                ++i;
            }
            for(Integer index : neighbors) {
                ret.get(index).neighbors.addAll(neighbors);
            }
        }
        int i = 0;
        for(SVG_ReturnValue val : ret) {
            val.neighbors.remove(i);
            ++i;
        }


        for(BeizierPath conn:connections)
        {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length-1];

            new DashedBeizierLine(conn); //this should probably not be done from here..
            SVG_ReturnValue first_val = null;
            int first_index = -1;

            SVG_ReturnValue second_val = null;
            int second_index = -1;

            i = 0;
            for(SVG_ReturnValue val : ret)
            {
                if(val.path.fill_mesh.isOnMesh2D(start))
                {
                    first_val = val;
                    first_index = i;
                }

                if(val.path.fill_mesh.isOnMesh2D(end))
                {
                    second_val = val;
                    second_index = i;
                }
                ++i;
            }
            if(first_val != null&&second_val!=null) {
                first_val.neighbors.add(second_index);
                second_val.neighbors.add(first_index);
            }
            else
            {
                System.out.println("NULL CONNECTION FAILUER... :/ "+start + "," + end);
            }
        }
        return ret;
    }

    public static class ReadRet
    {
        public ReadRet(BeizierPath path, boolean isDashed) {
            this.path = path;
            this.isDashed = isDashed;
        }

        BeizierPath path;
        boolean isDashed;
    }
}




