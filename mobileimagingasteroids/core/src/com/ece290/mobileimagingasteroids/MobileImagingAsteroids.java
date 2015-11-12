package com.ece290.mobileimagingasteroids;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.badlogic.gdx.Game;
import com.ece290.mobileimagingasteroids.screen.GameScreen;

public class MobileImagingAsteroids extends Game {
	SpriteBatch batch;
	Texture img;
    Texture img2;
    private GameScreen gs;
    private GameWorld gw;

    public GameWorld getGameWorld()
    {
        if(gw==null)
        {
            gw = gs.getWorld();
        }
        return gw;
    }

	@Override
	public void create () {
        AssetLoader.load();
        gs = new GameScreen();
        setScreen(gs);//new EndGameScreen(new GameWorld(Gdx.graphics.getWidth(),Gdx.graphics.getHeight())));

		/*batch = new SpriteBatch();
		img = new Texture("badlogic.jpg");
        img2 = new Texture("bg.png");*/

	}

    @Override
    public void dispose() {
        super.dispose();
        AssetLoader.dispose();
    }

	/*@Override
	public void render () {
		/*Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		batch.draw(img, 0, 0);
        batch.draw(img2, 500,500);
		batch.end();
	}*/
}
