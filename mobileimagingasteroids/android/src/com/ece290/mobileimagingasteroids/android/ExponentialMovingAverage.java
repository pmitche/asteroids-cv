package com.ece290.mobileimagingasteroids.android;

/**
 * Created by ethan_000 on 3/18/2015.
 */
public class ExponentialMovingAverage {
    static double calc(double val, double old, double alpha)
    {
        if(alpha > 1.0 || alpha < 0)
            throw new IllegalArgumentException();
        return val*alpha + (1-alpha)*old;
    }
}
