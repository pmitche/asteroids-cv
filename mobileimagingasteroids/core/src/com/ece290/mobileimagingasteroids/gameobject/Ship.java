package com.ece290.mobileimagingasteroids.gameobject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;


/**
 * Ship class of the Asteroids game
 *
 * Created by ethan_000 on 2/17/2015.
 */
public class Ship extends GameObject {

    private final float COOLDOWN_TIME = 0.2f; //TODO
    private final float SPEED = 250;

    private float shotCooldown = 0;

    //Number of lives left
    private int lives = 3;
    private boolean isDead = false;

    private Sound shotSound = Gdx.audio.newSound(Gdx.files.internal("shot_sound.mp3"));

    public Ship(int width, int height, float positionX, float positionY)
    {
        super (width, height, positionX, positionY);
    }

    public Shot shoot() {

        if(shotCooldown > 0)
            return null;

        Vector2 speed = new Vector2(0,-SPEED).rotate(getRotation());

        Shot shot = new Shot(getX()+getWidth()/2, getY()+getHeight()/2, speed.x, speed.y);
        shot.setRotation(getRotation());
        shotCooldown = COOLDOWN_TIME;

        shotSound.play();

        return shot;
    }


    @Override
    public void update(float delta)
    {
        mVelocity.add(mAcceleration.cpy().scl(delta));
        //mPosition.add(mVelocity.cpy().scl(delta));
        mPosition.add(mVelocity.cpy().scl(delta).rotate(mRotation.x));
        mRotation.add(mRotationUpdate.cpy().scl(delta));

        if(shotCooldown > 0)
            shotCooldown -= delta;
    }

    @Override
    protected Polygon getPolygonInternal() {

        Polygon p = new Polygon(new float[]{
                (1f/2f)*mWidth,(1f/8f)*mHeight,
                (7f/8f)*mWidth,(2f/3f)*mHeight,
                (1f/2f)*mWidth,(7f/8f)*mHeight,
                (1f/8f)*mWidth,(2f/3f)*mHeight});
        p.setOrigin(mWidth/2, mHeight/2);
        return p;
    }

    /**
     * To be called if the ship crashes
     * <p>Then it will reduce the number of lives</p>
     */
    public void crashed() {
                            if(--lives <= 0)    isDead = true;
    }
    public int getLives() {     return this.lives;}
    public boolean isDead(){    return isDead;}
}
