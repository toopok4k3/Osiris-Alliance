package data.scripts.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.Misc;

import java.lang.Math;
import java.awt.Color;
import java.util.List;

import org.lwjgl.util.vector.Vector2f;

public class OasAntimatterCloudOnFireEffect implements OnFireEffectPlugin {

	private static class CloudTrail extends BaseEveryFrameCombatPlugin {
		private final DamagingProjectileAPI projectile;
		private float time;

		private static final Color SMOKE_COLOR = new Color(150, 100, 255, 255);

		public CloudTrail(DamagingProjectileAPI projectile) {
			this.projectile = projectile;
			this.time = 0.25f;
		}

		@Override
		public void advance(float amount, List<InputEventAPI> events) {
			final CombatEngineAPI engine = Global.getCombatEngine();
			if (engine == null) return;
			if (engine.isPaused()) return;
			time = time - amount;
			if(this.projectile.isExpired() || !engine.isEntityInPlay(this.projectile)) {
				engine.removePlugin(this);
			}

			if(time <= 0.0f) {
				final Vector2f originalVelocity = projectile.getVelocity();
				final Vector2f velocity = new Vector2f(originalVelocity.x * 0.5f, originalVelocity.y * 0.5f);
				final Vector2f velocity2 = new Vector2f(originalVelocity.x * 0.3f, originalVelocity.y * 0.5f);
				engine.addNebulaSmokeParticle(projectile.getLocation(), velocity, 10.0f, 5.0f, 0.1f, 1.0f, 0.5f, SMOKE_COLOR);
				engine.addNegativeNebulaParticle(projectile.getLocation(), velocity2, 10.0f, 5.0f, 0.1f, 1.0f, 0.5f, SMOKE_COLOR);
				//addNegativeNebulaParticle(Vector2f loc, Vector2f vel, float size, float endSizeMult, float rampUpFraction, float fullBrightnessFraction, float totalDuration, Color color);
				//addNebulaSmokeParticle(Vector2f loc, Vector2f vel, float size, float endSizeMult, float rampUpFraction, float fullBrightnessFraction, float totalDuration, Color color);
				time = 0.1f + 0.2f * Misc.random.nextFloat();
			}
		}
	}

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		float speedMult = 0.25f + 0.75f * (float) Math.random();
		projectile.getVelocity().scale(speedMult);

		float angVel = (float) (Math.signum((float) Math.random() - 0.5f) * (0.5f + Math.random()) * 720f);
		projectile.setAngularVelocity(angVel);

		if (projectile instanceof MissileAPI) {
			MissileAPI missile = (MissileAPI) projectile;
			float flightTimeMult = 0.25f + 0.75f * (float) Math.random();
			missile.setMaxFlightTime(missile.getMaxFlightTime() * flightTimeMult);
		}

		if (weapon != null) {
			float delay = 0.5f + 0.75f * (float) Math.random();
			weapon.setRefireDelay(delay);
		}

		if(projectile != null) {
			CloudTrail trail = new CloudTrail(projectile);
			engine.addPlugin(trail);
		}
	}
}
