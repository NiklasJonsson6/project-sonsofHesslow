package com.example.niklas.projectsonsofhesslow;

import android.util.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Stack;

import gl_own.FilledBeizierPath;
import gl_own.Geometry.Beizier;
import gl_own.Geometry.BeizierPath;
import gl_own.Geometry.BeizierPathBuilder;
import gl_own.Geometry.Vector2;

/**
 * Created by daniel on 4/1/16.
 */
public class SvgReader {
    //todo support lineto maybe and handle bad formatting better.
    public static List<Pair<FilledBeizierPath,Integer[]>> read(InputStream svgStream) throws IOException
    {
        Scanner s = new Scanner(svgStream);
        s.useLocale(Locale.US);

        List<BeizierPath> paths = new ArrayList<>();
        List<BeizierPath> splits = new ArrayList<>();

        //parse all paths in the svg. add them into the appropriate category.
        while(true)
        {
            BeizierPath new_read = readPath(s);
            if(new_read != null)
            {
                for(int i = 0; i<new_read.points.length;i++)
                {
                    new_read.points[i]= Vector2.Mul(new_read.points[i],1 / -100f);
                }

                if(new_read.isClosed())
                {
                    paths.add(new_read);
                }
                else
                {
                    splits.add(new_read);
                }
            }
            else
            {
                break;
            }
        }

        //split the paths with the splits.
        //keep track of which split split what
        List<Pair<BeizierPath, Integer[]>> paths_with_info = new ArrayList<>(paths.size());
        for(BeizierPath b : paths)
            paths_with_info.add(new Pair<>(b, new Integer[0]));

        int c = 0;
        while(splits.size()>0)
        {
            boolean removed = false;
            for(int i = 0;i<splits.size();i++)
            {
                BeizierPath split = splits.get(i);
                int pathLen = paths_with_info.size();
                for(int j = 0;j<pathLen;j++)
                {
                    Pair<BeizierPath,Integer[]> path_with_info = paths_with_info.get(j);
                    BeizierPath[] new_paths = BeizierPath.splitBeizPath(path_with_info.first,split);
                    Integer[] new_info = ArrayUtils.concat(path_with_info.second,new Integer[]{c});
                    if(new_paths != null)
                    {
                        paths_with_info.remove(j);
                        --j;
                        --pathLen;
                        paths_with_info.add(new Pair<>(new_paths[0],new_info));
                        paths_with_info.add(new Pair<>(new_paths[1],new_info));
                        removed = true;
                    }
                }
                if(removed)
                {
                    ++c;
                    splits.remove(i);
                    --i;
                    break;
                }
            }
            if(!removed)
            {
                System.out.println("none removed failed...");
                break;
            }
        }

        List<Pair<FilledBeizierPath,Integer[]>> ret = new ArrayList<>(paths_with_info.size());
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
            Pair<BeizierPath,Integer[]> p = paths_with_info.get(i);

            ret.add(new Pair<>(new FilledBeizierPath(p.first),neigbours.toArray(new Integer[neigbours.size()])));
        }
        return ret;
    }

    private static BeizierPath readPath(Scanner s)
    {
        s.useDelimiter("(?<=,)|(?=,)|(\\s+)|(?<=\")|(?=\")");

        String currentToken = "";
        advanceTo(s,"<path");
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
                System.out.println("new token: " + currentToken);
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
                    return b.get(true);
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
        return b.get(false);
    }

    private static boolean advanceTo(Scanner s, String token)
    {
        String currentToken="";
        while(s.hasNext() )
        {
            //just advance;
            if(s.next().equals(token)) return true;
        }
        return false;
    }

    private static Vector2 nextVector2(Scanner s,Vector2 relativeTo)
    {
        System.out.println("next vector..");
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
