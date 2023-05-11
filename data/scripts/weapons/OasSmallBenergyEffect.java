package data.scripts.weapons;

import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.FluxTrackerAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.util.vector.Vector2f;

import java.util.Random;
import java.awt.Color;
import java.lang.Math;

public class OasSmallBenergyEffect implements EveryFrameWeaponEffectPlugin, OnHitEffectPlugin {

	private float previousT;

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused())
			return;
		ShipAPI ship = weapon.getShip();
		if (ship == null)
			return;
		final FluxTrackerAPI fluxTracker = ship.getFluxTracker();
		final boolean isOverloadedOrVenting;
		if(fluxTracker != null && fluxTracker.isOverloadedOrVenting()) {
			isOverloadedOrVenting = true;
		} else {
			isOverloadedOrVenting = false;
		}
		/*if (!isOverloadedOrVenting && !weapon.isDisabled() && weapon.getChargeLevel() > 0) {
			Global.getSoundPlayer().playLoop("oas_l_hyperloop", weapon, 0.25f + weapon.getChargeLevel() * 1.25f, 0.35f,
					weapon.getLocation(), weapon.getShip().getVelocity());
		}*/
		float t = previousT;
		if(weapon.isFiring()) {
			t = Math.min(t + amount, 0.4f); // spinup in 0.4s
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
			Global.getSoundPlayer().playLoop("oas_l_hyperloop", weapon, 0.25f + normalized * 1.25f, 0.35f * volume, weapon.getLocation(), weapon.getShip().getVelocity());
		}
		previousT = t;
	}

	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (!shieldHit && Misc.random.nextFloat() <= 0.05f) { // with 0.1s RoF, we get happening once per 2 seconds
			// arc!
			engine.spawnEmpArc(projectile.getSource(), point, target, target, projectile.getDamageType(), 0.0f,
					400.0f, 500.0f, null, 1.0f, new Color(0.3f, 0.35f, 1.0f, 1.0f),
					new Color(0.9f, 0.9f, 1.0f, 1.0f));
		}
	}
}
