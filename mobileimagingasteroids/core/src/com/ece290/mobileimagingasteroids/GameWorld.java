package com.ece290.mobileimagingasteroids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.ece290.mobileimagingasteroids.controls.TouchGestureListener;
import com.ece290.mobileimagingasteroids.gameobject.Asteroid;
import com.ece290.mobileimagingasteroids.gameobject.Shot;
import com.ece290.mobileimagingasteroids.gameobject.GameObject;
import com.ece290.mobileimagingasteroids.gameobject.Ship;

import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by ethan_000 on 2/15/2015.
 */
public class GameWorld {

    private final float ASTEROID_ARRIVAL_RATE = 0.05f;
    private final float ASTEROID_MAX = 10;

    private int mWidth, mHeight;

    private Ship mShip;
    private List<Asteroid> asteroids;
    private List<Shot> shots;

    private int score;
    private float runTime;
    private float asteroidSpawnTime;

    private double shipRotationUpdate;


    private boolean mShoot = false;

    private int deadTime = 300; //TODO

    private boolean hasCrashed = false; //TODO used?

    private Sound crashSound = Gdx.audio.newSound(Gdx.files.internal("crash_sound.mp3")); //TODO should reference the assetLoader instead..

    private GameState currentState;

    /**
     * Enum that describes the state of the gameworld.
     * <p>
     *     READY:   Game is initialized and ready to start.
     *     RUNNING: Game is currently running.
     *     GAMEOVER:Game finished.
     * </p>
     */
    public enum GameState {
        READY, RUNNING, GAMEOVER
    }
    public void setShipRotationUpdate(double rotation)
    {
        mShip.setRotationUpdate((float)(rotation*75));
    }
    public void setShoot(boolean shoot)
    {
        System.out.println("shoot:"+shoot);
        mShoot = shoot;

    }
    public void setShipSpeed(double speed)
    {
        mShip.setVelocityY((float)(-80*speed));
    }
    public GameWorld(int width, int height)
    {
        this.mWidth = width;
        this.mHeight = height;

        asteroidSpawnTime = (float) NegativeExponentialCalculator.calculate(ASTEROID_ARRIVAL_RATE);

        setGame();
        start();

        TouchGestureListener.addListenser(new ControlsListener() {
            @Override
            public void onRotationUpdate(int rotationUpdate) {
                mShip.setRotationUpdate(rotationUpdate);

            }

            @Override
            public void onVelocityUpdate(float velX, float velY) {
                /*
                mShip.mVelocity = mShip.mVelocity.add(new Vector2(velX, velY).rotate(mShip.getRotation()));
                mShip.setRotationUpdate(mShip.getRotation());
                */
            }

            @Override
            public void onShoot() {
                Shot shot = mShip.shoot();
                if (shot != null)
                    shots.add(shot);
            }

            @Override
            public void changeGameState() {
                if(currentState == GameState.GAMEOVER) {
                    restart();
                }
            }
        });
    }

    /**
     * Initializes the game environment
     */
    private void setGame() {
        asteroids = new ArrayList<Asteroid>();
        shots = new ArrayList<Shot>();
        mShip = new Ship(mWidth/10,mHeight/10, mWidth/2, mHeight/2);

        //TODO remove this.
        //mShip.setVelocityY(-30);
        asteroids.add(new Asteroid(200,200,0,50,-40,40));

        currentState = GameState.READY;
    }

    public void update(float delta) {

        switch (currentState) {
            case READY:
                updateReady(delta);
                break;
            case RUNNING:
                updateRunning(delta);
                break;
        }
    }

