package com.bitfire.uracer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.bitfire.uracer.screen.Screen;
import com.bitfire.uracer.screen.TestScreen;

public class URacer implements ApplicationListener
{
	private Screen screen;
	private Input input = new Input();
	private boolean running = false;

	private float timeAliasingAlpha = 0;
	private float timeAccumSecs = 0;
	private float oneOnOneBillion = 0;

	// stats
	private static float graphicsTime = 0;
	private static float physicsTime = 0;

	@Override
	public void create()
	{
		Config.asDefault();
		Art.load();
		Gdx.graphics.setVSync( true );
		input.releaseAllKeys();
		Gdx.input.setInputProcessor( input );
		setScreen( new TestScreen() );

		running = true;
		oneOnOneBillion = 1.0f / 1000000000.0f;
		timeAliasingAlpha = 0;
	}

	@Override
	public void render()
	{
		float deltaTime = Gdx.graphics.getDeltaTime();

		// avoid spiral of death
		if( deltaTime > 0.25f )
			deltaTime = 0.25f;

		long startTime = System.nanoTime();
		{
			timeAccumSecs += deltaTime * Physics.timeMultiplier;
			while( timeAccumSecs > Physics.dt )
			{
				input.tick();
				screen.tick( input );

				timeAccumSecs -= Physics.dt;
			}
		}
		physicsTime = (System.nanoTime() - startTime) * oneOnOneBillion;

		timeAliasingAlpha = timeAccumSecs * Physics.timestepHz; // opt away the divide-by-one-on-timestep

		startTime = System.nanoTime();
		{
			screen.render( timeAliasingAlpha );
		}
		graphicsTime = (System.nanoTime() - startTime) * oneOnOneBillion;
	}

	@Override
	public void resize( int width, int height )
	{
	}

	@Override
	public void pause()
	{
		running = false;
	}

	@Override
	public void resume()
	{
		running = true;
	}

	@Override
	public void dispose()
	{
		Physics.dispose();
	}

	public void setScreen( Screen newScreen )
	{
		if( screen != null )
			screen.removed();

		screen = newScreen;

		if( screen != null )
			screen.init( this );
	}

	public boolean isRunning()
	{
		return running;
	}

	public static float getRenderTime()
	{
		return graphicsTime;
	}

	public static float getPhysicsTime()
	{
		return physicsTime;
	}
}
