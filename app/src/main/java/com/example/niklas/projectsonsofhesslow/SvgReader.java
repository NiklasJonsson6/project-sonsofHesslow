package com.example.niklas.projectsonsofhesslow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
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
    //todo support lineto maybe and handle bad formatting better.
    public static FilledBeizierPath read(InputStream svgStream,float[] matrix) throws IOException
    {
        Scanner s = new Scanner(svgStream);
        s.useLocale(Locale.US);

        String currentToken = "";
        advanceTo(s,"<path", false);
        // abc q"as4,52 -> [abc],[q],[as4],[,],[52]
        s.useDelimiter("(?<=,)|(?=,)|(\\s+)|(\")");

        advanceTo(s, "d=", false);

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
                    done = true;
                    break;
                case "C":
                    relative = false;
                case "c":
                    Vector2 rel = relative ? pos : Vector2.Zero();
                    if(points.empty())
                        points.push(pos);
                    while(s.hasNextFloat())
                    {
                        points.push(nextVector2(s, rel));
                        points.push(nextVector2(s, rel));
                        points.push(nextVector2(s, rel));
                    }
                    pos = points.peek();
                    break;
                default:
                    System.out.println((Arrays.toString(points.toArray(new Vector2[points.size()]))));
                    throw new RuntimeException("fuck your format :\'"+ currentToken+"\'");
            }
        }

        points.pop();

        Vector2[] v = points.toArray(new Vector2[points.size()]);
        for(int i = 0; i<v.length;i++)
        {
            v[i]=Util.Mul(v[i], 1f / 1000f);
        }
        System.out.println(Arrays.toString(v));
        return new FilledBeizierPath(v,matrix);
    }
    private static void advanceTo(Scanner s, String token, boolean print)
    {
        while(s.hasNext() )
        {
            //just advance;
            String currentToken = s.next();
            if(currentToken.equals(token)) break;
            if(print) System.out.println(currentToken);
        }
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
        return Util.Add(new Vector2(x,y),relativeTo);
    }
}
