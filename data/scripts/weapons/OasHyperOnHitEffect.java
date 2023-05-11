package data.scripts.weapons;

import data.scripts.OasUtil;

import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.impl.combat.DisintegratorEffect;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.util.Misc;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector;

import java.util.Random;
import java.awt.Color;

public class OasHyperOnHitEffect implements OnHitEffectPlugin {
    
	@Override
	public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
			ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
		if (!shieldHit && target != null && projectile != null && projectile.getLocation() != null
				&& target.getVelocity() != null) {
			final Vector2f directionToCenter;
			final Vector2f center;
			if (target.getLocation() != null) {
				center = target.getLocation();
			} else {
				center = projectile.getLocation(); // silly fallback
			}
			directionToCenter = new Vector2f(center.x - point.x, center.y - point.y);
			final float angleToCenter = Misc.getAngleInDegrees(directionToCenter);
			final Color particleColor = new Color(0.4f, 1.0f, 0.4f, 1.0f);
			final Color particleColor2 = new Color(0.3f, 0.8f, 0.3f, 0.8f);
			for (int i = 0; i < 30; i++) {
				final float particleAngle = 180.0f;
				final Vector2f direction = Misc.getUnitVectorAtDegreeAngle(angleToCenter + particleAngle + 30.0f - Misc.random.nextFloat() * 60.0f);
				final float speedScale = 75.0f + (Misc.random.nextFloat() * 450.0f);
				final float xvel = direction.x * speedScale + target.getVelocity().x;
				final float yvel = direction.y * speedScale + target.getVelocity().y;
				final Vector2f velocity = new Vector2f(xvel, yvel);
				final float size = 4.0f * Misc.random.nextFloat() + 2.0f;
				final float brightness = 1.0f;// - (Misc.random.nextFloat() * 0.25f);
				final float duration = 0.05f + (Misc.random.nextFloat() * 0.25f);
				engine.addHitParticle(point, velocity, size, brightness, duration, particleColor);
				engine.addSmoothParticle(point, velocity, 2 * size, brightness, duration + 0.2f, particleColor2);
			}

			if(target instanceof ShipAPI) {
				OasUtil.dealArmorDamage(projectile.getDamageAmount() * 0.25f, projectile, (ShipAPI)target, point); // deal 50% of the damage as direct armor damage
			}
		}
	}
}
