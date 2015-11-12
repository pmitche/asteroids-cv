package com.ece290.mobileimagingasteroids.android.Fragment;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.input.GestureDetector;
import com.ece290.mobileimagingasteroids.GameWorld;
import com.ece290.mobileimagingasteroids.MobileImagingAsteroids;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;
import com.ece290.mobileimagingasteroids.controls.TouchGestureListener;

import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * Created by ethan_000 on 2/14/2015.
 */
public class GameFragment extends AndroidFragmentApplication
{
    private MobileImagingAsteroids mia;
    private GameWorld gw;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useWakelock = true;
        config.useAccelerometer = false;
        config.useCompass = false;
        mia = new MobileImagingAsteroids();
        return initializeForView(mia, config);
    }

    public void onRotationUpdate(double rotationUpdate, double speed, boolean shoot)
    {
        if(gw==null)
        {
            gw = mia.getGameWorld();
        }
        gw.setShipRotationUpdate(rotationUpdate);
        gw.setShoot(shoot);
        gw.setShipSpeed(speed);
        //Log.i("GameFragment", "rotationUpdate:"+rotationUpdate);
    }



}
