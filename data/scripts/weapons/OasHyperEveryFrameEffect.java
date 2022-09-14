package data.scripts.weapons;

import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI;

public class OasHyperEveryFrameEffect implements EveryFrameWeaponEffectPlugin {

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused()) return;
		ShipAPI ship = weapon.getShip();
		if(ship == null) return;
		if (!weapon.isDisabled() && weapon.getChargeLevel()>0){
			Global.getSoundPlayer().playLoop("oas_l_hyperloop"
					, weapon
					, 0.25f + weapon.getChargeLevel() * 0.75f
					, 0.43f
					, weapon.getLocation()
					, weapon.getShip().getVelocity());
		}
		//playLoop(String id, Object playingEntity, float pitch, float volume, Vector2f loc, Vector2f vel);
	}
}
