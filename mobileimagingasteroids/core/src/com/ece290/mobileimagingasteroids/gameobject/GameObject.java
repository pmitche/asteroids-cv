package com.ece290.mobileimagingasteroids.gameobject;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

import java.awt.image.BufferedImage;

/**
 * Created by ethan_000 on 2/17/2015.
 */
public abstract class GameObject {
    public Vector2 mPosition = new Vector2();
    public Vector2 mVelocity = new Vector2();
    public Vector2 mAcceleration = new Vector2();
    public Vector2 mRotation = new Vector2();
    public Vector2 mRotationUpdate = new Vector2();
    protected int mWidth, mHeight;
    //protected float mRotation, mRotationUpdate;
    private Polygon mPolygon;


    protected GameObject(int width, int height)
    {
        mWidth = width;
        mHeight = height;
    }

    protected GameObject(int width, int height, float positionX, float positionY)
    {
        this(width, height);
        mPosition = new Vector2(positionX, positionY);
    }

    protected GameObject(int width, int height, float positionX, float positionY, float velocityX, float velocityY)
    {
        this(width, height, positionX, positionY);
        mVelocity = new Vector2(velocityX, velocityY);
    }
    protected GameObject(int width, int height, float positionX, float positionY, float velocityX, float velocityY, float period)
    {
        this(width, height, positionX, positionY, velocityX, velocityY);
    }

    protected GameObject(int width, int height, float positionX, float positionY, float velocityX, float velocityY, float accelerationX, float accelerationY)
    {
        this(width, height, positionX, positionY, velocityX, velocityY);
        mAcceleration = new Vector2(accelerationX, accelerationY);
    }

    public void update(float delta)
    {
        mVelocity.add(mAcceleration.cpy().scl(delta));
        mPosition.add(mVelocity.cpy().scl(delta));
        //mRotation.add(mRotationUpdate.cpy().scl(delta));
        mRotation = mRotationUpdate.cpy().scl(delta);
    }

    protected abstract Polygon getPolygonInternal();

    public final Polygon getPolygon()
    {
        if(null==mPolygon)
        {
            mPolygon = getPolygonInternal();
        }
        if(null!=mPolygon)
        {
            mPolygon.setPosition(mPosition.x, mPosition.y);
            mPolygon.setRotation(mRotation.x);
        }
        return mPolygon;
    }

    public float getX() {
        return mPosition.x;
    }

    public float getY() {
        return mPosition.y;
    }

    public float getVelocityX()
    {
        return mVelocity.x;
    }

    public float getVelocityY()
    {
        return mVelocity.y;
    }

    public float getAccelerationX()
    {
        return mAcceleration.x;
    }

    public float getAccelerationY()
    {
        return mAcceleration.y;
    }

    public void setX(float x) {
        mPosition.x = x;
    }

    public void setY(float y) {
        mPosition.y = y;
    }

    public void setVelocityX(float x)
    {
        mVelocity.x = x;
    }

    public void setVelocityY(float y)
    {
        mVelocity.y = y;
    }

    public void setAccelerationX(float x)
    {
        mAcceleration.x = x;
    }

    public void setAccelerationY(float y)
    {
        mAcceleration.y = y;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public float getRotation() {
        return mRotation.x;
    }

    public void setRotation (float degrees) {
        mRotation.x = degrees;
    }

    /** Applies additional rotation to the polygon by the supplied degrees. */
    public void rotate (float degrees) {
        mRotation.add(degrees,0);
    }

    public void setRotationUpdate(float r)
    {
        mRotationUpdate.x = r;
    }

}

