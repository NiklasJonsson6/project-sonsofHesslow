package com.example.niklas.projectsonsofhesslow;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
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
    public static FilledBeizierPath[] read(InputStream svgStream,float[] matrix) throws IOException
    {
        Scanner s = new Scanner(svgStream);
        s.useLocale(Locale.US);

        BeizierPath path = readPath(s);
        BeizierPath line = readPath(s);

        for(int i = 0; i<path.points.length;i++)
        {
            path.points[i]= Vector2.Mul(path.points[i],1 / -1000f);
        }

        for(int i = 0; i<line.points.length;i++)
        {
            line.points[i]= Vector2.Mul(line.points[i],1 / -1000f);
        }

        if(!path.isClosed())throw new RuntimeException("first should be closed");
        if(line.isClosed())throw new RuntimeException("second shouldn't be closed");

        System.out.println(Arrays.toString(path.points));
        System.out.println(Arrays.toString(line.points));
        BeizierPath[] paths = BeizierPath.splitBeizPath(path,line);

        return  new FilledBeizierPath[]
        {
            new FilledBeizierPath(paths[0],matrix),
            new FilledBeizierPath(paths[1],matrix),
        };
    }

    private static BeizierPath readPath(Scanner s)
    {
        s.useDelimiter("(?<=,)|(?=,)|(\\s+)|(?<=\")|(?=\")");

        String currentToken = "";
        advanceTo(s,"<path", true);
        // abc q"as4,52 -> [abc],[q],[as4],[,],[52]

        advanceTo(s, "d=", true);
        if(!s.next().equals("\""))
        {
            throw new RuntimeException("bad format dude");
        }

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
                throw  new RuntimeException("bad format");
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
                    Vector2 end = pos;
                    do
                    {
                        Vector2 start = end;
                        Vector2 c1 =nextVector2(s, Vector2.Zero());
                        Vector2 c2 =nextVector2(s, Vector2.Zero());
                        end =nextVector2(s, Vector2.Zero());
                        b.addBeiz(new Beizier(start,c1,c2,end));
                    } while(s.hasNextFloat());
                    pos = end;
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

    private static void advanceTo(Scanner s, String token, boolean print)
    {
        String currentToken="";
        while(s.hasNext() )
        {
            //just advance;
            currentToken = s.next();
            if(currentToken.equals(token)) break;
            if(print) System.out.println(currentToken);
        }
        if(print) System.out.println("found: "+ currentToken);
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
