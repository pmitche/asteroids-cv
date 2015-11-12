package com.ece290.mobileimagingasteroids.android;

import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Kyrre on 06.03.2015.
 */
public class GestureDetector {
    private static LinkedList<LinkedList<Point>> fingerCache;
    static int count = 0;
    private static boolean firstTime = true;

    public static void detect(ArrayList<Point> fingerTips, Point centroid, Mat mRgba) {
        if (firstTime){
            init();
            firstTime = false;
        }
        for (Point p: fingerTips){
            Core.circle(mRgba, p, 10, new Scalar(150, 50, 255));
            Core.line(mRgba, p, centroid, new Scalar(150, 50, 50),10);
        }

        if (fingerTips.size() == 5){
            Point middleFinger = findMiddleFinger(fingerTips,centroid);
            if (middleFinger != null){
                Point thumb = findThumb(centroid, middleFinger, fingerTips);
                if (thumb != null){
                    Point indexFinger = findIndexFinger(middleFinger, thumb, fingerTips);
                    Point ringFinger = findRingFinger(middleFinger,thumb, indexFinger, fingerTips);
                    Point littleFinger = findLittleFinger(middleFinger,thumb, indexFinger, ringFinger, fingerTips);
                    cacheFingers(middleFinger,thumb,indexFinger,ringFinger,littleFinger);
                }
            }
        }else {
            //TODO: cache stuff goes here
        }
        count++;
    }

    private static void init() {
        fingerCache = new LinkedList<>();
        for (int i = 0; i < 5; i++) {
            fingerCache.add(new LinkedList<Point>());
        }
    }

    private static void cacheFingers(Point middleFinger, Point thumb, Point indexFinger, Point ringFinger, Point littleFinger) {
        fingerCache.get(0).add(middleFinger);
        fingerCache.get(1).add(thumb);
        fingerCache.get(2).add(indexFinger);
        fingerCache.get(3).add(ringFinger);
        fingerCache.get(4).add(littleFinger);
        for (LinkedList<Point> l: fingerCache){
            if (l.size() > 10){
                l.poll();
            }
        }
    }

    private static Point findLittleFinger(Point middleFinger, Point thumb, Point indexFinger, Point ringFinger, ArrayList<Point> fingerTips) {
        for (Point p: fingerTips){
            if (!p.equals(thumb) && !p.equals(middleFinger) && !p.equals(indexFinger) && !p.equals(ringFinger)){
                return p;
            }
        }
        return null;
    }

    private static Point findRingFinger(Point middleFinger, Point thumb, Point indexFinger, ArrayList<Point> fingerTips) {
        double distance = Double.MAX_VALUE;
        Point ringFinger = null;
        for (Point p: fingerTips){
            if (!p.equals(thumb) && !p.equals(middleFinger) && !p.equals(indexFinger)){
                if (distance(p, middleFinger) < distance){
                    ringFinger = p;
                    distance = distance(p, middleFinger);
                }
            }
        }
        return ringFinger;
    }

    private static Point findIndexFinger(Point middleFinger, Point thumb, ArrayList<Point> fingerTips) {
        double distance = Double.MAX_VALUE;
        Point indexFinger = null;
        for (Point p : fingerTips){
            if (!p.equals(thumb) && !p.equals(middleFinger)){
                double temp = distance(middleFinger, p)+distance(thumb,p);
                if (temp < distance){
                    distance = temp;
                    indexFinger = p;
                }
            }
        }
        return indexFinger;
    }

    private static Point findThumb(Point centroid, Point middleFinger, ArrayList<Point> fingerTips) {
        double x = (centroid.x - middleFinger.x);
        double y = (centroid.y - middleFinger.y);
        double mag = Math.sqrt(x * x + y * y);
        x = x/mag;
        y = y/mag;
        double temp = x;
        x = -y;
        y = temp;
        Point p0 = new Point(centroid.x +(200*x),centroid.y+(200*y));
        Point p1 = new Point(centroid.x-(200*x),centroid.y-(200*y));
        MatOfPoint2f thumbFinder = new MatOfPoint2f(p0, p1);
        double distance = -100000;
        Point thumb = null;
        for (Point p: fingerTips){
            double tmp = Imgproc.pointPolygonTest(thumbFinder, p, true);
            if (distance < tmp){
                distance = tmp;
                thumb = p;
            }
        }
        return thumb;
    }

    public static double distance(Point p0, Point p1){
        return Math.hypot((Math.abs(p0.x - p1.x)), (Math.abs(p0.y - p1.y)));
    }

    private static Point findMiddleFinger(ArrayList<Point> fingerTips, Point centroid) {
        if (fingerTips.size() != 5){
            Log.d("GestureDetector", "These are not the fingers your looking for");
            return null;
        }
        double length = 0;
        Point middleFinger = null;
        for (Point p: fingerTips){
            Log.d("Angle","----------------------------------");
            int sum = 0;
            for (Point pp: fingerTips){
                if (!p.equals(pp)){
                    if (angle(centroid, p, pp)>0){
                        sum++;
                    }else{
                        sum--;
                    }
                    Log.d("Angle","angle: "+angle(centroid, p, pp));
                }
            }
            if (sum == 0){
                return p;
            }
            Log.d("Angle","----------------------------------");
        }
        return null;
    }

    private static double angle(Point centroid, Point p0, Point p1){
        double x0 = p0.x - centroid.x;
        double y0 = p0.y - centroid.y;
        double x1 = p1.x - centroid.x;
        double y1 = p1.y - centroid.y;
        return Math.atan2(x0, y0)-Math.atan2(x1, y1);
    }
}
