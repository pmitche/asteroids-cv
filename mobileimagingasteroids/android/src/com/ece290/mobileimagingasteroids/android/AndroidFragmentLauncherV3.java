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
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;


public class AndroidFragmentLauncherV3 extends FragmentActivity implements AndroidFragmentApplication.Callbacks, CvCameraViewListener2, View.OnTouchListener {
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

    GameFragment gameFragment;

    private double mAngle = 0;
    private double mAngleAlpha = .3;
    private double mSpeed = 0;
    private double mSpeedAlpha = .5;

    private double mapAngle(double angle)
    {
        // middle:0, right:20, left:160
        if(angle < 30 && angle >0)
        {
            return angle/30.0;
        }
        else if(angle > 150 && angle < 180)
        {
            return (angle -180)/30.0;
        }
        return mAngle;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(AndroidFragmentLauncherV3.this);
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
        CONTOUR_COLOR = new Scalar(255,0,255,255);
    }

    public void onCameraViewStopped() {
        mRgba.release();
    }

    public boolean onTouch(View v, MotionEvent event) {
        int cols = mRgba.cols();
        int rows = mRgba.rows();

        int xOffset = (mOpenCvCameraView.getWidth() - cols) / 2;
        int yOffset = (mOpenCvCameraView.getHeight() - rows) / 2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.i(TAG, "Touch image coordinates: (" + x + ", " + y + ")");

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        Rect touchedRect = new Rect();

        int rectSize = 4;

        touchedRect.x = (x>rectSize) ? x-rectSize : 0;
        touchedRect.y = (y>rectSize) ? y-rectSize : 0;

        touchedRect.width = (x+rectSize < cols) ? x + rectSize - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+rectSize < rows) ? y + rectSize - touchedRect.y : rows - touchedRect.y;

