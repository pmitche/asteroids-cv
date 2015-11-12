package com.ece290.mobileimagingasteroids;

import java.util.Observable;

/**
 * Interface for controlling the game
 *
 * Created by ethan_000 on 3/5/2015.
 */
public interface ControlsListener{


    /**
     *  Update rotation on the ship
     * @param rotationUpdate degrees
     */
    public void onRotationUpdate(int rotationUpdate);

    /**
     *  Update the velocity of the ship
     * @param velX  Velocity on X-axis
     * @param velY  Velocity on Y-axis
     */
    public void onVelocityUpdate(float velX, float velY);

    /**
     *  Notify observer (@link com.ece290.mobileimagingasteroids.GameWorld) to perform the shoot command.
     */
    public void onShoot();

    public void changeGameState();

}
