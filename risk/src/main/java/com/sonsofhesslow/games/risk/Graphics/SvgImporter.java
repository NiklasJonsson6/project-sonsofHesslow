package com.sonsofhesslow.games.risk.graphics;

import android.util.Pair;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.sonsofhesslow.games.risk.graphics.geometry.Bezier;
import com.sonsofhesslow.games.risk.graphics.geometry.BezierPath;
import com.sonsofhesslow.games.risk.graphics.geometry.Vector2;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.DashedBezierLine;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.FilledBezierPath;
import com.sonsofhesslow.games.risk.graphics.graphicsObjects.Renderer;

/**
 * Created by daniel on 4/1/16.
 */
public class SvgImporter {

    // it appears as though the java scanner cannot properly handle documents with long lines.
    // parsing floats that reach past some arbitrary limit of x characters breaks them up.
    // this is not acceptable beaviour.

    public static class SVG_ReturnValue
    {

        public SVG_ReturnValue(FilledBezierPath path, Integer continent_id, Integer region_id,  Set<Integer> neighbors) {
            this.path = path;
            this.continent_id = continent_id;
            this.neighbors = neighbors;
            this.region_id = region_id;
        }

        public final FilledBezierPath path;
        public Integer continent_id;
        public Integer region_id;
        public Set<Integer> neighbors;
    }

