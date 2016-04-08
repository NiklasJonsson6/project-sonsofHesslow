package com.example.niklas.projectsonsofhesslow;

import java.lang.reflect.Array;

/**
 * Created by Daniel on 08/04/2016.
 */
public class ArrayUitls {

    public static <T> T[] concat(T[]... arrays)
    {
        int len_ack = 0;
        for(int i = 0; i< arrays.length;i++)
        {
            len_ack+= arrays[i].length;
        }

        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), len_ack);

        int elemenet_ack = 0;
        for(int i = 0; i< arrays.length;i++)
        {
            System.arraycopy(arrays[i], 0, ret, elemenet_ack, arrays[i].length);
            elemenet_ack += arrays[i].length;
        }

        return ret;
    }
    public static <T> T[] reverse(T[] arr)
    {
        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
        for(int i = 0;i<arr.length;i++)
        {
            ret[i] = arr[arr.length-i-1];
        }
        return ret;
    }
}
