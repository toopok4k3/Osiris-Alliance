package data.hullmods;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.hullmods.BaseLogisticsHullMod;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class OasRetinatorCore extends BaseLogisticsHullMod {
    
	//private static float DAMAGE_MISSILES_PERCENT = 50.0f;
	//private static float DAMAGE_FIGHTERS_PERCENT = 50.0f;
    private static float PROJECTILE_SPEED_PERCENT = 50.0f;
    private static float BALLISTIC_RANGE_BONUS = 400.0f;
    //private static float ENERGY_RANGE_BONUS = 200.0f;
    //private static float BEAM_RANGE_BONUS = 200.0f;
    private static float RECOIL_MULT = 0.25f;

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        //stats.getDamageToMissiles().modifyPercent(id, DAMAGE_MISSILES_PERCENT);
		//stats.getDamageToFighters().modifyPercent(id, DAMAGE_FIGHTERS_PERCENT);
        //stats.getEnergyWeaponRangeBonus().modifyFlat(id, ENERGY_RANGE_BONUS);
        //stats.getBeamPDWeaponRangeBonus().modifyFlat(id, BEAM_RANGE_BONUS);
        stats.getBallisticProjectileSpeedMult().modifyPercent(id, PROJECTILE_SPEED_PERCENT);
        stats.getBallisticWeaponRangeBonus().modifyFlat(id, BALLISTIC_RANGE_BONUS);
        stats.getMaxRecoilMult().modifyMult(id, RECOIL_MULT);
        stats.getAutofireAimAccuracy().modifyFlat(id, 1.0f);
		stats.getEngineDamageTakenMult().modifyMult(id, 0.0f);
        stats.getDynamic().getMod(Stats.PD_IGNORES_FLARES).modifyFlat(id, 1f);
    }

    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
		return null;
	}
}
