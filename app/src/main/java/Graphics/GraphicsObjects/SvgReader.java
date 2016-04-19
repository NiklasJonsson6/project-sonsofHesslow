package Graphics.GraphicsObjects;

import android.util.Pair;

import com.example.niklas.projectsonsofhesslow.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.ServiceConfigurationError;
import java.util.Stack;

import Graphics.Geometry.Beizier;
import Graphics.Geometry.BeizierPath;
import Graphics.Geometry.BeizierPathBuilder;
import Graphics.Geometry.Vector2;

/**
 * Created by daniel on 4/1/16.
 */
public class SvgReader {
    public static class SVG_ReturnValue
    {

        public SVG_ReturnValue(FilledBeizierPath path, Integer continent_id, Integer[] neighbors) {
            this.path = path;
            this.continent_id = continent_id;
            this.neighbors = neighbors;
        }

        public FilledBeizierPath path;
        public Integer continent_id;
        public Integer[] neighbors;
    }

    //todo support lineto maybe and handle bad formatting better. also clean me up a bunch
    public static List<SVG_ReturnValue> read(InputStream svgStream) throws IOException
    {
        Scanner s = new Scanner(svgStream);
        s.useLocale(Locale.US);

        List<BeizierPath> paths = new ArrayList<>();
        List<BeizierPath> splits = new ArrayList<>();
        List<BeizierPath> connections = new ArrayList<>();

        //parse all paths in the svg. add them into the appropriate category.
        while(true)
        {

            ReadRet new_read = readPath(s);
            if(new_read != null)
            {
                BeizierPath readBeiz = new_read.path;

                for(int i = 0; i< readBeiz.points.length;i++)
                {
                    readBeiz.points[i]= Vector2.Mul(readBeiz.points[i],1 / -100f);
                }

                if(readBeiz.isClosed())
                {
                    paths.add(readBeiz);
                }
                else
                {
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
        System.out.println("number of connections: "+connections.size() );

        //split the paths with the splits.
        //keep track of which split split what
        List<Pair<BeizierPath, Integer>> paths_with_info = new ArrayList<>(paths.size());

        int continentId = 0;
        for(BeizierPath b : paths)
        {
            paths_with_info.add(new Pair<>(b, continentId++));
        }

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
                    BeizierPath[] new_paths = BeizierPath.splitBeizPath(path_with_info.first, split);
                    Integer new_info = path_with_info.second;
                    if(new_paths != null)
                    {
                        paths_with_info.remove(j);
                        --j;
                        --pathLen;
                        paths_with_info.add(new Pair<>(new_paths[0],new_info));
                        paths_with_info.add(new Pair<>(new_paths[1], new_info));
                        removed = true;
                    }
                }
                if(removed)
                {
                    splits.remove(i);
                    --i;
                    break;
                }
            }
            if(!removed)
            {
                break;
            }
        }

        List<SVG_ReturnValue> ret = new ArrayList<>(paths_with_info.size());
        for(int i = 0; i< paths_with_info.size();i++)
        {
            List<Integer> neigbours = new ArrayList<>();
            for(int j = 0; j< paths_with_info.size();j++)
            {
                if(i == j)continue;
                if(BeizierPath.isNeigbour(paths_with_info.get(i).first, paths_with_info.get(j).first)) {
                    neigbours.add(j);
                }
            }
            Pair<BeizierPath,Integer> p = paths_with_info.get(i);

            ret.add(new SVG_ReturnValue(new FilledBeizierPath(p.first),p.second,neigbours.toArray(new Integer[neigbours.size()])));
        }

        for(BeizierPath conn:connections)
        {
            Vector2 start = conn.points[0];
            Vector2 end = conn.points[conn.points.length-1];

            new DashedBeizierLine(conn); //this should not be done from here. ONLY TEMP.
            SVG_ReturnValue first_val = null;
            int first_index = -1;

            SVG_ReturnValue second_val = null;
            int second_index = -1;

            int i = 0;
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
            first_val.neighbors = ArrayUtils.concat( first_val.neighbors , new Integer[]{second_index});
            second_val.neighbors = ArrayUtils.concat( second_val.neighbors , new Integer[]{first_index});
        }
        //check so that all neighbors are unique?

        return ret;
    }

