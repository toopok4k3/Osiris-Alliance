package data.scripts.weapons;

import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.EveryFrameWeaponEffectPlugin;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.util.vector.Vector2f;

import java.util.Random;
import java.awt.Color;

public class OasSmallBenergyEffect implements EveryFrameWeaponEffectPlugin, OnHitEffectPlugin {

	@Override
	public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
		if (engine.isPaused())
			return;
		ShipAPI ship = weapon.getShip();
		if (ship == null)
			return;
		if (!weapon.isDisabled() && weapon.getChargeLevel() > 0) {
			Global.getSoundPlayer().playLoop("oas_l_hyperloop", weapon, 0.25f + weapon.getChargeLevel() * 1.25f, 0.35f,
					weapon.getLocation(), weapon.getShip().getVelocity());
		}
	}

	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (!shieldHit && Misc.random.nextFloat() < 0.2f) { // with 0.2s RoF, we get happening once per second
			// arc!
			engine.spawnEmpArc(projectile.getSource(), point, target, target, projectile.getDamageType(), 0.0f,
					projectile.getDamageAmount() * 2.0f, 80.0f, null, 1.0f, new Color(0.3f, 0.35f, 1.0f, 1.0f),
					new Color(0.9f, 0.9f, 1.0f, 1.0f));
		}
	}
}
