package com.ece290.mobileimagingasteroids;
import java.lang.Math;
public class NegativeExponentialCalculator {
	
	public static double calculate(double arrivalRate)
	{
		double random = Math.random();//rand 0,1
		return (-1.0/arrivalRate)*Math.log(random);
	}
	
	public static float calculate(float arrivalRate, float min, float max)
    {
    	float d = (float) calculate(arrivalRate);
    	if(max > 0)
    		if(d > max)
    			d=max;
    	if(d < min)
    		d = min;
    	return d;
    }

}
