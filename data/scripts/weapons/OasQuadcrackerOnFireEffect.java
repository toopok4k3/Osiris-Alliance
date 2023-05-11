package data.scripts.weapons;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.OnFireEffectPlugin;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.Random;
import java.awt.Color;

public class OasQuadcrackerOnFireEffect implements OnFireEffectPlugin {

	final static private Color smokeColor = new Color(0.11f, 0.09f, 0.06f, 0.75f);

	@Override
	public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
		if(weapon != null) {
			final ShipAPI ship = weapon.getShip();
			if(ship != null) {
				final Vector2f shipVelocity = ship.getVelocity();
				final Vector2f projectileVelocity = projectile.getVelocity();
				final Vector2f location = projectile.getLocation();
				if(shipVelocity != null && projectileVelocity != null && weapon.getSpec() != null) {
					final Color flashGlowColor = weapon.getSpec().getGlowColor();
					if(flashGlowColor != null) {
						//void addNebulaSmokeParticle(Vector2f loc, Vector2f vel, float size,
						//float endSizeMult, float rampUpFraction,
						//float fullBrightnessFraction, float totalDuration, Color color);
						engine.addNebulaSmokeParticle(location, shipVelocity, 25.0f, 0.1f, 1.0f, 1.0f, 0.2f, flashGlowColor);
						for(int i = 0; i < 4; i++) {
							final float scale = 0.1f*Misc.random.nextFloat();
							Vector2f velocity = new Vector2f(shipVelocity.x + projectileVelocity.x * scale, shipVelocity.y + projectileVelocity.y * scale);
							engine.addNebulaSmokeParticle(location, velocity, 5.0f, 1.5f + 8f * Misc.random.nextFloat(), 0.66f, 0.1f, 0.8f-(scale*2.0f), smokeColor);
						}
					}
				}
			}
		}
	}
}
