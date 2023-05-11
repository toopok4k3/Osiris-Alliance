package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class OasFastFlames extends BaseHullMod {

	public static final float BONUS = 30f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int) BONUS + "%";
		return null;
	}
}
