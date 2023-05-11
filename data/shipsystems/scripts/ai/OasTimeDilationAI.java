package data.shipsystems.scripts.ai;

import java.util.List;
import java.lang.Math;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.AIHints;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

public class OasTimeDilationAI implements ShipSystemAIScript {

	private ShipAPI ship;
	private CombatEngineAPI engine;
	private ShipwideAIFlags flags;
	private ShipSystemAPI system;

	private IntervalUtil tracker = new IntervalUtil(0.5f, 1f);

	public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
		this.ship = ship;
		this.flags = flags;
		this.engine = engine;
		this.system = system;
	}

	private float sinceLast = 0f;
	//private Vector2f rangeVector = new Vector2f(0f, 0f);
	private float randomYes = 0.0f;
	private float randomNope = 0.0f;

	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);

		sinceLast += amount;

		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (system.isActive()) return;
			if(ship.getFluxTracker().isOverloadedOrVenting()) return;

			if (target == null)
				return;

			final Vector2f targetPos = target.getLocation();
			final Vector2f ownPos = ship.getLocation();
			if (targetPos == null || ownPos == null)
				return;
			//Vector2f.sub(targetPos, ownPos, rangeVector);
			final float rangeToTarget = Misc.getDistance(targetPos, ownPos);
			//final float rangeToTarget = rangeVector.length();

			// let's figure if we are in attack range or not.
			float smallestRange = 0.0f;

			final List<WeaponAPI> weapons = ship.getAllWeapons();
			for (WeaponAPI w : weapons) {
				float weaponRange = w.getRange();
				if (w.hasAIHint(WeaponAPI.AIHints.PD) || w.hasAIHint(WeaponAPI.AIHints.PD_ONLY))
					continue; // don't care about PD weapons
				// if(w.distanceFromArc(targetPos) > 0.0f) continue; // don't care about weapons
				// that cant aim at the target
				if (smallestRange == 0.0f) {
					smallestRange = weaponRange;
					continue;
				}
				if (weaponRange < smallestRange)
					smallestRange = weaponRange;
			}
			smallestRange = smallestRange + ship.getCollisionRadius()/2;

			float rangeDelta = 0.0f;
			if(flags.hasFlag(AIFlags.MANEUVER_RANGE_FROM_TARGET)) {
				Object manrange = flags.getCustom(AIFlags.MANEUVER_RANGE_FROM_TARGET);
				if(manrange instanceof Float) {
					rangeDelta = Math.abs(rangeToTarget - (Float)manrange);
				}
			}

			float singleAmmoWorth = 1.0f / (float)system.getMaxAmmo();
			float lowUseWeight = 1.0f - ((float)system.getAmmo() / (float)system.getMaxAmmo());
			float weaponRangeWeight = getRangeWeight(rangeToTarget, smallestRange);
			
			float fluxWeight = getFluxWeight(ship.getFluxTracker().getFluxLevel());
			fluxWeight = fluxWeight * fluxWeight;
			float useWeight = 0.0f;
			useWeight += ((rangeDelta  / 100.0f)*0.2f);
			if(flags.hasFlag(AIFlags.BACKING_OFF) || flags.hasFlag(AIFlags.AVOIDING_BORDER)) {
				useWeight += 0.25f;
			}
			if(flags.hasFlag(AIFlags.HAS_INCOMING_DAMAGE)) {
				useWeight += (0.5f * fluxWeight); // our shields should handle...
			}
			if(flags.hasFlag(AIFlags.TURN_QUICKLY)) {
				useWeight += 0.75f;
			}
			if(flags.hasFlag(AIFlags.RUN_QUICKLY)) {
				useWeight += 1.0f;
			}
			
			if(flags.hasFlag(AIFlags.PURSUING)) {
				useWeight += 0.75f;
			}
			float tooSoonWeight = Math.max(0.0f, 15.0f - sinceLast) / 15.0f;
			

			useWeight = randomYes + useWeight + fluxWeight - randomNope - lowUseWeight - weaponRangeWeight - tooSoonWeight;
			boolean use = false;
			if(useWeight >= 0.0f) use = true;
			
			// dont use: in weapon range, low uses
			// use: high flux, danger, target long range, pursuing outside weapon range


			if (use) {
				ship.useSystem();
				randomNope = Misc.random.nextFloat() * 0.25f;
				randomYes = Misc.random.nextFloat() * 0.25f;
				sinceLast = 0f;
				return;
			}
		}
	}

	private float getFluxWeight(float flux) {
		/*if(flux > 0.5f) {
			return ((flux * 2) - 0.5f) * 1.0f;
		} else {
			return flux - 0.5f;
		}*/
		return flux;
	}

	// let's use smallestWepRange + 250su as max range to get highest weight
	private float getRangeWeight(float range, float weaponRange) {
		/*float weight = 1.0f;
		float compValue = range - weaponRange;
		float max = weaponRange + 1000.0f;
		if(compValue > 0.0f && compValue <= max) {
			weight = 1.0f - (compValue / max);
		} else {
			weight = 0.0f;
		}*/
		float weight = 0.0f;
		if(weaponRange < range) {
			weight = 1.0f;
		}
		return weight;
	}

    /*public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
		tracker.advance(amount);
		
		sinceLast += amount;
		
		if (tracker.intervalElapsed()) {
			if (system.getCooldownRemaining() > 0) return;
			if (system.isOutOfAmmo()) return;
			if (system.isActive()) return;
			
			if (target == null) return;
			
            Vector2f targetPos = target.getLocation();
            Vector2f ownPos = ship.getLocation();
			if(targetPos == null || ownPos == null) return;
            Vector2f.sub(targetPos, ownPos, rangeVector);
            float rangeToTarget = rangeVector.length();

            float longestRangeBallisticWeapon = 0f;
			float smallestRangeBallisticWeapon = 0f;
			//float medianBallisticWeaponRange = 0f;

			List weapons = ship.getAllWeapons();
			for (int i = 0; i < weapons.size(); i++) {
				WeaponAPI w = (WeaponAPI) (weapons.get(i));
				if (w.getType() != WeaponType.BALLISTIC) continue;
                float weaponRange = w.getRange();
                if(weaponRange > longestRangeBallisticWeapon) longestRangeBallisticWeapon = weaponRange;
				if(smallestRangeBallisticWeapon <= 0f) smallestRangeBallisticWeapon = weaponRange;
				if(weaponRange < smallestRangeBallisticWeapon) smallestRangeBallisticWeapon = weaponRange;
			}
			if(longestRangeBallisticWeapon <= 0f || rangeToTarget <= 0f) return;
			final float rangeBonus = 1f;
			//final float rangeBonus = ship.getMutableStats().getBallisticWeaponRangeBonus().getBonusMult();
			final float sweetSpotRangeMin = (smallestRangeBallisticWeapon * rangeBonus * 1.25f); // arbitary multiplier...
			final float sweetSpotRangeMax = (longestRangeBallisticWeapon * rangeBonus * 1.5f); // is this ok...?
			if(rangeToTarget < sweetSpotRangeMin) return;

			boolean isTargetInSweetspot = rangeToTarget > sweetSpotRangeMin && rangeToTarget < sweetSpotRangeMax;

			float fluxLevel = ship.getFluxTracker().getFluxLevel();
			float remainingFluxLevel = 1f - fluxLevel;
			
			float fluxFractionPerUse = system.getFluxPerUse() / ship.getFluxTracker().getMaxFlux();
			if (fluxFractionPerUse > remainingFluxLevel) return;
			
			float fluxLevelAfterUse = fluxLevel + fluxFractionPerUse;
			if ((fluxLevelAfterUse > 0.9f && fluxFractionPerUse > 0.025f)) return;
			
			if (!isTargetInSweetspot && sinceLast < 10f) return;

			if (isTargetInSweetspot) {
				ship.useSystem();
				sinceLast = 0f;
				return;
			}
		}
	}*/
}
