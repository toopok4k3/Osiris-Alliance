package data.hullmods;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class OasTurboMount extends BaseHullMod {

	public static final float BONUS = 50f;
	
	public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
		stats.getBallisticAmmoBonus().modifyPercent(id, BONUS);
		stats.getEnergyAmmoBonus().modifyPercent(id, BONUS);
        stats.getBallisticRoFMult().modifyPercent(id, BONUS);
        stats.getEnergyRoFMult().modifyPercent(id, BONUS);
        stats.getEnergyWeaponFluxCostMod().modifyPercent(id, -BONUS);
        stats.getBallisticWeaponFluxCostMod().modifyPercent(id, -BONUS);
        stats.getBeamWeaponFluxCostMult().modifyPercent(id, -BONUS);
        stats.getBeamWeaponDamageMult().modifyPercent(id, BONUS);
        stats.getProjectileSpeedMult().modifyPercent(id, BONUS);
	}
	
	public String getDescriptionParam(int index, HullSize hullSize) {
		//if (index == 0) return "" + (int) BONUS + "%";
		return null;
	}
}
