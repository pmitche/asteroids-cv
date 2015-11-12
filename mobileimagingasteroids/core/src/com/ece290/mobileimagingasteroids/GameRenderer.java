package com.ece290.mobileimagingasteroids;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.ece290.mobileimagingasteroids.controls.TouchGestureListener;
import com.ece290.mobileimagingasteroids.gameobject.Asteroid;
import com.ece290.mobileimagingasteroids.gameobject.Ship;
import com.ece290.mobileimagingasteroids.gameobject.Shot;

/**
 * Created by ethan_000 on 2/15/2015.
 */
public class GameRenderer {
    private GameWorld world;
    private OrthographicCamera cam;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch mSpriteBatch;

    public GameRenderer(final GameWorld world){
        this.world = world;

        cam = new OrthographicCamera();
        cam.setToOrtho(true, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        mSpriteBatch = new SpriteBatch();
        mSpriteBatch.setProjectionMatrix(cam.combined);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(cam.combined);

        GestureDetector gd = new GestureDetector(new TouchGestureListener());
        Gdx.input.setInputProcessor(gd);

    }
    public void render() {
        //Gdx.app.log("GameRenderer", "render");

        //Gdx.gl.glClearColor(1.0f, 1.0f, .5f, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        mSpriteBatch.begin();
        AssetLoader.bgSprite.draw(mSpriteBatch);
        //mSpriteBatch.draw(AssetLoader.shipTexture, world.getShip().getX(), world.getShip().getY());
        Ship ship = world.getShip();
        mSpriteBatch.draw(AssetLoader.shipSprite, ship.getX(), ship.getY(),ship.getWidth()/2,ship.getHeight()/2,ship.getWidth(),ship.getHeight(),1,1,ship.getRotation());
        //Gdx.app.log("GameRnderer", "rotation:"+ship.getRotation());

        for (Asteroid a : world.getAsteroids())
        {
            mSpriteBatch.draw(AssetLoader.asteroidSprite, a.getX(), a.getY(),a.getWidth()/2,a.getHeight()/2,a.getWidth(),a.getHeight(),1,1,a.getRotation());
        }

        for(Shot s : world.getShots()) {
            mSpriteBatch.draw(AssetLoader.shotSprite, s.getX(), s.getY(), s.getWidth()/2, s.getHeight()/2, s.getWidth(), s.getHeight(),1,1,s.getRotation());
        }


        BitmapFont scoreDisplay = new BitmapFont(Gdx.files.internal("skins/default.fnt"), true);
        scoreDisplay.setColor(1.0f, 1.0f, 1.0f, 1.0f);

        if (world.isGameOver()) {
            scoreDisplay.setScale(5, 5);
            scoreDisplay.drawMultiLine(mSpriteBatch,
                                       "Game over!\nYour final score was:\n" + world.getScore() +
                                       "\n\n Touch to restart", 50, 200);
        } else {
            scoreDisplay.setScale(3, 3);
            scoreDisplay.drawMultiLine(mSpriteBatch, "Score: " + world.getScore() + "\n" +
                                       "Lives left: " + ship.getLives(), 50, 100);
        }
        mSpriteBatch.end();

        //Draw Polygons
        /*shapeRenderer.begin(ShapeType.Line);
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.polygon(ship.getPolygon().getTransformedVertices());
        for(Asteroid a : world.getAsteroids())
            shapeRenderer.polygon(a.getPolygon().getTransformedVertices());

        for(Shot s: world.getShots())
            shapeRenderer.polygon(s.getPolygon().getTransformedVertices());

        shapeRenderer.end();*/
    }
}
