package com.example.niklas.projectsonsofhesslow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Scanner;
import java.util.Stack;
import java.util.regex.Pattern;

import gl_own.FilledBeizierPath;
import gl_own.Geometry.Util;
import gl_own.Geometry.Vector2;

/**
 * Created by daniel on 4/1/16.
 */
public class SvgReader {
    public static FilledBeizierPath read(InputStream svgStream,float[] matrix) throws IOException
    {
        Scanner s = new Scanner(svgStream);
        String currentToken = "";
        while(s.hasNext() )
        {
            //just advance;
            currentToken = s.next();
            if(currentToken.equals("<path")) break;
            System.out.println(currentToken);
        }
        // abc q"as4,52 -> [abc],[q],[as4],[,],[52]
        s.useDelimiter("(?<=,)|(?=,)|(\\s+)|(\")");

        //parsing
        Vector2 pos = new Vector2(0,0);
        boolean done = false;

        Stack<Vector2> points = new Stack<>();
        //todo support lineto maybe and handle bad formatting better.

        while(!done)
        {
            System.out.println(currentToken);
            if("z".equals(currentToken) || "Z".equals(currentToken))
                break;
            while(s.hasNextFloat() && !done)
            {
                System.out.println("*");
                switch (currentToken)
                {
                    case "M": {
                        pos = nextVector2(s);
                    } break;
                    case "m":{
                        pos = Util.Add(pos,nextVector2(s));
                    } break;
                    case "Z":
                        System.out.println("end");
                        done = true;
                        break;
                    case "z":
                        System.out.println("end");
                        done = true;
                        break;
                    case "C":
                        System.out.println("C");
                        if(points.empty())
                            points.push(pos);
                        points.push(nextVector2(s));
                        points.push(nextVector2(s));
                        points.push(nextVector2(s));
                        break;
                    case "c":
                        System.out.println("c");
                        if(points.empty())
                            points.push(pos);
                        points.push(Util.Add(pos, nextVector2(s)));
                        points.push(Util.Add(pos, nextVector2(s)));
                        points.push(Util.Add(pos, nextVector2(s)));
                        break;
                    default:
                        System.out.println((Arrays.toString(points.toArray(new Vector2[points.size()]))));
                        throw new RuntimeException("fuck your format :\'"+ currentToken+"\'");
                }

            }
            if(s.hasNext()){
                currentToken = s.next();
                System.out.println("new token");
            }
            else {
                System.out.println("trying to exit, how hard can it fucking be?");
                done = true;
                break;
            }
        }
        points.pop();

        Vector2[] v = points.toArray(new Vector2[points.size()]);
        for(int i = 0; i<v.length;i++)
        {
            v[i]=Util.Mul(v[i], 1f / 800f);
        }
        System.out.println(Arrays.toString(v));
        return new FilledBeizierPath(v,matrix);
    }

    private static Vector2 nextVector2(Scanner s)
    {
        System.out.println("next vector..");
        float x = s.nextFloat();
        String string = s.next();
        if(!",".equals(string))throw new IllegalArgumentException("Expected two floats separated with a comma. Got:"+string);
        float y = s.nextFloat();

        return new Vector2(x,y);
    }
}
