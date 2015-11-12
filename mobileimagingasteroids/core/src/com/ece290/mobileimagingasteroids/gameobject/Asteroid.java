package com.ece290.mobileimagingasteroids.gameobject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Polygon;
import com.ece290.mobileimagingasteroids.AssetLoader;
import com.ece290.mobileimagingasteroids.RandomUtils;

/**
 * Created by ethan_000 on 3/2/2015.
 */
public class Asteroid extends GameObject {

    public Asteroid(int width, int height, float positionX, float positionY, float velocityX, float velocityY)
    {
        super (width, height, positionX, positionY, velocityX, velocityY);
    }

    public Asteroid(int width, int height)
    {
        super(width, height);
        int positionX=0-width;
        int positionY=0-height;
        if(Math.random()<.5)
        {
            positionX=RandomUtils.randomWithRange(0,Gdx.graphics.getWidth());
            if(Math.random()<.5)
                positionY=Gdx.graphics.getHeight();
        }
        else
        {
            positionY=RandomUtils.randomWithRange(0,Gdx.graphics.getHeight());
            if(Math.random()<.5)
                positionX=Gdx.graphics.getWidth();
        }
        this.setX(positionX);
        this.setY(positionY);

        this.setVelocityX(RandomUtils.randomWithRange(-60,60));
        this.setVelocityY(RandomUtils.randomWithRange(-60,60));

        this.setRotationUpdate(RandomUtils.randomWithRange(-20,20));
    }

    @Override
    protected Polygon getPolygonInternal() {
        //TODO fix polygon for different asteroid shapes

        Polygon p = new Polygon(new float[]{
                (1f/4f)*mWidth,(1f/8f)*mHeight,
                (7f/9f)*mWidth,(1f/6f)*mHeight,
                (7f/8f)*mWidth,(7f/8f)*mHeight,
                (1f/8f)*mWidth,(7f/8f)*mHeight});
        p.setOrigin(mWidth/2, mHeight/2);
        return p;
    }
}
