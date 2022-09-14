package data.scripts.weapons;

import com.fs.starfarer.api.combat.OnHitEffectPlugin;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.Global;

import org.lwjgl.util.vector.Vector2f;

public class OasKineticOnHitEffect implements OnHitEffectPlugin {
	@Override
	public void onHit(DamagingProjectileAPI projectile
		, CombatEntityAPI target
		, Vector2f point
		, boolean shieldHit
		, ApplyDamageResultAPI damageResult
		, CombatEngineAPI engine) {
	if(shieldHit && target != null && projectile != null && projectile.getLocation() != null && target.getVelocity() != null) {
			Global.getSoundPlayer().playSound("oas_snapper_hit", 1f, 1f, projectile.getLocation(), target.getVelocity());
			Global.getCombatEngine().applyDamage(projectile, target, point, projectile.getDamageAmount(), projectile.getDamageType(), 0f, false, false, projectile.getSource(), true);
		}
	}
}
