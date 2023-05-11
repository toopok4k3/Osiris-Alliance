package data.scripts.weapons;

import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class OasHyperEveryFrameEffect implements EveryFrameWeaponEffectPlugin {

	private float previousT;

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused()) return;
		ShipAPI ship = weapon.getShip();
		if(ship == null) return;
		/*final FluxTrackerAPI fluxTracker = ship.getFluxTracker();
		final boolean isOverloadedOrVenting;
		if(fluxTracker != null && fluxTracker.isOverloadedOrVenting()) {
			isOverloadedOrVenting = true;
		} else {
			isOverloadedOrVenting = false;
		}
		if (!isOverloadedOrVenting && !weapon.isDisabled() && weapon.getChargeLevel()>0){
			Global.getSoundPlayer().playLoop("oas_l_hyperloop"
					, weapon
					, 0.25f + weapon.getChargeLevel() * 0.75f
					, 0.43f
					, weapon.getLocation()
					, weapon.getShip().getVelocity());
		}*/
		//playLoop(String id, Object playingEntity, float pitch, float volume, Vector2f loc, Vector2f vel);

		final FluxTrackerAPI fluxTracker = ship.getFluxTracker();
		final boolean isOverloadedOrVenting;
		if(fluxTracker != null && fluxTracker.isOverloadedOrVenting()) {
			isOverloadedOrVenting = true;
		} else {
			isOverloadedOrVenting = false;
		}
		float t = previousT;
		if(weapon.isFiring()) {
			t = Math.min(t + amount, 0.5f); // spinup in 0.4s
		} else {
			t = Math.max(t - amount, 0.0f);
		}

		if(isOverloadedOrVenting || weapon.isDisabled()) {
			t = Math.max(t - (1.5f * amount), 0.0f); // ensure spindown.
		}

		final float normalized = t / 0.4f;
		final float volume;
		if(previousT > t || previousT < t) {
			// we spin up or down
			volume = normalized;
		} else {
			volume = 1.0f;
		}
		if(t > 0.0f) {
			Global.getSoundPlayer().playLoop("oas_l_hyperloop", weapon, 0.25f + normalized * 0.75f, 0.43f * volume, weapon.getLocation(), weapon.getShip().getVelocity());
		}
		previousT = t;
	}
}
