package com.example.niklas.projectsonsofhesslow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;

import gl_own.FilledBeizierPath;
import gl_own.Geometry.Beizier;
import gl_own.Geometry.BeizierPath;
import gl_own.Geometry.BeizierPathBuilder;
import gl_own.Geometry.Util;
import gl_own.Geometry.Vector2;

/**
 * Created by daniel on 4/1/16.
 */
public class SvgReader {
    //todo support lineto maybe and handle bad formatting better.
    public static FilledBeizierPath[] read(InputStream svgStream) throws IOException
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

        while(splits.size()>0)
        {
            boolean removed = false;
            for(int i = 0;i<splits.size();i++)
            {
                BeizierPath split = splits.get(i);
                int pathLen = paths.size();
                List<Integer> removeIndices = new ArrayList<>();
                for(int j = 0;j<pathLen;j++)
                {
                    BeizierPath path = paths.get(j);
                    BeizierPath[] new_paths = BeizierPath.splitBeizPath(path,split);
                    if(new_paths != null)
                    {
                        paths.remove(j);
                        --j;
                        --pathLen;
                        paths.add(new_paths[0]);
                        paths.add(new_paths[1]);
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
                System.out.println("none removed failed...");
                break;
            }
        }

        FilledBeizierPath[] ret = new FilledBeizierPath[paths.size()];
        for(int i = 0; i< ret.length;i++)
        {
            ret[i] = new FilledBeizierPath(paths.get(i));
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
