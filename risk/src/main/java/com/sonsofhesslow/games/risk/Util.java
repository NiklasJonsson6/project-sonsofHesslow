package com.sonsofhesslow.games.risk;

/**
 * Created by fredr on 2016-05-08.
 */
public class Util {
    public static int getIntFromColor(float Red, float Green, float Blue){
        int R = Math.round(255 * Red);
        int G = Math.round(255 * Green);
        int B = Math.round(255 * Blue);

        R = (R << 16) & 0x00FF0000;
        G = (G << 8) & 0x0000FF00;
        B = B & 0x000000FF;

        return 0xFF000000 | R | G | B;
    }
    public static int getIntFromColor(float[] c){
        return getIntFromColor(c[0], c[1], c[2]);
    }
}
