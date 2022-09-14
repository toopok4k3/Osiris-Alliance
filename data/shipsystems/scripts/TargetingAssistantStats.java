package data.shipsystems.scripts;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.plugins.ShipSystemStatsScript;


public class TargetingAssistantStats extends BaseShipSystemScript {

	public static final float RANGE_BONUS = 0.5f;
	public static final float RECOIL_BONUS = 0.75f;
	public static final float PROJECTILE_SPEED_BONUS = 0.20f;

	public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
		float mult = 1f + (RANGE_BONUS * effectLevel);
		stats.getBallisticWeaponRangeBonus().modifyMult(id, mult);
		stats.getBeamWeaponRangeBonus().modifyMult(id, mult);
		mult = 1f - (RECOIL_BONUS * effectLevel);
		stats.getRecoilDecayMult().modifyMult(id, mult);
		stats.getRecoilPerShotMult().modifyMult(id, mult);
		stats.getMaxRecoilMult().modifyMult(id, mult);
		
		mult = 1f + (PROJECTILE_SPEED_BONUS * effectLevel);
		stats.getProjectileSpeedMult().modifyMult(id, mult);

	}
	public void unapply(MutableShipStatsAPI stats, String id) {
		stats.getBallisticWeaponRangeBonus().unmodify(id);
		stats.getBeamWeaponRangeBonus().unmodify(id);
		stats.getRecoilDecayMult().unmodify(id);
		stats.getRecoilPerShotMult().unmodify(id);
		stats.getMaxRecoilMult().unmodify(id);
		stats.getProjectileSpeedMult().unmodify(id);
	}
	
	public StatusData getStatusData(int index, State state, float effectLevel) {
		float mult = 1f + RANGE_BONUS * effectLevel;
		float bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 0) {
			return new StatusData("Ballistic & Beam weapon range +" + (int) bonusPercent + "%", false);
		}
		mult = 1f + (RECOIL_BONUS * effectLevel);
		bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 1) {
			return new StatusData("Recoil bonus +" + (int) bonusPercent + "%", false);
		}
		mult = 1f + (PROJECTILE_SPEED_BONUS * effectLevel);
		bonusPercent = (int) ((mult - 1f) * 100f);
		if (index == 2) {
			return new StatusData("Projectile speed +" + (int) bonusPercent + "%", false);
		}
		return null;
	}
}
