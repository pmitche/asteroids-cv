package com.ece290.mobileimagingasteroids.screen;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.ece290.mobileimagingasteroids.GameRenderer;
import com.ece290.mobileimagingasteroids.GameWorld;

/**
 * Created by ethan_000 on 2/13/2015.
 */
public class GameScreen implements Screen {
    private float runTime;
    private GameWorld world;
    private GameRenderer renderer;

    public GameWorld getWorld()
    {
        return world;
    }

    public GameScreen()
    {
        Gdx.app.log("GameScreen", "Attached");

        world = new GameWorld(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        renderer = new GameRenderer(world);
    }

    @Override
    public void render(float delta) {
        //Gdx.app.log("GameScreen", "render");
        runTime += delta;
        //Gdx.app.log("GameScreen", "FPS:"+(1 / delta) + " ");

        /*Gdx.gl.glClearColor(1, .5f, .2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);*/

        world.update(delta);
        renderer.render();

    }

    @Override
    public void dispose() {
        Gdx.app.log("GameScreen", "dispose");
        // TODO Auto-generated method stub
        //mRenderer.dispose();
        //mWorld.dispose();
        //game.setScreen(new GameOverScreen(game));
    }

    @Override
    public void hide() {
        Gdx.app.log("GameScreen", "hide");
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        Gdx.app.log("GameScreen", "resume");
        // TODO Auto-generated method stub
    }

    @Override
    public void show() {
        Gdx.app.log("GameScreen", "show");
    }

    @Override
    public void pause() {
        Gdx.app.log("GameScreen", "pause");
        // TODO Auto-generated method stub

    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("GameScreen", "resize");
        // TODO Auto-generated method stub

    }
}
