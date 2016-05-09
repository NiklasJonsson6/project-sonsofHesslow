package com.sonsofhesslow.games.risk;

import java.lang.reflect.Array;

public class ArrayUtils {

    public static <T> T[] concat(T[]... arrays) {
        int len_ack = 0;
        for (int i = 0; i < arrays.length; i++) {
            len_ack += arrays[i].length;
        }

        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(arrays[0].getClass().getComponentType(), len_ack);

        int elemenet_ack = 0;
        for (int i = 0; i < arrays.length; i++) {
            System.arraycopy(arrays[i], 0, ret, elemenet_ack, arrays[i].length);
            elemenet_ack += arrays[i].length;
        }

        return ret;
    }

    public static <T> boolean contains(T[] a, T[] b) {
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (a[i].equals(b[j])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static <T> boolean contains(T[] a, T b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == b) {
                return true;
            }
        }
        return false;
    }


    public static <T> T[] reverse(T[] arr) {
        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(arr.getClass().getComponentType(), arr.length);
        for (int i = 0; i < arr.length; i++) {
            ret[i] = arr[arr.length - i - 1];
        }
        return ret;
    }
}