package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;


public class BallisticDriveStats extends BaseShipSystemScript {

	public static final float BONUS = 1f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float mult = 1f + BONUS * effectLevel;
		stats.getBallisticRoFMult().modifyMult(id, mult);
		stats.getBallisticWeaponFluxCostMod().modifyMult(id, 0.5f);

		if (state == ShipSystemStatsScript.State.OUT) {
			stats.getMaxSpeed().unmodify(id); // to slow down ship to its regular top speed while powering drive down
		} else {
			stats.getMaxSpeed().modifyMult(id, mult);
			stats.getMaxSpeed().modifyFlat(id, (100f*effectLevel));
			stats.getAcceleration().modifyFlat(id, (150f*effectLevel));
		}
	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticRoFMult().unmodify(id);
		stats.getBallisticWeaponFluxCostMod().unmodify(id);

		stats.getMaxSpeed().unmodify(id);
		//stats.getMaxTurnRate().unmodify(id);
		//stats.getTurnAcceleration().unmodify(id);
		stats.getAcceleration().unmodify(id);
		//stats.getDeceleration().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		if (index == 0) {
			return new StatusData("increased engine power", false);
		}
		float mult = 1f + BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 1) {
			return new StatusData("ballistic rate of fire +" + (int) bonusPercent + "%", false);
		}
		if (index == 2) {
			return new StatusData("ballistic flux use -50%", false);
		}
		return null;
	}
}
