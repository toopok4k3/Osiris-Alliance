package data.hullmods;

import java.util.HashMap;
import java.util.Map;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.util.IntervalUtil;

public class OasInspiredCrew extends BaseHullMod {

	private static final IntervalUtil interval = new IntervalUtil(2f, 2f);
	private static final float COMBAT_BUFF_DURATION = 60.0f;
	private static String ID = "OAS_INSPIRE_BONUS";

	private static Map mag = new HashMap();
	static {
		mag.put(HullSize.FRIGATE, 2f);
		mag.put(HullSize.DESTROYER, 3f);
		mag.put(HullSize.CRUISER, 4f);
		mag.put(HullSize.CAPITAL_SHIP, 5f);
	}

	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
		if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
		if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
		if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
		return null;
	}

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		super.advanceInCombat(ship, amount);
		if(ship != null) {
			interval.advance(amount);
			if(interval.intervalElapsed()) {
				CombatEngineAPI engine = Global.getCombatEngine();
				if(engine == null) return;
				
				final float timeInCombat = engine.getTotalElapsedTime(false);
				final float deltaTime = COMBAT_BUFF_DURATION - timeInCombat;
				float boostPower = 0.0f;
				if(deltaTime > 0) boostPower = deltaTime / COMBAT_BUFF_DURATION;
				
				MutableShipStatsAPI mutableStats = ship.getMutableStats();
				HullSize hullSize = ship.getHullSize();
				final Float hullBonus = (Float) mag.get(hullSize);
				final float hb = 0.0f;
				if(hullBonus != null) hb = hullBonus;
				final float flatmod = hb * boostPower;
				final String combinedId = ship.getId() + "_" + ID;
				mutableStats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_FLAT).modifyFlat(combinedId, flatmod);
			}
		}
	}

	@Override
	public Color getNameColor() {
		return new Color(210,200,183,255);
	}
}