    public static List<SVG_ReturnValue> read(InputStream svgStream,Renderer renderer) throws IOException
    {
        List<BezierPath> paths = new ArrayList<>();
        List<BezierPath> splits = new ArrayList<>();
        List<BezierPath> connections = new ArrayList<>();
        List<BezierPath> regionConnections= new ArrayList<>();
        List<BezierPath> continentConnections= new ArrayList<>();

        SvgReader sr = new SvgReader(svgStream);
        //parse all paths in the svg. add them into the appropriate category.
        while(true) {
            SVGPath new_read = sr.readPath();
            if(new_read != null) {
                BezierPath readBeiz = new_read.path;

                for(int i = 0; i< readBeiz.points.length;i++) {
                    readBeiz.points[i]= new Vector2(-readBeiz.points[i].x/250,-readBeiz.points[i].y/250);
                }

                if(readBeiz.isClosed()) {
                    paths.add(readBeiz);
                }
                else {
                    if(new_read.isDashed)
                        connections.add(readBeiz);
                    else if(new_read.isContinent)continentConnections.add(readBeiz);
                    else if(new_read.isRegion)regionConnections.add(readBeiz);
                    else splits.add(readBeiz);
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
        List<Pair<BezierPath, Integer>> paths_with_info = new ArrayList<>(paths.size());

        int continentId = 0;
        for(BezierPath b : paths){
            paths_with_info.add(new Pair<>(b, continentId++));
        }

        List<Vector2> splitPoints = new ArrayList<>();
        while(splits.size()>0)
        {
            boolean removed = false;
            for(int i = 0;i<splits.size();i++)
            {
                BezierPath split = splits.get(i);
                int pathLen = paths_with_info.size();
                for(int j = 0;j<pathLen;j++)
                {
                    Pair<BezierPath,Integer> path_with_info = paths_with_info.get(j);
                    BezierPath.splitReturn new_paths = BezierPath.splitBeizPath(path_with_info.first, split);
                    if(new_paths != null)
                    {
                        paths_with_info.remove(j);
                        --j;
                        --pathLen;
                        paths_with_info.add(new Pair<>(new_paths.first, path_with_info.second));
                        paths_with_info.add(new Pair<>(new_paths.second, path_with_info.second));
                        removed = true;
                        splitPoints.add(new_paths.secondSplitPoint);
                        splitPoints.add(new_paths.firstSplitPoint);
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
        int c = 0;
        for(Pair<BezierPath,Integer> p : paths_with_info) {
            // using a hashset here might be slow...
            ret.add(new SVG_ReturnValue(new FilledBezierPath(p.first,renderer),p.second, c, new HashSet<Integer>()));
            ++c;
        }

        //could still be sped up a bunch. probably should be as well. we'll see...
        for(Vector2 point : splitPoints) {
            List<Integer> neighbors = new ArrayList<>();
            int i = 0;
            for(SVG_ReturnValue val : ret)
            {
                for(Bezier b : val.path.path)
                {
                    if(b.isOnCurve(point,0.1f))
                    {
                        neighbors.add(i);
                        break;
                    }
                }
                ++i;
            }
            for(Integer index : neighbors) {
                ret.get(index).neighbors.addAll(neighbors);
            }
        }
        int i;
        for(BezierPath conn:connections)
        {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length-1];

            new DashedBezierLine(conn,renderer); //this should probably not be done from here..
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
            else{
                System.out.println("NULL CONNECTION FAILUER... :/ "+start + "," + end);
            }
        }

        // hadling the region fusion
        for(BezierPath conn:regionConnections)
        {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length-1];
            SVG_ReturnValue first_val = null;
            SVG_ReturnValue second_val = null;

            i = 0;
            for(SVG_ReturnValue val : ret) {
                if(val.path.fill_mesh.isOnMesh2D(start)) {first_val = val;}
                if(val.path.fill_mesh.isOnMesh2D(end)) {second_val = val;}
                ++i;
            }

            if(first_val != null&&second_val!=null) {
                //notify that the objects no longer need rendering.
                first_val.path.Remove();
                second_val.path.Remove();
                //merge the objects
                first_val.path.mergeWith(second_val.path);
                //add toghether the neigbors
                first_val.neighbors.addAll(second_val.neighbors);
                ret.remove(second_val);

                //notify that the modified shape needs to be drawn (and initialized);
                renderer.delayedInit(first_val.path);
                //handle the removed neigbor ids
                for(SVG_ReturnValue val : ret)
                {
                    boolean contians = false;
                    for(Integer neig: val.neighbors)
                    {
                        if(((int)neig) == second_val.region_id){
                            contians = true;
                        }
                    }
                    if(contians) {
                        val.neighbors.remove(second_val.region_id);
                        val.neighbors.add(first_val.region_id);
                    }
                }
            }
            else{
                System.out.println("NULL CONNECTION_REGION FAILUER... :/ "+start + "," + end);
            }
        }


        // hadling the continent fusion
        for(BezierPath conn:continentConnections)
        {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length-1];
            int first_continent_id =-1;
            int second_continent_id =-1;

            i = 0;
            for(SVG_ReturnValue val : ret)
            {
                if(val.path.fill_mesh.isOnMesh2D(start)){first_continent_id = val.continent_id;}
                if(val.path.fill_mesh.isOnMesh2D(end)) {second_continent_id = val.continent_id;}
                ++i;
            }
            if(first_continent_id !=-1 && second_continent_id != -1) {
                for(SVG_ReturnValue val : ret) {
                    if(val.continent_id== second_continent_id)val.continent_id = first_continent_id;
                }
            }
            else{
                System.out.println("NULL CONNECTION_REGION FAILUER... :/ "+start + "," + end);
            }
        }


        //set the continent_ids & region_ids to the range 0-(n-1)
        //also make sure nobody has itself as a neighbor.
        List<Integer> region_ids = new ArrayList<>(ret.size());
        TreeSet<Integer> continent_ids = new TreeSet<>();
        for(SVG_ReturnValue val : ret) {
            region_ids.add(val.region_id);
            continent_ids.add(val.continent_id);
        }
        Collections.sort(region_ids);

        i = 0;
        for(SVG_ReturnValue val : ret)
        {
            val.continent_id = continent_ids.headSet(val.continent_id).size();
            val.region_id = region_ids.indexOf(val.region_id);
            Set<Integer> new_neighs = new HashSet<>();
            for(Integer neigh : val.neighbors){
                int new_neigh = region_ids.indexOf(neigh);
                if(new_neigh != i)
                    new_neighs.add(new_neigh);
            }
            val.neighbors = new_neighs;
            ++i;
        }

        return ret;
    }


}




