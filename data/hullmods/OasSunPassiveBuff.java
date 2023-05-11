package data.hullmods;

import java.util.HashMap;
import java.util.Map;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import java.awt.Color;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CollisionGridAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.EmpArcEntityAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.Misc;

public class OasSunPassiveBuff extends BaseHullMod {
	private static final String SUN_BOOST_KEY = "oas_sunboost_active";

	private static final float COMBAT_BUFF_DURATION = 10.0f;

	private Map<ShipAPI, Float> shipClocks = new HashMap<ShipAPI, Float>();

	private static float RANGE = 1000.0f;

	private static final float PD_MINUS_MULT = -0.40f;
	private static final SunStat ERROR_STAT = new SunStat(5.0f,	0.0f, 0.0f);
	private static final Map<HullSize, SunStat> statIndex = new HashMap<HullSize, SunStat>();
	static {
		statIndex.put(HullSize.FIGHTER,new SunStat(1.5f, 100.0f, 0.4f));
		statIndex.put(HullSize.FRIGATE, new SunStat(3.0f, 90.0f, 0.5f));
		statIndex.put(HullSize.DESTROYER, new SunStat(6.0f, 80.0f, 0.65f));
		statIndex.put(HullSize.CRUISER, new SunStat(9.0f, 60.0f, 0.8f));
		statIndex.put(HullSize.CAPITAL_SHIP,new SunStat(15.0f, 40.0f, 1.0f));
	}

	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) ((SunStat)statIndex.get(HullSize.FRIGATE)).range).intValue() + "%";
		if (index == 1) return "" + ((Float) ((SunStat)statIndex.get(HullSize.DESTROYER)).range).intValue() + "%";
		if (index == 2) return "" + ((Float) ((SunStat)statIndex.get(HullSize.CRUISER)).range).intValue() + "%";
		if (index == 3) return "" + ((Float) ((SunStat)statIndex.get(HullSize.CAPITAL_SHIP)).range).intValue() + "%";
		return null;
	}

	private static class SunStat {
		private float untilNext, range, scale;
		public SunStat(float untilNext, float range, float scale) {
			this.untilNext = untilNext;
			this.range = range;
			this.scale = scale;
		}
	}

	private static class Buff extends BaseEveryFrameCombatPlugin {
		private float time = 0.0f;
		final private ShipAPI target;
		final private SunStat sunStat;
		private boolean triggeredEndSound = false;

		public Buff(ShipAPI target) {
			this.target = target;
			if(target != null && statIndex.containsKey(target.getHullSize())) {
				Object object = statIndex.get(target.getHullSize()); // because janino lol
				sunStat = (SunStat) object; // because janino lol
			} else {
				sunStat = ERROR_STAT;
			}
			if(target == null) return;
			final CombatEngineAPI engine = Global.getCombatEngine();
			if(engine == null) return;
			activateSunBoostFlag(target);
			engine.addPlugin(this);
			final SoundPlayerAPI soundPlayer = Global.getSoundPlayer();
			soundPlayer.playSound("oas_system_time_start", 1.0f, sunStat.scale, target.getLocation(), target.getVelocity());
		}

		public float getNextTime() {
			return sunStat.untilNext;
		}

		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			final CombatEngineAPI engine = Global.getCombatEngine();
			if(engine == null) return;
			if(engine.isPaused()) return;
			final SoundPlayerAPI soundPlayer = Global.getSoundPlayer();
			
			this.time += amount;

			final float level;
			if(time < 1.0f) {
				level = Math.min(1.0f, time);
				
			} else if(time > COMBAT_BUFF_DURATION - 1.0f) {
				level = Math.max(0.0f, COMBAT_BUFF_DURATION - time);
				if(!triggeredEndSound) {
					triggeredEndSound = true;
					soundPlayer.playSound("oas_system_time_off", 1.0f, sunStat.scale, target.getLocation(), target.getVelocity());
				}
			} else {
				level = 1.0f;
			}
			if(engine.getPlayerShip() == target) {
				String txt = String.format("%.2f", sunStat.range * level);
				engine.maintainStatusForPlayerShip(this, "targeting_assistant_icon2", "ORB Link", txt +" % increased weapon range", false);
			}
			//void maintainStatusForPlayerShip(Object key, String spriteName, String title, String data, boolean isDebuff);
			MutableShipStatsAPI stats = target.getMutableStats();
			if(time > COMBAT_BUFF_DURATION || !target.isAlive()) {
				unapplyStats(stats);
				engine.removePlugin(this);
				deactivateSunBoostFlag(target);
			} else {
				soundPlayer.playLoop("oas_system_time_loop", target, 1.0f, 1.0f*level*sunStat.scale, target.getLocation(), target.getVelocity());
				target.setWeaponGlow(level, new Color(100,165,255,255), EnumSet.of(WeaponType.BALLISTIC, WeaponType.ENERGY));
				applyStats(stats, level);
			}
		}

		private void applyStats(MutableShipStatsAPI stats, float level) {
			stats.getBallisticWeaponRangeBonus().modifyPercent(SUN_BOOST_KEY, sunStat.range * level);
			stats.getEnergyWeaponRangeBonus().modifyPercent(SUN_BOOST_KEY, sunStat.range * level);
			stats.getNonBeamPDWeaponRangeBonus().modifyPercent(SUN_BOOST_KEY, (sunStat.range*PD_MINUS_MULT) * level);
			stats.getBeamPDWeaponRangeBonus().modifyPercent(SUN_BOOST_KEY, (sunStat.range*PD_MINUS_MULT) * level);
		}

		private void unapplyStats(MutableShipStatsAPI stats) {
			stats.getBallisticWeaponRangeBonus().unmodify(SUN_BOOST_KEY);
			stats.getEnergyWeaponRangeBonus().unmodify(SUN_BOOST_KEY);
			stats.getNonBeamPDWeaponRangeBonus().unmodify(SUN_BOOST_KEY);
			stats.getBeamPDWeaponRangeBonus().unmodify(SUN_BOOST_KEY);
		}
	}

	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
		final CombatEngineAPI engine = Global.getCombatEngine();
		if(engine == null) return;
		if(engine.isPaused()) return;
		if(ship != null) {
			float clock = 2.5f;
			Float storedClock = (Float) shipClocks.get(ship);
			if(storedClock != null) {
				clock = storedClock;
			}
			clock -= amount;
			if(clock <= 0.0f) {
				final float thickness = 40.0f;
				final float coreWidthMult = 0.67f;
				final Color fringe =  new Color(100,100,255,255);
				final Vector2f orbLocation = getOrbLocation(ship);
				
				final ShipAPI target = findClosestAllyShipInRange(ship);
				
				Buff buff = new Buff(target);
				clock = buff.getNextTime();
				if(orbLocation != null) {
					engine.addSmoothParticle(orbLocation, ship.getVelocity(), 80.0f, 1.0f, 0.1f, Color.WHITE);
				}
				if(orbLocation != null && target != null) {
					EmpArcEntityAPI arc = engine.spawnEmpArcVisual(orbLocation,
						ship,
						target.getLocation(),
						target,
						thickness * buff.sunStat.scale,
						fringe,
						Color.white);
					arc.setCoreWidthOverride(thickness * coreWidthMult * buff.sunStat.scale);
					arc.setSingleFlickerMode();
					engine.addFloatingText(target.getLocation(), "ORB Data Linked", 30.0f*buff.sunStat.scale, Color.white, target, 2.0f, 2.0f);
				}
			}
			shipClocks.put(ship, new Float(clock));
		}
	}

	private static ShipAPI findClosestAllyShipInRange(final ShipAPI ship) {
		final CombatEngineAPI engine = Global.getCombatEngine();
		final CollisionGridAPI grid = engine.getShipGrid();
		final float size = 2 * getMaxRange(ship);
		ShipAPI closestShip = null;
		float closest = Float.MAX_VALUE;
		Iterator<Object> iterator = grid.getCheckIterator(ship.getLocation(), size, size);
		while(iterator.hasNext()) {
			Object obj = iterator.next();
			ShipAPI other = (ShipAPI) obj;
			if (other == ship) continue;
			if (other.getHullSize().ordinal() < HullSize.FRIGATE.ordinal()) continue;
			if (other.isShuttlePod()) continue;
			if (other.isHulk()) continue;
			if (ship.getOwner() == other.getOwner() && other.getOwner() != 100 && !isSunBoostActive(other)) {
				float distance = Misc.getDistance(ship.getLocation(), other.getLocation());
				if(distance < closest) {
					closestShip = other;
					closest = distance;
				}
			}
		}
		return closestShip;
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

	private static Vector2f getOrbLocation(ShipAPI ship) {
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