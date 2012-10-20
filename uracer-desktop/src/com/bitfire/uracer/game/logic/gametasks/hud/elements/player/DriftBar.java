
package com.bitfire.uracer.game.logic.gametasks.hud.elements.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.WindowedMean;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.game.logic.gametasks.hud.HudLabel;
import com.bitfire.uracer.game.logic.gametasks.hud.Positionable;
import com.bitfire.uracer.resources.Art;
import com.bitfire.uracer.resources.BitmapFontFactory.FontFace;
import com.bitfire.uracer.utils.AMath;
import com.bitfire.uracer.utils.ColorUtils;
import com.bitfire.uracer.utils.Convert;
import com.bitfire.uracer.utils.NumberString;
import com.bitfire.utils.ShaderLoader;

public class DriftBar extends Positionable implements Disposable {
	public static final float MaxSeconds = 10f;
	public static final int MaxTicks = (int)(MaxSeconds * Config.Physics.PhysicsTimestepHz);

	private final float scale;
	private float seconds;
	private HudLabel labelSeconds;
	private final WindowedMean driftStrength;

	private final Texture texHalf, texHalfMask;// , texProgress, texProgressMask;
	private final ShaderProgram shProgress;
	private final Sprite sProgress, sDriftStrength;
	private final float offX, offY, w, h;

	public DriftBar (float scale, float width) {
		this.scale = scale;
		seconds = 0;

		labelSeconds = new HudLabel(scale, FontFace.Roboto, "s", false, 0.5f);
		labelSeconds.setAlpha(0);

		//
		texHalf = Art.texCircleProgressHalf;
		texHalfMask = Art.texCircleProgressHalfMask;
// texProgress = Art.texCircleProgress;
// texProgressMask = Art.texCircleProgressMask;

		w = texHalf.getWidth();
		h = texHalf.getHeight();
		offX = w / 2;
		offY = h / 2;
		shProgress = ShaderLoader.fromFile("progress", "progress");

		// drift points
		sProgress = new Sprite(texHalf);
		sProgress.flip(false, true);

		// drift strength
		driftStrength = new WindowedMean((int)(1 / 0.25f));
		sDriftStrength = new Sprite(texHalf);
	}

	public void setSeconds (float seconds) {
		this.seconds = MathUtils.clamp(seconds, 0, MaxSeconds);
	}

	public float getSeconds () {
		return seconds;
	}

	public void setDriftStrength (float strength) {
		driftStrength.addValue(strength);
	}

	public float getDriftStrength () {
		return driftStrength.getMean();
	}

	public void showSecondsLabel () {
		labelSeconds.queueShow(300);
	}

	public void hideSecondsLabel () {
		labelSeconds.queueHide(300);
	}

	@Override
	public void dispose () {
	}

	public void tick () {
		labelSeconds.tick();
	}

	public void render (SpriteBatch batch, float cameraZoom) {
		labelSeconds.setScale(0.5f * cameraZoom, false);
		labelSeconds.setString(NumberString.format(seconds) + "s", true);
		labelSeconds.setPosition(position.x, position.y + Convert.scaledPixels(80) * cameraZoom * scale);
		labelSeconds.render(batch);

		// circle progress for remaining time
		float s = 0.8f;
		float scl = cameraZoom * scale * s;
		float px = position.x - offX;
		float py = position.y - offY + Convert.scaledPixels(32) * cameraZoom * s;
		float a = 1f;

		batch.setShader(shProgress);
		texHalfMask.bind(1);
		Gdx.gl.glActiveTexture(GL10.GL_TEXTURE0);
		shProgress.setUniformi("u_texture1", 1);

		float ratio = seconds / MaxSeconds;
		shProgress.setUniformf("progress", ratio);
		sProgress.setColor(ColorUtils.paletteRYG(ratio, 1));
		sProgress.setScale(scl);
		sProgress.setPosition(px, py);
		sProgress.draw(batch, a);
		batch.flush();

		float amount = driftStrength.getMean();
		if (!AMath.isZero(amount)) {
			scl = cameraZoom * scale * 0.8f;

			// drift strength
			py = position.y - offY - Convert.scaledPixels(32) * cameraZoom * s;
			shProgress.setUniformf("progress", MathUtils.clamp(amount, 0, 1));

			sDriftStrength.setColor(1, 1, 1, 1);
			sDriftStrength.setScale(scl);
			sDriftStrength.setPosition(px, py);
			sDriftStrength.draw(batch, a);
			batch.flush();
		}

		batch.setShader(null);
	}
}