        Mat touchedRegionRgba = mRgba.submat(touchedRect);

        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        // Calculate average color of touched region
        mBlobColorHsv = Core.mean(touchedRegionHsv);
        /*mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width*touchedRect.height;
        for (int i = 0; i < mBlobColorHsv.val.length; i++)
            mBlobColorHsv.val[i] /= pointCount;*/

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
        mRgba = inputFrame.rgba();
        if (mIsColorSelected) {
            mDetector.process(mRgba);
            List<MatOfPoint> contours = mDetector.getContours();

            if(gameFragment==null) {
                gameFragment = (GameFragment) getSupportFragmentManager().findFragmentById(R.id.game_fragment);
            }

            if (!contours.isEmpty()) {
                MatOfPoint handContour = findBiggestContour(contours);
                Point[] contourPts = handContour.toArray();
                MatOfInt convexHullMatOfInt = new MatOfInt();
                MatOfInt4 convexityDefects = new MatOfInt4();
                Imgproc.convexHull(handContour, convexHullMatOfInt);
                try {
                    Imgproc.convexityDefects(handContour, convexHullMatOfInt, convexityDefects);
                }catch (Exception e ){
                    return mRgba;
                }
                Point topMost = contourPts[0];
                for(Point p : contourPts)
                {
                    if(p.y < topMost.y)
                    {
                        topMost = p;
                    }
                }
                //Core.circle(mRgba,topMost,20, new Scalar(200,20,20),5);

                List<Integer> convexityDefectsList = convexityDefects.toList();
                // Convert Point arrays into MatOfPoint
                MatOfPoint convexHullMatOfPoints = matOfIntToMatOfPoint(convexHullMatOfInt, handContour);
                Point centroid = centerOfMass(convexHullMatOfPoints);


                MatOfPoint2f contourMat2f = new MatOfPoint2f();
                contourMat2f.fromArray(contourPts);
                //contourMat2f.fromList(convexHullMatOfPoints.toList());

                RotatedRect rotatedRect = null;
                try {
                    rotatedRect = Imgproc.fitEllipse(contourMat2f);
                }
                catch (Exception e){
                    return mRgba;
                }
                Rect rect = Imgproc.boundingRect(handContour);
                Core.rectangle(mRgba, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height), new Scalar(255, 0, 0, 254), 3);

                if(rect.width > rect.height)
                {
                    return mRgba;
                }

                //Core.fillPoly(mRgba,contours, new Scalar(20,200,20));

                //List<Point> enclosingCircle = new ArrayList<Point>();
                int fingerDefects=0;
                boolean thumbDefects=false;
                List<MatOfPoint> triangles= new ArrayList<MatOfPoint>();
                for(int i=0; i<convexityDefectsList.size(); i+=4)
                {
                    List<Point> lp = new ArrayList<Point>();
                    lp.add(contourPts[convexityDefectsList.get(i)]);
                    lp.add(contourPts[convexityDefectsList.get(i+1)]);
                    lp.add(contourPts[convexityDefectsList.get(i+2)]);
                    double area = calcAreaTriangle(contourPts[convexityDefectsList.get(i)],contourPts[convexityDefectsList.get(i+1)],contourPts[convexityDefectsList.get(i+2)]);
                    area = area/rect.area();

                    //if((area > .075) ) {
                    if((area > .075
                    ) ) {
                        thumbDefects = true;
                        MatOfPoint triangle = new MatOfPoint();

                        triangle.fromList(lp);
                        Core.fillConvexPoly(mRgba,triangle, new Scalar(200,20,20));

                        //double angle = Math.atan2(lp.get(0).x,lp.get(0).y) - Math.atan2(lp.get(1).x,lp.get(1).y);
                        //angle = Math.toDegrees(angle);




                    }
                    else if (area > .025)
                    {
                        fingerDefects+=1;
                        MatOfPoint triangle = new MatOfPoint();
                        triangle.fromList(lp);
                        triangles.add(triangle);
                        //Core.fillConvexPoly(mRgba,triangle, new Scalar(20,200,20));
                    }
                    if(fingerDefects>2)
                    {
                        for(MatOfPoint mop : triangles)
                            Core.fillConvexPoly(mRgba, mop,new Scalar(20,200,20));
                    }

                }

                boolean open = false;
                double angle = rotatedRect.angle;
                if(fingerDefects > 2)
                {
                    open = true;
                }

                Core.ellipse(mRgba,rotatedRect, new Scalar(255,127,58),3);


                mAngle = ExponentialMovingAverage.calc(mapAngle(angle),mAngle,mAngleAlpha);
                mSpeed = ExponentialMovingAverage.calc(mSpeed,open ? 0:1,mSpeedAlpha);

                gameFragment.onRotationUpdate(mAngle, mSpeed, thumbDefects);
                 /*System.out.println("rotatedRect Angle:"+rotatedRect.angle); // middle:0, right:20, left:160
                   System.out.println("Angle:"+angle); // middle:0, right:20, left:160
                   System.out.println("mapped Angle:"+mapAngle(angle)); //-1 left
                   System.out.println("weighted:"+mAngle);*/

                /*RotatedRect r2 = Imgproc.minAreaRect(contourMat2f);

                Point[] r2Points = new Point[4];
                r2.points(r2Points);
                List<MatOfPoint> r2List = new ArrayList<MatOfPoint>();
                r2List.add(new MatOfPoint(r2Points));
                Imgproc.drawContours(mRgba, r2List, 0, new Scalar(224, 255, 127));*/

                Core.circle(mRgba, centroid, 10, new Scalar(0, 0, 255));
                List<MatOfPoint> hax = new ArrayList<MatOfPoint>();
                hax.add(convexHullMatOfPoints);
                Imgproc.drawContours(mRgba, hax, 0, new Scalar(0, 255, 0));

                Imgproc.drawContours(mRgba, contours, -1, CONTOUR_COLOR);

            }
            else {
                gameFragment.onRotationUpdate(0, 0, false);
            }

            Mat colorLabel = mRgba.submat(4, 68, 4, 68);
            colorLabel.setTo(mBlobColorRgba);

            Mat spectrumLabel = mRgba.submat(4, 4 + mSpectrum.rows(), 70, 70 + mSpectrum.cols());
            mSpectrum.copyTo(spectrumLabel);



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
