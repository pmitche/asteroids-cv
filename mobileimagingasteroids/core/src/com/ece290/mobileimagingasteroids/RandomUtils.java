package com.ece290.mobileimagingasteroids;

/**
 * Created by ethan_000 on 3/6/2015.
 */
public class RandomUtils {
    public static int randomWithRange(int min, int max)
    {
        int range = (max - min) + 1;
        return (int)(Math.random() * range) + min;
    }
}
