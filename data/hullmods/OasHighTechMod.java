package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

import java.awt.Color;

public class OasHighTechMod extends BaseHullMod {

	//public static final float BONUS = 30f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getMaxSpeed().modifyFlat(id, 30.0f);
		stats.getAcceleration().modifyFlat(id, 30.0f);
		stats.getTurnAcceleration().modifyFlat(id, 15.f);
		stats.getMaxTurnRate().modifyFlat(id, 30.0f);
		stats.getFluxCapacity().modifyFlat(id, 4000.0f);
		stats.getFluxDissipation().modifyFlat(id, 250.0f);
		stats.getArmorBonus().modifyFlat(id, -400.0f);
	}

	private static final Color RING_COLOR = new Color(255, 255, 255, 255);
	private static final Color INNER_COLOR = new Color(125,125,255,75);

	@Override
	public void advanceInCombat(ShipAPI ship, float amount) {
		if(ship != null) {
			ShieldAPI shield = ship.getShield();
			if(shield != null) {
				shield.setRingColor(RING_COLOR);
				shield.setInnerColor(INNER_COLOR);
			}
		}
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		if (index == 0) return "30";
		if (index == 1) return "4000";
		if (index == 2) return "250";
		if (index == 3) return "-400";
		//if (index == 3) return "30";
		return null;
	}
}
