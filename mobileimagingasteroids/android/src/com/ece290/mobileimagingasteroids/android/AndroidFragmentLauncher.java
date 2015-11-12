package com.ece290.mobileimagingasteroids.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.ece290.mobileimagingasteroids.android.Fragment.GameFragment;

import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


public class AndroidFragmentLauncher extends FragmentActivity implements AndroidFragmentApplication.Callbacks, CvCameraViewListener2, View.OnTouchListener {
    private static final String  TAG              = "AndroidFragmentLauncher";

    private JavaCameraView mOpenCvCameraView;
    private boolean mIsColorSelected = false;
    private Mat                  mRgba;
    private Scalar               mBlobColorRgba;
    private Scalar               mBlobColorHsv;
    private ColorBlobDetector    mDetector;
    private Mat                  mSpectrum;
    private Size SPECTRUM_SIZE;
    private Scalar CONTOUR_COLOR;
    private Point centroid;
    private int counter = 0;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    //mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(AndroidFragmentLauncher.this);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.android_fragment_launcher);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.java_camera_view);
        //mOpenCvCameraView.setMaxFrameSize(1000, 1000);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mDetector = new ColorBlobDetector();
        mSpectrum = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
        SPECTRUM_SIZE = new Size(200, 64);
        CONTOUR_COLOR = new Scalar(255,0,0,255);

    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        return findContour(event.getX(), event.getY());
    }

    private boolean findContour(float xCord, float yCord){
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)xCord - xOffset;
        int y = (int)yCord - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        touchedRect.x = (x>4) ? x-4 : 0;
        touchedRect.y = (y>4) ? y-4 : 0;

        touchedRect.width = (x+4 < cols) ? x + 4 - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+4 < rows) ? y + 4 - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;

        mBlobColorRgba = converScalarHsv2Rgba(mBlobColorHsv);

        Log.i(TAG, "Touched rgba color: (" + mBlobColorRgba.val[0] + ", " + mBlobColorRgba.val[1] +
                ", " + mBlobColorRgba.val[2] + ", " + mBlobColorRgba.val[3] + ")");

        mDetector.setHsvColor(mBlobColorHsv);

        Imgproc.resize(mDetector.getSpectrum(), mSpectrum, SPECTRUM_SIZE);

        mIsColorSelected = true;

        touchedRegionRgba.release();
        touchedRegionHsv.release();

        return false; // don't need subsequent touch events
    }
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (centroid != null && counter > 10){
            //findContour((float)centroid.x, (float)centroid.y);
            counter = 0;
        }
        mRgba = inputFrame.rgba();
        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();


            if (!contours.isEmpty()) {
                MatOfPoint handContour = findBiggestContour(contours);
                Point[] contourPts = handContour.toArray();
                MatOfInt convexHullMatOfInt = new MatOfInt();
                MatOfInt4 convexityDefects = new MatOfInt4();
                Imgproc.convexHull(handContour, convexHullMatOfInt);
                Imgproc.convexityDefects(handContour, convexHullMatOfInt, convexityDefects);
                List<Integer> convexityDefectsList = convexityDefects.toList();

                //<Point, isStartpoint()>
                HashMap<Point, Boolean> fingerPoints = new HashMap<Point, Boolean>();
                ArrayList<Point> fingerTipCandidats = new ArrayList<Point>();

                for (int i = 2; i < convexityDefectsList.size()-1; i+=4) {
                    if (convexityDefectsList.get(i+1) > 100) {

                        double x0 = contourPts[convexityDefectsList.get(i - 2)].x - contourPts[convexityDefectsList.get(i)].x;
                        double y0 = contourPts[convexityDefectsList.get(i - 2)].y - contourPts[convexityDefectsList.get(i)].y;
                        double x1 = contourPts[convexityDefectsList.get(i - 1)].x - contourPts[convexityDefectsList.get(i)].x;
                        double y1 = contourPts[convexityDefectsList.get(i - 1)].y - contourPts[convexityDefectsList.get(i)].y;

                        double angle = Math.atan2(x0, y0)-Math.atan2(x1, y1);

                        if (Math.abs(angle) < 1.8){
                            fingerPoints.put(contourPts[convexityDefectsList.get(i - 2)], true);
                            fingerPoints.put(contourPts[convexityDefectsList.get(i - 1)], false);
                        }
                    }
                }
                HashSet<Point> done = new HashSet<>();
                for (Map.Entry<Point, Boolean> entry0: fingerPoints.entrySet()){
                    for (Map.Entry<Point, Boolean> entry1: fingerPoints.entrySet()){
                        if (!done.contains(entry0.getKey()) && !done.contains(entry1.getKey()) && !entry0.getKey().equals(entry1.getKey())){
                            double diff = Math.hypot((Math.abs(entry0.getKey().x - entry1.getKey().x)), (Math.abs(entry0.getKey().y - entry1.getKey().y)));
                            if (diff < 60 && entry0.getValue() != entry1.getValue()){
                                //fingerTipCandidats.add(new Point((entry0.getKey().x + entry1.getKey().x) / 2, (entry0.getKey().y + entry1.getKey().y) / 2));
                                if (!entry0.getValue()){
                                    fingerTipCandidats.add(new Point((entry0.getKey().x), (entry0.getKey().y)));
                                } else {
                                    fingerTipCandidats.add(new Point((entry1.getKey().x), (entry1.getKey().y)));
                                }
                                done.add(entry0.getKey());
                                done.add(entry1.getKey());
                            }
                        }
                    }
                }
                for (Point p: fingerPoints.keySet()){
                    if (!done.contains(p)){
                        fingerTipCandidats.add(p);
                    }
                }

                // Convert Point arrays into MatOfPoint
                MatOfPoint convexHullMatOfPoints = matOfIntToMatOfPoint(convexHullMatOfInt, handContour);
                centroid = centerOfMass(convexHullMatOfPoints);

                ArrayList<Point> fingerTips = new ArrayList<>();
                //TODO: Draw for debug
                for (Point p: fingerTipCandidats){
                    //if (p.x < centroid.x){
                        fingerTips.add(p);
                    //}
                }

                GestureDetector.detect(fingerTips,centroid,mRgba);
                Core.circle(mRgba, centroid, 10, new Scalar(0, 0, 255));
                List<MatOfPoint> hax = new ArrayList<MatOfPoint>();
                hax.add(convexHullMatOfPoints);
                Imgproc.drawContours(mRgba, hax, 0, new Scalar(0, 255, 0));
            }
            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);

            GameFragment gameFragment = (GameFragment)getSupportFragmentManager().findFragmentById(R.id.game_fragment);
            //gameFragment.onRotationUpdate(37);
            counter++;
        }

        return mRgba;
    }

    private Point centerOfMass(MatOfPoint convexHull){
        Point centroid = new Point();
        Moments moments = Imgproc.moments(convexHull);
        centroid.x = moments.get_m10() / moments.get_m00();
        centroid.y = moments.get_m01() / moments.get_m00();
        return centroid;
    }

    private MatOfPoint matOfIntToMatOfPoint(MatOfInt convexHullMatOfInt, MatOfPoint contour){
        Point[] convexHullPoints = new Point[convexHullMatOfInt.rows()];
        for(int j=0; j < convexHullMatOfInt.rows(); j++){
            int index = (int)convexHullMatOfInt.get(j, 0)[0];
            convexHullPoints[j] = new Point(contour.get(index, 0)[0], contour.get(index, 0)[1]);
        }

        // Convert Point arrays into MatOfPoint
        MatOfPoint convexHullMatOfPoints = new MatOfPoint();
        convexHullMatOfPoints.fromArray(convexHullPoints);
        return convexHullMatOfPoints;
    }


    private MatOfPoint findBiggestContour(List<MatOfPoint> contours) {
        double biggest = 0;
        MatOfPoint current = null;
        for (MatOfPoint contour : contours) {
            double size = Imgproc.contourArea(contour);
            if(size > biggest){
                biggest = size;
                current = contour;
            }
        }
        return current;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void exit() {}

    private Scalar converScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        return new Scalar(pointMatRgba.get(0, 0));
    }


    private double calcAreaTriangle(Point a, Point b, Point c)
    {
        return Math.abs(   (a.x*(b.y-c.y) + b.x*(c.y-a.y) + c.x*(a.y-b.y) )/2.0  );
    }

}
