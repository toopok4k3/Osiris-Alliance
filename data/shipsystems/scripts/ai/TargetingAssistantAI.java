package data.shipsystems.scripts.ai;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponSize;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.util.IntervalUtil;

public class TargetingAssistantAI implements ShipSystemAIScript {

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
    private Vector2f rangeVector = new Vector2f(0f, 0f);
	
	@SuppressWarnings("unchecked")
	public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
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
			if(longestRangeBallisticWeapon <= 0f || rangeToTarget <= 0f /*|| medianBallisticWeaponRange <= 0f*/) return;
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
	}
}
