package data.shipsystems.scripts;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.EnumSet;
import java.awt.Color;
import java.lang.Comparable;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.StatBonus;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript.State;
import com.fs.starfarer.api.util.Misc;

public class OasSunBoostStats extends BaseShipSystemScript {
	private static final String SUN_BOOST_KEY = "oas_sunboost_active";

	private static class SunStat {
		private float dilation, range, sfx;
		public SunStat(float dilation, float range, float sfx) {
			this.dilation = dilation;
			this.range = range;
			this.sfx = sfx;
		}
	}
	private static final float PD_MINUS_MULT = -0.40f;
	private static final SunStat ERROR_STAT = new SunStat(1.0f,	0.0f, 0.0f);
	private static final Map<HullSize, SunStat> statIndex = new HashMap<HullSize, SunStat>();
	static {
		statIndex.put(HullSize.FIGHTER,new SunStat(3.0f, 100.0f, 0.4f));
		statIndex.put(HullSize.FRIGATE, new SunStat(3.0f, 90.0f, 0.5f));
		statIndex.put(HullSize.DESTROYER, new SunStat(2.5f, 80.0f, 0.65f));
		statIndex.put(HullSize.CRUISER, new SunStat(2.0f, 60.0f, 0.8f));
		statIndex.put(HullSize.CAPITAL_SHIP,new SunStat(1.5f, 40.0f, 1.0f));
	}

	protected static float RANGE = 2000f;

	private boolean endSoundTriggered = false;

	@Override
	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final ShipAPI systemUserShip;
		final boolean player;
		if (stats.getEntity() instanceof ShipAPI) {
			systemUserShip = (ShipAPI) stats.getEntity();
			player = systemUserShip == engine.getPlayerShip();
			id = id + "_" + systemUserShip.getId();
		} else {
			return;
		}
		if(systemUserShip == null) return; // should never happen, rite?

