
package com.bitfire.uracer.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.bitfire.uracer.Input;
import com.bitfire.uracer.URacer;
import com.bitfire.uracer.configuration.UserPreferences;
import com.bitfire.uracer.configuration.UserPreferences.Preference;
import com.bitfire.uracer.configuration.UserProfile;
import com.bitfire.uracer.game.Game;
import com.bitfire.uracer.game.GameLevels;
import com.bitfire.uracer.game.screens.GameScreensFactory.ScreenType;
import com.bitfire.uracer.screen.Screen;

public class GameScreen extends Screen {
	private Game game = null;
	private Input input = null;
	private boolean paused = false;

	@Override
	public void init () {
		// simulate slowness
		// try { Thread.sleep( 1000 ); } catch( InterruptedException e ) {}

		input = URacer.Game.getInputSystem();
		if (!GameLevels.levelIdExists(ScreensShared.selectedLevelId)) {
			Gdx.app.error("GameScreen", "The specified track could not be found.");
			URacer.Game.show(ScreenType.MainScreen);
		} else {
			// save as last played track
			UserPreferences.string(Preference.LastPlayedTrack, ScreensShared.selectedLevelId);
			UserProfile userProfile = new UserProfile();
			game = new Game(userProfile, ScreensShared.selectedLevelId);
			game.start();
		}
	}

	@Override
	public void dispose () {
		if (game != null) game.dispose();
		game = null;
	}

	@Override
	public void tick () {
		if (game != null && !paused) game.tick();

		// debug
		if (input.isPressed(Keys.Q) || input.isPressed(Keys.ESCAPE) || input.isPressed(Keys.BACK)) {
			paused = !paused;
			if (paused) {
				game.pause();
			} else {
				game.resume();
			}
		}
	}

	@Override
	public void tickCompleted () {
		if (game != null && !paused) game.tickCompleted();
	}

	@Override
	public void render (FrameBuffer dest) {
		if (game != null) game.render(dest);
	}

	@Override
	public void pause () {
		if (game != null) game.pause();
	}

	@Override
	public void resume () {
		if (game != null) game.resume();
	}
}
