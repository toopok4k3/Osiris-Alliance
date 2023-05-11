package data.shipsystems.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

// Copied mainly from the basegame api, added speed bonuses and changed color.
public class OasRedTimeDilationStats extends BaseShipSystemScript {
	public static final float MAX_TIME_MULT = 3f;
	public static final float MIN_TIME_MULT = 0.1f;
	public static final float DAM_MULT = 0.1f;
	// [255,0,0,90]
	public static final Color JITTER_COLOR = new Color(255, 0, 0, 55);
	public static final Color JITTER_UNDER_COLOR = new Color(255, 100, 100, 80);
	public static final Color AFTER_IMAGE_COLOR = new Color(215, 0, 0, 5);
	private Vector2f afterImageVel = new Vector2f(0f, 0f);

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}

		float fluxmod = 0.0f;
		if(ship.getMaxFlux() > 0.0f) {
			fluxmod = ship.getCurrFlux() / ship.getMaxFlux();
		}
		if(fluxmod > 1.0f) fluxmod = 1.0f;
		if(fluxmod < 0.0f) fluxmod = 0.0f;

		float jitterLevel = effectLevel;
		float jitterRangeBonus = 0f;
		float maxRangeBonus = 10f;
		if (state == ShipSystemStatsScript.State.IN) {
			jitterLevel = effectLevel / (1f / ship.getSystem().getChargeUpDur());
			if (jitterLevel > 1f) {
				jitterLevel = 1f;
			}
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		} else if (state == State.ACTIVE) {
			jitterLevel = 1f;
			jitterRangeBonus = maxRangeBonus;
		} else if (state == State.OUT) {
			jitterRangeBonus = jitterLevel * maxRangeBonus;
		}
		jitterLevel = (float) Math.sqrt(jitterLevel);
		//effectLevel *= effectLevel; // why??

		ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0f, 0f + jitterRangeBonus);
		ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);
		// ship.setAlphaMult(0.0f);

		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}

		ship.getEngineController().fadeToOtherColor(this, JITTER_COLOR, new Color(0, 0, 0, 0), effectLevel, 0.5f);
		ship.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);

		/*
		 * ship.addAfterimage(java.awt.Color color,
		 * float locX,
		 * float locY,
		 * float velX,
		 * float velY,
		 * float maxJitter,
		 * float in,
		 * float dur,
		 * float out,
		 * boolean additive,
		 * boolean combineWithSpriteColor,
		 * boolean aboveShip);
		 */
		afterImageVel.setX(ship.getVelocity().x);
		afterImageVel.setY(ship.getVelocity().y);
		ship.addAfterimage(AFTER_IMAGE_COLOR,
				0.0f,
				0.0f,
				-afterImageVel.x,
				-afterImageVel.y,
				5f,
				0.1f,
				0.1f,
				1f,
				false,
				false,
				false);

		// float mult = 1f + BONUS * effectLevel;
		float fluxDecrease = (shipTimeMult * 0.75f) * fluxmod;
		float mult = shipTimeMult - fluxDecrease; // reduce the effect if we have high flux

		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
			// stats.getAcceleration().unmodify(id);
			// stats.getDeceleration().unmodify(id);
			// stats.getTurnAcceleration().unmodify(id);
			stats.getMaxTurnRate().unmodify(id);
		} else {
			stats.getMaxSpeed().modifyMult(id, mult);
			stats.getAcceleration().modifyMult(id, mult * 2);
			stats.getDeceleration().modifyMult(id, mult * 2);
			stats.getTurnAcceleration().modifyMult(id, mult * 2);
			stats.getMaxTurnRate().modifyMult(id, mult);
			stats.getShieldDamageTakenMult().modifyPercent(id, 50f*effectLevel);
		}
	}

	public void unapply(MutableShipStatsAPI stats, String id) {
		ShipAPI ship = null;
		boolean player = false;
		if (stats.getEntity() instanceof ShipAPI) {
			ship = (ShipAPI) stats.getEntity();
			player = ship == Global.getCombatEngine().getPlayerShip();
			id = id + "_" + ship.getId();
		} else {
			return;
		}
		// if(ship != null) {
		// ship.setAlphaMult(1f);
		// }
		Global.getCombatEngine().getTimeMult().unmodify(id);
		stats.getTimeMult().unmodify(id);
		stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		stats.getAcceleration().unmodify(id);
		stats.getDeceleration().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
		stats.getShieldDamageTakenMult().unmodify(id);
	}

	public StatusData getStatusData(int index, State state, float effectLevel) {
		float shipTimeMult = 1f + (MAX_TIME_MULT - 1f) * effectLevel;
		if (index == 0) {
			return new StatusData("time flow altered", false);
		}
		if (index == 1) {
			return new StatusData("Reduced shield efficiency", true);
		}
		return null;
	}
}