		final String targetKey = systemUserShip.getId() + "_sunboost_target";
		final Object storedObject = engine.getCustomData().get(targetKey);
		final List<ShipAPI> foundTargets;
		if (storedObject instanceof List<?>) {
			foundTargets = (List<ShipAPI>) storedObject;
		} else {
			foundTargets = null;
		}
		final SoundPlayerAPI soundPlayer = Global.getSoundPlayer();
		if (state == State.IN && foundTargets == null) {
			endSoundTriggered = false;
			final List<ShipAPI> targets = findAllyShipsInRange(systemUserShip, getMaxRange(systemUserShip));
			if (targets.isEmpty()) {
				targets.add(systemUserShip);
			}
			
			final float thickness = 40.0f;
			final float coreWidthMult = 0.67f;
			final Color fringe =  new Color(100,100,255,255);
			final Vector2f orbLocation = getOrbLocation(systemUserShip);
			for(ShipAPI target : targets) {
				activateSunBoostFlag(target);
				final HullSize hullSize = target.getHullSize();
				final SunStat sunStat;
				if(statIndex.containsKey(hullSize)) {
					sunStat = (SunStat)statIndex.get(hullSize);
				} else {
					sunStat = ERROR_STAT;
				}
				EmpArcEntityAPI arc = engine.spawnEmpArcVisual(orbLocation,
						systemUserShip,
						target.getLocation(),
						target,
						thickness * sunStat.sfx,
						fringe,
						Color.white);
				arc.setCoreWidthOverride(thickness * coreWidthMult * sunStat.sfx);
				arc.setSingleFlickerMode();
				soundPlayer.playSound("oas_system_time_start", 1.0f, sunStat.sfx, target.getLocation(), target.getVelocity());
				engine.addFloatingText(target.getLocation(), "ORB Data Linked", 30.0f*sunStat.sfx, Color.white, target, 2.0f, 2.0f);
				//Vector2f loc, String text, float size, Color color, CombatEntityAPI attachedTo, float flashFrequency, float flashDuration
				
			}
			engine.getCustomData().put(targetKey, targets);
		} else if (effectLevel > 0.0f && foundTargets != null) {
			boolean playEndSound = false;
			if(state == State.OUT && effectLevel < 1.0f && !endSoundTriggered) {
				endSoundTriggered = true;
				playEndSound = true;
			}
			for(ShipAPI foundTarget : foundTargets) {
				if(playEndSound) {
					final HullSize hullSize = foundTarget.getHullSize();
					final SunStat sunStat;
					if(statIndex.containsKey(hullSize)) {
						sunStat = (SunStat)statIndex.get(hullSize);
					} else {
						sunStat = ERROR_STAT;
					}
					soundPlayer.playSound("oas_system_time_off", 1.0f, sunStat.sfx, foundTarget.getLocation(), foundTarget.getVelocity());
				}
				applyTargetStats(id, systemUserShip, foundTarget, effectLevel);
				
			}
		} else if (state == State.OUT && foundTargets != null) {
			for(ShipAPI foundTarget : foundTargets) {
				unapplyTargetStats(id, systemUserShip, foundTarget);
				deactivateSunBoostFlag(foundTarget);
			}
			engine.getCustomData().remove(targetKey);
		}
	}

	private void applyTargetStats(final String id, final ShipAPI systemUserShip, final ShipAPI target, final float effectLevel) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final SoundPlayerAPI soundPlayer = Global.getSoundPlayer();
		final boolean player = target == engine.getPlayerShip();
		MutableShipStatsAPI stats = target.getMutableStats();
		final SunStat sunStat;
		if(statIndex.containsKey(target.getHullSize())) {
			Object object = statIndex.get(target.getHullSize()); // because janino lol
			sunStat = (SunStat) object; // because janino lol
		} else {
			sunStat = ERROR_STAT;
		}

		//if(systemUserShip != target) {
			// range
			if(systemUserShip != target) { // no range bonus to itself
				stats.getBallisticWeaponRangeBonus().modifyPercent(id, sunStat.range);
				stats.getEnergyWeaponRangeBonus().modifyPercent(id, sunStat.range);
				stats.getNonBeamPDWeaponRangeBonus().modifyPercent(id, sunStat.range*PD_MINUS_MULT);
				stats.getBeamPDWeaponRangeBonus().modifyPercent(id, sunStat.range*PD_MINUS_MULT);
			}

			// time dilation
			final float dilation = 1.0f + ((sunStat.dilation - 1.0f) * effectLevel);
			stats.getTimeMult().modifyMult(id, dilation);
			if(player) {
				soundPlayer.applyLowPassFilter(0.75f + (0.25f - (0.25f * effectLevel)), 0.5f + (0.5f - (0.5f * effectLevel)));
				engine.getTimeMult().modifyMult(id, 1f / dilation);
			} else {
				engine.getTimeMult().unmodify(id);
			}

			target.setWeaponGlow(effectLevel, new Color(100,165,255,255), EnumSet.of(WeaponType.BALLISTIC, WeaponType.ENERGY));
			//Object source, Color color, float durIn, float durOut, float maxShift
			// jitter
			//final float jitterLevel = (float) Math.sqrt(effectLevel);
			//final float maxRangeBonus = 5f;
			//final float jitterRangeBonus = jitterLevel * maxRangeBonus;
			//target.setJitter(this, OasTimeDilationStats.JITTER_COLOR, jitterLevel, 5, 0f, 0f + jitterRangeBonus);
			//target.setJitterUnder(this, OasTimeDilationStats.JITTER_UNDER_COLOR, jitterLevel, 20, 0f, 7f + jitterRangeBonus);
			//target.getEngineController().fadeToOtherColor(this, OasTimeDilationStats.JITTER_COLOR, new Color(0,0,0,0), effectLevel, 0.5f);
			//target.getEngineController().extendFlame(this, -0.25f, -0.25f, -0.25f);

			// sound
			
			soundPlayer.playLoop("oas_system_time_loop", target, 1.0f, 1.0f*effectLevel*sunStat.sfx, target.getLocation(), target.getVelocity());
		//}
	}

	private void unapplyTargetStats(final String id, final ShipAPI systemUserShip, final ShipAPI target) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		//final boolean player = target == engine.getPlayerShip();
		MutableShipStatsAPI stats = target.getMutableStats();
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getEnergyWeaponRangeBonus().unmodify(id);
		stats.getNonBeamPDWeaponRangeBonus().unmodify(id);
		stats.getBeamPDWeaponRangeBonus().unmodify(id);
		//if(systemUserShip != target) {
			stats.getTimeMult().unmodify(id);
		//}
		engine.getTimeMult().unmodify(id);
	}

	@Override
	public void unapply(MutableShipStatsAPI stats, String id) {
		final ShipAPI systemUserShip;
		final boolean player;
		final CombatEngineAPI engine = Global.getCombatEngine();
		if (stats.getEntity() instanceof ShipAPI) {
			systemUserShip = (ShipAPI) stats.getEntity();
			player = systemUserShip == engine.getPlayerShip();
			id = id + "_" + systemUserShip.getId();
		} else {
			return;
		}

		/*final String targetKey = systemUserShip.getId() + "_sunboost_target";
		final Object storedObject = engine.getCustomData().get(targetKey);
		final ShipAPI foundTarget;
		if (storedObject instanceof ShipAPI) {
			foundTarget = (ShipAPI) storedObject;
		} else {
			foundTarget = null;
		}
		if(foundTarget != null) {
			unapplyTargetStats(id, systemUserShip, foundTarget);
			engine.getCustomData().remove(targetKey);
		}*/

		final String targetKey = systemUserShip.getId() + "_sunboost_target";
		final Object storedObject = engine.getCustomData().get(targetKey);
		final List<ShipAPI> foundTargets;
		if (storedObject instanceof List<?>) {
			foundTargets = (List<ShipAPI>) storedObject;
		} else {
			foundTargets = null;
		}
		if(foundTargets != null) {
			for(ShipAPI foundTarget : foundTargets) {
				unapplyTargetStats(id, systemUserShip, foundTarget);
				deactivateSunBoostFlag(foundTarget);
			}
			engine.getCustomData().remove(targetKey);
		}
	}

	private static ShipAPI findClosestAllyShip(final ShipAPI ship, final float maxRange) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final List<ShipAPI> ships = engine.getShips();
		float minDist = Float.MAX_VALUE;
		ShipAPI closest = null;
		for (ShipAPI other : ships) {
			if (other == ship) continue;
			if (other.getHullSize().ordinal() < HullSize.FRIGATE.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (ship.getOwner() == other.getOwner() && other.getOwner() != 100) {
				final float dist = Misc.getDistance(ship.getLocation(), other.getLocation()) + other.getCollisionRadius();
				if (dist > maxRange)
					continue;
				if (dist < minDist) {
					closest = other;
					minDist = dist;
				}
			}
		}
		return closest;
	}

	private static class DistanceHolder implements Comparable<Object> {
		private final float distance;
		private final ShipAPI ship;

		public DistanceHolder(float distance, ShipAPI ship) {
			this.ship = ship;
			this.distance = distance;
		}

		@Override
		public int compareTo(Object o) {
			DistanceHolder o2 = (DistanceHolder) o;
			return Float.compare(this.distance, o2.distance);
		}
	}

	private static List<ShipAPI> findAllyShipsInRange(final ShipAPI ship, final float maxRange) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final List<ShipAPI> ships = engine.getShips();
		//float minDist = Float.MAX_VALUE;
		final List<ShipAPI> closestShips = new ArrayList<ShipAPI>();
		final List<ShipAPI> pickedShips = new ArrayList<ShipAPI>(14);
		//float dpLeft = 60.0f;
		//int fighterPicked = 0;
		
		for (final ShipAPI other : ships) {
			//if (dpLeft <=0.0f && fighterPicked >= 8) break;
			if (other == ship) continue;
			if (other.getHullSize().ordinal() < HullSize.FIGHTER.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (ship.getOwner() == other.getOwner() && other.getOwner() != 100 && !isSunBoostActive(other)) {
				final float dist = Misc.getDistance(ship.getLocation(), other.getLocation()) + other.getCollisionRadius();
				if (dist < maxRange) {
					closestShips.add(new DistanceHolder(dist, other));
					/*if(other.getHullSize() == HullSize.FIGHTER && fighterPicked < 8) {
						pickedShips.add(other);
						fighterPicked++;
					} else if(other.getHullSize() != HullSize.FIGHTER) {
						final float dp = other.getMutableStats().getSuppliesPerMonth().getModifiedValue(); // is this the right dp value?
						if(dpLeft - dp >= -5.0f) { // let's have 5dp tolerance for no reason.
							dpLeft = dpLeft - dp;
							pickedShips.add(other);
						}
					}*/
					
				}
			}
		}

		Collections.sort(closestShips);

		final Map<HullSize, Integer> counters = new HashMap<HullSize, Integer>(5);
		counters.put(HullSize.FIGHTER, 8);
		counters.put(HullSize.FRIGATE, 3);
		counters.put(HullSize.DESTROYER, 2);
		counters.put(HullSize.CRUISER, 1);
		counters.put(HullSize.CAPITAL_SHIP, 0);

		for(final DistanceHolder holder : closestShips) {
			final ShipAPI other = holder.ship;
			final HullSize size = other.getHullSize();
			if(!counters.containsKey(size)) {
				continue;
			}
			final int counter = (Integer) counters.get(size);
			final boolean add;
			if(counter > 0) {
				add = true;
				counters.put(size, counter - 1);
			} else if(size == HullSize.CAPITAL_SHIP
					&& (Integer) counters.get(HullSize.CRUISER) == 1
					&& (Integer) counters.get(HullSize.DESTROYER) == 2) {
				counters.put(HullSize.CRUISER, 0);
				counters.put(HullSize.DESTROYER, 0);
				add = true;
			} else {
				add = false;
			}
			if(add && !pickedShips.contains(other)) {
				pickedShips.add(other);
			}
		}

		return pickedShips;
	}

	private static void activateSunBoostFlag(ShipAPI ship) {
		if(ship == null) return;
		ship.setCustomData(SUN_BOOST_KEY, true);
	}

	private static void deactivateSunBoostFlag(ShipAPI ship) {
		if(ship == null) return;
		ship.removeCustomData(SUN_BOOST_KEY);
	}

	private static boolean isSunBoostActive(ShipAPI ship) {
		if(ship == null) return false;
		if(ship.getCustomData() == null) return false;
		if(ship.getCustomData().containsKey(SUN_BOOST_KEY)) return true;
		return false;
	}

	private static float getMaxRange(ShipAPI ship) {
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
	}
	
	private Vector2f getOrbLocation(ShipAPI ship) {
		Vector2f retval = ship.getLocation();
		List<WeaponAPI> weapons = ship.getAllWeapons();
		for(WeaponAPI weapon : weapons) {
			if("oas_orb_sun".equals(weapon.getId())) {
				retval = weapon.getLocation();
			}
		}
		return retval;
	}
}