    static class ReadRet
    {
        public ReadRet(BeizierPath path, boolean isDashed) {
            this.path = path;
            this.isDashed = isDashed;
        }

        BeizierPath path;
        boolean isDashed;
    }

    private static ReadRet readPath(Scanner s)
    {
        s.useDelimiter("(?<=,)|(?=,)|(\\s+)|(?<=\")|(?=\")|(?<=;)|(?=;)"); //crashes on if we escape on : investigate...
        String currentToken = "";
        advanceTo(s, "<path");
        boolean isDashed = attributeContains(s,"stroke-dasharray:6");
        // abc q"as4,52 -> [abc],[q],[as4],[,],[52]

        advanceTo(s, "d=");
        if(s.hasNext())
            s.next(); // the " char after in d="

        BeizierPathBuilder b = new BeizierPathBuilder();
        //parsing
        Vector2 pos = new Vector2(0,0);
        Stack<Vector2> points = new Stack<>();
        boolean done = false;
        while(!done)
        {
            if(s.hasNext()){
                currentToken = s.next();
            }
            else {
                return null;
            }
            boolean relative = true;
            switch (currentToken)
            {
                case "M": {
                    pos = nextVector2(s, Vector2.Zero());
                } break;
                case "m":{
                    pos = nextVector2(s, pos);
                } break;
                case "Z":
                case "z":
                    return new ReadRet(b.get(true),false);
                case "C":
                    do
                    {
                        Vector2 start = pos;
                        Vector2 c1 =nextVector2(s, Vector2.Zero());
                        Vector2 c2 =nextVector2(s, Vector2.Zero());
                        pos = nextVector2(s, Vector2.Zero());
                        b.addBeiz(new Beizier(start,c1,c2,pos));
                    } while(s.hasNextFloat());

                    break;
                case "c":
                    do
                    {
                        Vector2 start = pos;
                        Vector2 c1 =nextVector2(s,pos);
                        Vector2 c2 =nextVector2(s, pos);
                        pos = nextVector2(s,pos);
                        b.addBeiz(new Beizier(start,c1,c2,pos));
                    } while(s.hasNextFloat());
                    break;
                case "\"":
                    done = true;
                    break;
                default:
                    System.out.println((Arrays.toString(points.toArray(new Vector2[points.size()]))));
                    throw new RuntimeException("fuck your format :\'"+ currentToken+"\'");
            }
        }
        return new ReadRet(b.get(false), isDashed);
    }

    private static boolean attributeContains(Scanner s, String token)
    {
        advanceTo(s,"\"");
        String currentToken="";
        while(s.hasNext() )
        {
            currentToken = s.next();
            System.out.println("current token::"+currentToken);
            if(currentToken.equals(token)) return true;
            if(currentToken.equals("\"")) break;
        }
        return false;
    }

    private static boolean advanceTo(Scanner s, String token)
    {
        while(s.hasNext() )
        {
            //just advance;
            if(s.next().equals(token)) return true;
        }
        return false;
    }

    private static Vector2 nextVector2(Scanner s,Vector2 relativeTo)
    {
        if(!s.hasNextFloat())
            throw new IllegalArgumentException("1: Expected a floats got: '" + s.next()+"'");

        float x = s.nextFloat();

        String string = s.next();
        if(!",".equals(string))throw new IllegalArgumentException("2: Expected two floats separated with a comma. Got:"+string);
        if(!s.hasNextFloat())
            throw new IllegalArgumentException("3: Expected a floats got: '" + s.next()+"'");

        float y =  s.nextFloat();
        return Vector2.Add(new Vector2(x, y), relativeTo);
    }
}