    public void updateRunning(float delta) {
        //Gdx.app.log("GameWorld", "update");

        runTime += delta;

        if(runTime > asteroidSpawnTime)
        {
            if(asteroids.size() < ASTEROID_MAX) {
                asteroids.add(new Asteroid(mWidth / 4, mHeight / 4));
            }
            asteroidSpawnTime = runTime + (float) NegativeExponentialCalculator.calculate(ASTEROID_ARRIVAL_RATE);
        }

        mShip.update(delta);
        //mShip.setRotationUpdate(5);
        resetGameObjectInScreenBounds(mShip);

        for(Asteroid a : asteroids) {
            resetGameObjectInScreenBounds(a);
            //a.setRotationUpdate(10);
            a.update(delta);
        }

        if(mShoot)
        {
            Shot shot = mShip.shoot();
            if(shot != null && shots.size()<10)
               shots.add(shot);

        }

        for(Shot s : shots) {
            s.update(delta);
        }

        if(hasCrashed){
            deadTime-=delta;
            if(deadTime<=0){
                hasCrashed = false;
                deadTime = 300;
            }
        }

        List<Asteroid> astrCopy = asteroids;
        for (int i = 0; i < shots.size(); i++) {
            Shot currShot = shots.get(i);
            for (int j = 0; j < astrCopy.size(); j++) {
                Asteroid currAsteroid = asteroids.get(j);

                if (Intersector.overlapConvexPolygons(currShot.getPolygon(), currAsteroid.getPolygon())) {
                    shots.remove(currShot);
                    score += 10;

                    if (currAsteroid.getWidth() > 60 && currAsteroid.getHeight() > 60) {
                        asteroids.remove(j);

                        asteroids.add(new Asteroid(Math.round(currAsteroid.getWidth() / 2),
                                Math.round(currAsteroid.getHeight() / 2),
                                currAsteroid.getX() + currAsteroid.getWidth() / 4,
                                currAsteroid.getY() + currAsteroid.getHeight() / 4,
                                currAsteroid.getVelocityX(),
                                currAsteroid.getVelocityY()));

                        asteroids.add(new Asteroid(Math.round(currAsteroid.getWidth() / 2),
                                Math.round(currAsteroid.getHeight() / 2),
                                currAsteroid.getX() + currAsteroid.getWidth() / 4,
                                currAsteroid.getY() + currAsteroid.getHeight() / 4,
                                currAsteroid.getVelocityX(),
                                currAsteroid.getVelocityY()));

                        randomizeAsteroidVelocityOnSplit(asteroids.get(asteroids.size()-1),
                                                         asteroids.get(asteroids.size()-2));
                    } else {
                        asteroids.remove(j);
                    }
                }
            }
        }

        Iterator<Shot> itr = shots.iterator();
        if (itr.hasNext()) {
            Shot currentShot = itr.next();
            if (!isGameObjectInScreenBounds(currentShot)){
                shots.remove(currentShot);
            }
        }

        for(Asteroid a : asteroids)
        {
            if(Intersector.overlapConvexPolygons(mShip.getPolygon(), a.getPolygon()))
            {
                if(!hasCrashed){
                    //System.out.println("SHIP HIT");
                    crashSound.play();
                    hasCrashed = true;
                    mShip.crashed();
                }
            }
        }

        //Check if game is lost
        if(mShip.isDead())
            currentState = GameState.GAMEOVER;

    }

    public void updateReady(float delta) {
        //TODO
    }

    private void randomizeAsteroidVelocityOnSplit(Asteroid a1, Asteroid a2) {
        a1.setVelocityX(MathUtils.random(-30, 30));
        a1.setVelocityY(MathUtils.random(-30, 30));
        a2.setVelocityX(MathUtils.random(-30, 30));
        a2.setVelocityY(MathUtils.random(-30, 30));

        //a1.rotate(MathUtils.random(-20, 20));
        //a2.rotate(MathUtils.random(-20, 20));
    }

    private void resetGameObjectInScreenBounds(GameObject o)
    {
        if (o.getX() > Gdx.graphics.getWidth())
            o.setX(0);
        if (o.getX() < 0 )
            o.setX(Gdx.graphics.getWidth());
        if (o.getY() > Gdx.graphics.getHeight())
            o.setY(0);
        if (o.getY() < 0 )
            o.setY(Gdx.graphics.getHeight());
    }

    private boolean isGameObjectInScreenBounds(GameObject o)
    {
        if (o.getX() > Gdx.graphics.getWidth())
            return false;
        if (o.getX() < 0 )
            return false;
        if (o.getY() > Gdx.graphics.getHeight())
            return false;
        if (o.getY() < 0 )
            return false;
        return true;
    }

    public boolean isReady() {      return currentState == GameState.READY;}
    public boolean isGameOver() {   return currentState == GameState.GAMEOVER;}

    /**
     * Start the game
     */
    public void start(){
        currentState = GameState.RUNNING;
    }

    /**
     * Put the game in ready-state.
     */
    public void restart() {
        score = 0; runTime = 0;
        setGame();
        start();
    }

    //Getters
    public Ship getShip(){return mShip;}
    public List<Asteroid> getAsteroids(){return asteroids;}
    public List<Shot> getShots() {return shots;}
    public String getScore() {
        return score + (int) runTime + "";
    }
    public int getLives() { return mShip.getLives();}
}
