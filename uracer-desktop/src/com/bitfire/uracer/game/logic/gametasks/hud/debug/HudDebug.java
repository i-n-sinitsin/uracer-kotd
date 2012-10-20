
package com.bitfire.uracer.game.logic.gametasks.hud.debug;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.bitfire.uracer.events.GameRendererEvent;
import com.bitfire.uracer.events.GameRendererEvent.Order;
import com.bitfire.uracer.game.GameEvents;
import com.bitfire.uracer.game.logic.gametasks.GameTasksManager;
import com.bitfire.uracer.game.logic.gametasks.hud.HudElement;
import com.bitfire.uracer.game.logic.gametasks.trackeffects.TrackEffectType;
import com.bitfire.uracer.game.logic.gametasks.trackeffects.effects.PlayerSkidMarks;
import com.bitfire.uracer.game.logic.gametasks.trackeffects.effects.PlayerSmokeTrails;
import com.bitfire.uracer.game.player.PlayerCar;
import com.bitfire.uracer.game.player.PlayerDriftState;
import com.bitfire.uracer.game.rendering.GameRenderer;
import com.bitfire.uracer.resources.Art;
import com.bitfire.uracer.utils.CarUtils;

/** Encapsulates a special hud element that won't render as usual, but it will schedule its drawing operations by registering to
 * the GameRenderer's BatchDebug event.
 * 
 * @author bmanuel */
public class HudDebug extends HudElement {

	private final PlayerCar player;
	private final PlayerDriftState driftState;
	private final GameTasksManager gameTaskManager;
	private final PlayerSmokeTrails smokeTrails;
	private final PlayerSkidMarks skidMarks;

	private HudDebugMeter meterDriftStrength, meterSkidMarks, meterSpeed, meterSmokeTrails;

	private Array<HudDebugMeter> meters = new Array<HudDebugMeter>();
	private Vector2 pos = new Vector2();

	private GameRendererEvent.Listener gameRendererEvent = new GameRendererEvent.Listener() {
		@Override
		public void gameRendererEvent (GameRendererEvent.Type type, Order order) {
			onDebug(GameEvents.gameRenderer.batch);
		}
	};

	public HudDebug (PlayerCar player, PlayerDriftState driftState, GameTasksManager manager) {
		GameEvents.gameRenderer.addListener(gameRendererEvent, GameRendererEvent.Type.BatchDebug, GameRendererEvent.Order.DEFAULT);

		this.player = player;
		this.driftState = driftState;
		this.gameTaskManager = manager;

		skidMarks = (PlayerSkidMarks)gameTaskManager.effects.getEffect(TrackEffectType.CarSkidMarks);
		smokeTrails = (PlayerSmokeTrails)gameTaskManager.effects.getEffect(TrackEffectType.CarSmokeTrails);

		// meter lateral forces
		meterDriftStrength = new HudDebugMeter(100, 5);
		meterDriftStrength.setLimits(0, 1);
		meterDriftStrength.setName("drift strength");
		meters.add(meterDriftStrength);

		// meter skid marks count
		if (skidMarks != null) {
			meterSkidMarks = new HudDebugMeter(100, 5);
			meterSkidMarks.setLimits(0, skidMarks.getMaxParticleCount());
			meterSkidMarks.setName("skid marks");
			meters.add(meterSkidMarks);
		}

		// meter smoke trails count
		if (smokeTrails != null) {
			meterSmokeTrails = new HudDebugMeter(100, 5);
			meterSmokeTrails.setLimits(0, smokeTrails.getMaxParticleCount());
			meterSmokeTrails.setName("smoke trails");
			meters.add(meterSmokeTrails);
		}

		// player speed, km/h
		meterSpeed = new HudDebugMeter(100, 5);
		meterSpeed.setLimits(0, CarUtils.mtSecToKmHour(player.getCarModel().max_speed));
		meterSpeed.setName("speed km/h");
		meters.add(meterSpeed);
	}

	@Override
	public void dispose () {
		GameEvents.gameRenderer.removeListener(gameRendererEvent, GameRendererEvent.Type.BatchDebug,
			GameRendererEvent.Order.DEFAULT);

		for (HudDebugMeter m : meters) {
			m.dispose();
		}
	}

	@Override
	public void onTick () {
		// lateral forces
		meterDriftStrength.setValue(driftState.driftStrength);

		if (driftState.isDrifting) {
			meterDriftStrength.color.set(.3f, 1f, .3f, 1f);
		} else {
			meterDriftStrength.color.set(1f, 1f, 1f, 1f);
		}

		// skid marks count
		if (skidMarks != null) {
			meterSkidMarks.setValue(skidMarks.getParticleCount());
		}

		// smoke trails count
		if (smokeTrails != null) {
			meterSmokeTrails.setValue(smokeTrails.getParticleCount());
		}

		// player's speed
		meterSpeed.setValue(CarUtils.mtSecToKmHour(player.getInstantSpeed()));
	}

	public void onDebug (SpriteBatch batch) {

		float prevHeight = 0;
		int index = 0;
		for (HudDebugMeter m : meters) {

			pos.set(GameRenderer.ScreenUtils.worldPxToScreen(player.state().position));
			pos.x += m.getWidth() * 0.5f;
			pos.y += 100;

			// offset by index
			pos.y += index * (prevHeight + Art.DebugFontHeight);

			m.setPosition(pos);
			m.render(batch);

			index++;
			prevHeight = m.getHeight();
		}
	}

	@Override
	public void onRender (SpriteBatch batch) {
		// nope, see onDebug instead
	}

	@Override
	public void onReset () {
	}
}