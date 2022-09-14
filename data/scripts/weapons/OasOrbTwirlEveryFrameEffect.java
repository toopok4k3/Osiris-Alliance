package data.scripts.weapons;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipAPI;

import java.awt.Color;
import java.lang.Math;

public class OasOrbTwirlEveryFrameEffect implements EveryFrameWeaponEffectPlugin {

	private static final float degreesPerSecond = 50.0f;
	private float lastSystemUse = 0.0f;

	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused()) return;
		ShipAPI ship = weapon.getShip();
		if(ship == null) return;
		ShipSystemAPI system = ship.getSystem();
		if(system == null) return;
		final float effectLevel = system.getEffectLevel();
		final boolean showHal = effectLevel > 0.0f && !ship.isHulk();
		lastSystemUse += amount;
		if(!ship.isAlive()) {
			weapon.getSprite().setColor(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		} else if(showHal) {
			weapon.getAnimation().setFrame(1);
			weapon.getSprite().setColor(new Color(1.0f, 1.0f, 1.0f, effectLevel));
			weapon.setCurrAngle(ship.getFacing());
			lastSystemUse = 0.0f;
		} else {
			float alpha = Math.min(1.0f, lastSystemUse / 5.0f); // slowly fade alpha up after system use, takes 5s
			float currentAngle = weapon.getCurrAngle();
			weapon.getAnimation().setFrame(0);
			weapon.getSprite().setColor(new Color(1.0f, 1.0f, 1.0f, alpha));
			currentAngle += amount * degreesPerSecond;
			if(currentAngle >= 360.0f) {
				currentAngle = currentAngle - 360.0f;
			}
			weapon.setCurrAngle(currentAngle);
		}
	}
}
