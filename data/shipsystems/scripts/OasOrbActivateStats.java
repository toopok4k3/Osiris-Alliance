package data.shipsystems.scripts;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;

public class OasOrbActivateStats extends BaseShipSystemScript {
	private static final Color UNDER_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
	private static final Color JITTER_COLOR = new Color(1.0f, 1.0f, 1.0f, 1.0f);
	private static final float FLARE_TIME = 2.0f;
	private static final float DILATION = 3.0f;

	private boolean triggeredBoomSound = false;
	private float flareTimeLeft = 0.0f;

	@Override
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
		if(Global.getCombatEngine() == null) return;
		final float amount = Global.getCombatEngine().getElapsedInLastFrame();
		final SoundPlayerAPI sound = Global.getSoundPlayer();
		// let's shake things up until we are at full effectLevel
		final float shipTimeMult;
		if (state == State.IN && effectLevel < 1.0f) {
			if(sound != null) {
				sound.playLoop("oas_system_redtime_loop", ship, 0.25f+1.25f*effectLevel, effectLevel*effectLevel, ship.getLocation(), ship.getVelocity());
			}
			visualCharge(ship, effectLevel);
			shipTimeMult = 1.0f;
		} else if (state == State.ACTIVE && effectLevel >= 1.0f) {
			if (!triggeredBoomSound && sound != null) {
				if (sound != null) {
					float pitch = 1.0f;
					float volume = 1.0f;
					Vector2f loc = ship.getLocation();
					Vector2f vel = ship.getVelocity();
					sound.playSound("oas_system_redtime_start", pitch, volume, loc, vel);
				}
				triggeredBoomSound = true;
				flareTimeLeft = FLARE_TIME;
				shipTimeMult = 1.0f + DILATION;
				ghostImage(ship);
			} else {
				flareTimeLeft = flareTimeLeft - amount;
				final float dilationLevel = flareTimeLeft / FLARE_TIME;
				addLensFlare(ship, flareTimeLeft);
				// dilate time for brief period after boom to go vroom
				if(dilationLevel >= 0.0f) {
					shipTimeMult = 1.0f + (dilationLevel*DILATION);
				} else {
					shipTimeMult = 1.0f;
				}
			}
		} else {
			shipTimeMult = 1.0f;
		}
		stats.getTimeMult().modifyMult(id, shipTimeMult);
		if (player) {
			Global.getCombatEngine().getTimeMult().modifyMult(id, 1f / shipTimeMult);
		} else {
			Global.getCombatEngine().getTimeMult().unmodify(id);
		}
		if (state == State.ACTIVE || state == State.OUT) {
			// let's do stats!
			stats.getBallisticAmmoRegenMult().modifyPercent(id, 100.0f * effectLevel);
			stats.getBallisticRoFMult().modifyPercent(id, 100.0f * effectLevel);
			stats.getEnergyAmmoRegenMult().modifyPercent(id, 100.0f * effectLevel);
			stats.getEnergyRoFMult().modifyPercent(id, 100.0f * effectLevel);

			// movement
			if(state == State.OUT) {
				stats.getMaxSpeed().unmodify(id);
			} else {
				stats.getMaxSpeed().modifyFlat(id, 50.0f * effectLevel);
			}
			stats.getAcceleration().modifyFlat(id, 60.0f * effectLevel);
			stats.getTurnAcceleration().modifyFlat(id, 30.0f * effectLevel);
			stats.getMaxTurnRate().modifyFlat(id, 60.0f * effectLevel);
		}
	}

	@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		triggeredBoomSound = false;

		stats.getTimeMult().unmodify(id);
		// let's do stats!
		stats.getBallisticAmmoRegenMult().unmodify(id);
		stats.getBallisticRoFMult().unmodify(id);
		stats.getEnergyAmmoRegenMult().unmodify(id);
		stats.getEnergyRoFMult().unmodify(id);
	
		// movement
		stats.getMaxSpeed().unmodify(id);
		stats.getAcceleration().unmodify(id);
		stats.getTurnAcceleration().unmodify(id);
		stats.getMaxTurnRate().unmodify(id);
	}

	private static void addLensFlare(ShipAPI ship, float timeLeft) {
		if (timeLeft < 0.0f) {
			return;
		}
	}

	private static void ghostImage(ShipAPI ship) {
		if(ship == null) return;
		Color color = new Color(1.0f, 1.0f, 1.0f, 1.0f);
		float locX = 0.0f;
		float locY = 0.0f;
		float velX = -(ship.getVelocity().x * 0.05f);
		float velY = -(ship.getVelocity().y * 0.05f);
		float maxJitter = 2.0f;
		float in = 0.5f;
		float dur = 1.5f;
		float out = 0.5f;
		boolean additive = true;
		boolean combineWithSpriteColor = true;
		boolean aboveShip = true;
		ship.addAfterimage(color, locX, locY, velX,	velY, maxJitter, in, dur, out, additive, combineWithSpriteColor, aboveShip);
	}

	private static void visualCharge(ShipAPI ship, float effectLevel) {
		final float intensity = effectLevel;
		final int copies = 2;
		final float range = effectLevel * 50.0f;
		final float rangeUnder = (1.0f - effectLevel) * 50.0f;
		ship.setJitterUnder(ship, UNDER_COLOR, intensity, copies, rangeUnder);
		ship.setJitter(ship, JITTER_COLOR, intensity, copies, range);
	}
}
