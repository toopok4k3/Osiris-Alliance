package data.scripts.weapons;

import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector;

import java.util.Random;
import java.awt.Color;

public class OasMagnetronOnHitEffect implements OnHitEffectPlugin {

	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (shieldHit && target != null && projectile != null && projectile.getLocation() != null
				&& target.getVelocity() != null) {
			ShieldAPI shield = target.getShield();
			final Vector2f directionToCenter;
			final Vector2f center;
			if (shield != null) {
				center = shield.getLocation();
			} else if (target.getLocation() != null) {
				center = target.getLocation();
			} else {
				center = projectile.getLocation(); // silly fallback
			}
			directionToCenter = new Vector2f(center.x - point.x, center.y - point.y);
			final float angleToCenter = Misc.getAngleInDegrees(directionToCenter);
			final Color particleColor = new Color(1.0f, 1.0f, 1.0f, 1.0f);
			final Color particleColor2 = new Color(0.8f, 0.3f, 0.3f, 0.8f);
			final int particleCount = getParticleCount(projectile.getWeapon());
			for (int i = 0; i < particleCount; i++) {
				final float particleAngle;
				if (i % 2 == 0) {
					particleAngle = -90.0f;
				} else {
					particleAngle = 90.0f;
				}
				final Vector2f direction = Misc.getUnitVectorAtDegreeAngle(angleToCenter + particleAngle + 30.0f - Misc.random.nextFloat() * 60.0f);
				final float speedScale = 25.0f + (Misc.random.nextFloat() * 350.0f);
				final float xvel = direction.x * speedScale + target.getVelocity().x;
				final float yvel = direction.y * speedScale + target.getVelocity().y;
				final Vector2f velocity = new Vector2f(xvel, yvel);
				final float size = 7.0f * Misc.random.nextFloat() + 3.0f;
				final float brightness = 1.0f;// - (Misc.random.nextFloat() * 0.25f);
				final float duration = 0.1f + (Misc.random.nextFloat() * 0.65f);
				engine.addHitParticle(point, velocity, size, brightness, duration, particleColor);
				engine.addSmoothParticle(point, velocity, 2 * size, brightness, duration + 0.2f,
						particleColor2);
			}
		} /* naaah let's just add kinetic dmg, simpler for the player to understand the stats
		else if(!shieldHit && target != null && projectile != null && projectile.getLocation() != null) {
			// let's add frag damage.
			engine.applyDamage(target, projectile.getLocation(), getFragmentationDamage(projectile.getWeapon()),
					DamageType.FRAGMENTATION, 0.0f, false, false, projectile.getSource(), false);
		}*/
	}

	private static int getParticleCount(WeaponAPI weapon) {
		final int particles;
		if (weapon == null) {
			particles = 10;
		} else if (WeaponAPI.WeaponSize.SMALL == weapon.getSize()) {
			particles = 10;
		} else if (WeaponAPI.WeaponSize.MEDIUM == weapon.getSize()) {
			particles = 20;
		} else {
			particles = 40;
		}
		return particles;
	}

	/*private static float getFragmentationDamage(WeaponAPI weapon) {
		final float damage;
		if (weapon == null) {
			damage = 20;
		} else if (WeaponAPI.WeaponSize.SMALL == weapon.getSize()) {
			damage = 20;
		} else if (WeaponAPI.WeaponSize.MEDIUM == weapon.getSize()) {
			damage = 100;
		} else {
			damage = 400;
		}
		return damage;
	}*/
}